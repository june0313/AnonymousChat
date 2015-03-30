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
	// 소켓 통신을 위한 객체
	Socket socket;
	BufferedWriter networkWriter;
	BufferedReader networkReader;
	ObjectInputStream ois;
	
	// 강의 목록을 담기위한 리스트
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
	    actionbar.setTitle("강의 추가"); 
	    actionbar.setDisplayHomeAsUpEnabled(true);
	    
	    // 서버에 강의 목록을 요청한다.
	    new SetSocket().execute();
	    new ReqSubjList().execute();
	    
	    
	    subjInfoList = new ArrayList<SubjectInfo>();
	    subjectListView = (ListView)findViewById(R.id.add_subject_list);
	}
	
	// 메뉴 생성
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_subject_menu, menu);
		MenuItem item  = menu.findItem(R.id.search_subject);
		SearchView searchView = (SearchView)item.getActionView();
		
		return super.onCreateOptionsMenu(menu);
	}
	
	// 메뉴 아이템 클릭시
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
	 * 서버와 연결을 맺기 위한 클래스
	 */
	public class SetSocket extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				// 소켓 연결
				socket = new Socket(getString(R.string.server_ip),
						Integer.parseInt(getString(R.string.server_port)));
				
				// 소켓으로부터 InputSteam과 OutputStream을 가져온다.
				// 한글이 깨지는것을 막기 위해 인코딩을 EUC-KR로 설정한다.
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
	 * 서버로부터 강의 목록을 요청한다.
	 */
	class ReqSubjList extends AsyncTask<Void, Void, Void> {
		protected void onPreExecute() {
			pd = new ProgressDialog(SubjectListActivity.this);
			pd.setMessage("강의 목록을 가져오는 중입니다...");
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
			
			// 서버로부터 받은 리스트를 리스트뷰에 출력한다.
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
		content += "\n학수번호 : " + subjInfoList.get(position).getSubjectNo();
		content += "\n분반 :  " + subjInfoList.get(position).getClassNo();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("선택한 과목을 채팅목록에 추가하시겠습니까?");
		builder.setMessage(content);
		builder.setCancelable(true);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				ArrayList<SubjectInfo> addedSubjList = new ArrayList<SubjectInfo>();
				
				// 기존에 추가한 채팅 목록이 있으면 파일로부터 기존 목록을 가져온다.
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
				
				// 목록에 새로운 채팅을 추가하여 파일에 저장한다.
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
						subjInfoList.get(position).getSubjectName() + " 과목이 추가되었습니다", 
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
