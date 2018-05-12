package fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import com.meetapp.free.loveme.R;

import java.util.ArrayList;

import include.AsyncRequest;
import include.AsyncResponse;
import include.IFY;
import include.IFY.User;
import include.IntentHelper;
import lazylist.LazyAdapter;

public class Visitors extends Activity implements AsyncResponse {

    private IFY ify;
    private ProgressBar progressBar;
    ListView list;
    LazyAdapter adapter;
    private AsyncRequest request;
    private ArrayList<User> visitors;
    private Button btnRemoveVisitors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.visitors);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        getActionBar().setIcon(
                new ColorDrawable(getResources().getColor(
                        android.R.color.transparent)));
        setTitle("Visitors");

        btnRemoveVisitors = (Button) findViewById(R.id.btnRemoveVisitors);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        request = new AsyncRequest();
        request.delegate = this;

        ify = new IFY();
        ify.init(getApplicationContext());

        progressBar.setVisibility(View.VISIBLE);

        String url = IFY.SERVICE_URL + "visitors.php?user_id="
                + ify.currUser.getId() + "&user_hash="
                + ify.currUser.getUser_hash();
        request.execute(url);
    }

    @Override
    public void processFinish(String output) {

        visitors = ify.parseJson(output);

        if (visitors.isEmpty())
            setTitle("No Visitors");

        setAdapter();

    }

    @Override
    public void processBitmapFinish(Bitmap output) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processMessageFinish(String output) {

    }

    private void setAdapter() {

        list = (ListView) findViewById(R.id.listVisitors);
        adapter = new LazyAdapter(ify.getContext(), visitors, true);
        list.setAdapter(adapter);
        registerForContextMenu(list);

        progressBar.setVisibility(View.GONE);

        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                User user = visitors.get(position);
                IntentHelper.addObjectForKey(user, "key");

                Intent i = new Intent(ify.context, UserProfileView.class);
                startActivity(i);

            }

        });

        list.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {

                final int currPosition = position;
                final User selectedUser = (User) visitors.get(currPosition);

                AlertDialog.Builder builder = new AlertDialog.Builder(Visitors.this);

                builder.setCancelable(false);
                builder.setMessage("Remove from visitors?")
                        .setTitle("Confirmation")
                        .setCancelable(false)
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {

                                        ify.currUser.remove_curr_visitors(selectedUser);
                                        visitors.remove(selectedUser);
                                        adapter.notifyDataSetChanged();

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

                return false;
            }
        });


        if (!visitors.isEmpty()) {
            
            btnRemoveVisitors.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove_visitors();
                }
            });
        }
        else
            btnRemoveVisitors.setText("No Visitors");


    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar actions click
        switch (item.getItemId()) {
            case android.R.id.home: {

                finish();
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void remove_visitors() {

        if (visitors.isEmpty())
            return;


        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Remove all visitors?")
                .setTitle("Confirmation")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ify.currUser.remove_visitors();
                                visitors.clear();
                                adapter.notifyDataSetChanged();
                                setTitle("No visitors");

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
}
