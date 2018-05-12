package include;

import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.IO.Options;
import com.github.nkzawa.socketio.client.Socket;

import android.content.Context;
import android.util.Log;

public class MySocket extends IFY {

	public Socket socket;
	private Options opts;

	public MySocket(Context _context) {

		super();
		init(_context);

		try {
			opts = new Options();
			opts.forceNew = true;

			socket = IO.socket("http://ifychat.herokuapp.com:80", opts);

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				Log.d("ify - EVENT_CONNECT", "EVENT_CONNECT");
				join();
			}

		}).on("event", new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				//
			}

		}).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

			@Override
			public void call(Object... args) {

				Log.d("ify - EVENT_DISCONNECT", "EVENT_DISCONNECT");
			}

		}).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

			@Override
			public void call(Object... args) {

				Log.d("ify - EVENT_CONNECT_ERROR", "EVENT_CONNECT_ERROR");
			}

		});

		socket.connect();

	}

	public void join() {

		JSONObject user = new JSONObject();
		try {
			String id = String.valueOf(currUser.getId());
			user.putOpt("id", id);
			socket.emit("join", user);

		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private JSONObject getUserData(String _message, int _send_to) {

		JSONObject message = new JSONObject();
		try {

			String user_id = String.valueOf(currUser.getId());
			String send_to = String.valueOf(_send_to);

			message.putOpt("user_id", user_id);
			message.putOpt("send_to", send_to);
			message.putOpt("username", currUser.getUsername());
			message.putOpt("date", "");
			message.putOpt("message", _message);
			message.putOpt("ImageName", currUser.ChatImageName);
			message.putOpt("ThumbName", currUser.ChatThumbName);
			message.putOpt("code", 0);
			message.putOpt("state", 1);
			message.putOpt("status", 0);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return message;

	}

	public void sendMessageTo(String _message, int _send_to) {

		JSONObject message = getUserData(_message, _send_to);
		if (socket != null) {
			socket.emit("send_message", message);
		}

	}

	public void typing(String _message, int _send_to) {

		JSONObject message = getUserData(_message, _send_to);
		if (socket != null) {
			socket.emit("typing", message);
		}

	}

	public void stop_typing(String _message, int _send_to) {

		JSONObject message = getUserData(_message, _send_to);
		if (socket != null) {
			socket.emit("stop_typing", message);
		}

	}

	public void send_seen(String _message, int _send_to) {

		JSONObject message = getUserData(_message, _send_to);
		if (socket != null) {
			socket.emit("seen", message);
		}

	}
}
