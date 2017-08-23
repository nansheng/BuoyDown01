package com.example.lonejourneyman.buoydownnofragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lonejourneyman.buoydownnofragment.data.BuoysContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lonejourneyman on 3/31/17.
 */

public class BuoyListAdapter extends RecyclerView.Adapter<BuoyListAdapter.BuoyListHolder> {

    private String TAG = getClass().getSimpleName();

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
        String details = mCursor.getString(mCursor.getColumnIndex(BuoysContract.BuoysEntry.COLUMN_DETAILS));
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

        holder.buoyIndex.setText(String.valueOf(id));
        holder.buoyDescriptionView.setText(description);
        holder.buoyDetailsView.setText(details);
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

    class BuoyListHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.database_index) TextView buoyIndex;
        @BindView(R.id.buoy_date) TextView buoyDate;
        @BindView(R.id.buoy_description) TextView buoyDescriptionView;
        @BindView(R.id.database_details) TextView buoyDetailsView;
        @BindView(R.id.buoy_longitude) TextView buoyLongTextView ;
        @BindView(R.id.buoy_latitude) TextView buoyLatTextView;

        public BuoyListHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onClick(View v) {
            Intent detailIntent = new Intent(mContext, DetailActivity.class);
            detailIntent.putExtra("DatabaseIndex", buoyIndex.getText().toString());
            detailIntent.putExtra("Description", buoyDescriptionView.getText().toString());
            detailIntent.putExtra("DatabaseDetails", buoyDetailsView.getText().toString());
            detailIntent.putExtra("Longitude", buoyLongTextView.getText().toString());
            detailIntent.putExtra("Latitude", buoyLatTextView.getText().toString());
            detailIntent.putExtra("Date", buoyDate.getText().toString());
            mContext.startActivity(detailIntent);
        }
    }
}