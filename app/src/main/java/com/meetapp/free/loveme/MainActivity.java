package com.meetapp.free.loveme;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import fragments.ChatView;
import fragments.Favorites;
import fragments.LostPassword;
import fragments.MyProfileView;
import fragments.Settings;
import fragments.SignUpActivity;
import fragments.SlidingTabLayout;
import fragments.UserProfileView;
import fragments.ViewPagerAdapter;
import fragments.Visitors;
import fragments.YouTube;
import fragments.facebook;
import include.AsyncBitmap;
import include.AsyncRequest;
import include.AsyncResponse;
import include.BackgroundService;
import include.EndlessScrollListener;
import include.IFY;
import include.IFY.User;
import include.IntentHelper;
import lazylist.LazyAdapter;

import static android.R.attr.permission;

public class MainActivity extends AppCompatActivity implements AsyncResponse {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private IFY ify;
    Toolbar toolbar;
    public static ViewPager pager;
    public static ViewPagerAdapter viewPagerAdapter;
    public static SlidingTabLayout tabs;
    CharSequence Titles[] = {"People", "Chat"};
    int Numboftabs = 2;

    GridView UserList;
    ListView listMessages;

    LazyAdapter lazyAdapter;
    private AsyncRequest request;
    private int online = 0;
    private int newest = 0;
    private int male;
    private int female;
    private int with_photo;
    private int age_from;
    private int age_to;
    private int startIndex = 0;

    private ProgressBar progressBar;
    private int appState = 0;
    private boolean exit;
    private int endIndex = 50;
    private int GetTabChangedListener = 0;
    private Button btnRemoveMessages;

    private static String TAG = "PermissionDemo";
    private static final int REQUEST_WRITE_STORAGE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ify = new IFY();
        ify.init(this.getApplicationContext());

        if (!isNetworkAvailable(ify.context)) {
            closeApp_No_Internet();
        }

