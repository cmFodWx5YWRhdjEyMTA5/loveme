package include;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AsyncBitmap extends AsyncTask<String, Integer, Bitmap> {
	public AsyncResponse delegate = null;
	private Bitmap myBitmap;

	@Override
	protected void onPreExecute() {

		if (myBitmap != null) {

			myBitmap.recycle();

		}

	}

	@Override
	protected void onCancelled() {
		// Handle what you want to do if you cancel this task
	}

	@Override
	protected Bitmap doInBackground(String... params) {

		try {
			URL url = new URL(params[0]);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			myBitmap = BitmapFactory.decodeStream(input);

			return myBitmap;

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}

	}

	@Override
	protected void onPostExecute(Bitmap img) {
		super.onPostExecute(img);
		delegate.processBitmapFinish(img);
	}

}
