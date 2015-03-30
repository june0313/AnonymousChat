

import java.io.*;
import java.net.*;
import java.util.*;

import ssg.chat.SubjectInfo;

public class ChatServer {
	int port;								// port number
	ServerSocket serverSocket;				// ���� ����
	Socket s;								// Ŭ���̾�Ʈ�� ����ϱ� ���� ����
	BufferedReader br;
	
	PrintWriter pw;
	Vector<ServerThread> v;					// �����带 �����ϴ� ���� ��ü
	
	ArrayList<SubjectInfo> subjInfoList;	// ���� ���� ����Ʈ
	
	public ChatServer(int port) {
		this.port = port;
		
		getSubjInfo();	// ���� ����� �ҷ��´�.
		
		v = new Vector<ServerThread>(10, 10); // �����带 �����ϴ� ���� ��ü ����
		try {
			serverSocket = new ServerSocket(port); // ��Ʈ�� �����Ͽ� �������� ����
			System.out.println("������" + port + "���� ���� �����");
			
			// Ŭ���̾�Ʈ�� ������ �׻� �޾Ƶ��� �� �ֵ��� ���ѷ����� ����
			while (true) {
				// Ŭ���̾�Ʈ�� ������ ��ٸ�
				s = serverSocket.accept();
				
				// Ŭ���̾�Ʈ�� �����ϸ� �����͸� �ۼ��� �ϱ����� �����带 ����� 
				// ������ ��ü�� �����ڷ� ���� ���ϰ� Ŭ���̾�Ʈ ������ �Ѱ���
				ServerThread serverThread = new ServerThread(this, s);
				
				// ������ ������ ��ü�� ���Ϳ� �߰��Ѵ�
				this.addThread(serverThread);
				
				// ������ ����
				serverThread.start();
			}
		} catch (IOException ioe) {
			// ���ܿ� ���� ó��
		}
	}
	
	// ���� ������ ����Ʈ�� ��´�.
	public void getSubjInfo() {
		System.out.println("���� ����� �ҷ�������...");
		subjInfoList = ExcelHandler.ExcelParser();
		
		for( int i = 0; i < subjInfoList.size(); i++ ) {
			System.out.print(subjInfoList.get(i).getSubjectName() + " / ");
			System.out.print(subjInfoList.get(i).getSubjectNo() + " / ");
			System.out.println(subjInfoList.get(i).getClassNo());
		}
		
		System.out.println(subjInfoList.size() + "���� ���� �ҷ����� ����!\n");
	}
	
	
	public void addThread(ServerThread st) {
		v.addElement(st);
	}
	
	public void removeThread(ServerThread st) {
		v.removeElement(st);
	}
	
	/*
	 * ���� �޽����� ��� Ŭ���̾�Ʈ���� ��ε�ĳ���� �ϱ� ���� �޼���
	 */
	public void broadCast(String classId, String msg) {
		for( int i = 0; i < v.size(); i++ ) {
			ServerThread st = v.elementAt(i); // ���Ϳ� ��ϵ� ��� �����带 ���´�.
			
			// classId�� ��ġ�ϴ� �����常
			// if( st.classId.equals(classId))
				st.sendMessage(msg); // �� �����忡 �޽��� ����
		}
	}
	
	
	public static void main(String[] args) {
		new ChatServer(9999);
	}
	
	
	
	class ServerThread extends Thread {
		ChatServer server;
		Socket s; 				// Ŭ���̾�Ʈ�� ����ϱ� ���� ����
		BufferedReader br; 		// Ŭ���̾�Ʈ�κ��� �����͸� ���Źޱ� ���� ��Ʈ��
		BufferedWriter bw;
		String classId = "1";	// �� ä�ù��� �����ϱ� ���� ID
		
		/*
		 * PrintWriter : 
		 * �پ��� ������ �����͸� ���ڿ��� ����(println)�� ����ϰų�, ���ڿ��� ���·� ����(printf)�Ͽ� ����Ѵ�. 
		 */
		PrintWriter pw; // Ŭ���̾�Ʈ�� �����͸� �۽��ϱ� ���� ��Ʈ��
		String message;

		ObjectOutputStream oos;
		
		/*
		 * ������ : ����� ��Ʈ���� ����
		 */
		public ServerThread(ChatServer server, Socket s) throws IOException{
			this.server = server;
			this.s = s; // Ŭ���̾�Ʈ�� ����� �� �ִ� ���� ������ s�� ������.
			
			// Ŭ���̾�Ʈ�� �޽����� �ְ� �ޱ� ���� ����� ��Ʈ���� �����´�
			this.oos = new ObjectOutputStream( s.getOutputStream() );// ��ü ��� ��Ʈ��
			oos.flush();
			this.br = new BufferedReader( new InputStreamReader(s.getInputStream(), "EUC-KR")); // �Է� ��Ʈ��
			this.pw = new PrintWriter( s.getOutputStream(), true); // ��� ��Ʈ��	
			
			
			// Ŭ���̾�Ʈ ���� IP ������ ��� ����Ѵ�.
			System.out.println(s.getInetAddress() + "���� ������");
		}
		
		public void sendMessage(String str) {
			pw.println(str);			
		}
		
		@Override
		public void run() {
			//Ŭ���̾�Ʈ�κ��� ���� �����͸� Ŭ���̾�Ʈ���� �۽��Ѵ�.
			try {
				// �Է� ��Ʈ��(br)�� ���� Ŭ���̾�Ʈ�� ���� �޽����� �о��
				while((message = br.readLine()) != null ) {
					// Ŭ���̾�Ʈ�κ��� ���� ��� ��û�� ������ ���� ����� �����Ѵ�.
					if(message.equals("REQUEST SUBJECT LIST"))
						oos.writeObject(subjInfoList);
					// �Ϲ� �޽����� ��� ��� Ŭ���̾�Ʈ���� �ѷ���
					else 
						server.broadCast("12345", message); 
				}
			} catch (IOException ioe) {
				System.out.println(ioe);
				server.removeThread(this);
			} finally {
				System.out.println(s.getInetAddress() + "���� ���� ����");
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