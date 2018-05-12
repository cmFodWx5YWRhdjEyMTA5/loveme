package fragments;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.meetapp.free.loveme.R;

import java.util.ArrayList;

import include.ChatMessage;
import lazylist.ChatImageLoader;

public class ChatArrayAdapter extends ArrayAdapter<ChatMessage> {

	private TextView chatText;
	private ArrayList<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();
	private ImageView chat_send_photo;
	private LayoutInflater inflater;
	private ChatMessage chatMessageObj;
	private String imgIndex;

	@Override
	public void add(ChatMessage object) {
		chatMessageList.add(object);
		super.add(object);
	}

	public ChatArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);

	}

	public int getCount() {
		return this.chatMessageList.size();
	}

	public ChatMessage getItem(int index) {
		return this.chatMessageList.get(index);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		chatMessageObj = getItem(position);

		View row = convertView;
		if (row == null) {
			inflater = (LayoutInflater) this.getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
		}

		if (chatMessageObj.left) {
			row = inflater.inflate(R.layout.right, parent, false);
			chat_send_photo = (ImageView) row
					.findViewById(R.id.right_chat_send_photo);
		} else {
			row = inflater.inflate(R.layout.left, parent, false);
			chat_send_photo = (ImageView) row
					.findViewById(R.id.left_chat_send_photo);
		}

		chatText = (TextView) row.findViewById(R.id.msgr);

		if (chatMessageObj.message.length() > 0) {

			chat_send_photo.setVisibility(View.GONE);
			if ((chatMessageObj.hasPhoto)
					&& (chatMessageObj.ThumbName.length() > 0)) {

				if (chatMessageObj.ThumbName != null) {

					chat_send_photo.getLayoutParams().height = 220;
					chat_send_photo.getLayoutParams().width = 220;

					chat_send_photo.setVisibility(View.VISIBLE);
					ChatImageLoader ChatLoader = new ChatImageLoader(
							chatMessageObj.context);
					ChatLoader.DisplayImage(chatMessageObj.ThumbName,
							chat_send_photo);

					chatText.setVisibility(View.GONE);
				}
			} else {

				int index = chatMessageObj.message.indexOf(".png");

				if (index == -1) {

					chatText.setVisibility(View.VISIBLE);
					String s = "<b>" + chatMessageObj.message + "</b><br/>"
							+ "<small><i>" + chatMessageObj.date
							+ "</i></small>";

					chatText.setText(Html.fromHtml(s));

				} else {

					imgIndex = chatMessageObj.message.substring(index - 2,
							index);

					imgIndex = imgIndex.replace("\"", "");

					String s = "<b>" + chatMessageObj.message + "</b>"
							+ "<small><i>" + chatMessageObj.date
							+ "</i></small>";

					Spanned cs = Html.fromHtml(s,
							chatMessageObj.imageGetter(imgIndex), null);
					chatText.setText(cs);
				}

			}
		}

		return row;
	}

}
