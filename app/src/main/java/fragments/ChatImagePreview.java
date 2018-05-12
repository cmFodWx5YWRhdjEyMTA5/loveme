package fragments;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.meetapp.free.loveme.R;

import include.AsyncBitmap;
import include.AsyncResponse;
import include.ChatMessage;
import include.IFY;
import include.IntentHelper;
import include.TouchImageView;

public class ChatImagePreview extends AppCompatActivity implements AsyncResponse {

	private IFY ify;
	private ChatMessage message;
	private AsyncBitmap asyncBitmap;

	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.image_preview);

		Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
		setSupportActionBar(toolbar);

		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		ify = new IFY();
		ify.init(getBaseContext());

		message = (ChatMessage) IntentHelper.getObjectForKey("key");

		progressBar.setVisibility(View.VISIBLE);

		asyncBitmap = new AsyncBitmap();
		asyncBitmap.delegate = this;
		asyncBitmap.execute(message.ImageName);

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

        MenuItem action_menu_done = menu.findItem(R.id.action_menu_done);
        action_menu_done.setVisible(false);

        return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle action bar actions click
		switch (item.getItemId()) {
		case R.id.action_menu_done: {

			finish();
		}
		case android.R.id.home: {

			finish();
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void processFinish(String output) {

	}

	@Override
	public void processBitmapFinish(Bitmap output) {

		// imgPreview.setImageBitmap(output);
		TouchImageView touch = (TouchImageView) findViewById(R.id.imagePreview);
		touch.setImageBitmap(output);
		progressBar.setVisibility(View.GONE);

	}

	@Override
	public void processMessageFinish(String output) {

	}

}