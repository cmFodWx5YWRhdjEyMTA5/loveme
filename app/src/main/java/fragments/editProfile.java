package fragments;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.meetapp.free.loveme.R;

import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import include.AsyncRequest;
import include.AsyncResponse;
import include.IFY;
import include.IFY.User;

public class editProfile extends Activity implements AsyncResponse {

	public AsyncResponse delegate = null;
	private AsyncRequest request;
	private IFY ify;
	private ProgressBar progressBar;
	private SeekBar ageMinControl;
	private int minProgressChanged;
	private TextView age_text;

	private Switch switch_gender;

	private EditText edtEmail;
	private EditText edtDesc;

	private String email;
	private String gender;
	private String descr;
	private Button btnDone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		setContentView(R.layout.edit_profile);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setIcon(
				new ColorDrawable(getResources().getColor(
						android.R.color.transparent)));

		ify = new IFY();
		ify.init(getBaseContext());

		request = new AsyncRequest();
		request.delegate = this;

		setTitle(ify.currUser.getUsername() + ", " + ify.currUser.getAge());

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		ageMinControl = (SeekBar) findViewById(R.id.ageMinControl);
		age_text = (TextView) findViewById(R.id.age_text);

		edtEmail = (EditText) findViewById(R.id.edtEmail);
		edtDesc = (EditText) findViewById(R.id.edtDesc);
		switch_gender = (Switch) findViewById(R.id.switch_gender);
		btnDone = (Button) findViewById(R.id.btnDone);

		// init fields
		ageMinControl.setProgress(Integer.valueOf(ify.currUser.getAge()));
		age_text.setText("Age: " + ify.currUser.getAge());

		edtEmail.setText(ify.currUser.getEmail());
		edtDesc.setText(ify.currUser.getDescr());

		gender = ify.currUser.getGender();
		minProgressChanged = Integer.valueOf(ify.currUser.getAge());

		boolean isGender = (gender.equals("Man")) ? false : true;

		switch_gender.setChecked(isGender);

		switch_gender.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
										 boolean isChecked) {

				gender = (isChecked) ? "Woman" : "Man";

			}
		});

		ageMinControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {

				minProgressChanged = progress + 18;

				age_text.setText("Age - "
						+ Integer.valueOf(minProgressChanged));

			}

			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});

		btnDone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SaveSettings();
			}
		});

		progressBar.setVisibility(View.GONE);

	}

	private void SaveSettings() {

		email = edtEmail.getText().toString();
		descr = edtDesc.getText().toString();

		if (email.length() <= 0) {
			Toast.makeText(getBaseContext(), "Type your email address!",
					Toast.LENGTH_LONG).show();

			return;
		}

		if (!isValidEmail(email)) {
			Toast.makeText(getBaseContext(),
					"Please provide a valid email address", Toast.LENGTH_LONG)
					.show();

			return;
		}

		progressBar.setVisibility(View.VISIBLE);

		try {
			descr = URLEncoder.encode(descr, HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String url = IFY.SERVICE_URL + "edit_profile.php?user_id="
				+ ify.currUser.getId() + "&email=" + email + "&age="
				+ minProgressChanged + "&gender=" + gender + "&descr=" + descr
				+ "&user_hash=" + ify.currUser.getUser_hash();

		request.execute(url);
	}

	@Override
	public void processFinish(String output) {

		if (output.length() > 10) {
			ArrayList<User> users = ify.parseJson(output);
			if (!users.isEmpty()) {
				ify.currUser = users.get(0);
				ify.setSession(true);

				progressBar.setVisibility(View.GONE);
				finish();
			}
		}

	}

	@Override
	public void processBitmapFinish(Bitmap output) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processMessageFinish(String output) {

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);

		MenuItem action_you_tube = menu.findItem(R.id.action_you_tube);
		action_you_tube.setVisible(false);

		MenuItem action_refresh = menu.findItem(R.id.action_refresh);
		action_refresh.setVisible(false);

		MenuItem action_search = menu.findItem(R.id.action_search);
		action_search.setVisible(false);

		MenuItem action_settings = menu.findItem(R.id.action_settings);
		action_settings.setVisible(false);

		MenuItem action_menu_chat = menu.findItem(R.id.action_menu_chat);
		action_menu_chat.setVisible(false);

		MenuItem action_menu_done = menu.findItem(R.id.action_menu_done);
		action_menu_done.setVisible(false);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle action bar actions click
		switch (item.getItemId()) {
		case android.R.id.home: {

			finish();
		}

		case R.id.action_menu_done: {

			SaveSettings();
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