package fragments;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.meetapp.free.loveme.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import include.AsyncBitmap;
import include.AsyncRequest;
import include.AsyncResponse;
import include.ChatMessage;
import include.EmoticonsGridAdapter;
import include.EmoticonsPagerAdapter;
import include.IFY;
import include.IFY.User;
import include.IntentHelper;
import include.MySocket;
import include.PullToRefreshListView;
import include.PullToRefreshListView.OnRefreshListener;
import upload.ChatUploadActivity;

@SuppressLint("InflateParams")
public class ChatView extends Activity implements AsyncResponse, EmoticonsGridAdapter.KeyClickListener {

	private IFY ify;
	public AsyncResponse delegate = null;
	private AsyncRequest request;
	private EditText edtMessage;
	private User user;
	private ArrayList<ChatMessage> messages;
	private AsyncBitmap asyncBitmap;
	private LinearLayout linearLayout;
	private ImageView imageView;
	private Button btn_chat_send;
	private TextView seen_msg;
	private LinearLayout seen_msg_panel;
	private ProgressBar progressBar;
	private int limitedMessage = 10;
	private RelativeLayout pull_to_refresh_header;
	private Button btn_chat_upload;
	private String imagepath;
	private Uri fileUri;

	public static Runnable runnable = null;
	public Handler handler = null;

