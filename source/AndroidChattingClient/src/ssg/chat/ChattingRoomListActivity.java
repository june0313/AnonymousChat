package ssg.chat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ChattingRoomListActivity extends Activity implements OnItemClickListener {
	
	ArrayList<SubjectInfo> subjInfoList;
	ArrayList<HashMap<String,String>> subjectList;
	HashMap<String,String> subjInfo;
	SimpleAdapter sAdapter;
	ListView subjectListView;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.subject_list);
	    
	    // 액션바 설정
	    ActionBar actionbar = getActionBar();
	    actionbar.setTitle("SSG 익명 채팅");
	    actionbar.setSubtitle("강의 목록");
	}
	
	// 채팅방 입장
	@Override
	public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
		Intent intent = new Intent(ChattingRoomListActivity.this, ChattingActivity.class);
		intent.putExtra("subjectName", subjInfoList.get(position).getSubjectName());
		intent.putExtra("subjectNo", subjInfoList.get(position).getSubjectNo());
		intent.putExtra("classNo", subjInfoList.get(position).getClassNo());
		startActivity(intent);
	}
	
	// 메뉴 생성
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	// 메뉴 아이템 클릭시
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch(item.getItemId()) {
		case R.id.add_subject:
			intent = new Intent(ChattingRoomListActivity.this, SubjectListActivity.class);
			startActivity(intent);
			return true;
			
		default: 
			return false;
		}
	}
	
	public void onResume() {
		super.onResume();
		
		subjInfoList = new ArrayList<SubjectInfo>();
		subjectList = new ArrayList<HashMap<String,String>>();
		
		// 파일로부터 채팅 목록을 읽어온다.
	    FileInputStream fis = null;
	    ObjectInputStream ois = null;
	    
	    try {
	    	
			fis = openFileInput("subject.lst");
			ois = new ObjectInputStream( fis );
			subjInfoList = (ArrayList<SubjectInfo>) ois.readObject();
			
			
			for( int i = 0; i < subjInfoList.size(); i++ ) {
				subjInfo = new HashMap<String, String>();
				subjInfo.put("title", subjInfoList.get(i).getSubjectName());
				subjInfo.put("info", subjInfoList.get(i).getSubjectNo() + " / " + subjInfoList.get(i).getClassNo());
				subjectList.add(subjInfo);
			}
			
			ois.close();
			fis.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	    // 채팅 목록을 리스트뷰에 출력한다.
	    sAdapter = new SimpleAdapter(this, subjectList, android.R.layout.simple_list_item_2,
	    		new String[]{"title", "info"}, new int[]{android.R.id.text1, android.R.id.text2});
	    
	    subjectListView = (ListView)findViewById(R.id.subject_list);
	    subjectListView.setAdapter(sAdapter);
	    subjectListView.setOnItemClickListener(this);
	    
	    // 채팅 목록이 없을 때 보여줄 뷰 설정
	    View emptyView = (TextView)findViewById(R.id.empty);
	    subjectListView.setEmptyView(emptyView);
	}
}


