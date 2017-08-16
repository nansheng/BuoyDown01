package com.example.lonejourneyman.buoydownnofragment;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.lonejourneyman.buoydownnofragment.data.BuoysContract;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TASK_LOADER_ID = 0;
    private static final int MY_PERMISSION_LOCATION = 1;
    TextView mLocationDisplayText;
    RecyclerView mRecyclerView;
    private String TAG = getClass().getSimpleName();
    private GoogleApiClient mApiClient;
    private BuoyListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        final Context context = this;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.all_buoy_list_view);
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
            }
        }).attachToRecyclerView(mRecyclerView);

        //mLocationDisplayText = (TextView) findViewById(R.id.location_display);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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

        initialAwarenessTask();
    }

    private void initialAwarenessTask() {
        new AwarenessQueryTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

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
                try {
                    return getContentResolver().query(BuoysContract.BuoysEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            BuoysContract.BuoysEntry.COLUMN_TIMESTAMP + " DESC");
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
                            Log.i(TAG, "Lat: " + location.getLatitude() +
                                    ", Lng: " + location.getLongitude());
                            addLocationTask(location);
                        }
                    });
        } else {
            Log.i(TAG, "Not going into getLocation");
        }
    }

    private void addLocationTask(Location location) {
        ContentValues cv = new ContentValues();
        cv.put(BuoysContract.BuoysEntry.COLUMN_DESCRIPTION, "Description");
        cv.put(BuoysContract.BuoysEntry.COLUMN_LONG, location.getLongitude());
        cv.put(BuoysContract.BuoysEntry.COLUMN_LAT, location.getLatitude());

        Uri uri = getContentResolver().insert(BuoysContract.BuoysEntry.CONTENT_URI, cv);
        if (uri != null) {
            //Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
            getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, MainActivity.this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

//        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
//        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

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