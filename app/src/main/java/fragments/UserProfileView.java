package fragments;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.meetapp.free.loveme.R;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import include.AsyncBitmap;
import include.AsyncRequest;
import include.AsyncResponse;
import include.IFY;
import include.IFY.User;
import include.IntentHelper;
import lazylist.LazyAdapter;

public class UserProfileView extends AppCompatActivity implements AsyncResponse {

    ViewPager pager;
    ViewUserAdapter user_adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[] = {"Profile", "Photos"};
    int Numboftabs = 2;

    private AsyncBitmap asyncBitmap;
    private IFY ify;
    private User user;

    private AsyncRequest request;
    private ArrayList<User> users;
    private GridView UserList;
    private ProgressBar progressBar;
    private TextView user_info;
    private TextView username;
    private MenuItem action_menu_like;
    private MenuItem action_menu_dislike;
    private LazyAdapter lazyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.fragment_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        ify = new IFY();
        ify.init(this.getBaseContext());
        user = (User) IntentHelper.getObjectForKey("key");

        progressBar.setVisibility(View.VISIBLE);

        initPhotos();
        initHome();

    }

    private void initPhotos() {

        // load user images
        request = new AsyncRequest();
        request.delegate = this;

        String url = IFY.SERVICE_URL + "user_images.php?id=" + user.getId()
                + "&user_id=" + ify.currUser.getId() + "&user_hash="
                + ify.currUser.getUser_hash();

        request.execute(url);
    }

    private void initTabs() {

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        user_adapter = new ViewUserAdapter(getSupportFragmentManager(), Titles, Numboftabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(user_adapter);

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

        // init adbaner
        AdView adView = (AdView) findViewById(R.id.mainAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);


    }

    private void initHome() {

        asyncBitmap = new AsyncBitmap();
        asyncBitmap.delegate = this;
        asyncBitmap.execute(user.getImageName());
    }

    @Override
    public void processBitmapFinish(Bitmap output) {

        initUserDetails();

        ImageView imageView = (ImageView) findViewById(R.id.img_thumbnail);

        if (output != null) {

            try {

                // get phone display size
                Display display = getWindowManager().getDefaultDisplay();

                if (display != null) {
                    Point size = new Point();
                    display.getSize(size);
                    int height = size.y;

                    Bitmap sbmp = Bitmap.createScaledBitmap(output, height,
                            height, false);
                    imageView.setImageBitmap(sbmp);
                } else
                    imageView.setImageBitmap(output);

            } catch (Throwable e) {
                e.printStackTrace();
            }

        } else {

            imageView.getLayoutParams().width = 100;
            imageView.getLayoutParams().height = 100;

            if (user.getNumber_gender() == 1)
                imageView.setBackgroundResource(R.drawable.male);
            else
                imageView.setBackgroundResource(R.drawable.female);

        }


        imageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (ify.currUser.getThumbName().length() > 0) {
                    IntentHelper.addObjectForKey(user, "key");

                    Intent i = new Intent(ify.context, ImagePreview.class);
                    startActivity(i);
                }

            }
        });

        progressBar.setVisibility(View.GONE);

    }

    @Override
    public void processMessageFinish(String output) {

    }


    @Override
    public void processFinish(String output) {

        // load user images
        users = ify.parseJson(output);

        Titles[1] = "Photos (" + users.size() + ")";
        initTabs();

        UserList = (GridView) findViewById(R.id.listPeople);
        lazyAdapter = new LazyAdapter(this.getApplicationContext(), users, false);
        UserList.setAdapter(lazyAdapter);

        UserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                User user = users.get(position);
                IntentHelper.addObjectForKey(user, "key");

                Intent i = new Intent(getBaseContext(), ImagePreview.class);
                startActivity(i);

            }

        });

        progressBar.setVisibility(View.GONE);

        IFY.adUserViewCount++;
        if (IFY.adUserViewCount == IFY.maxAdCount) {

            IFY.adUserViewCount = 0;
            ify.InterstitialAd();

        }

    }

    private void initUserDetails() {

        if (user != null) {

            username = (TextView) findViewById(R.id.tv_username);
            user_info = (TextView) findViewById(R.id.tv_user_dscr);

            username.setText(user.getUsername() + " , " + user.getAge());
            setTitle("Likes (" + user.getThumbs() + ")");
            // get user action
            try {
                String value = URLDecoder.decode(user.getAction(), "UTF-8").replaceAll("\\\\", "");

                // get other info
                String s = "<h3>Status</h3>" + value + "<br/><h3>Other info</h3><b>Like - </b><small><i>"
                        + user.getThumbs() + "</small></i><br/>"
                        + "<b>Dislike - </b><small><i>" + user.getDislike()
                        + "</small></i><br/>"
                        + "<b>Description/Location - </b><small><i>"
                        + user.getDescr();

                user_info.setText(Html.fromHtml(s));

            } catch (UnsupportedEncodingException e) {

                e.printStackTrace();
            }

        }

        // add to visitor if not added
        ify.currUser.addToVisitor(user);

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
        action_menu_chat.setVisible(true);

        action_menu_like = menu.findItem(R.id.action_menu_like);
        action_menu_like.setVisible(true);

        action_menu_dislike = menu.findItem(R.id.action_menu_dislike);
        action_menu_dislike.setVisible(true);


        MenuItem action_menu_done = menu.findItem(R.id.action_menu_done);
        action_menu_done.setVisible(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar actions click
        switch (item.getItemId()) {

            case android.R.id.home: {

                finish();
                return true;
            }
            case R.id.action_menu_done: {
                finish();
                return true;
            }

            case R.id.action_menu_chat: {

                IntentHelper.addObjectForKey(user, "key");
                Intent i = new Intent(ify.context, ChatView.class);
                startActivity(i);
                return true;
            }


            case R.id.action_menu_like: {

                boolean isOk = ify.currUser.addToFavrote(user);
                if (isOk) {

                    ify.currUser.setThumb(user);
                    setTitle("Likes (" + user.getThumbs() + ")");
                    action_menu_like.setEnabled(false);

                    Toast.makeText(
                            this.getApplicationContext(),
                            user.getUsername() + "has been added to favorites",
                            Toast.LENGTH_LONG).show();
                }
                return true;
            }


            case R.id.action_menu_dislike: {

                ify.currUser.setUserDislike(user);
                action_menu_dislike.setEnabled(false);
                Toast.makeText(
                        this.getApplicationContext(),
                        "Sorry but i don't like you...",
                        Toast.LENGTH_LONG).show();
                return true;
            }


            default:
                return super.onOptionsItemSelected(item);
        }
    }

}