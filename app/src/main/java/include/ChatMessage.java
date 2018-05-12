package include;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Html.ImageGetter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatMessage {
	public boolean left;
	public String username;
	public String message;
	public int user_id;
	public int send_to;
	public String date;
	public String ThumbName;
	public String ImageName;
	public ChatMessage ChatMessage;
	public int code;
	public boolean hasPhoto;
	public static Context context;

	public ChatMessage(Context _context) {

		context = _context;
		this.left = false;
		this.username = "";
		this.message = "";
		this.user_id = 0;
		this.send_to = 0;
		this.date = "";
		this.ThumbName = "";
		this.ImageName = "";
		this.ChatMessage = null;
		this.code = 0;
		this.hasPhoto = false;

	}

	@SuppressLint("SimpleDateFormat")
	public ChatMessage(boolean left, String message, boolean hasPhoto) {
		super();

		this.left = left;
		this.message = message;
		this.hasPhoto = hasPhoto;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String currentDateandTime = sdf.format(new Date());
		this.date = currentDateandTime;
	}

	public ChatMessage(JSONObject element) throws JSONException {

		this.user_id = element.getInt("user_id");
		this.send_to = element.getInt("send_to");

		this.username = element.getString("username");

		if (element.has("date"))
			this.date = element.getString("date");
		else
			this.message = "";

		if (element.has("Chat_ImageName"))
			this.ThumbName = element.getString("Chat_ThumbsName");
		else
			this.ThumbName = "";

		if (element.has("Chat_ThumbsName"))
			this.ImageName = element.getString("Chat_ImageName");
		else
			this.ImageName = "";

		if (element.has("message"))
			this.message = element.getString("message");
		else
			this.message = "";

		if (element.has("code"))
			this.code = element.getInt("code");
		else
			this.code = 0;
	}

	public ArrayList<ChatMessage> parseJson(String result) {

		JSONArray jsonArray = null;
		try {
			jsonArray = new JSONArray(result);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();
		for (int i = 0; i < jsonArray.length(); ++i) {

			JSONObject element = null;
			try {
				element = jsonArray.getJSONObject(i);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				ChatMessage = new ChatMessage(element);
				if (ChatMessage.user_id > 0)
					messages.add(ChatMessage);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return messages;

	}

	public ImageGetter imageGetter(final String imgIndex) {

		return new ImageGetter() {
			public Drawable getDrawable(String source) {
				Drawable d = new BitmapDrawable(context.getResources(),
						IFY.emoticons[Integer.valueOf(imgIndex) - 1]);
				d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
				return d;
			}
		};

	}

}