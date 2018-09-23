package fragments;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.meetapp.free.loveme.R;

import include.AsyncResponse;
import include.IFY;

public class Settings extends AppCompatActivity implements AsyncResponse {

    private CheckBox chkWoman, chkMale, chkPhoto;
    private IFY ify;
    private SeekBar ageMinControl;
    private SeekBar ageMaxControl;
    private TextView txt_min_age;
    private TextView txt_max_age;
    private int minProgressChanged;
    private int maxProgressChanged;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.fragment_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        ify = new IFY();
        ify.init(this.getBaseContext());

        chkMale = (CheckBox) findViewById(R.id.chkMale);
        chkWoman = (CheckBox) findViewById(R.id.chkWoman);
        chkPhoto = (CheckBox) findViewById(R.id.chkPhoto);

        // init

        chkMale.setChecked(ify.currUser.getUserSettings().isMale());
        chkWoman.setChecked(ify.currUser.getUserSettings().isFemale());
        chkPhoto.setChecked(ify.currUser.getUserSettings().isWithPhoto());

        chkMale.setOnClickListener(new OnClickListener() {

            // Run when button is clicked
            @Override
            public void onClick(View v) {
                ify.currUser.getUserSettings().setMale(
                        (((CheckBox) v).isChecked()));
            }
        });

        chkWoman.setOnClickListener(new OnClickListener() {

            // Run when button is clicked
            @Override
            public void onClick(View v) {
                ify.currUser.getUserSettings().setFemale(
                        (((CheckBox) v).isChecked()));
            }
        });

        chkPhoto.setOnClickListener(new OnClickListener() {

            // Run when button is clicked
            @Override
            public void onClick(View v) {
                ify.currUser.getUserSettings().setWithPhoto(
                        (((CheckBox) v).isChecked()));
            }
        });


        RangeSeekBar();

    }

    private void showAlert(String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);

        builder.setMessage(message)
                .setTitle("Confirmation")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                ify.setSession(false);
                                restartSelf();

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

    private void restartSelf() {

        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

    }


    private void RangeSeekBar() {

        txt_min_age = (TextView) findViewById(R.id.txt_min_age);
        ageMinControl = (SeekBar) findViewById(R.id.ageMinControl);

        txt_min_age.setText("Min age: "
                + ify.currUser.getUserSettings().getAge_from());

        minProgressChanged = ify.currUser.getUserSettings().getAge_from();

        ageMinControl.setProgress(minProgressChanged);

        ageMinControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {

                minProgressChanged = progress + 18;

                if ((minProgressChanged >= 18) || (minProgressChanged == 60)) {

                    if (minProgressChanged > maxProgressChanged)
                        ageMinControl.setProgress(maxProgressChanged - 18);
                    else {
                        txt_min_age.setText("Min age: "
                                + Integer.valueOf(minProgressChanged));

                        ify.currUser.getUserSettings().setAge_from(
                                minProgressChanged);
                    }

                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        txt_max_age = (TextView) findViewById(R.id.txt_max_age);
        ageMaxControl = (SeekBar) findViewById(R.id.ageMaxControl);

        txt_max_age.setText("Max age: "
                + ify.currUser.getUserSettings().getAge_to());

        maxProgressChanged = ify.currUser.getUserSettings().getAge_to();

        ageMaxControl.setProgress(maxProgressChanged);

        ageMaxControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {

                maxProgressChanged = progress + 18;

                if ((maxProgressChanged >= 18) || (maxProgressChanged == 60)) {

                    if (maxProgressChanged < minProgressChanged)
                        ageMaxControl.setProgress(minProgressChanged - 18);
                    else {
                        txt_max_age.setText("Max age: "
                                + Integer.valueOf(maxProgressChanged));

                        ify.currUser.getUserSettings().setAge_to(
                                maxProgressChanged);
                    }
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {

                //
            }

            public void onStopTrackingTouch(SeekBar seekBar) {

                //
            }
        });

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

        MenuItem action_privacy = menu.findItem(R.id.action_privacy);
        action_privacy.setVisible(true);

        MenuItem action_menu_done = menu.findItem(R.id.action_menu_done);
        action_menu_done.setVisible(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar actions click
        int id = item.getItemId();

        if (id == R.id.home) {
            finish();
            return true;
        }


        if (id == R.id.action_rate_app) {

            Uri uri = Uri.parse("market://details?id="
                    + ify.context.getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id="
                                + ify.context.getPackageName())));
            }

        }

        if (id == R.id.action_privacy_police) {

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://kazanlachani.com/#/contacts"));
            startActivity(browserIntent);

        }

        if (id == R.id.action_logout_app) {

            showAlert("Are you sure you want to exit?");

        }

        if (id == R.id.action_deactivate_app) {
            showAlert("Are you sure you want to deactivate your account?");
        }

        if (id == R.id.action_menu_done) {


            ify.people.clear();
            ify.currUser.getUserSettings().SaveSettings();
            restartSelf();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}