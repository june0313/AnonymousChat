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
	
	Socket socket; 							// 서버와 통신하기 위한 소켓
	
	BufferedReader networkReader;			// 소켓으로 부터 데이터를 가져오기 위한 InputStream
    BufferedWriter networkWriter;			// 소켓으로 데이터를 보내기 위한 OutputStream
    
    Handler mHandler;						// 쓰레드와 통신하기 위한 핸들러
    		
    EditText edMsg;							// 사용자 입력 메시지
    Button btnSend;							// 메시지 전송 버튼
    ListView lvChatListView;				// 채팅화면 리스트뷰
    
    ArrayList<MessageData> alMsgDataList;	// 채팅 내용을 담을 리스트
    MultiLayoutAdapter mlaChatListAdapter;	// 채팅화면(리스트뷰)과 채팅 내용 리스트를 연결시켜줄 어뎁터
    
    
    TelephonyManager teleMgr;				// 디바이스의 ID를 얻기 위한 객체
    String deviceId;						// 디바이스의 ID
    int userId;								// 사용자의 ID
    String chatRoomId;						// 채팅방 ID
    
  
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// 전달받은 데이터 받기
		Intent intent = getIntent();
		
		// 채팅방 ID 설정
		chatRoomId = intent.getStringExtra("subjectNo") + intent.getStringExtra("classNo");
		
		ActionBar actionbar = getActionBar();
		actionbar.setTitle(intent.getStringExtra("subjectName"));
		actionbar.setSubtitle(intent.getStringExtra("subjectNo") + " / " + intent.getStringExtra("classNo"));
		
		mHandler = new Handler(); // RecvThread와 통신하기위한 핸들러
		
		// 디바이스의 ID를 가져온다.
		teleMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		deviceId = teleMgr.getDeviceId();
		
		// 유저 ID를 랜덤하게 생성
		Random random = new Random();
		userId = random.nextInt(9999);	
		
		edMsg = (EditText) findViewById(R.id.msg);		// 메시지 입력 창
        btnSend = (Button) findViewById(R.id.send);		// 전송버튼	
        lvChatListView = (ListView) findViewById(R.id.chat_list);	// 채팅화면
        
        // 리스트뷰 아이템 클릭시 색상이 바뀌지 않도록 빈 selector를 설정해준다.
        lvChatListView.setSelector(R.drawable.listview_selector);
        
        // 채팅 목록을 담을 Array
        alMsgDataList = new ArrayList<MessageData>();
        
        // ListView에 Array를 연결해주기위한 ArrayAdapter 생성
        mlaChatListAdapter = new MultiLayoutAdapter(this, alMsgDataList);
        
        // ListView에 ArrayAdapter 설정
        lvChatListView.setAdapter(mlaChatListAdapter);
        
        // 버튼 클릭 이벤트
        btnSend.setOnClickListener(new OnClickListener() {
       	 
            public void onClick(View v) {
            	if( !edMsg.getText().toString().equals("") ){
            		// 버튼 클릭시 메시지를 보내는 AsyncTask(Thread) 실행
            		new SendMsg().execute();
            		// 메시지 전송 후 입력창을 비움
            		edMsg.setText("");
            	}
            }
        }); 
        
        /* 텍스트 변경 이벤트 
        	: 입력창에 텍스트가 없으면 전송버튼을 비활성화 하고
        	: 텍스트가 있으면 활성화한다. 
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
        
        // 서버와 소켓 연결
     	new SetSocket().execute();			
	}
	
	// 채팅장이 종료될 때 실행되는 onDestroy 메서드
	public void onDestroy(){
		super.onDestroy();
		// 소켓 연결 종료
		try {
			if( networkReader != null ) networkReader.close();
			if( networkWriter != null ) networkWriter.close();
			if( socket != null) socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * 서버와 연결을 맺기 위한 클래스
	 */
	class SetSocket extends AsyncTask<Void, Void, Void> {
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
	 * 서버에 메시지를 보내기 위한 클래스
	 */
	class SendMsg extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			PrintWriter out = new PrintWriter(networkWriter, true);
			
			// 채팅방ID + 디바이스ID + 유저ID + 메시지 형태로 문자열을 구성한다.
			String return_msg = chatRoomId + deviceId
					+ String.format("%04d", userId)
					+ edMsg.getText().toString();
			
			out.println(return_msg);
			return null;
		}
	}
	
	/*
	 * 서버로부터 메시지를 수신하고 UI 쓰레드에 메시지를 출력하기 위한 쓰레드
	 */
	class RecvThread extends Thread{
		BufferedReader brSocketInput;		// 서버로부터 데이터를 수신받기 위한 스트림
		String line = null;					// 서버로부터 수신 받은 데이터를 저장하기 위한 변수
		String chatRoomId;					// 수신된 메시지의 채팅방 ID
		String senderDeviceId;				// 수신된 메시지의 디바이스의 ID
		String senderUID;					// 수신된 메시지의 유저 ID
		String message;						// 실제 메시지 내용
		StringBuilder sBuilder = new StringBuilder();
		
		public RecvThread(Socket s ) throws IOException{
			// 서버로부터 데이터를 수신 받기 위한 스트림 생성
			brSocketInput = new BufferedReader( new InputStreamReader( s.getInputStream(), "EUC-KR" ) );
		}

		public void run() {
			// 입력 스트림을 통해 데이터를 읽어와서 출력한다.
			try {
				while ((line = brSocketInput.readLine()) != null) {
					// 불필요한 문자 제거
					if( line.charAt(2) > '9' || line.charAt(2) < '0' )
						line = line.substring(3);
					
					chatRoomId = line.substring(0, 9);			// 채팅방 ID 파싱
					
					// 채팅방 ID가 다르면 수신하지 않는다.
					if ( !this.chatRoomId.equals(ChattingActivity.this.chatRoomId) )
						continue;
					
					senderDeviceId = line.substring(9, 24);	 	// 송신자의 디바이스 ID 파싱
					senderUID = line.substring(24, 28);			// 유저 ID 파싱
					message = line.substring(28); 				// 수신받은 메시지 파싱

					// 핸들러를 통해서 메시지를 채팅화면(리스트뷰)에 추가한다.
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
	
	// 메시지 데이터 : 메시지의 내용과 타입을 저장
	class MessageData {
		int type;
		String userName;
		String msg;
		
		public MessageData( int _type, String _uid, String _msg ) {
			type = _type;
			userName = "유저" + Integer.parseInt(_uid);
			msg = _msg;
		}
	}
	
	
}