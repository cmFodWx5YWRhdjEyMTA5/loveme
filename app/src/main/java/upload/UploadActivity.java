package upload;

import include.IFY;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.meetapp.free.loveme.MainActivity;
import com.meetapp.free.loveme.R;

public class UploadActivity extends Activity {
	// LogCat tag
	private static final String TAG = MainActivity.class.getSimpleName();

	private String filePath = null;
	private ImageView imgPreview;
	private Button btnUpload;
	private File sourceFile;
	private IFY ify;

	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_upload);

		ify = new IFY();
		ify.init(getApplicationContext());

		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		btnUpload = (Button) findViewById(R.id.btnUpload);
		imgPreview = (ImageView) findViewById(R.id.imgPreview);

		// Receiving the data from previous activity
		Intent i = getIntent();

		// image or video path that is captured in previous activity
		filePath = i.getStringExtra("filePath");

		// boolean flag to identify the media type, image or video
		boolean isImage = i.getBooleanExtra("isImage", true);

		if (filePath != null) {
			// Displaying the image or video on the screen
			previewMedia(isImage);
		} else {
			Toast.makeText(getApplicationContext(),
					"Sorry, file path is missing!", Toast.LENGTH_LONG).show();
		}

		btnUpload.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// uploading the file to server
				progressBar.setVisibility(View.VISIBLE);

				new UploadFileToServer().execute();
			}
		});

	}

	/**
	 * Displaying captured image/video on the screen
	 * */
	private void previewMedia(boolean isImage) {
		// Checking whether captured media is image or video
		if (isImage) {
			imgPreview.setVisibility(View.VISIBLE);
			// bimatp factory
			BitmapFactory.Options options = new BitmapFactory.Options();

			// down sizing image as it throws OutOfMemory Exception for larger
			// images
			options.inSampleSize = 8;

			final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

			imgPreview.setImageBitmap(bitmap);
		} else {
			imgPreview.setVisibility(View.GONE);

		}
	}

	/**
	 * Uploading the file to server
	 * */
	private class UploadFileToServer extends AsyncTask<Void, Integer, String> {

		@Override
		protected void onPreExecute() {
			// setting progress bar to zero

			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			//
		}

		@Override
		protected String doInBackground(Void... params) {
			return uploadFile();
		}

		private String uploadFile() {
			String responseString = null;

			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(Config.FILE_UPLOAD_URL);

			try {

				AndroidMultiPartEntity entity = new AndroidMultiPartEntity();

				sourceFile = new File(filePath);

				// Adding file data to http body
				entity.addPart("file", sourceFile);

				// Extra parameters if you want to pass to server

				entity.addPart("test", "test"); // neznam zashto no bez tozi red
												// ne stava

				String id = Integer.toString(ify.currUser.getId());
				entity.addPart("id", id);
				entity.addPart("username", ify.currUser.getUsername());

				httppost.setEntity(entity);

				// Making server call
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity r_entity = response.getEntity();

				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 200) {
					// Server response
					responseString = EntityUtils.toString(r_entity);
				} else {
					responseString = "Error occurred! Http Status Code: "
							+ statusCode;
				}

			} catch (ClientProtocolException e) {
				responseString = e.toString();
			} catch (IOException e) {
				responseString = e.toString();
			}

			return responseString;

		}

		@Override
		protected void onPostExecute(String result) {
			Log.e(TAG, "Response from server: " + result);

			// showing the server response in an alert dialog
			showAlert(result);

			super.onPostExecute(result);
		}

	}

	/**
	 * Method to show alert dialog
	 * */
	private void showAlert(String message) {

		progressBar.setVisibility(View.GONE);

		ify.InterstitialAd();

		ify.currUser.setThumbName(IFY.IMAGE_URL + "uploads/thumbs/"
				+ ify.currUser.getUsername() + "_" + sourceFile.getName());
		ify.currUser.setImageName(IFY.IMAGE_URL + "uploads/"
				+ ify.currUser.getUsername() + "_" + sourceFile.getName());
		ify.setSession(true);
		
		ify.currUser.make_profile_photo();
		
		Intent i = new Intent(ify.context, MainActivity.class);
		startActivity(i);
		finish();
		
	}
}