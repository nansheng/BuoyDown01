package com.example.lonejourneyman.buoydownnofragment;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.SearchView;

import com.example.lonejourneyman.buoydownnofragment.data.BuoysContract;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback,
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ACTION_DATA_UPDATED =
            "com.example.lonejourneyman.buoydownnofragment.ACTION_DATA_UPDATED";
    private static final int TASK_LOADER_ID = 0;
    private static final int TASK_SEARCH_ID = 1;
    private static final int MY_PERMISSION_LOCATION = 1;
    private static GoogleApiClient mApiClient;
    //RecyclerView mRecyclerView;
    private String TAG = getClass().getSimpleName();
    private BuoyListAdapter mAdapter;
    private SharedPreferences prefs;

    Boolean initialCountdown = true;
    CountDownTimer triggerCountdown;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.all_buoy_list_view) RecyclerView mRecyclerView;
    @BindView(R.id.fab) FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        final Context context = this;
        setContentView(R.layout.activity_main);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ButterKnife.bind(this);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //mRecyclerView = (RecyclerView) findViewById(R.id.all_buoy_list_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new BuoyListAdapter(context);
        mRecyclerView.setAdapter(mAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int id = (int) viewHolder.itemView.getTag();

                String stringId = Integer.toString(id);
                Uri uri = BuoysContract.BuoysEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(stringId).build();

                getContentResolver().delete(uri, null, null);

                getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, MainActivity.this);

                // Updating widget
                Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                        .setPackage(context.getPackageName());
                context.sendBroadcast(dataUpdatedIntent);
            }
        }).attachToRecyclerView(mRecyclerView);

        //FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_content_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AwarenessQueryTask().execute();
            }
        });

        mApiClient = new GoogleApiClient.Builder(context)
                .addApi(Awareness.API)
                .enableAutoManage(this, 1, null)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.i(TAG, "Google API Client is connected");
                    }
                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.i(TAG, "Google API Client is suspended!");
                    }
                })
                .build();

        if (prefs.getBoolean("autosave_switch", true)) {
            initialAwarenessTask();
        }
    }

    private void initialAwarenessTask() {
        new AwarenessQueryTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, MainActivity.this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {

        return new AsyncTaskLoader<Cursor>(this) {

            Cursor mTaskData = null;

            @Override
            protected void onStartLoading() {
                if (mTaskData != null) {
                    deliverResult(mTaskData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {

                String sortString = (prefs.getString("Sorting", "Shit").equals("0"))
                        ? BuoysContract.BuoysEntry.COLUMN_TIMESTAMP + " DESC" :
                        BuoysContract.BuoysEntry.COLUMN_DESCRIPTION + " ASC";

                String buoySelection = null;
                if (id == 1)
                buoySelection = (args.getString("query").isEmpty()) ? null :
                        BuoysContract.BuoysEntry.COLUMN_DESCRIPTION +
                        " LIKE '%" + args.getString("query") + "%'";

                try {
                    return getContentResolver().query(BuoysContract.BuoysEntry.CONTENT_URI,
                            null,
                            buoySelection,
                            null,
                            sortString);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load location data.");
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResult(Cursor data) {
                mTaskData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private void getLocationSnapshot() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Awareness.SnapshotApi.getLocation(mApiClient)
                    .setResultCallback(new ResultCallback<LocationResult>() {
                        @Override
                        public void onResult(@NonNull LocationResult locationResult) {
                            if (!locationResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Could not get location.");
                                return;
                            }
                            Location location = locationResult.getLocation();
                            //Log.d(TAG, "Lat: " + location.getLatitude() +
                            //        ", Lng: " + location.getLongitude());
                            addLocationTask(location);
                        }
                    });
        } else {
            Log.i(TAG, "Not going into getLocation");
        }
    }

    private void addLocationTask(Location location) {
        ContentValues cv = new ContentValues();
        cv.put(BuoysContract.BuoysEntry.COLUMN_DESCRIPTION, "#TBD");
        cv.put(BuoysContract.BuoysEntry.COLUMN_DETAILS, "#DETAILS");
        cv.put(BuoysContract.BuoysEntry.COLUMN_LONG, location.getLongitude());
        cv.put(BuoysContract.BuoysEntry.COLUMN_LAT, location.getLatitude());

        Uri uri = getContentResolver().insert(BuoysContract.BuoysEntry.CONTENT_URI, cv);
        if (uri != null) {

            getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, MainActivity.this);

            // notification
            if (prefs.getBoolean("send_notification", true)) {

                //Setting for Auto Messaging when Read
                int thisConversationId = 6;
                Intent msgHeardIntent = new Intent()
                        .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                        .setAction("com.example.lonejourneyman.buoydownnofragment.ACTION_MESSAGE_READ")
                        .putExtra("conversation_id", thisConversationId);
                PendingIntent msgHeardPendingIntent =
                        PendingIntent.getBroadcast(getApplicationContext(), thisConversationId,
                                msgHeardIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                //Setting for Auto Messaging when Reply
                Intent msgReplyIntent = new Intent()
                        .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                        .setAction("com.example.lonejourneyman.buoydownnofragment.ACTION_MESSAGE_REPLY")
                        .putExtra("conversation_id", thisConversationId);
                PendingIntent msgReplyPendingIntent =
                        PendingIntent.getBroadcast(getApplicationContext(), thisConversationId,
                                msgReplyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                //Auto Remote
                RemoteInput remoteInput = new RemoteInput.Builder("voice_reply_key")
                        .setLabel("Prompt text")
                        .build();
                //Auto Conversation
                String conversationName = "BUOY DOWN";
                NotificationCompat.CarExtender.UnreadConversation.Builder unreadConvBuild =
                        new android.support.v4.app.NotificationCompat.CarExtender.UnreadConversation.Builder(
                                conversationName)
                        .setReadPendingIntent(msgHeardPendingIntent)
                        .setReplyAction(msgReplyPendingIntent, remoteInput);
                unreadConvBuild.addMessage("New Location Saved!")
                        .setLatestTimestamp(System.currentTimeMillis());

                NotificationCompat.Builder mBuilder =
                        (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.buoy_one)
                                .setContentTitle("BUOY DOWN! Notification")
                                .setContentText("New Location Saved!")
                                .setAutoCancel(true)
                                .extend(new android.support.v4.app.NotificationCompat.CarExtender()
                                        .setUnreadConversation(unreadConvBuild.build()));
                //.setContentIntent(pendingIntent); when user click on the notificasitaon
                //.setLargeIcon(Bitmap.... - large left icon
                //.setSubText(String)

//                NotificationManager mNotifyMgr =
//                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                NotificationManagerCompat mNotifyMgr =
                        NotificationManagerCompat.from(getApplicationContext());
                mNotifyMgr.notify(001, mBuilder.build());

                // Updating widget
                Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                        .setPackage(this.getPackageName());
                this.sendBroadcast(dataUpdatedIntent);
            }
        }
    }

    private boolean checkAndRequestWeatherPermissions() {
        if (ContextCompat.checkSelfPermission(
                MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_LOCATION
                );
            } else {
                Log.i(TAG, "Permission previously denied and app shouldn't ask again.");
            }
            return false;
        } else {
            return true;
        }
    }

    private void searchBuoys(String query) {
        Bundle qBundle = new Bundle();
        qBundle.putString("query",query);

        getSupportLoaderManager().restartLoader(TASK_SEARCH_ID, qBundle, MainActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchBuoys(query);
                searchView.clearFocus();
                //searchView.setQuery("", false);
                //searchView.setIconified(true);
                searchItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                if (!initialCountdown) {
                    triggerCountdown.cancel();
                }
                triggerCountdown = new CountDownTimer(2000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }
                    @Override
                    public void onFinish() {
                        searchBuoys(newText);
                    }
                };
                triggerCountdown.start();
                initialCountdown = false;
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class AwarenessQueryTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            if (checkAndRequestWeatherPermissions()) {
                getLocationSnapshot();
            }
            return null;
        }
    }
}