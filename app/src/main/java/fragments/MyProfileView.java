package fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import android.view.ContextMenu;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.meetapp.free.loveme.MainActivity;
import com.meetapp.free.loveme.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import include.AsyncBitmap;
import include.AsyncRequest;
import include.AsyncResponse;
import include.IFY;
import include.IFY.User;
import include.IntentHelper;
import lazylist.LazyAdapter;
import upload.Config;
import upload.UploadActivity;

public class MyProfileView extends AppCompatActivity implements AsyncResponse {

    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();

    // Camera activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int GALLERY_PICTURE = 1;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final int COMPRESS = 100 ;


    private int MENU_CONTEXT_DELETE_PHOTO = 1;
    private int MENU_CONTEXT_MAKE_PROFILE = 2;

    ViewPager pager;
    ViewUserAdapter user_adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[] = {"Profile", "Photos"};
    int Numboftabs = 2;

    private AsyncBitmap asyncBitmap;
    private IFY ify;
    private User user;
    private Button chat;

    private AsyncRequest request;
    private ArrayList<User> users;
    private GridView UserList;
    private ProgressBar progressBar;
    private TextView user_info;
    private TextView username;

    private LazyAdapter lazyAdapter;
    private MenuItem action_menu_exist_photo;
    private MenuItem action_menu_take_photo;
    private MenuItem action_menu_set_status;
    private MenuItem action_menu_edit_profile;
    private String imagepath;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.fragment_main);
        setTitle("");

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        ify = new IFY();
        ify.init(this.getBaseContext());
        user = ify.currUser;

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

            ShowAlertBox();
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

    private void ShowAlertBox() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Profile configuration!");
        String str = "Reach more users, please Upload a Photo to your Profile";

        builder.setMessage(Html.fromHtml(str))
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                       takeFromGallery();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
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
        registerForContextMenu(UserList);

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

    }

    private void initUserDetails() {

        if (user != null) {

            username = (TextView) findViewById(R.id.tv_username);
            user_info = (TextView) findViewById(R.id.tv_user_dscr);
            username.setText(user.getUsername() + " , " + user.getAge());

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

        action_menu_exist_photo = menu.findItem(R.id.action_menu_exist_photo);
        action_menu_exist_photo.setVisible(true);

        action_menu_take_photo = menu.findItem(R.id.action_menu_take_photo);
        action_menu_take_photo.setVisible(true);

        action_menu_set_status = menu.findItem(R.id.action_menu_set_status);
        action_menu_set_status.setVisible(true);

        action_menu_edit_profile = menu.findItem(R.id.action_menu_edit_profile);
        action_menu_edit_profile.setVisible(true);

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


            case R.id.action_menu_exist_photo: {
                takeFromGallery();
                return true;
            }

            case R.id.action_menu_take_photo: {
                captureImage();
                return true;
            }


            case R.id.action_menu_set_status: {
                setMyStatus();
                return true;
            }

            case R.id.action_menu_edit_profile: {
                Intent i = new Intent(ify.context, editProfile.class);
                startActivity(i);
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void launchUploadFromGallery(Intent data) {

        if (Build.VERSION.SDK_INT < 19) {
            Uri selectedImageUri = data.getData();
            imagepath = getPath(selectedImageUri);
            File imageFile = new File(imagepath);

            fileUri = Uri.fromFile(imageFile);
            IntentHelper.addObjectForKey(fileUri, "file_uri");

            launchUploadActivity(true);
        }
        else
        {
            InputStream imInputStream = null;
            try {
                imInputStream = getContentResolver().openInputStream(data.getData());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = BitmapFactory.decodeStream(imInputStream);
            String smallImagePath = saveGalaryImageOnLitkat(bitmap);

            File imageFile = new File(smallImagePath);

            fileUri = Uri.fromFile(imageFile);
            IntentHelper.addObjectForKey(fileUri, "file_uri");

            launchUploadActivity(true);
        }

    }

    private String saveGalaryImageOnLitkat(Bitmap bitmap) {
        try {
            File cacheDir;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                cacheDir = new File(Environment.getExternalStorageDirectory(), getResources().getString(R.string.app_name));
            else
                cacheDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (!cacheDir.exists())
                cacheDir.mkdirs();
            String filename = System.currentTimeMillis() + ".jpg";
            File file = new File(cacheDir, filename);
            File temp_path = file.getAbsoluteFile();
            // if(!file.exists())
            file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS, out);
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    private void launchUploadActivity(boolean isImage) {
        fileUri = (Uri) IntentHelper.getObjectForKey("file_uri");

        Intent i = new Intent(ify.context, UploadActivity.class);
        i.putExtra("filePath", fileUri.getPath());
        i.putExtra("isImage", isImage);
        startActivity(i);
    }

    private void takeFromGallery() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, "Complete action using"),
                GALLERY_PICTURE);

    }

    public String getPath(Uri uri) {

        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = ify.context.getContentResolver().query(uri, proj, null,
                null, null);
        if (cursor.moveToFirst()) {
            ;
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;

    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        IntentHelper.addObjectForKey(fileUri, "file_uri");

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Config.IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create " + Config.IMAGE_DIRECTORY_NAME
                        + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp);
        } else {
            return null;
        }

        return mediaFile;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE
                && resultCode == Activity.RESULT_OK)
            launchUploadActivity(true);

        else if (requestCode == GALLERY_PICTURE
                && resultCode == Activity.RESULT_OK)
            launchUploadFromGallery(data);

        else {

            // Toast.makeText(ify.context, "User cancelled image capture",
            // Toast.LENGTH_SHORT).show();
        }

    }

    private void setMyStatus(){

        AlertDialog.Builder alert = new AlertDialog.Builder(MyProfileView.this);

        alert.setIcon(android.R.drawable.ic_dialog_info);
        alert.setTitle("What are you up to");
        alert.setMessage("What's on your mind?");
        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        String value = input.getText().toString();
                        try {

                            value = URLEncoder.encode(value, "utf-8");
                            ify.currUser.updateStatus(value);

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



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        menu.setHeaderTitle("Options");
        menu.add(0, MENU_CONTEXT_DELETE_PHOTO, 0, "Delete");
        menu.add(0, MENU_CONTEXT_MAKE_PROFILE, 0, "Make Profile");
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {

        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        final User selectedUser = (User) users.get(info.position);

        AlertDialog.Builder builder = new AlertDialog.Builder(MyProfileView.this);

        String text;

        if (item.getItemId() == 1)
            text = "Delete this photo?";
        else
            text = "Make profile photo?";

        builder.setMessage(text)
                .setTitle("Confirmation")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                switch (item.getItemId()) {
                                    case 1: {
                                        selectedUser.remove_photo();
                                        users.remove(info.position);
                                        lazyAdapter.notifyDataSetChanged();

                                        if (users.isEmpty()) {
                                            Intent i = new Intent(ify.context, MainActivity.class);
                                            startActivity(i);
                                            finish();
                                        }

                                        break;
                                    }
                                    case 2: {
                                        selectedUser.make_profile_photo();
                                        ify.currUser.refreshProfile(selectedUser);

                                        Intent i = new Intent(ify.context, MainActivity.class);
                                        startActivity(i);
                                        finish();

                                        break;
                                    }
                                    default:
                                        break;
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

        return false;
    }

}