package com.example.lonejourneyman.buoydownnofragment;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lonejourneyman.buoydownnofragment.data.BuoysContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by lonejourneyman on 3/31/17.
 */

public class BuoyListAdapter extends RecyclerView.Adapter<BuoyListAdapter.BuoyListHolder> {

    private Cursor mCursor;
    private Context mContext;

    public BuoyListAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public BuoyListHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.buoy_list_item, parent, false);

        return new BuoyListHolder(view);
    }

    @Override
    public void onBindViewHolder(BuoyListHolder holder, int position) {
        if (!mCursor.moveToPosition(position))
            return;

        int idIndex = mCursor.getColumnIndex(BuoysContract.BuoysEntry._ID);
        String description = mCursor.getString(mCursor.getColumnIndex(BuoysContract.BuoysEntry.COLUMN_DESCRIPTION));
        String longitude = mCursor.getString(mCursor.getColumnIndex(BuoysContract.BuoysEntry.COLUMN_LONG));
        String latitude = mCursor.getString(mCursor.getColumnIndex(BuoysContract.BuoysEntry.COLUMN_LAT));
        String timeStamp = mCursor.getString(mCursor.getColumnIndex(BuoysContract.BuoysEntry.COLUMN_TIMESTAMP));

        final int id = mCursor.getInt(idIndex);
        holder.itemView.setTag(id);

        SimpleDateFormat inputSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        inputSDF.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date myDate = inputSDF.parse(timeStamp);
            //SimpleDateFormat outputSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat outputSDF = new SimpleDateFormat("EEEE  MMM dd, yyyy  HH:mm a");
            holder.buoyDate.setText(outputSDF.format(myDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //holder.buoyDate.setText(timeStamp);
        //holder.buoyDate.setVisibility(View.GONE);

        holder.buoyDescriptionView.setText(description);
        holder.buoyLongTextView.setText(longitude);
        holder.buoyLatTextView.setText(latitude);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public Cursor swapCursor(Cursor c) {
        if (mCursor == c) {
            return null;
        }
        Cursor temp = mCursor;
        this.mCursor = c;

        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }

    class BuoyListHolder extends RecyclerView.ViewHolder {

        TextView buoyDate;
        TextView buoyDescriptionView;
        TextView buoyLongTextView;
        TextView buoyLatTextView;

        public BuoyListHolder(View itemView) {
            super(itemView);

            buoyDate = (TextView) itemView.findViewById(R.id.buoy_date);
            buoyDescriptionView = (TextView) itemView.findViewById(R.id.buoy_description);
            buoyLongTextView = (TextView) itemView.findViewById(R.id.buoy_longitude);
            buoyLatTextView = (TextView) itemView.findViewById(R.id.buoy_latitude);
        }
    }
}