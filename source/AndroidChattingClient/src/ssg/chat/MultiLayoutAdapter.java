package ssg.chat;

import java.util.ArrayList;

import ssg.chat.ChattingActivity.MessageData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

// 채팅창(ListView)에 두 개의 레이아웃(본인의 메시지, 다른사람의 메시지)을 출력하기 위한 커스텀 어뎁터
class MultiLayoutAdapter extends BaseAdapter {

	LayoutInflater mInflater;
	ArrayList<MessageData> msgData;

	// 생성자
	public MultiLayoutAdapter(Context context, ArrayList<MessageData> _msgData) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		msgData = _msgData;
	}

	// 어뎁터가 참조하는 리스트의 개수를 리턴
	@Override
	public int getCount() {
		return msgData.size();
	}

	//
	@Override
	public Object getItem(int position) {
		return msgData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		return msgData.get(position).type;
	}

	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int res = 0;

		// 최초 호출시 아이템뷰를 생성한다.
		if (convertView == null) {

			// 현재 아이템의 타입을 체크한다.
			res = getItemViewType(position);

			switch (res) {
			case 0: /* 다른사람으로부터 받은 메시지 */
				res = R.layout.message_box;
				break;
			case 1: /* 자신이 전송한 메시지 */
				res = R.layout.my_message_box;
				break;
			}

			// 타입에 맞는 레이아웃으로 뷰를 구성한다.
			convertView = mInflater.inflate(res, parent, false);
		}

		// 항목뷰의 내용을 채워넣는다.
		TextView tvMsg = (TextView)convertView.findViewById(R.id.msg);
		tvMsg.setText(msgData.get(position).msg);
		
		res = getItemViewType(position);
		
		switch(res) {
		case 0: /* 다른사람으로부터 받은 메시지 */
			TextView tvUser = (TextView)convertView.findViewById(R.id.user);
			tvUser.setText(msgData.get(position).userName);
			break;
		}
		
		// 항목뷰를 리턴한다.
		return convertView;
	}
}