        if (ify.getSesson()) {

            setContentView(R.layout.activity_main);
            StartApp();
            //startService(new Intent(this, BackgroundService.class));

        } else {
            setContentView(R.layout.init_fragment);

            if (savedInstanceState == null) {
                initApp();
            }
        }

    }

    private void checkPermission() {

        //ask for the permission in android M
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Permission to access the SD-CARD is required for this app.")
                        .setTitle("Permission required");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "Clicked");
                        makeRequest();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                makeRequest();
            }
        }
    }

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_WRITE_STORAGE);

        reloadUsers();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {

                if (grantResults.length == 0
                        || grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "Permission has been denied by user");

                } else {

                    Log.i(TAG, "Permission has been granted by user");

                }
                return;
            }
        }
    }

    private void checkForUpdate() {
        this.appState = 2;
        String url = IFY.SERVICE_URL + "baseUrl.php?";
        request = new AsyncRequest();
        request.delegate = this;
        request.execute(url);
    }


    private void initApp() {

        Button btnFbLogin = (Button) findViewById(R.id.btnFbLogin);
        Button btnSignIn = (Button) findViewById(R.id.btnSingIn);
        Button btnSignUp = (Button) findViewById(R.id.btnSignUp);
        Button btnForgotPass = (Button) findViewById(R.id.btnForgotPass);

        btnFbLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent in = new Intent(getApplicationContext(), facebook.class);
                startActivity(in);
                // finish();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                SignIn();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent in = new Intent(getApplicationContext(),
                        SignUpActivity.class);
                startActivity(in);
                // finish();
            }
        });

        btnForgotPass.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent in = new Intent(getApplicationContext(),
                        LostPassword.class);
                startActivity(in);
                // finish();
            }
        });
    }

    private void StartApp() {

        Titles[1] = "Chat (" + ify.currUser.getMessages_count() + ")";

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        InitTabs();
        Handler myHandler = new Handler();
        myHandler.postDelayed(RunnableUsers, 2000);

        /*
        AsyncBitmap asyncBitmap = new AsyncBitmap();
        asyncBitmap.delegate = this;
        asyncBitmap.execute(ify.currUser.getImageName());
        */

    }

    private void InitTabs() {

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), Titles, Numboftabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(viewPagerAdapter);
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

        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (position == 1) {
                    getAllMessages();
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void getAllMessages() {

        appState = 1;
        request = new AsyncRequest();
        request.delegate = this;

        progressBar.setVisibility(View.VISIBLE);

        String url = IFY.SERVICE_URL + "messages.php?id="
                + ify.currUser.getId() + "&user_hash="
                + ify.currUser.getUser_hash();

        request.execute(url);
    }


    private void getAllUsers() {

        appState = 0;
        online = 0;
        newest = 0;
        male = (ify.currUser.getUserSettings().isMale()) ? 1 : 0;
        female = (ify.currUser.getUserSettings().isFemale()) ? 1 : 0;
        with_photo = (ify.currUser.getUserSettings().isWithPhoto()) ? 1 : 0;

        age_from = ify.currUser.getUserSettings().getAge_from();
        age_to = ify.currUser.getUserSettings().getAge_to();

        makeRequest(startIndex, endIndex);

    }


    private void makeRequest(int startIndex, int endIndex) {

        request = new AsyncRequest();
        request.delegate = this;

        String url = IFY.SERVICE_URL + "people.php?online=" + online
                + "&newest=" + newest + "&male=" + male + "&female=" + female
                + "&with_photo=" + with_photo + "&age_from=" + age_from
                + "&age_to=" + age_to + "&user_id=" + ify.currUser.getId()
                + "&user_hash=" + ify.currUser.getUser_hash() + "&startIndex="
                + startIndex + "&endIndex=" + endIndex;

        request.execute(url);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (!ify.getSesson()) return false;

        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        if (id == R.id.action_profile_id) {
            Intent i = new Intent(ify.context, MyProfileView.class);
            startActivity(i);
            return true;
        }


        if (id == R.id.action_visitor_id) {
            Intent i = new Intent(ify.context, Visitors.class);
            startActivity(i);
            return false;
        }


        if (id == R.id.action_favorite_id) {
            Intent i = new Intent(ify.context, Favorites.class);
            startActivity(i);
            return false;
        }

        if (id == R.id.action_setting_id) {
            Intent i = new Intent(ify.context, Settings.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.action_refresh) {
            GetTabChangedListener = 0;
            reloadUsers();
            return true;
        }

        if (id == R.id.action_all_users) {
            GetTabChangedListener = 0;
            reloadUsers();
            return true;
        }

        if (id == R.id.action_online_users) {
            appState = 0;
            GetTabChangedListener = 1;
            IFY.people.clear();
            startIndex = 0;
            getOnlineUsers();
            return true;
        }

        if (id == R.id.action_new_users) {
            appState = 0;
            GetTabChangedListener = 2;
            IFY.people.clear();
            startIndex = 0;
            getNewestUsers();
            return true;
        }


        if (id == R.id.action_you_tube) {
            Intent i = new Intent(ify.context, YouTube.class);
            startActivity(i);
            return false;
        }

        if (id == R.id.action_user_search) {
            this.searchUser();
            return false;
        }


        return super.onOptionsItemSelected(item);
    }


    private void filterByUserName(String username) {

        request = new AsyncRequest();
        request.delegate = this;

        progressBar.setVisibility(View.VISIBLE);

        String url = IFY.SERVICE_URL + "search_user.php?username=" + username
                + "&user_id=" + ify.currUser.getId() + "&user_hash="
                + ify.currUser.getUser_hash();

        request.execute(url);

    }

    private void searchUser(){

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

        alert.setIcon(android.R.drawable.ic_dialog_info);
        alert.setTitle("Find People by Username");
        alert.setMessage("Type UserName?");
        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        String value = input.getText().toString();
                        try {
                            ify.people.clear();
                            appState = 0;
                            value = URLEncoder.encode(value, "utf-8");
                            filterByUserName(value);

                        } catch (UnsupportedEncodingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        // Do something with value!

                    }
                });

        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        // Canceled.
                    }
                });

        final AlertDialog dialog = alert.create();

        dialog.show();

        ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);

        input.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (TextUtils.isEmpty(s)) {

                    ((AlertDialog) dialog).getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(
                            false);

                } else {

                    ((AlertDialog) dialog).getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(
                            true);

                }

            }
        });
    }


    private void reloadUsers() {

        IFY.people.clear();
        startIndex = 0;
        getAllUsers();

        // init adbaner
        AdView adView = (AdView) findViewById(R.id.mainAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    public void processFinish(String output) {

        if (appState == 0) {

            if (IFY.people.isEmpty()) {
                IFY.people = ify.parseJson(output);
                setAdapter();
            } else {
                ArrayList<IFY.User> addMoreUsers = ify.parseJson(output);
                IFY.people.addAll(addMoreUsers);
            }
            progressBar.setVisibility(View.GONE);
        }

        if (appState == 1) {
            IFY.messages = ify.parseJson(output);
            progressBar.setVisibility(View.GONE);
            setAdapter();
        }

        if (appState == -1) {

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

        // check for update
        if (appState == 2) {

            try {

                JSONArray jsonArray = new JSONArray(output);
                for (int i = 0; i < jsonArray.length(); ++i) {

                    JSONObject element = null;
                    try {
                        element = jsonArray.getJSONObject(i);
                        int pack_state = element.getInt("pack_state");
                        String pack_name = element.getString("pack_name");

                        if (pack_state > 0){
                            progressBar.setVisibility(View.GONE);
                            updateAlertDialog(pack_name);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    private void updateAlertDialog(final String pack_name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setMessage("New version is available please download?")
                .setTitle("Confirmation")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                Uri uri = Uri.parse("market://details?id="
                                        + pack_name);
                                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                                try {
                                    startActivity(goToMarket);
                                } catch (ActivityNotFoundException e) {
                                    startActivity(new Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("http://play.google.com/store/apps/details?id="
                                                    + pack_name)));
                                }


                            }
                        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setAdapter() {

        if (appState == 0) {

            UserList = (GridView) findViewById(R.id.listPeople);
            lazyAdapter = new LazyAdapter(this.getApplicationContext(), IFY.people, false);
            UserList.setAdapter(lazyAdapter);

            Handler myHandler = new Handler();
            myHandler.postDelayed(RunnableMessages, 2000);


            UserList.setOnScrollListener(new EndlessScrollListener() {
                @Override
                public void onLoadMore(int page, int totalItemsCount) {

                    if (IFY.people.size() >= 50) {
                        progressBar.setVisibility(View.VISIBLE);
                        startIndex += 50;

                        if (GetTabChangedListener == 0)
                            getAllUsers();

                        if (GetTabChangedListener == 1)
                            getOnlineUsers();

                        if (GetTabChangedListener == 2)
                            getNewestUsers();

                    }
                }
            });

            UserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    User user = IFY.people.get(position);
                    IntentHelper.addObjectForKey(user, "key");

                    Intent i = new Intent(ify.context, UserProfileView.class);
                    startActivity(i);

                }

            });


        }

        if (appState == 1) {

            btnRemoveMessages = (Button) findViewById(R.id.btnRemoveAllMessages);

            listMessages = (ListView) findViewById(R.id.listMessages);
            lazyAdapter = new LazyAdapter(this.getApplicationContext(), IFY.messages, true);
            listMessages.setAdapter(lazyAdapter);

            IFY.refreshTabTitle(1, "Chat (" + String.valueOf(IFY.messages.size()) + ")");

            listMessages.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    User user = IFY.messages.get(position);
                    IntentHelper.addObjectForKey(user, "key");

                    Intent i = new Intent(ify.context, ChatView.class);
                    startActivity(i);

                }

            });


            listMessages.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view,
                                               int position, long id) {

                    final int currPosition = position;
                    final User selectedUser = (User) IFY.messages.get(currPosition);

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            MainActivity.this);

                    builder.setMessage("Delete conversation?")
                            .setTitle(selectedUser.getUsername() + " Confirmation")
                            .setCancelable(false)
                            .setPositiveButton("Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int id) {
                                            ify.currUser.removeChat(selectedUser);
                                            lazyAdapter.notifyDataSetChanged();
                                            return;

                                        }
                                    })
                            .setNegativeButton("No",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();

                    return true;
                }
            });


            if (!IFY.messages.isEmpty()) {

                btnRemoveMessages.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeAllMessages();
                    }
                });
            }
            else
                btnRemoveMessages.setText("No Messages");
        }

    }


    private void removeAllMessages() {

        if (IFY.messages.isEmpty()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainActivity.this);

        builder.setMessage("Delete All conversations?")
                .setTitle("Confirmation")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {

                                String ids = getUserIds();
                                ify.currUser.remove_all_messages(ids);
                                btnRemoveMessages.setText("No messages");
                                lazyAdapter.notifyDataSetChanged();
                                getAllMessages();

                            }
                        })
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();

    }


    private String getUserIds() {

        String ids = "";
        int index = 0;
        String mask = "";

        for (User user : IFY.messages) {

            if (index > 0)
                mask = ",";

            ids = ids + mask + String.valueOf(user.getId());
            index++;
        }

        return ids;
    }

    @Override
    public void processBitmapFinish(Bitmap output) {

        //if (output == null) ShowAlertBox();

    }

    @Override
    public void processMessageFinish(String output) {
        //
    }


    private void ShowAlertBox() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission denied!");
        String str = "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission";

        builder.setMessage(str)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }



    @Override
    public void onDestroy() {

        // stopService(new Intent(this, BackgroundService.class));
        // LocalBroadcastManager.getInstance(this).unregisterReceiver(BReceiver);
        super.onDestroy();
    }

    /*
    private BroadcastReceiver BReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String messagesCount = intent.getExtras()
                    .getString("messagesCount");

            String title = "Chat (" + messagesCount + ")";
            IFY.refreshTabTitle(1, title);

        }
    };
    */

    protected void onResume() {
        super.onResume();
        //LocalBroadcastManager.getInstance(this).registerReceiver(BReceiver, new IntentFilter("message"));
    }

    protected void onPause() {
        super.onPause();
        // LocalBroadcastManager.getInstance(this).unregisterReceiver(BReceiver);
    }


    public static boolean isNetworkAvailable(Context context) {
        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }

    public void closeApp_No_Internet() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(
                "There are no active networks.Please check your internet connection.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.exit(0);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {

        if (!ify.getSesson()) {
            finish();
            return false;
        }

        switch (keycode) {
            case KeyEvent.KEYCODE_MENU: {
                //???
                return true;
            }
        }

        return super.onKeyDown(keycode, e);
    }


    @Override
    public void onBackPressed() {

        if (getFragmentManager().getBackStackEntryCount() == 0)
            exitApp();

        else {
            getFragmentManager().popBackStack();
        }

    }

    private void exitApp() {

        if (exit) {

            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            startActivity(i);

        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }

    private void getOnlineUsers() {

        online = 1;
        newest = 0;
        male = (ify.currUser.getUserSettings().isMale()) ? 1 : 0;
        female = (ify.currUser.getUserSettings().isFemale()) ? 1 : 0;
        with_photo = (ify.currUser.getUserSettings().isWithPhoto()) ? 1 : 0;

        age_from = ify.currUser.getUserSettings().getAge_from();
        age_to = ify.currUser.getUserSettings().getAge_to();

        makeRequest(startIndex, endIndex);

    }

    private void getNewestUsers() {

        online = 0;
        newest = 2;
        male = (ify.currUser.getUserSettings().isMale()) ? 1 : 0;
        female = (ify.currUser.getUserSettings().isFemale()) ? 1 : 0;
        with_photo = (ify.currUser.getUserSettings().isWithPhoto()) ? 1 : 0;

        age_from = ify.currUser.getUserSettings().getAge_from();
        age_to = ify.currUser.getUserSettings().getAge_to();

        makeRequest(startIndex, endIndex);

    }

    private Runnable RunnableUsers = new Runnable() {
        @Override
        public void run() {

            // init adbaner
            AdView adView = (AdView) findViewById(R.id.mainAdView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);

            final InterstitialAd interstitial  = new InterstitialAd(ify.context);
            interstitial.setAdUnitId(ify.interstitial_key);

            adRequest = new AdRequest.Builder().build();
            interstitial.loadAd(adRequest);

            interstitial.setAdListener(new AdListener() {
                public void onAdLoaded() {

                    if (interstitial.isLoaded()) {
                        interstitial.show();
                        IFY.people.clear();
                        startIndex = 0;
                        getAllUsers();
                    }
                }

                public void onAdClosed(){
                    checkPermission();
                }
            });

            checkForUpdate();
        }
    };


    private Runnable RunnableMessages = new Runnable() {
        @Override
        public void run() {
            getAllMessages();
        }
    };


    private void SignIn() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.activity_sign_in_screen, null);

        builder.setView(dialogView)
                .setPositiveButton("Sign in", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        tryToSignIn(dialogView);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void tryToSignIn(View dialogView) {

        appState = -1;

        EditText username = (EditText) dialogView.findViewById(R.id.username);
        EditText password = (EditText) dialogView.findViewById(R.id.password);

        String _username = username.getText().toString();
        String _password = password.getText().toString();

        if ((!_username.isEmpty()) && (!_password.isEmpty())) {

            String params = "username=" + _username + "&password="
                    + _password;

            String url = IFY.SERVICE_URL + "sign_in.php?" + params;
            request = new AsyncRequest();
            request.delegate = this;
            request.execute(url);

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

}
