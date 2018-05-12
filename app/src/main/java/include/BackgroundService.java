package include;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import com.meetapp.free.loveme.MainActivity;
import com.meetapp.free.loveme.R;

public class BackgroundService extends Service implements AsyncResponse {

    public Context context = this;
    public Handler handler = null;
    private IFY ify;
    private static int NOTIFICATION_ID = 999;
    public static Runnable runnable = null;
    private boolean chat_sound = true;
    private NotificationManager mManager = null;
    private Bitmap userPhoto;
    private int user_id;
    private String messagesCount;
    private AsyncMessages messageRequest;

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onCreate() {

        ify = new IFY();
        ify.init(context);

        handler = new Handler();
        runnable = new Runnable() {
            public void run() {

                if (android.os.Build.VERSION.SDK_INT > 9) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                }

                try {
                    messageNotification();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                handler.postDelayed(runnable, 30000);
            }

        };

        handler.postDelayed(runnable, 30000);
    }

   /*
    public String convertStandardJSONString(String data_json) {
        data_json = data_json.replace("\\", "");
        data_json = data_json.replace("\"{", "{");
        data_json = data_json.replace("}\",", "},");
        data_json = data_json.replace("}\"", "}");
        return data_json;
    }
    */


    @Override
    public void onDestroy() {
        //
    }

    @Override
    public void onStart(Intent intent, int startid) {
        //
    }

    private void messageNotification() throws IOException, JSONException {

        if ((ify != null) && (ify.currUser != null)) {

            messageRequest = new AsyncMessages();
            messageRequest.delegate = this;

            String Url = "https://ifymessages.herokuapp.com/load.php?user_id=" + ify.currUser.getId();
            messageRequest.execute(Url);

            /*
             String result = executeUrl(JsonObject);
            if ( (result != null) && (result.length() > 1))
                addNotification(result);
            */


        }

    }

    /*
    private void getUserImage() throws IOException {

        this.userPhoto = null;
        if (user_id > 0) {
            String imageUrl = "http://kazanlachani.com/ify/services/imageNotify.php?id="
                    + user_id;

            imageUrl = executeUrl(imageUrl);
            imageUrl = imageUrl.replace("ttp", "http");

            // get bitmap
            if ((user_id > 0) && (imageUrl.length() > 0))
                this.userPhoto = imageSource(imageUrl);
        }
    }


    protected Bitmap imageSource(String param) throws IOException {

        InputStream in;

        URL url = new URL(param);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();
        in = connection.getInputStream();
        Bitmap myBitmap = BitmapFactory.decodeStream(in);
        return myBitmap;

    }

    public String executeUrl(String url) throws IOException {

        if (url.length() <= 0)
            return "";

        BufferedReader inputStream = null;

        URL jsonUrl = new URL(url);

        URLConnection dc = jsonUrl.openConnection();

        dc.setConnectTimeout(5000);
        dc.setReadTimeout(5000);

        inputStream = new BufferedReader(new InputStreamReader(
                dc.getInputStream()));

        // read the JSON results into a string
        if (inputStream.read() > -1)
            return inputStream.readLine();
        else
            return "";

    }

    */

    private void addNotification(String msg) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Love App")
                .setContentText(msg);

        if (!isActivityRunning()) {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

        } else {
            PendingIntent contentIntent = PendingIntent.getActivity(
                    getApplicationContext(), 0, new Intent(this,
                            MainActivity.class), 0);
            builder.setContentIntent(contentIntent);
        }

        // Add as notification
        mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = builder.build();

        notification.ledARGB = 0x00FF00;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;

        notification.ledOnMS = 100;
        notification.ledOffMS = 100;

        //notification.defaults |= Notification.DEFAULT_LIGHTS;

        if (chat_sound)
            notification.defaults |= Notification.DEFAULT_SOUND;

        mManager.notify(NOTIFICATION_ID, notification);

    }

    protected Boolean isActivityRunning() {
        ActivityManager activityManager = (ActivityManager) getBaseContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        @SuppressWarnings("deprecation")
        List<ActivityManager.RunningTaskInfo> tasks = activityManager
                .getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (MainActivity.class.getCanonicalName().equalsIgnoreCase(
                    task.baseActivity.getClassName()))
                return true;
        }

        return false;
    }

    @Override
    public void processFinish(String output) {

        ify.currUser.calculateMessagesCount(output);
        messagesCount = String.valueOf(IFY.messages.size());
        sendBroadcast(messagesCount);
    }

    @Override
    public void processBitmapFinish(Bitmap output) {
        //
    }

    @Override
    public void processMessageFinish(String output) {

        if ( (output != null) && (output.length() > 1))
            addNotification(output);

    }

    private void sendBroadcast(String titleMessages) {
        Intent intent = new Intent("message");
        intent.putExtra("messagesCount", titleMessages);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
