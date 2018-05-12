package include;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.meetapp.free.loveme.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import fragments.ChatArrayAdapter;

public class IFY extends Activity implements AsyncResponse {

    public static int adUserViewCount = 0;
    public static int adChatViewCount = 0;

    public static int maxAdCount = 5;

    private boolean Session;
    private SharedPreferences sharedPrefs;
    public Context context;

    public static ListView ChatListView;
    public static MySocket Mysocket;
    public static ChatArrayAdapter chatArrayAdapter;

    public final String IS_LOGIN = "isLogin";
    public static String IMAGE_URL = "http://kazanlachani.com/ify/";
    public static String SERVICE_URL = "http://kazanlachani.com/chatar_services/";
    public static String interstitial_key = "ca-app-pub-2108590561691007/4300312372";

    public static ArrayList<User> people = new ArrayList<User>();
    public static ArrayList<User> messages = new ArrayList<User>();
    public static ArrayList<User> favorites = new ArrayList<User>();
    public static ArrayList<User> youtube = new ArrayList<User>();
    public static Bitmap[] emoticons;

    public User currUser = null;

    private int MENU_MESSAGES = 3;
    private int MENU_FAVORITES = 4;
    private int MENU_VISITORS = 5;

    public static final int NO_OF_EMOTICONS = 54;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public IFY() {

        this.Session = false;
    }

    public void init(Context context) {

        this.context = context;
        this.Session = this.getSesson();
        readEmoticons();
    }


    public boolean getSesson() {

        sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.context);
        this.Session = sharedPrefs.getBoolean(IS_LOGIN, false);

        if (this.Session) {

            this.currUser = new User();

            this.currUser.id = sharedPrefs.getInt("id", 0);
            this.currUser.username = sharedPrefs.getString("username", "");
            this.currUser.email = sharedPrefs.getString("email", "");
            this.currUser.age = sharedPrefs.getString("age", "");
            this.currUser.gender = sharedPrefs.getString("gender", "");

            this.currUser.thumbName = sharedPrefs.getString("thumbName", "");
            this.currUser.ImageName = sharedPrefs.getString("imageName", "");

            // store for chat thumbs
            this.currUser.ChatThumbName = this.currUser.thumbName;
            this.currUser.ChatImageName = this.currUser.ImageName;

            this.currUser.descr = sharedPrefs.getString("descr", "");
            this.currUser.action = sharedPrefs.getString("action", "");

            this.currUser.registration_type = sharedPrefs.getInt(
                    "registration_type", -1);

            this.currUser.faceUserName = sharedPrefs.getString("faceUserName",
                    "");

            this.currUser.messages_count = sharedPrefs.getString(
                    "messages_count", "");

            this.currUser.favorites_count = sharedPrefs.getString(
                    "favorites_count", "");

            this.currUser.visitors_count = sharedPrefs.getString(
                    "visitors_count", "");

            this.currUser.getUserSettings().loadSetings(currUser);
            this.currUser.user_hash = sharedPrefs.getString("user_hash", "");
            this.currUser.thumbs = sharedPrefs.getString("thumbs", "");
            this.currUser.dislike = sharedPrefs.getString("dislike", "");
        }

