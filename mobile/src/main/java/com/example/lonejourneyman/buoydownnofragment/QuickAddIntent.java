package com.example.lonejourneyman.buoydownnofragment;

import android.Manifest;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.lonejourneyman.buoydownnofragment.data.BuoysContract;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

/**
 * Created by lonejourneyman on 8/19/17.
 */

public class QuickAddIntent extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public static final String ACTION_QUICK_ADD =
            "com.example.lonejourneyman.buoydownnofragment.ACTION_QUICK_ADD";
    public static final String ACTION_DATA_UPDATED =
            "com.example.lonejourneyman.buoydownnofragment.ACTION_DATA_UPDATED";

    private String TAG = getClass().getSimpleName();
    private GoogleApiClient mApiClient;

    public QuickAddIntent() {
        super("QuickAddIntent");
    }

    public static void startActionQuickAdd(Context context) {

        Log.d("quickadd", "start qiocl add");

        Intent intent = new Intent(context, QuickAddIntent.class);
        intent.setAction(ACTION_QUICK_ADD);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_QUICK_ADD.equals(action)) {
                handleActionQuickAdd();
            }
        }

    }

    private void handleActionQuickAdd() {
        Log.d("quickadd", "handle qiocl add");

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Awareness.API)
                .build();
        mApiClient.connect();


        Log.d("quickadd", "permission " + PackageManager.PERMISSION_GRANTED + " | " +
                mApiClient.isConnected() + " | " + Awareness.API.toString() + " | " +
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION));


        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("quickadd", "awareness " + mApiClient.toString());
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
            return;
        }
        mApiClient.disconnect();
    }

    private void addLocationTask(Location location) {
        ContentValues cv = new ContentValues();
        cv.put(BuoysContract.BuoysEntry.COLUMN_DESCRIPTION, "#TBD");
        cv.put(BuoysContract.BuoysEntry.COLUMN_DETAILS, "#DETAILS");
        cv.put(BuoysContract.BuoysEntry.COLUMN_LONG, location.getLongitude());
        cv.put(BuoysContract.BuoysEntry.COLUMN_LAT, location.getLatitude());

        Uri uri = getContentResolver().insert(BuoysContract.BuoysEntry.CONTENT_URI, cv);
        if (uri != null) {
            // notification
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (prefs.getBoolean("send_notification", true)) {
                NotificationCompat.Builder mBuilder =
                        (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.buoy_one)
                                .setContentTitle("BUOY DOWN! Notification")
                                .setContentText("New Location Saved!");
                NotificationManager mNotifyMgr =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotifyMgr.notify(003, mBuilder.build());
            }

            Intent dataUpdaetadIntent = new Intent(ACTION_DATA_UPDATED);
            getApplicationContext().sendBroadcast(dataUpdaetadIntent);
        }
    }
}
