package ssg.chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


public class ChattingActivity extends Activity {
	
	Socket socket; 							// ������ ����ϱ� ���� ����
	
	BufferedReader networkReader;			// �������� ���� �����͸� �������� ���� InputStream
    BufferedWriter networkWriter;			// �������� �����͸� ������ ���� OutputStream
    
    Handler mHandler;						// ������� ����ϱ� ���� �ڵ鷯
    		
    EditText edMsg;							// ����� �Է� �޽���
    Button btnSend;							// �޽��� ���� ��ư
    ListView lvChatListView;				// ä��ȭ�� ����Ʈ��
    
    ArrayList<MessageData> alMsgDataList;	// ä�� ������ ���� ����Ʈ
    MultiLayoutAdapter mlaChatListAdapter;	// ä��ȭ��(����Ʈ��)�� ä�� ���� ����Ʈ�� ��������� ���
    
    
    TelephonyManager teleMgr;				// ����̽��� ID�� ��� ���� ��ü
    String deviceId;						// ����̽��� ID
    int userId;								// ������� ID
    String chatRoomId;						// ä�ù� ID
    
  
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// ���޹��� ������ �ޱ�
		Intent intent = getIntent();
		
		// ä�ù� ID ����
		chatRoomId = intent.getStringExtra("subjectNo") + intent.getStringExtra("classNo");
		
		ActionBar actionbar = getActionBar();
		actionbar.setTitle(intent.getStringExtra("subjectName"));
		actionbar.setSubtitle(intent.getStringExtra("subjectNo") + " / " + intent.getStringExtra("classNo"));
		
		mHandler = new Handler(); // RecvThread�� ����ϱ����� �ڵ鷯
		
		// ����̽��� ID�� �����´�.
		teleMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		deviceId = teleMgr.getDeviceId();
		
		// ���� ID�� �����ϰ� ����
		Random random = new Random();
		userId = random.nextInt(9999);	
		
		edMsg = (EditText) findViewById(R.id.msg);		// �޽��� �Է� â
        btnSend = (Button) findViewById(R.id.send);		// ���۹�ư	
        lvChatListView = (ListView) findViewById(R.id.chat_list);	// ä��ȭ��
        
        // ����Ʈ�� ������ Ŭ���� ������ �ٲ��� �ʵ��� �� selector�� �������ش�.
        lvChatListView.setSelector(R.drawable.listview_selector);
        
        // ä�� ����� ���� Array
        alMsgDataList = new ArrayList<MessageData>();
        
        // ListView�� Array�� �������ֱ����� ArrayAdapter ����
        mlaChatListAdapter = new MultiLayoutAdapter(this, alMsgDataList);
        
        // ListView�� ArrayAdapter ����
        lvChatListView.setAdapter(mlaChatListAdapter);
        
        // ��ư Ŭ�� �̺�Ʈ
        btnSend.setOnClickListener(new OnClickListener() {
       	 
            public void onClick(View v) {
            	if( !edMsg.getText().toString().equals("") ){
            		// ��ư Ŭ���� �޽����� ������ AsyncTask(Thread) ����
            		new SendMsg().execute();
            		// �޽��� ���� �� �Է�â�� ���
            		edMsg.setText("");
            	}
            }
        }); 
        