        return this.Session;
    }

    public void setSession(boolean _value) {

        Editor editor = sharedPrefs.edit();
        editor.putBoolean(IS_LOGIN, _value);

        editor.putInt("id", currUser.getId());
        editor.putString("username", currUser.getUsername());
        editor.putString("email", currUser.getEmail());
        editor.putString("age", currUser.getAge());
        editor.putString("gender", currUser.getGender());
        editor.putString("thumbName", currUser.getThumbName());
        editor.putString("imageName", currUser.getImageName());
        editor.putString("descr", currUser.getDescr());
        editor.putString("action", currUser.getAction());
        editor.putString("faceUserName", currUser.getFaceUserName());
        editor.putString("messages_count", currUser.getMessages_count());
        editor.putString("favorites_count", currUser.getFavorites_count());
        editor.putString("visitors_count", currUser.getVisitors_count());

        editor.putString("user_hash", currUser.getUser_hash());
        editor.putInt("registration_type", currUser.getRegistration_type());
        editor.putString("thumbs", currUser.getThumbs());
        editor.putString("dislike", currUser.getDislike());

        editor.commit();

    }

    public static boolean isNetworkAvailable(Context context) {
        return ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo() != null;
    }

    public static void refreshTabTitle(int position, String title){

        MainActivity.viewPagerAdapter.setPageTitle(position, title);
        MainActivity.tabs.setViewPager(MainActivity.pager);
    }
    public ArrayList<User> parseJson(String result) {

        if (!isNetworkAvailable(context)) {
            System.exit(0);
        }

        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(result);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ArrayList<User> users = new ArrayList<User>();
        for (int i = 0; i < jsonArray.length(); ++i) {

            JSONObject element = null;
            try {
                element = jsonArray.getJSONObject(i);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            User user = null;
            try {
                user = new User(element);
                if (user.getId() > 0)
                    users.add(user);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        return users;

    }

    public Context getContext() {

        return this.context;
    }

    private void readEmoticons() {

        emoticons = new Bitmap[NO_OF_EMOTICONS];
        for (short i = 0; i < NO_OF_EMOTICONS; i++) {
            emoticons[i] = getImage((i + 1) + ".png");
        }
    }

    private Bitmap getImage(String path) {
        AssetManager mngr = context.getAssets();
        InputStream in = null;
        try {
            in = mngr.open("emoticons/" + path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bitmap temp = BitmapFactory.decodeStream(in, null, null);
        return temp;
    }

    public void InterstitialAd() {

        final InterstitialAd interstitial  = new InterstitialAd(this.context);
        interstitial.setAdUnitId(this.interstitial_key);

        AdRequest adRequest = new AdRequest.Builder().build();
        interstitial.loadAd(adRequest);

        interstitial.setAdListener(new AdListener() {
            public void onAdLoaded() {

                if (interstitial.isLoaded()) {
                    interstitial.show();
                }
            }
        });

    }

    public void sendDBMessage(User user, String _message) {
        AsyncRequest request = new AsyncRequest();
        request.delegate = this; // listen for callback

        try {
            String username = URLEncoder
                    .encode(currUser.getUsername(), "utf-8");
            String message = URLEncoder.encode(_message, "utf-8");

            String url = SERVICE_URL + "chat_insert.php?user_id="
                    + currUser.getId() + "&send_to=" + user.getId()
                    + "&ImageName=" + user.ChatImageName + "&ThumbsName="
                    + user.ChatThumbName + "&username=" + username
                    + "&message=" + message + "&state=1" + "&versionCode=4"
                    + "&user_hash=" + currUser.getUser_hash();

            request.execute(url);

        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        }

    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bmp, int _radius) {

        Bitmap sbmp;
        int radius = _radius;
        if (bmp.getWidth() != radius || bmp.getHeight() != radius)
            sbmp = Bitmap.createScaledBitmap(bmp, radius, radius, false);
        else
            sbmp = bmp;
        Bitmap output = Bitmap.createBitmap(sbmp.getWidth(), sbmp.getHeight(),
                Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(sbmp.getWidth() / 2 + 0.7f,
                sbmp.getHeight() / 2 + 0.7f, sbmp.getWidth() / 2 + 0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);

        return output;
    }

    @SuppressWarnings("serial")
    public class User implements Serializable, AsyncResponse {

        private AsyncRequest request;

        private int id;
        private String username;
        private String reg_date;
        private String email;
        private String age;
        private String gender;
        private int number_gender;
        private int ImageId;
        private String thumbName;
        private String ImageName;
        private String action;
        private String descr;
        private int registration_type;
        private String faceUserName;
        private String thumbs;
        private String dislike;

        private String message;
        private String messageDate;
        public String ChatThumbName;
        public String ChatImageName;

        private String messages_count;
        private String favorites_count;
        private String visitors_count;

        private Setting UserSettings;
        private String user_hash;
        private String youtubeId;

        public int getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getAge() {
            return age;
        }

        public String getGender() {

            return gender;
        }

        public String getThumbName() {

            return thumbName;
        }

        public void setThumbName(String _value) {
            thumbName = _value;
        }

        public String getImageName() {
            return ImageName;
        }

        public void setImageName(String _value) {
            ImageName = _value;
        }

        public String getDescr() {
            return descr;
        }

        public String getAction() {
            if (action.length() > 0)
                return action;
            else
                return "Status is not defined!";
        }

        public int getRegistration_type() {
            return registration_type;
        }

        public String getFaceUserName() {
            return faceUserName;
        }

        public String getMessage() {
            return message;
        }

        public String getMessageDate() {
            return messageDate;
        }

        public void setMessage(String _value) {
            message = _value;
        }

        public String getMessages_count() {
            return messages_count;
        }

        public void setMessages_count(String _value) {
            messages_count = _value;
        }

        public String getVisitors_count() {
            return visitors_count;
        }

        public void setVisitors_count(String value) {
            visitors_count = value;
        }

        public Setting getUserSettings() {

            if (UserSettings == null)
                UserSettings = new Setting();

            return UserSettings;
        }

        public User() {

            this.id = 0;
            this.username = "";
            this.reg_date = "";
            this.email = "";
            this.age = "";
            this.gender = "";
            this.thumbName = "";
            this.ImageName = "";
            this.ChatImageName = "";
            this.ChatThumbName = "";
            this.descr = "";
            this.action = "";
            this.registration_type = -1;
            this.faceUserName = "";
            this.message = "";
            this.messageDate = "";
            this.messages_count = "";
            this.favorites_count = "";
            this.visitors_count = "";
            this.user_hash = "";

        }

        public User(JSONObject element) throws JSONException {

            this.id = element.getInt("id");

            this.username = element.getString("username");

            if (element.has("reg_date"))
                this.reg_date = element.getString("reg_date");
            else
                this.reg_date = "";

            if (element.has("email"))
                this.email = element.getString("email");
            else
                this.email = "";

            if (element.has("age"))
                this.age = element.getString("age");
            else
                this.age = "";

            if (element.has("ImageId"))
                this.ImageId = element.getInt("ImageId");
            else
                this.ImageId = 0;

            if (element.has("gender"))
                this.gender = element.getString("gender");
            else
                this.gender = "";

            if (element.has("ThumbName"))
                this.thumbName = element.getString("ThumbName");
            else
                this.thumbName = "";

            if (element.has("ImageName"))
                this.ImageName = element.getString("ImageName");
            else
                this.ImageName = "";

            this.ChatImageName = "";
            this.ChatThumbName = "";

            if (element.has("descr")) {
                this.descr = element.getString("descr");

                if (this.descr.length() > 50)
                    this.descr = this.descr.substring(0, 50);
            } else
                this.descr = "";

            if (element.has("action")) {
                this.action = element.getString("action");

                if (this.action.length() > 50)
                    this.action = this.action.substring(0, 50);

                if (this.action == null)
                    this.action = "";
            } else
                this.action = "";

            if (element.has("registration_type"))
                this.registration_type = element.getInt("registration_type");
            else
                this.registration_type = -1;

            if (element.has("faceUserName"))
                this.faceUserName = element.getString("faceUserName");
            else
                this.faceUserName = "";

            if (element.has("message"))
                this.message = element.getString("message");
            else
                this.message = "";

            if (element.has("ch_date"))
                this.messageDate = element.getString("ch_date");
            else
                this.messageDate = "";

            if (element.has("messages_count"))
                this.messages_count = element.getString("messages_count");
            else
                this.messages_count = "";

            if (element.has("favorites"))
                this.favorites_count = element.getString("favorites");
            else
                this.favorites_count = "";

            if (element.has("visitors"))
                this.visitors_count = element.getString("visitors");
            else
                this.visitors_count = "";

            if (element.has("thumb"))
                this.thumbs = element.getString("thumb");
            else
                this.thumbs = "";

            if (element.has("dislike"))
                this.dislike = element.getString("dislike");
            else
                this.dislike = "";

            if (element.has("user_hash")) {
                this.user_hash = element.getString("user_hash");
            } else
                this.user_hash = "";


            if (element.has("youtubeId")) {
                this.youtubeId = element.getString("youtubeId");
            } else
                this.youtubeId = "";

            this.getFinalThumbName();
        }

        public void getFinalThumbName() {


            if (this.ImageName != "") {
                this.ImageName = IMAGE_URL + this.ImageName;
                this.thumbName = IMAGE_URL + this.thumbName;
            } else {

                String defaultImage = "";
                if (gender.equals("Man"))
                    defaultImage = "man_icon.png";
                else
                    defaultImage = "female_icon.png";

                this.ImageName = IMAGE_URL + "images/" + defaultImage;
                this.thumbName = ImageName;
            }

            // facebook
            if (this.registration_type == 3) {

                this.ImageName = "https://graph.facebook.com/"
                        + this.faceUserName + "/picture?width=320&height=480";
                this.thumbName = "https://graph.facebook.com/"
                        + this.faceUserName + "/picture?type=normal";

            }

            //youtube
            if (this.registration_type == 4) {
                this.thumbName = "https://i.ytimg.com/vi/" + this.getYoutubeId() + "/default.jpg";
            }


        }

        public void updateStatus(String value) {

            this.action = value;
            setSession(true);

            // update db user action
            request = new AsyncRequest();
            request.delegate = this;

            String url = SERVICE_URL + "my_status.php?id=" + currUser.getId()
                    + "&value=" + value + "&user_hash=" + getUser_hash();

            request.execute(url);

        }

        public void remove_photo() {

            String url = SERVICE_URL + "remove_photo.php?imageId="
                    + getImageId() + "&user_id=" + currUser.getId()
                    + "&user_hash=" + currUser.getUser_hash();

            request = new AsyncRequest();
            request.delegate = this;
            request.execute(url);

        }

        public void make_profile_photo() {

            String url = SERVICE_URL + "make_profile_photo.php?imageId="
                    + getImageId() + "&user_id=" + currUser.getId()
                    + "&user_hash=" + currUser.getUser_hash();

            request = new AsyncRequest();
            request.delegate = this;
            request.execute(url);

        }

        public void refreshProfile(User selectedUser) {

            setThumbName(selectedUser.getThumbName());
            setImageName(selectedUser.getImageName());
            setSession(true);

        }

        public boolean addToFavrote(User user) {

            if (IFY.favorites.indexOf(user) == -1) {

                String url = SERVICE_URL + "add_favorite.php?user_id="
                        + getId() + "&send_to=" + user.getId() + "&user_hash="
                        + getUser_hash();

                request = new AsyncRequest();
                request.delegate = this;
                request.execute(url);
                workingWithFavorite(true, user);
                return true;

            } else {

                Toast.makeText(context,
                        user.getUsername() + " is already added!",
                        Toast.LENGTH_LONG).show();

                return false;
            }

        }

        public void removeFromFavrote(User user) {

            String url = SERVICE_URL + "remove_favorite.php?user_id=" + getId()
                    + "&send_to=" + user.getId() + "&user_hash="
                    + getUser_hash();

            request = new AsyncRequest();
            request.delegate = this;
            request.execute(url);

            workingWithFavorite(false, user);

        }

        private void workingWithFavorite(boolean type, User user) {

            getSesson();

            int count = Integer.valueOf(currUser.getFavorites_count());

            if ((type == false) && (count == 0))
                return;

            if (type)
                count++;
            else
                count--;

            if (type) {

                IFY.favorites.add(0, user);
                currUser.favorites_count = String.valueOf(count);
                setSession(true);

            } else {
                IFY.favorites.remove(user);
                currUser.favorites_count = String.valueOf(count);
                setSession(true);
            }

        }

        public void addToVisitor(User user) {

            // add as a visitor
            request = new AsyncRequest();
            request.delegate = this;

            String visit_url = SERVICE_URL + "add_visitors.php?user_id="
                    + user.getId() + "&send_to=" + getId() + "&user_hash="
                    + getUser_hash();

            request.execute(visit_url);

        }

        public void setThumb(User user) {

            // set thumb
            request = new AsyncRequest();
            request.delegate = this;

            String thumb_url = SERVICE_URL + "set_thumb.php?my_id=" + getId()
                    + "&user_id=" + user.getId() + "&user_hash="
                    + getUser_hash();

            request.execute(thumb_url);

            int index = Integer.valueOf(user.getThumbs());
            index++;
            String thumbs = String.valueOf(index);
            user.setThumbs(thumbs);

        }

        public void setUserDislike(User user) {

            // set thumb
            request = new AsyncRequest();
            request.delegate = this;

            String dislike_url = SERVICE_URL + "dislike.php?my_id=" + getId()
                    + "&user_id=" + user.getId() + "&user_hash="
                    + getUser_hash();

            request.execute(dislike_url);

            int index = Integer.valueOf(user.getDislike());
            index++;
            String dislike = String.valueOf(index);
            user.setDislike(dislike);

        }

        public void removeChat(User user) {

            String url = SERVICE_URL + "remove_chat.php?user_id=" + getId()
                    + "&send_to=" + user.getId() + "&user_hash="
                    + getUser_hash();

            request = new AsyncRequest();
            request.delegate = this;
            request.execute(url);

            IFY.messages.remove(user);
            currUser.messages_count = String.valueOf(IFY.messages.size());
            // refresh  tab title
            String title = "Chat (" + currUser.getMessages_count() + ")";
            IFY.refreshTabTitle(1, title);
            setSession(true);
        }

        public void remove_all_messages(String ids) {

            String url = SERVICE_URL + "remove_all_messages.php?user_id="
                    + getId() + "&ids=" + ids + "&user_hash=" + getUser_hash();

            request = new AsyncRequest();
            request.delegate = this;
            request.execute(url);

            IFY.messages = new ArrayList<User>();
            setMessages_count("0");
            setSession(true);

        }

        public void calculateMessagesCount(String output) {

            IFY.messages = parseJson(output);

            refreshTabTitle(1, "Chat (" + String.valueOf(IFY.messages.size()) + ")");
            setMessages_count(String.valueOf(IFY.messages.size()));
            setSession(true);
        }


        public void sendNewMessage(String message, User user, boolean hasPhoto) {

            Mysocket.sendMessageTo(message, user.getId());
            sendDBMessage(user, message);

            ChatMessage ch_message = new ChatMessage(false, message, hasPhoto);

            ch_message.ThumbName = user.ChatThumbName;
            ch_message.ImageName = user.ChatImageName;

            chatArrayAdapter.add(ch_message);

            IFY.ChatListView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    IFY.ChatListView.setSelection(IFY.chatArrayAdapter
                            .getCount() - 1);
                }
            }, 500);

        }


        public void remove_curr_visitors(User user) {

            String url = SERVICE_URL + "remove_curr_visitor.php?user_id=" + getId()
                    + "&send_to=" + user.getId() + "&user_hash="
                    + getUser_hash();

            request = new AsyncRequest();
            request.delegate = this;
            request.execute(url);
        }

        public void remove_visitors() {

            String url = SERVICE_URL + "remove_visitors.php?user_id=" + getId()
                    + "&user_hash=" + getUser_hash();

            request = new AsyncRequest();
            request.delegate = this;
            request.execute(url);

            setVisitors_count("0");
            setSession(true);

        }

        public void refreshVisitors(String count) {

            setVisitors_count(count);
            setSession(true);
        }

        @Override
        public void processFinish(String output) {
            // TODO Auto-generated method stub

        }

        @Override
        public void processBitmapFinish(Bitmap output) {
            // TODO Auto-generated method stub

        }

        @Override
        public void processMessageFinish(String output) {
            //
        }

        public int getImageId() {
            return ImageId;
        }

        public void setImageId(int imageId) {
            ImageId = imageId;
        }

        public int getNumber_gender() {

            if (gender.equals("Man"))
                this.number_gender = 1;
            else
                this.number_gender = 2;

            return number_gender;
        }

        public void setNumber_gender(int number_gender) {
            this.number_gender = number_gender;
        }

        public String getUser_hash() {
            return user_hash;
        }

        public void setUser_hash(String user_hash) {
            this.user_hash = user_hash;
        }

        public String getThumbs() {
            return thumbs;
        }

        public void setThumbs(String thumbs) {
            this.thumbs = thumbs;
        }

        public String getFavorites_count() {
            return favorites_count;
        }

        public void setFavorites_count(String favorites_count) {
            this.favorites_count = favorites_count;
        }

        public String getDislike() {
            return dislike;
        }

        public void setDislike(String dislike) {
            this.dislike = dislike;
        }

        public String getReg_date() {
            return reg_date;
        }

        public void setReg_date(String reg_date) {
            this.reg_date = reg_date;
        }

        public String getYoutubeId() {
            return youtubeId;
        }

        public void setYoutubeId(String youtubeId) {
            this.youtubeId = youtubeId;
        }
    }

    public void scaleImage(ImageView view) {
        Drawable drawing = view.getDrawable();
        if (drawing == null) {
            return;
        }
        Bitmap bitmap = ((BitmapDrawable) drawing).getBitmap();

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int bounding_x = ((View) view.getParent()).getWidth();// EXPECTED WIDTH
        int bounding_y = ((View) view.getParent()).getHeight();// EXPECTED
        // HEIGHT

        float xScale = ((float) bounding_x) / width;
        float yScale = ((float) bounding_y) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(xScale, yScale);

        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                matrix, true);
        width = scaledBitmap.getWidth();
        height = scaledBitmap.getHeight();
        BitmapDrawable result = new BitmapDrawable(this.context.getResources(),
                scaledBitmap);

        view.setImageDrawable(result);

    }

    @Override
    public void processFinish(String output) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processBitmapFinish(Bitmap output) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processMessageFinish(String output) {
        //
    }

    public class Setting {

        private boolean female;
        private boolean male;
        private boolean withPhoto;
        private int age_from;
        private int age_to;

        public Setting() {

        }

        public void loadSetings(User currUser) {

            this.male = sharedPrefs.getBoolean("male",
                    (currUser.getNumber_gender() == 1) ? false : true);

            this.female = sharedPrefs.getBoolean("female",
                    (currUser.getNumber_gender() == 2) ? false : true);

            this.withPhoto = sharedPrefs.getBoolean("with_photo", true);

            this.age_from = sharedPrefs.getInt("age_from", 18);
            this.age_to = sharedPrefs.getInt("age_to", 40);
        }

        public void SaveSettings() {

            Editor editor = sharedPrefs.edit();
            editor.putBoolean("male", male);
            editor.putBoolean("female", female);
            editor.putBoolean("with_photo", withPhoto);
            editor.putInt("age_from", age_from);
            editor.putInt("age_to", age_to);

            editor.commit();
        }

        public boolean isWithPhoto() {
            return withPhoto;
        }

        public void setWithPhoto(boolean withPhoto) {
            this.withPhoto = withPhoto;
        }

        public boolean isFemale() {
            return female;
        }

        public void setFemale(boolean female) {
            this.female = female;
        }

        public boolean isMale() {
            return male;
        }

        public void setMale(boolean male) {
            this.male = male;
        }

        public int getAge_from() {
            return age_from;
        }

        public void setAge_from(int age_from) {
            this.age_from = age_from;
        }

        public int getAge_to() {
            return age_to;
        }

        public void setAge_to(int age_to) {
            this.age_to = age_to;
        }
    }

}
