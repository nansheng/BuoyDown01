package com.example.lonejourneyman.buoydownnofragment.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by lonejourneyman on 3/31/17.
 */

public class BuoysDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "buoyslist.db";
    private static final int DATABASE_VERSION = 1;

    public BuoysDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_BUOYSLIST_TABLE = "CREATE TABLE " +
                BuoysContract.BuoysEntry.TABLE_NAME + " (" +
                BuoysContract.BuoysEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BuoysContract.BuoysEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                BuoysContract.BuoysEntry.COLUMN_LONG + " DOUBLE NOT NULL, " +
                BuoysContract.BuoysEntry.COLUMN_LAT + " DOUBLE NOT NULL, " +
                BuoysContract.BuoysEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                "); ";
        db.execSQL(SQL_CREATE_BUOYSLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BuoysContract.BuoysEntry.TABLE_NAME);
        onCreate(db);
    }
}
