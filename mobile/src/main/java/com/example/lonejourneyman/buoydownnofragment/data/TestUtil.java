package com.example.lonejourneyman.buoydownnofragment.data;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lonejourneyman on 3/31/17.
 */

public class TestUtil {

    public static void insertFakeData(SQLiteDatabase db) {
        if (db == null) {
            return;
        }
        //create a list of fake guests
        List<ContentValues> list = new ArrayList<ContentValues>();

        ContentValues cv = new ContentValues();
        cv.put(BuoysContract.BuoysEntry.COLUMN_DESCRIPTION, "John");
        cv.put(BuoysContract.BuoysEntry.COLUMN_LONG, 12);
        cv.put(BuoysContract.BuoysEntry.COLUMN_LAT, 12);
        list.add(cv);

        cv = new ContentValues();
        cv.put(BuoysContract.BuoysEntry.COLUMN_DESCRIPTION, "Tim");
        cv.put(BuoysContract.BuoysEntry.COLUMN_LONG, 2);
        cv.put(BuoysContract.BuoysEntry.COLUMN_LAT, 2);
        list.add(cv);

        cv = new ContentValues();
        cv.put(BuoysContract.BuoysEntry.COLUMN_DESCRIPTION, "Jessica");
        cv.put(BuoysContract.BuoysEntry.COLUMN_LONG, 99);
        cv.put(BuoysContract.BuoysEntry.COLUMN_LAT, 99);
        list.add(cv);

        cv = new ContentValues();
        cv.put(BuoysContract.BuoysEntry.COLUMN_DESCRIPTION, "Larry");
        cv.put(BuoysContract.BuoysEntry.COLUMN_LONG, 1);
        cv.put(BuoysContract.BuoysEntry.COLUMN_LAT, 1);
        list.add(cv);

        cv = new ContentValues();
        cv.put(BuoysContract.BuoysEntry.COLUMN_DESCRIPTION, "Kim");
        cv.put(BuoysContract.BuoysEntry.COLUMN_LONG, 45);
        cv.put(BuoysContract.BuoysEntry.COLUMN_LAT, 45);
        list.add(cv);

        //insert all guests in one transaction
        try {
            db.beginTransaction();
            //clear the table first
            db.delete(BuoysContract.BuoysEntry.TABLE_NAME, null, null);
            //go through the list and add one by one
            for (ContentValues c : list) {
                db.insert(BuoysContract.BuoysEntry.TABLE_NAME, null, c);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            //too bad :(
        } finally {
            db.endTransaction();
        }

    }

}
