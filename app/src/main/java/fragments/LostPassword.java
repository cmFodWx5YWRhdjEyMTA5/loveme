package fragments;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.meetapp.free.loveme.R;

import include.AsyncRequest;
import include.AsyncResponse;
import include.IFY;

public class LostPassword extends Activity implements AsyncResponse {

	public AsyncResponse delegate = null;
	private AsyncRequest request;
	private IFY ify;
	private Activity currAtivity;
	private ProgressBar progressBar;
	protected EditText edtlostEmail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.lost_password);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		Button btnLostPassword = (Button) findViewById(R.id.btnLostPassword);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setVisibility(View.GONE);

		ify = new IFY();
		ify.init(getBaseContext());
		currAtivity = this;

		btnLostPassword.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				edtlostEmail = (EditText) findViewById(R.id.edtlostEmail);

				String email = edtlostEmail.getText().toString();
				if (email.isEmpty()) {

					Toast.makeText(getBaseContext(),
							"Please provide valid email!", Toast.LENGTH_LONG)
							.show();
					return;
				}

				progressBar.setVisibility(View.VISIBLE);

				String url = IFY.SERVICE_URL + "lost_password.php?email="
						+ email;

				request = new AsyncRequest();
				request.delegate = (AsyncResponse) currAtivity;
				request.execute(url);
			}
		});
	}

	@Override
	public void processFinish(String output) {

		output = output.replace("\"", "");

		Toast.makeText(getBaseContext(), output, Toast.LENGTH_LONG).show();

		edtlostEmail.setText("");

		progressBar.setVisibility(View.GONE);
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