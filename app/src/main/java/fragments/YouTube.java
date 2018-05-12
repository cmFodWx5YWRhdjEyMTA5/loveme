package fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.meetapp.free.loveme.R;

import include.AsyncRequest;
import include.AsyncResponse;
import include.IFY;
import include.IFY.User;
import lazylist.LazyAdapter;

public class YouTube extends Activity implements AsyncResponse {

	private IFY ify;
	private ProgressBar progressBar;
	ListView list;
	LazyAdapter adapter;
	private AsyncRequest request;
    private AdView MainAdView;

    @Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.youtube);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        getActionBar().setIcon(
                new ColorDrawable(getResources().getColor(
                        android.R.color.transparent)));

		setTitle("Music Video Collection - YouTube");

		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		request = new AsyncRequest();
		request.delegate = this;

		ify = new IFY();
		ify.init(this.getBaseContext());

		progressBar.setVisibility(View.VISIBLE);

        // init ads
        MainAdView = (AdView) findViewById(R.id.youtubeAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        MainAdView.loadAd(adRequest);

		loadData();

	}

	private void loadData() {

		String url = IFY.SERVICE_URL + "youtube.php?user_id="
				+ ify.currUser.getId() + "&user_hash="
				+ ify.currUser.getUser_hash();

		request.execute(url);

	}

	private void setAdapter() {

		list = (ListView) findViewById(R.id.list_youtube);
		adapter = new LazyAdapter((Activity) this, IFY.youtube, true);
		list.setAdapter(adapter);
		registerForContextMenu(list);

		progressBar.setVisibility(View.GONE);

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				User user = IFY.youtube.get(position);

				Toast.makeText(ify.context, "Please wait...", Toast.LENGTH_LONG)
						.show();

				Intent i = new Intent(Intent.ACTION_VIEW, Uri
						.parse("https://www.youtube.com/watch?v="
								+ user.getYoutubeId()));
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			}

		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		super.onPrepareOptionsMenu(menu);

		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle action bar actions click
		switch (item.getItemId()) {

		case android.R.id.home: {

			finish();
			return false;
		}

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void processFinish(String output) {

		IFY.youtube = ify.parseJson(output);
		setAdapter();

	}

	@Override
	public void processBitmapFinish(Bitmap output) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processMessageFinish(String output) {

	}
}