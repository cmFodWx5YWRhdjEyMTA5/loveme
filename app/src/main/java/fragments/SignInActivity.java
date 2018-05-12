package fragments;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.meetapp.free.loveme.MainActivity;
import com.meetapp.free.loveme.R;

import java.util.ArrayList;

import include.AsyncRequest;
import include.AsyncResponse;
import include.IFY;
import include.IFY.User;

public class SignInActivity extends Activity implements AsyncResponse {

	public AsyncResponse delegate = null;
	private AsyncRequest request;
	private IFY ify;
	private Activity currAtivity;
	private ProgressBar progressBar;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_sign_in_screen);


		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		getActionBar().setIcon(
				new ColorDrawable(getResources().getColor(
						android.R.color.transparent)));

		
		Button btnSingIn = (Button) findViewById(R.id.btnSingIn);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		ify = new IFY();
		ify.init(getBaseContext());
		currAtivity = this;
		
		progressBar.setVisibility(View.GONE);

		btnSingIn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				EditText username = (EditText) findViewById(R.id.etUserName);
				EditText password = (EditText) findViewById(R.id.etPass);

				String _username = username.getText().toString();
				String _passwrod = password.getText().toString();

				if ((_username.isEmpty()) || (_passwrod.isEmpty())) {

					Toast.makeText(getBaseContext(),
							"Please provide valid username and password!",
							Toast.LENGTH_LONG).show();
					return;
				}
				String params = "username=" + _username + "&password="
						+ _passwrod;

				progressBar.setVisibility(View.VISIBLE);
				String url = IFY.SERVICE_URL + "sign_in.php?" + params;
				request = new AsyncRequest();
				request.delegate = (AsyncResponse) currAtivity;
				request.execute(url);
			}
		});
	}

	@Override
	public void processFinish(String output) {

		progressBar.setVisibility(View.GONE);
		ArrayList<User> users = ify.parseJson(output);
		if (!users.isEmpty()) {
			ify.currUser = users.get(0);
			ify.setSession(true);
			restartSelf();
		} else {
			Toast.makeText(getBaseContext(),
					"Please provide valid username and password!",
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