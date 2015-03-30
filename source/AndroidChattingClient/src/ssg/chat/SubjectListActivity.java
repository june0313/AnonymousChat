package ssg.chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import ssg.chat.ChattingActivity.RecvThread;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class SubjectListActivity extends Activity implements OnItemClickListener {
	// ���� ����� ���� ��ü
	Socket socket;
	BufferedWriter networkWriter;
	BufferedReader networkReader;
	ObjectInputStream ois;
	
	// ���� ����� ������� ����Ʈ
	ArrayList<SubjectInfo> subjInfoList;
	ArrayList<HashMap<String,String>> subjectList;
	ListView subjectListView;
	SimpleAdapter sAdapter;
	
	ProgressDialog pd;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.add_subject);
	    
	    ActionBar actionbar = getActionBar();
	    actionbar.setTitle("���� �߰�"); 
	    actionbar.setDisplayHomeAsUpEnabled(true);
	    
	    // ������ ���� ����� ��û�Ѵ�.
	    new SetSocket().execute();
	    new ReqSubjList().execute();
	    
	    
	    subjInfoList = new ArrayList<SubjectInfo>();
	    subjectListView = (ListView)findViewById(R.id.add_subject_list);
	}
	
	// �޴� ����
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_subject_menu, menu);
		MenuItem item  = menu.findItem(R.id.search_subject);
		SearchView searchView = (SearchView)item.getActionView();
		
		return super.onCreateOptionsMenu(menu);
	}
	
	// �޴� ������ Ŭ����
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;

		default:
			return false;
		}
	}
	
	/*
	 * ������ ������ �α� ���� Ŭ����
	 */
	public class SetSocket extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				// ���� ����
				socket = new Socket(getString(R.string.server_ip),
						Integer.parseInt(getString(R.string.server_port)));
				
				// �������κ��� InputSteam�� OutputStream�� �����´�.
				// �ѱ��� �����°��� ���� ���� ���ڵ��� EUC-KR�� �����Ѵ�.
				networkWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "EUC-KR"));
	            networkReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "EUC-KR"));
	            ois = new ObjectInputStream(socket.getInputStream());
	            
			} catch (UnknownHostException e) {
				e.printStackTrace();
				Log.e("socket", e.getMessage());
			} catch (IOException e) {
				Log.e("socket", e.getMessage());
				e.printStackTrace();
			}
			return null;
		}		
	}
	
	/*
	 * �����κ��� ���� ����� ��û�Ѵ�.
	 */
	class ReqSubjList extends AsyncTask<Void, Void, Void> {
		protected void onPreExecute() {
			pd = new ProgressDialog(SubjectListActivity.this);
			pd.setMessage("���� ����� �������� ���Դϴ�...");
			pd.setCancelable(true);
			pd.setIndeterminate(true);
			pd.show();
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Void doInBackground(Void... params) {
			PrintWriter out = new PrintWriter(networkWriter, true);
			String requestMsg = "REQUEST SUBJECT LIST";
			
			out.println(requestMsg);
			
			try {
				subjInfoList = (ArrayList<SubjectInfo>)ois.readObject();
				
			} catch (OptionalDataException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result){
			pd.dismiss();
			
			// �����κ��� ���� ����Ʈ�� ����Ʈ�信 ����Ѵ�.
			HashMap<String,String> subjInfo;
			subjectList = new ArrayList<HashMap<String,String>>();
			
			for(int i = 0; i < subjInfoList.size(); i++ ) {
				subjInfo = new HashMap<String,String>();
			    subjInfo.put("title", subjInfoList.get(i).getSubjectName());
			    subjInfo.put("info", subjInfoList.get(i).getSubjectNo() + " / " + subjInfoList.get(i).getClassNo());
			    subjectList.add(subjInfo);
			}
			
			sAdapter = new SimpleAdapter(SubjectListActivity.this, subjectList, android.R.layout.simple_list_item_2,
		    		new String[]{"title", "info"}, new int[]{android.R.id.text1, android.R.id.text2});
			
			subjectListView.setAdapter(sAdapter);
			subjectListView.setOnItemClickListener(SubjectListActivity.this);
			// Toast.makeText(AddSubjectActivity.this, subjectList.get(1000).get("info").toString(), 0).show();
		}	
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
		String content = subjInfoList.get(position).getSubjectName();
		content += "\n�м���ȣ : " + subjInfoList.get(position).getSubjectNo();
		content += "\n�й� :  " + subjInfoList.get(position).getClassNo();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("������ ������ ä�ø�Ͽ� �߰��Ͻðڽ��ϱ�?");
		builder.setMessage(content);
		builder.setCancelable(true);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				ArrayList<SubjectInfo> addedSubjList = new ArrayList<SubjectInfo>();
				
				// ������ �߰��� ä�� ����� ������ ���Ϸκ��� ���� ����� �����´�.
				FileInputStream fis;
				ObjectInputStream ois;
				try {
					
					fis = openFileInput("subject.lst");
					ois = new ObjectInputStream( fis );
					addedSubjList = (ArrayList<SubjectInfo>) ois.readObject();
					
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (StreamCorruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				
				// ��Ͽ� ���ο� ä���� �߰��Ͽ� ���Ͽ� �����Ѵ�.
				FileOutputStream fos;
				ObjectOutputStream oos;
				try {
					
					fos  = openFileOutput("subject.lst", MODE_PRIVATE);
					oos = new ObjectOutputStream( fos );
					addedSubjList.add(subjInfoList.get(position));
					oos.writeObject(addedSubjList);
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				Toast.makeText(SubjectListActivity.this, 
						subjInfoList.get(position).getSubjectName() + " ������ �߰��Ǿ����ϴ�", 
						0).show();
				
				SubjectListActivity.this.finish();
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		AlertDialog ad = builder.create();
		ad.show();
	}
}