	private View popUpView;
	private PopupWindow popupWindow;
	private boolean isKeyBoardVisible;
	private int keyboardHeight;
	private LinearLayout emoticonsCover;
	private LinearLayout parentLayout;
	private static final int GALLERY_PICTURE = 1;
	private static final int COMPRESS = 100 ;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.fragment_chat_view);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		getActionBar().setIcon(
				new ColorDrawable(getResources().getColor(
						android.R.color.transparent)));

		ify = new IFY();
		ify.init(this.getBaseContext());

		// init controls
		seen_msg_panel = (LinearLayout) findViewById(R.id.seen_msg_panel);
		seen_msg = (TextView) findViewById(R.id.seen_msg);
		IFY.ChatListView = (ListView) findViewById(R.id.msgview);

		edtMessage = (EditText) findViewById(R.id.msg);

		btn_chat_upload = (Button) findViewById(R.id.chat_upload);
		btn_chat_send = (Button) findViewById(R.id.send);

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		imageView = (ImageView) findViewById(R.id.userPhoto);
		linearLayout = (LinearLayout) findViewById(R.id.userPhoto_layout);
		pull_to_refresh_header = (RelativeLayout) findViewById(R.id.pull_to_refresh_header);
		pull_to_refresh_header.setVisibility(View.GONE);

		linearLayout.setVisibility(View.GONE);
		seen_msg_panel.setVisibility(View.GONE);

		// refresh ads
		handler = new Handler();
		runnable = new Runnable() {
			public void run() {
				handler.postDelayed(runnable, 15000);
			}

		};

		handler.postDelayed(runnable, 15000);

		initAdapter();
		initEmoji();

		if (messages == null) {
			user = (User) IntentHelper.getObjectForKey("key");
			progressBar.setVisibility(View.VISIBLE);
			// load last user messages
			if (user != null) {
				chatMessages();
				setTitle(user.getUsername() + ", " + user.getAge());
			}
		}

		btn_chat_upload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
						Intent.createChooser(intent, "Complete action using"),
						GALLERY_PICTURE);

			}
		});

		// create new instance main socket
		IFY.Mysocket = new MySocket(ify.context);

		/*---------------------------1----------------------------- */

		btn_chat_send.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				String message = edtMessage.getText().toString();

				if (!message.isEmpty()) {

					int idnex = Html.toHtml(edtMessage.getText())
							.indexOf("src");

					if (idnex >= 0)
						ify.currUser.sendNewMessage(
								Html.toHtml(edtMessage.getText()), user, false);
					else
						ify.currUser.sendNewMessage(message, user, false);

					edtMessage.setText("");
					IFY.Mysocket.stop_typing("", user.getId());

					setTitle(user.getUsername() + ", " + user.getAge());

					linearLayout.setVisibility(View.GONE);
					seen_msg_panel.setVisibility(View.GONE);
					pull_to_refresh_header.setVisibility(View.VISIBLE);
				}
			}
		});

		edtMessage.addTextChangedListener(new TextWatcher() {

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (user != null)
					IFY.Mysocket.typing("", user.getId());
			}
		});

		/*-----------------------------2--------------------------- */
		IFY.Mysocket.socket.on("send_message", new Emitter.Listener() {

			@Override
			public void call(final Object... arg0) {

				if (this == null)
					return;

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						JSONObject data = (JSONObject) arg0[0];

						try {
							JSONObject content = new JSONObject(data.toString());

							String json = content.getString("content");
							JSONObject relJosn = new JSONObject(json);

							String message = relJosn.getString("message");
							int user_id = relJosn.getInt("user_id");

							if (user_id == user.getId()) {

								if (message.equals("Photo notification")) {

									limitedMessage = 10;
									initAdapter();
									chatMessages();

								} else {

									IFY.chatArrayAdapter.add(new ChatMessage(
											true, message, false));

									IFY.ChatListView
											.setSelection(IFY.chatArrayAdapter
													.getCount() - 1);
									IFY.Mysocket.send_seen("", user_id);
									seen_msg_panel.setVisibility(View.GONE);
								}
							}

						} catch (JSONException e) {
							return;
						}

					}
				});

			}

		});

		IFY.Mysocket.socket.on("typing", new Emitter.Listener() {

			@Override
			public void call(final Object... arg0) {

				if (this == null)
					return;

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						JSONObject data = (JSONObject) arg0[0];

						try {
							JSONObject content = new JSONObject(data.toString());

							String json = content.getString("content");
							JSONObject relJosn = new JSONObject(json);

							int user_id = relJosn.getInt("user_id");

							if (user_id == user.getId()) {
								setTitle("typing...");
							}

						} catch (JSONException e) {
							return;
						}

					}
				});

			}

		});

		IFY.Mysocket.socket.on("stop_typing", new Emitter.Listener() {

			@Override
			public void call(final Object... arg0) {

				if (this == null)
					return;

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						setTitle(user.getUsername() + ", " + user.getAge());

					}
				});

			}

		});

		IFY.Mysocket.socket.on("seen", new Emitter.Listener() {

			@Override
			public void call(final Object... arg0) {

				if (this == null)
					return;

				runOnUiThread(new Runnable() {
					@SuppressLint("SimpleDateFormat")
					@Override
					public void run() {

						seen_msg_panel.setVisibility(View.VISIBLE);
						SimpleDateFormat time = new SimpleDateFormat("HH:mm");
						String currTime = time.format(new Date());
						String s = "<b>Seen</b><br/>" + "<small><i>" + currTime
								+ "</i></small>";

						seen_msg.setText(Html.fromHtml(s));
						IFY.ChatListView.setSelection(IFY.chatArrayAdapter
								.getCount() - 1);

					}
				});

			}

		});

	}

	private void chatMessages() {

		// prepare server request
		request = new AsyncRequest();
		request.delegate = this; // listen for callback

		String url = IFY.SERVICE_URL + "chat_messages.php?user_id="
				+ ify.currUser.getId() + "&send_to=" + user.getId()
				+ "&limitedMessage=" + limitedMessage + "&user_hash="
				+ ify.currUser.getUser_hash();

		request.execute(url);

	}

	@Override
	public void processFinish(String output) {

		// get call back from request and set listview
		ChatMessage Message = new ChatMessage(ify.context);

		messages = Message.parseJson(output);

		if (messages.isEmpty())
			linearLayout.setVisibility(View.VISIBLE);
		else {
			pull_to_refresh_header.setVisibility(View.VISIBLE);

			for (ChatMessage message : messages) {

				message.left = !(message.user_id == ify.currUser.getId());
				message.hasPhoto = message.ThumbName.length() > 0;
				IFY.chatArrayAdapter.add(message);
			}

			IFY.ChatListView.postDelayed(new Runnable() {
				@Override
				public void run() {
					IFY.ChatListView.setSelection(IFY.chatArrayAdapter
							.getCount() - 1);

					IFY.ChatListView
							.setOnItemClickListener(new OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> parent,
										View view, int position, long id) {

                                    checkForUrl(position);
                                    checkForPhoto(position);
								}

							});

				}
			}, 500);

		}

		asyncBitmap = new AsyncBitmap();
		asyncBitmap.delegate = this;
		asyncBitmap.execute(user.getThumbName());

		progressBar.setVisibility(View.GONE);

		// Set a listener to be invoked when the list should be refreshed.
		((PullToRefreshListView) IFY.ChatListView)
				.setOnRefreshListener(new OnRefreshListener() {

					@Override
					public void onRefresh() {
						progressBar.setVisibility(View.VISIBLE);
						limitedMessage = limitedMessage + 10;
						initAdapter();
						chatMessages();

					}

				});

	}


	private void checkForUrl(int position) {

		String message = IFY.chatArrayAdapter.getItem(position - 1).message;
		List<String> extractUrls = extractUrls(message);
		if (!extractUrls.isEmpty()) {

			Toast.makeText(ify.context, "Please wait...", Toast.LENGTH_LONG)
					.show();

			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(extractUrls
					.get(0)));
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
		}

	}

	private void checkForPhoto(int position) {

		ChatMessage ch_message = messages.get(position - 1);

		if (ch_message.ThumbName.isEmpty())
			return;
		if (ch_message.ImageName.isEmpty())
            return;

		IntentHelper.addObjectForKey(ch_message, "key");

		Intent i = new Intent(ify.context, ChatImagePreview.class);
		startActivity(i);

	}

	private void initAdapter() {

		IFY.ChatListView.setAdapter(null);
		IFY.chatArrayAdapter = new ChatArrayAdapter(ify.context, R.layout.right);
		IFY.ChatListView.setAdapter(IFY.chatArrayAdapter);
		IFY.ChatListView
				.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		IFY.ChatListView.setAdapter(IFY.chatArrayAdapter);

	}

	public static List<String> extractUrls(String text) {
		List<String> containedUrls = new ArrayList<String>();
		String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
		Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
		Matcher urlMatcher = pattern.matcher(text);

		while (urlMatcher.find()) {
			containedUrls.add(text.substring(urlMatcher.start(0),
					urlMatcher.end(0)));
		}

		return containedUrls;
	}

	@Override
	public void processBitmapFinish(Bitmap output) {

		// set start chat image view
		if (messages.isEmpty()) {

			TextView textView = (TextView) findViewById(R.id.txt_username);

			// set title image view
			if (output != null)
				imageView.setImageBitmap(IFY
						.getRoundedCornerBitmap(output, 100));
			else {

				if (user.getNumber_gender() == 1)
					imageView.setBackgroundResource(R.drawable.male);
				else
					imageView.setBackgroundResource(R.drawable.female);

			}

			String username = "<br/><b>" + user.getUsername() + ", "
					+ user.getAge() + "</b>";

			textView.append(Html.fromHtml(username));

			imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					IntentHelper.addObjectForKey(user, "key");

					Intent i = new Intent(getBaseContext(), ImagePreview.class);
					startActivity(i);
				}

			});

		}

		setupActionBar(output);
		((PullToRefreshListView) IFY.ChatListView).onRefreshComplete();

		IFY.adChatViewCount++;
		if (IFY.adChatViewCount == IFY.maxAdCount) {

			IFY.adChatViewCount = 0;
			ify.InterstitialAd();

		}

	}

	@Override
	public void processMessageFinish(String output) {

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		return false;
	}

	@SuppressLint({ "NewApi", "InflateParams" })
	private void setupActionBar(Bitmap output) {

		ActionBar ab = getActionBar();
		ab.setDisplayShowCustomEnabled(true);
		ab.setDisplayShowTitleEnabled(true);

		LayoutInflater inflator = (LayoutInflater) getBaseContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflator.inflate(R.layout.action_bar_title, null);

		ImageView titleImageView = (ImageView) v.findViewById(R.id.userPhoto);

		// set title image view
		if (output != null)
			titleImageView.setImageBitmap(IFY.getRoundedCornerBitmap(output,
					100));
		else {

			if (user.getNumber_gender() == 1)
				titleImageView.setBackgroundResource(R.drawable.male);
			else
				titleImageView.setBackgroundResource(R.drawable.female);

		}

		ab.setCustomView(v);

		setTitle(user.getUsername() + ", " + user.getAge());

		titleImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				IntentHelper.addObjectForKey(user, "key");

				Intent i = new Intent(ify.context, UserProfileView.class);
				startActivity(i);

			}

		});

	}

	@Override
	public void onDestroy() {
		IFY.Mysocket.socket.disconnect();
		handler.removeCallbacks(runnable);
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle action bar actions click
		switch (item.getItemId()) {
		case android.R.id.home: {

			finish();
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == GALLERY_PICTURE && resultCode == Activity.RESULT_OK)
			launchUploadFromGallery(data);

		else {

			//
		}

	}

	private void launchUploadFromGallery(Intent data) {

		if (Build.VERSION.SDK_INT < 19) {
			Uri selectedImageUri = data.getData();
			imagepath = getPath(selectedImageUri);
			File imageFile = new File(imagepath);

			fileUri = Uri.fromFile(imageFile);
			IntentHelper.addObjectForKey(fileUri, "file_uri");

			launchUploadActivity(true);
		}
		else
		{
			InputStream imInputStream = null;
			try {
				imInputStream = getContentResolver().openInputStream(data.getData());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			Bitmap bitmap = BitmapFactory.decodeStream(imInputStream);
			String smallImagePath = saveGalaryImageOnLitkat(bitmap);

			File imageFile = new File(smallImagePath);

			fileUri = Uri.fromFile(imageFile);
			IntentHelper.addObjectForKey(fileUri, "file_uri");

			launchUploadActivity(true);
		}
	}

	private String saveGalaryImageOnLitkat(Bitmap bitmap) {
		try {
			File cacheDir;
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				cacheDir = new File(Environment.getExternalStorageDirectory(), getResources().getString(R.string.app_name));
			else
				cacheDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
			if (!cacheDir.exists())
				cacheDir.mkdirs();
			String filename = System.currentTimeMillis() + ".jpg";
			File file = new File(cacheDir, filename);
			File temp_path = file.getAbsoluteFile();
			// if(!file.exists())
			file.createNewFile();
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS, out);
			return file.getAbsolutePath();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}
	public String getPath(Uri uri) {

		String res = null;
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = ify.context.getContentResolver().query(uri, proj, null,
				null, null);
		if (cursor.moveToFirst()) {
			;
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			res = cursor.getString(column_index);
		}
		cursor.close();
		return res;

	}

	private void launchUploadActivity(boolean isImage) {

		IntentHelper.addObjectForKey(user, "key");
		fileUri = (Uri) IntentHelper.getObjectForKey("file_uri");

		Intent i = new Intent(ify.context, ChatUploadActivity.class);
		i.putExtra("filePath", fileUri.getPath());
		i.putExtra("isImage", isImage);
		startActivity(i);
	}

	private void initEmoji() {

		parentLayout = (LinearLayout) findViewById(R.id.list_parent);

		emoticonsCover = (LinearLayout) findViewById(R.id.footer_for_emoticons);

		popUpView = getLayoutInflater().inflate(R.layout.emoticons_popup, null);

		// Defining default height of keyboard which is equal to 230 dip
		final float popUpheight = getResources().getDimension(
				R.dimen.keyboard_height);
		changeKeyboardHeight((int) popUpheight);

		// Showing and Dismissing pop up on clicking emoticons button
		ImageView emoticonsButton = (ImageView) findViewById(R.id.emoticons_button);
		emoticonsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!popupWindow.isShowing()) {

					popupWindow.setHeight((int) (keyboardHeight));

					if (isKeyBoardVisible) {
						emoticonsCover.setVisibility(LinearLayout.GONE);
					} else {
						emoticonsCover.setVisibility(LinearLayout.VISIBLE);
					}
					popupWindow.showAtLocation(parentLayout, Gravity.BOTTOM, 0,
							0);

				} else {
					popupWindow.dismiss();
				}

			}
		});

		enablePopUpView();
		checkKeyboardHeight(parentLayout);
		enableFooterView();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (popupWindow.isShowing()) {
			popupWindow.dismiss();
			return false;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	private void enablePopUpView() {

		ViewPager pager = (ViewPager) popUpView
				.findViewById(R.id.emoticons_pager);
		pager.setOffscreenPageLimit(3);

		ArrayList<String> paths = new ArrayList<String>();

		for (short i = 1; i <= IFY.NO_OF_EMOTICONS; i++) {
			paths.add(i + ".png");
		}

		EmoticonsPagerAdapter adapter = new EmoticonsPagerAdapter(
				ChatView.this, paths, this);

		pager.setAdapter(adapter);

		// Creating a pop window for emoticons keyboard
		popupWindow = new PopupWindow(popUpView, LayoutParams.MATCH_PARENT,
				(int) keyboardHeight, false);

		TextView backSpace = (TextView) popUpView.findViewById(R.id.back);
		backSpace.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0,
						0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
				edtMessage.dispatchKeyEvent(event);
			}
		});

		popupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				emoticonsCover.setVisibility(LinearLayout.GONE);
			}
		});
	}

	private void changeKeyboardHeight(int height) {

		if (height > 100) {
			keyboardHeight = height;
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, keyboardHeight - 75);
			emoticonsCover.setLayoutParams(params);
		}

	}

	int previousHeightDiffrence = 0;

	private void checkKeyboardHeight(final View parentLayout) {

		parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {

						Rect r = new Rect();
						parentLayout.getWindowVisibleDisplayFrame(r);

						int screenHeight = parentLayout.getRootView()
								.getHeight();
						int heightDifference = screenHeight - (r.bottom);

						if (previousHeightDiffrence - heightDifference > 50) {
							popupWindow.dismiss();
						}

						previousHeightDiffrence = heightDifference;
						if (heightDifference > 100) {

							isKeyBoardVisible = true;
							changeKeyboardHeight(heightDifference);

						} else {

							isKeyBoardVisible = false;

						}

					}
				});

	}

	private void enableFooterView() {

		edtMessage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (popupWindow.isShowing()) {

					popupWindow.dismiss();

				}

			}
		});
	}

	@Override
	public void keyClickedIndex(final String index) {

		ImageGetter imageGetter = new ImageGetter() {
			public Drawable getDrawable(String source) {
				StringTokenizer st = new StringTokenizer(index, ".");
				Drawable d = new BitmapDrawable(getResources(),
						IFY.emoticons[Integer.parseInt(st.nextToken()) - 1]);
				d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
				return d;
			}
		};

		Spanned cs = Html.fromHtml("<img src ='" + index + "'/>", imageGetter,
				null);

		int cursorPosition = edtMessage.getSelectionStart();
		edtMessage.getText().insert(cursorPosition, cs);

	}

}