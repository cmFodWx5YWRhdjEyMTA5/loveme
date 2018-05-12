package fragments;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.meetapp.free.loveme.MainActivity;
import com.meetapp.free.loveme.R;

import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;

import include.AsyncRequest;
import include.AsyncResponse;
import include.IFY;
import include.IFY.User;

@SuppressWarnings("deprecation")
public class facebook extends Activity implements AsyncResponse {

	private static String APP_ID = "2024292781135858";

	private Facebook facebook;
	private AsyncFacebookRunner mAsyncRunner;
	private SharedPreferences mPrefs;
	private View btnFbLogin;

	private IFY ify;
	public String username;
	public String email;
	public String faceUserName;
	public String gender;
	public String descr;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.facebook);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		getActionBar().setIcon(
				new ColorDrawable(getResources().getColor(
						android.R.color.transparent)));

		ify = new IFY();
		ify.init(getBaseContext());

		facebook = new Facebook(APP_ID);
		mAsyncRunner = new AsyncFacebookRunner(facebook);

		btnFbLogin = (Button) findViewById(R.id.btnFbLogin);

		btnFbLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				loginToFacebook();

			}
		});

	}

	public void loginToFacebook() {
		mPrefs = getPreferences(MODE_PRIVATE);
		String access_token = mPrefs.getString("access_token", null);
		long expires = mPrefs.getLong("access_expires", 0);

		if (access_token != null) {
			facebook.setAccessToken(access_token);
		}

		if (expires != 0) {
			facebook.setAccessExpires(expires);
		}

		if (!facebook.isSessionValid()) {
			Toast.makeText(getApplicationContext(), "Please wait...",
					Toast.LENGTH_LONG).show();

			facebook.authorize(this, new String[] { "email", "public_profile" }, new DialogListener() {

				@Override
				public void onCancel() {
					Toast.makeText(getApplicationContext(), "onCancel",
							Toast.LENGTH_LONG).show();
				}

				@Override
				public void onComplete(Bundle values) {

					SharedPreferences.Editor editor = mPrefs.edit();
					editor.putString("access_token", facebook.getAccessToken());
					editor.putLong("access_expires",
							facebook.getAccessExpires());
					editor.commit();

					getProfileInformation();

				}

				@Override
				public void onError(DialogError arg0) {
					Toast.makeText(getApplicationContext(), "onError",
							Toast.LENGTH_LONG).show();

				}

				@Override
				public void onFacebookError(FacebookError arg0) {
					Toast.makeText(getApplicationContext(), "onFacebookError",
							Toast.LENGTH_LONG).show();

				}

			});
		} else
			getProfileInformation();
	}

	@SuppressWarnings("deprecation")
	public void getProfileInformation() {
		mAsyncRunner.request("me", new RequestListener() {
			@Override
			public void onComplete(String response, Object state) {

				String json = response;
				try {
					JSONObject profile = new JSONObject(json);

					faceUserName = profile.getString("id");
					username = profile.getString("name");

					//Log.d("-------------", profile.toString());

					email = "bgjoin@gmail.com";
					if (profile.has("email"))
						email = profile.getString("email");

					gender = "Man";
					if (profile.has("gender")) {
						gender = profile.getString("gender");
						gender = (gender.equals("male")) ? "Man" : "Woman";
					}

					descr = "";
					if (profile.has("bio"))
						descr = profile.getString("bio");

					runOnUiThread(new Runnable() {

						@Override
						public void run() {

							if (email.isEmpty())
								Toast.makeText(
										getApplicationContext(),
										"Email addres is not valid, please check your Email validation period.",
										Toast.LENGTH_LONG).show();

							else
								user_registration();
						}

					});

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onIOException(IOException e, Object state) {
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {
			}

			@Override
			public void onMalformedURLException(MalformedURLException e,
					Object state) {
			}

			@Override
			public void onFacebookError(FacebookError e, Object state) {
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		facebook.authorizeCallback(requestCode, resultCode, data);
	}

	private void user_registration() {

		Toast.makeText(this.getApplicationContext(), "Please wait...",
				Toast.LENGTH_LONG).show();

		try {
			username = URLEncoder.encode(username, HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String sign_up_url = "username=" + username + "&password=" + "facebook"
				+ "&email=" + email + "&age=" + 18 + "&gender=" + gender
				+ "&descr=" + "Hi" + "&registration_type=3" + "&faceUserName="
				+ faceUserName;

		String url = IFY.SERVICE_URL + "sign_up.php?" + sign_up_url;

		AsyncRequest request = new AsyncRequest();
		request.delegate = this;

		request.execute(url);

	}

	@Override
	public void processFinish(String output) {

		if (output.length() > 10) {
			ArrayList<User> users = ify.parseJson(output);
			if (!users.isEmpty()) {
				ify.currUser = users.get(0);
				ify.setSession(true);
				restartSelf();
			}
		} else {
			Toast.makeText(getBaseContext(),
					"Sorry username already taken.Please choose another!",
					Toast.LENGTH_LONG).show();

		}

	}

	public void restartSelf() {

		Intent mStartActivity = new Intent(getBaseContext(), MainActivity.class);
		int mPendingIntentId = 123456;
		PendingIntent mPendingIntent = PendingIntent.getActivity(
				getBaseContext(), mPendingIntentId, mStartActivity,
				PendingIntent.FLAG_CANCEL_CURRENT);
		@SuppressWarnings("static-access")
		AlarmManager mgr = (AlarmManager) getBaseContext().getSystemService(
				getBaseContext().ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100,
				mPendingIntent);
		System.exit(0);
	}

	@Override
	public void processBitmapFinish(Bitmap output) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processMessageFinish(String output) {

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

}