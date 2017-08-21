package com.example.lonejourneyman.buoydownnofragment;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.lonejourneyman.buoydownnofragment.data.BuoysContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class BuoyListIntent extends RemoteViewsService {

    static final int INDEX_BUOY_ID = 0;
    static final int INDEX_BUOY_DESC = 1;
    static final int INDEX_BUOY_DETAIL = 2;
    static final int INDEX_BUOY_LONG = 3;
    static final int INDEX_BUOY_LAT = 4;
    static final int INDEX_BUOY_TIMESTAMP = 5;
    private static final String[] BUOY_COLUMNS = {
            BuoysContract.BuoysEntry.TABLE_NAME + "." + BuoysContract.BuoysEntry._ID,
            BuoysContract.BuoysEntry.COLUMN_DESCRIPTION,
            BuoysContract.BuoysEntry.COLUMN_DETAILS,
            BuoysContract.BuoysEntry.COLUMN_LONG,
            BuoysContract.BuoysEntry.COLUMN_LAT,
            BuoysContract.BuoysEntry.COLUMN_TIMESTAMP
    };
    public final String TAG = BuoyListIntent.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;


            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();
                Uri buoyListUri = BuoysContract.BuoysEntry.CONTENT_URI;
                data = getContentResolver().query(buoyListUri,
                        null,
                        null,
                        null,
                        BuoysContract.BuoysEntry.COLUMN_TIMESTAMP + "DESC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null ||
                        !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.list_buoy_detail_widget);
                String buoyDate = data.getString(INDEX_BUOY_TIMESTAMP);

                SimpleDateFormat inputSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                inputSDF.setTimeZone(TimeZone.getTimeZone("UTC"));
                try {
                    Date myDate = inputSDF.parse(buoyDate);
                    //SimpleDateFormat outputSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    SimpleDateFormat outputSDF = new SimpleDateFormat("EEEE  MMM dd, yyyy  HH:mm a");
                    buoyDate = outputSDF.format(myDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                String buoyDescription = data.getString(INDEX_BUOY_DESC);
                String buoyLong = data.getString(INDEX_BUOY_LONG);
                String buoyLat = data.getString(INDEX_BUOY_LAT);

                views.setTextViewText(R.id.widget_buoy_date, buoyDate);
                views.setTextViewText(R.id.widget_buoy_description, buoyDescription);
                views.setTextViewText(R.id.widget_buoy_latitude, buoyLat);
                views.setTextViewText(R.id.widget_buoy_longitude, buoyLong);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_buoy_detail_widget);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position)) return data.getLong(INDEX_BUOY_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
