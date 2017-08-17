package com.example.lonejourneyman.buoydownnofragment.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by lonejourneyman on 3/31/17.
 */

public class BuoysContract {

    public static final String AUTHORITY = "com.example.lonejourneyman.buoydownnofragment";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_TASKS = "tasks";

    public static final class BuoysEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASKS).build();

        public static final String TABLE_NAME = "buoyslist";
        public static final String COLUMN_DESCRIPTION = "buoyDescription";
        public static final String COLUMN_LONG = "buoyLongitude";
        public static final String COLUMN_LAT = "buoyLatitude";
        public static final String COLUMN_TIMESTAMP = "buoyTimestamp";
    }

}
