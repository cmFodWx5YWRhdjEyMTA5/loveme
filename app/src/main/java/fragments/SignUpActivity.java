package fragments;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.meetapp.free.loveme.MainActivity;
import com.meetapp.free.loveme.R;

import java.util.ArrayList;

import include.AsyncRequest;
import include.AsyncResponse;
import include.IFY;
import include.IFY.User;

public class SignUpActivity extends Activity implements AsyncResponse {

	public AsyncResponse delegate = null;
	private AsyncRequest request = null;
	private IFY ify;

	private ProgressBar progressBar;

	private EditText etUserName;
	private EditText etPass;
	private EditText etRePass;
	private EditText edtEmail;
	private EditText edtDesc;
	private String username;
	private String passwrod;
	private String re_passwrod;
	private String email;
	private String age;
	private String gender;
	private String descr;
	private Spinner user_selected_gender;
	private Spinner user_selected_age;
	private SignUpActivity currAtivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_sign_up_screen);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		getActionBar().setIcon(
				new ColorDrawable(getResources().getColor(
						android.R.color.transparent)));

		Button btnSingUp = (Button) findViewById(R.id.btnSingUp);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		etUserName = (EditText) findViewById(R.id.etUserName);
		etPass = (EditText) findViewById(R.id.etPass);
		etRePass = (EditText) findViewById(R.id.etRePass);

		edtEmail = (EditText) findViewById(R.id.edtEmail);
		edtDesc = (EditText) findViewById(R.id.edtDesc);
		user_selected_age = (Spinner) findViewById(R.id.user_selected_age);
		user_selected_gender = (Spinner) findViewById(R.id.user_selected_gender);

		ify = new IFY();
		ify.init(getBaseContext());
		currAtivity = this;

		initAges();
		initGender();

		progressBar.setVisibility(View.GONE);

		btnSingUp.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				username = etUserName.getText().toString();
				passwrod = etPass.getText().toString();
				re_passwrod = etRePass.getText().toString();
				email = edtEmail.getText().toString();
				descr = edtDesc.getText().toString();

				if ((username.length() <= 3)) {

					Toast.makeText(
							getBaseContext(),
							"Your username must be at least 4 characters long!",
							Toast.LENGTH_LONG).show();
					return;
				}

				if ((username.isEmpty()) || (passwrod.isEmpty())) {

					Toast.makeText(getBaseContext(),
							"Please provide a valid password!",
							Toast.LENGTH_LONG).show();
					return;
				}

				if ((passwrod.isEmpty()) || (re_passwrod.isEmpty())) {

					Toast.makeText(getBaseContext(),
							"Please provide valid username and password!",
							Toast.LENGTH_LONG).show();
					return;
				}

				if (!passwrod.equals(re_passwrod)) {
					Toast.makeText(
							getBaseContext(),
							"You must enter the same password twice in order to confirm it.",
							Toast.LENGTH_LONG).show();

					return;
				}

				if (email.length() <= 0) {
					Toast.makeText(getBaseContext(),
							"Enter your email address!", Toast.LENGTH_LONG)
							.show();

					return;
				}

				if (!isValidEmail(email)) {
					Toast.makeText(getBaseContext(),
							"Please provide a valid email address",
							Toast.LENGTH_LONG).show();

					return;
				}

				if (!isNumeric(age)) {
					Toast.makeText(getBaseContext(),
							"Please provide a valid age", Toast.LENGTH_LONG)
							.show();

					return;
				}

				if (gender == "") {
					Toast.makeText(getBaseContext(),
							"Please provide a valid gender", Toast.LENGTH_LONG)
							.show();

					return;
				}

				String sign_up_url = "username=" + username + "&password="
						+ passwrod + "&email=" + email + "&age=" + age
						+ "&gender=" + gender + "&descr=" + descr
						+ "&registration_type=1";

				progressBar.setVisibility(View.VISIBLE);

				String url = IFY.SERVICE_URL + "sign_up.php?" + sign_up_url;

				request = new AsyncRequest();
				request.delegate = (AsyncResponse) currAtivity;

				request.execute(url);

			}
		});

	}

	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional
												// '-' and decimal.
	}

	private void initAges() {

		ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter
				.createFromResource(this,
						R.array.user_age_spinner_dropdown_item,
						android.R.layout.simple_spinner_item);

		staticAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		user_selected_age.setAdapter(staticAdapter);

		age = "";

		user_selected_age
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {

						age = (position > 0) ? (String) parent
								.getItemAtPosition(position) : "";

					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub
					}
				});

	}

	private void initGender() {

		ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter
				.createFromResource(this,
						R.array.user_gender_spinner_dropdown_item,
						android.R.layout.simple_spinner_item);

		staticAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		user_selected_gender.setAdapter(staticAdapter);

		gender = "";

		user_selected_gender
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {

						if (position > 0) {
							gender = (String) parent
									.getItemAtPosition(position);
							gender = (gender.equals("Male")) ? "Man" : "Woman";

						} else
							gender = "";

					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub
					}
				});

	}

	@Override
	public void processFinish(String output) {

		progressBar.setVisibility(View.GONE);
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

	public final static boolean isValidEmail(CharSequence target) {
		return !TextUtils.isEmpty(target)
				&& android.util.Patterns.EMAIL_ADDRESS.matcher(target)
						.matches();
	}
}