        /* �ؽ�Ʈ ���� �̺�Ʈ 
        	: �Է�â�� �ؽ�Ʈ�� ������ ���۹�ư�� ��Ȱ��ȭ �ϰ�
        	: �ؽ�Ʈ�� ������ Ȱ��ȭ�Ѵ�. 
        */
        edMsg.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				if( edMsg.getText().toString().equals("") ){
					btnSend.setEnabled(false);
				} else {
					btnSend.setEnabled(true);
				}	
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}      	
        });
        
        // ������ ���� ����
     	new SetSocket().execute();			
	}
	
	// ä������ ����� �� ����Ǵ� onDestroy �޼���
	public void onDestroy(){
		super.onDestroy();
		// ���� ���� ����
		try {
			if( networkReader != null ) networkReader.close();
			if( networkWriter != null ) networkWriter.close();
			if( socket != null) socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * ������ ������ �α� ���� Ŭ����
	 */
	class SetSocket extends AsyncTask<Void, Void, Void> {
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
	            
			} catch (UnknownHostException e) {
				e.printStackTrace();
				Log.e("socket", e.getMessage());
			} catch (IOException e) {
				Log.e("socket", e.getMessage());
				e.printStackTrace();
			}
			return null;
		}		
		
		@Override
		protected void onPostExecute(Void result){
			try {
				RecvThread rt = new RecvThread( socket );
				rt.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * ������ �޽����� ������ ���� Ŭ����
	 */
	class SendMsg extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			PrintWriter out = new PrintWriter(networkWriter, true);
			
			// ä�ù�ID + ����̽�ID + ����ID + �޽��� ���·� ���ڿ��� �����Ѵ�.
			String return_msg = chatRoomId + deviceId
					+ String.format("%04d", userId)
					+ edMsg.getText().toString();
			
			out.println(return_msg);
			return null;
		}
	}
	
	/*
	 * �����κ��� �޽����� �����ϰ� UI �����忡 �޽����� ����ϱ� ���� ������
	 */
	class RecvThread extends Thread{
		BufferedReader brSocketInput;		// �����κ��� �����͸� ���Źޱ� ���� ��Ʈ��
		String line = null;					// �����κ��� ���� ���� �����͸� �����ϱ� ���� ����
		String chatRoomId;					// ���ŵ� �޽����� ä�ù� ID
		String senderDeviceId;				// ���ŵ� �޽����� ����̽��� ID
		String senderUID;					// ���ŵ� �޽����� ���� ID
		String message;						// ���� �޽��� ����
		StringBuilder sBuilder = new StringBuilder();
		
		public RecvThread(Socket s ) throws IOException{
			// �����κ��� �����͸� ���� �ޱ� ���� ��Ʈ�� ����
			brSocketInput = new BufferedReader( new InputStreamReader( s.getInputStream(), "EUC-KR" ) );
		}

		public void run() {
			// �Է� ��Ʈ���� ���� �����͸� �о�ͼ� ����Ѵ�.
			try {
				while ((line = brSocketInput.readLine()) != null) {
					// ���ʿ��� ���� ����
					if( line.charAt(2) > '9' || line.charAt(2) < '0' )
						line = line.substring(3);
					
					chatRoomId = line.substring(0, 9);			// ä�ù� ID �Ľ�
					
					// ä�ù� ID�� �ٸ��� �������� �ʴ´�.
					if ( !this.chatRoomId.equals(ChattingActivity.this.chatRoomId) )
						continue;
					
					senderDeviceId = line.substring(9, 24);	 	// �۽����� ����̽� ID �Ľ�
					senderUID = line.substring(24, 28);			// ���� ID �Ľ�
					message = line.substring(28); 				// ���Ź��� �޽��� �Ľ�

					// �ڵ鷯�� ���ؼ� �޽����� ä��ȭ��(����Ʈ��)�� �߰��Ѵ�.
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							if (senderDeviceId.equals(deviceId)) {
								alMsgDataList.add(new MessageData(1, senderUID,
										message));
							} else
								alMsgDataList.add(new MessageData(0, senderUID,
										message));

							mlaChatListAdapter.notifyDataSetChanged();
						}
					});

					// sBuilder.delete(0, sBuilder.length());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
	
	// �޽��� ������ : �޽����� ����� Ÿ���� ����
	class MessageData {
		int type;
		String userName;
		String msg;
		
		public MessageData( int _type, String _uid, String _msg ) {
			type = _type;
			userName = "����" + Integer.parseInt(_uid);
			msg = _msg;
		}
	}
	
	
}