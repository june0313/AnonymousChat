

import java.io.*;
import java.net.*;
import java.util.*;

import ssg.chat.SubjectInfo;

public class ChatServer {
	int port;								// port number
	ServerSocket serverSocket;				// 서버 소켓
	Socket s;								// 클라이언트와 통신하기 위한 소켓
	BufferedReader br;
	
	PrintWriter pw;
	Vector<ServerThread> v;					// 쓰레드를 저장하는 벡터 객체
	
	ArrayList<SubjectInfo> subjInfoList;	// 강의 정보 리스트
	
	public ChatServer(int port) {
		this.port = port;
		
		getSubjInfo();	// 강의 목록을 불러온다.
		
		v = new Vector<ServerThread>(10, 10); // 쓰레드를 저장하는 백터 객체 생성
		try {
			serverSocket = new ServerSocket(port); // 포트를 지정하여 서버소켓 생성
			System.out.println("서버가" + port + "에서 접속 대기중");
			
			// 클라이언트의 접속을 항상 받아들일 수 있도록 무한루프를 돌림
			while (true) {
				// 클라이언트의 접속을 기다림
				s = serverSocket.accept();
				
				// 클라이언트가 접속하면 데이터를 송수신 하기위해 쓰레드를 만들고 
				// 쓰레드 객체의 생성자로 서버 소켓과 클라이언트 소켓을 넘겨줌
				ServerThread serverThread = new ServerThread(this, s);
				
				// 생성된 쓰레드 객체를 벡터에 추가한다
				this.addThread(serverThread);
				
				// 쓰레드 시작
				serverThread.start();
			}
		} catch (IOException ioe) {
			// 예외에 대한 처리
		}
	}
	
	// 강의 정보를 리스트에 담는다.
	public void getSubjInfo() {
		System.out.println("강의 목록을 불러오는중...");
		subjInfoList = ExcelHandler.ExcelParser();
		
		for( int i = 0; i < subjInfoList.size(); i++ ) {
			System.out.print(subjInfoList.get(i).getSubjectName() + " / ");
			System.out.print(subjInfoList.get(i).getSubjectNo() + " / ");
			System.out.println(subjInfoList.get(i).getClassNo());
		}
		
		System.out.println(subjInfoList.size() + "개의 강의 불러오기 성공!\n");
	}
	
	
	public void addThread(ServerThread st) {
		v.addElement(st);
	}
	
	public void removeThread(ServerThread st) {
		v.removeElement(st);
	}
	
	/*
	 * 받은 메시지를 모든 클라이언트에게 브로드캐스팅 하기 위한 메서드
	 */
	public void broadCast(String classId, String msg) {
		for( int i = 0; i < v.size(); i++ ) {
			ServerThread st = v.elementAt(i); // 벡터에 등록된 모든 쓰레드를 얻어온다.
			
			// classId가 일치하는 쓰레드만
			// if( st.classId.equals(classId))
				st.sendMessage(msg); // 각 쓰레드에 메시지 전송
		}
	}
	
	
	public static void main(String[] args) {
		new ChatServer(9999);
	}
	
	
	
	class ServerThread extends Thread {
		ChatServer server;
		Socket s; 				// 클라이언트와 통신하기 위한 소켓
		BufferedReader br; 		// 클라이언트로부터 데이터를 수신받기 위한 스트림
		BufferedWriter bw;
		String classId = "1";	// 각 채팅방을 구분하기 위한 ID
		
		/*
		 * PrintWriter : 
		 * 다양한 형태의 데이터를 문자열의 형태(println)로 출력하거나, 문자열의 형태로 조합(printf)하여 출력한다. 
		 */
		PrintWriter pw; // 클라이언트로 데이터를 송신하기 위한 스트림
		String message;

		ObjectOutputStream oos;
		
		/*
		 * 생성자 : 입출력 스트림을 얻음
		 */
		public ServerThread(ChatServer server, Socket s) throws IOException{
			this.server = server;
			this.s = s; // 클라이언트와 통신할 수 있는 소켓 정보를 s에 저장함.
			
			// 클라이언트와 메시지를 주고 받기 위한 입출력 스트림을 가져온다
			this.oos = new ObjectOutputStream( s.getOutputStream() );// 객체 출력 스트림
			oos.flush();
			this.br = new BufferedReader( new InputStreamReader(s.getInputStream(), "EUC-KR")); // 입력 스트림
			this.pw = new PrintWriter( s.getOutputStream(), true); // 출력 스트림	
			
			
			// 클라이언트 에서 IP 정보를 얻어 출력한다.
			System.out.println(s.getInetAddress() + "에서 접속함");
		}
		
		public void sendMessage(String str) {
			pw.println(str);			
		}
		
		@Override
		public void run() {
			//클라이언트로부터 받은 데이터를 클라이언트에게 송신한다.
			try {
				// 입력 스트림(br)을 통해 클라이언트가 보낸 메시지를 읽어옴
				while((message = br.readLine()) != null ) {
					// 클라이언트로부터 강의 목록 요청이 들어오면 강의 목록을 전송한다.
					if(message.equals("REQUEST SUBJECT LIST"))
						oos.writeObject(subjInfoList);
					// 일반 메시지의 경우 모든 클라이언트에게 뿌려줌
					else 
						server.broadCast("12345", message); 
				}
			} catch (IOException ioe) {
				System.out.println(ioe);
				server.removeThread(this);
			} finally {
				System.out.println(s.getInetAddress() + "접속 연결 종료");
				server.removeThread(this);
				try {
					s.close();
				} catch (IOException ioe) {
					System.out.println(ioe);
				}
			}
		}
	}
}