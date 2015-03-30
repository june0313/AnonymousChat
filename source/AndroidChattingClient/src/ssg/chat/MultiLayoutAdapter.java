package ssg.chat;

import java.util.ArrayList;

import ssg.chat.ChattingActivity.MessageData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

// ä��â(ListView)�� �� ���� ���̾ƿ�(������ �޽���, �ٸ������ �޽���)�� ����ϱ� ���� Ŀ���� ���
class MultiLayoutAdapter extends BaseAdapter {

	LayoutInflater mInflater;
	ArrayList<MessageData> msgData;

	// ������
	public MultiLayoutAdapter(Context context, ArrayList<MessageData> _msgData) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		msgData = _msgData;
	}

	// ��Ͱ� �����ϴ� ����Ʈ�� ������ ����
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

		// ���� ȣ��� �����ۺ並 �����Ѵ�.
		if (convertView == null) {

			// ���� �������� Ÿ���� üũ�Ѵ�.
			res = getItemViewType(position);

			switch (res) {
			case 0: /* �ٸ�������κ��� ���� �޽��� */
				res = R.layout.message_box;
				break;
			case 1: /* �ڽ��� ������ �޽��� */
				res = R.layout.my_message_box;
				break;
			}

			// Ÿ�Կ� �´� ���̾ƿ����� �並 �����Ѵ�.
			convertView = mInflater.inflate(res, parent, false);
		}

		// �׸���� ������ ä���ִ´�.
		TextView tvMsg = (TextView)convertView.findViewById(R.id.msg);
		tvMsg.setText(msgData.get(position).msg);
		
		res = getItemViewType(position);
		
		switch(res) {
		case 0: /* �ٸ�������κ��� ���� �޽��� */
			TextView tvUser = (TextView)convertView.findViewById(R.id.user);
			tvUser.setText(msgData.get(position).userName);
			break;
		}
		
		// �׸�並 �����Ѵ�.
		return convertView;
	}
}
