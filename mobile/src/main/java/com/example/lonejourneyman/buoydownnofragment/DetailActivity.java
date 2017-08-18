package com.example.lonejourneyman.buoydownnofragment;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.lonejourneyman.buoydownnofragment.data.BuoysDbHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    BuoysDbHelper mBuoysDbHelper;
    String detailIndex;
    String detailDesc;
    String detailDetails;
    String detailLong;
    String detailLat;
    LatLng detailLoc;
    EditText editTitle;
    EditText editDescription;
    SQLiteDatabase db;
    Boolean initialCountdown = true;
    CountDownTimer triggerCountdown;
    private String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mBuoysDbHelper = new BuoysDbHelper(getBaseContext());
        db = mBuoysDbHelper.getWritableDatabase();

//        Uri uri = BuoysContract.BuoysEntry.CONTENT_URI;
//        uri = uri.buildUpon().appendPath(detailIndex).build();

        Intent intent = getIntent();
        detailIndex = intent.getStringExtra("DatabaseIndex");
        detailDesc = intent.getStringExtra("Description");
        detailDetails = intent.getStringExtra("DatabaseDetails");
        detailLong = intent.getStringExtra("Longitude");
        detailLat = intent.getStringExtra("Latitude");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().
                findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        editTitle = (EditText) findViewById(R.id.buoy_description);
        editTitle.setText(detailDesc);
        editTitle.addTextChangedListener(new GenericTextWatcher(editTitle));

        editDescription = (EditText) findViewById(R.id.text_description);
        editDescription.setText(detailDetails);
        editDescription.addTextChangedListener(new GenericTextWatcher(editDescription));

        Double dLat = Double.valueOf(detailLat);
        Double dLong = Double.valueOf(detailLong);
        detailLoc = new LatLng(dLat, dLong);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> address = geocoder.getFromLocation(dLat, dLong, 1);
            if (address != null && address.size() > 0) {
                String detAddress = address.get(0).getAddressLine(0);
                String detCity = address.get(0).getLocality();
                String detState = address.get(0).getSubAdminArea();
                String detCountry = address.get(0).getCountryName();
                String detPostal = address.get(0).getPostalCode();

                Log.d(TAG, "ADD : " + detailIndex);

                TextView detLat = (TextView) findViewById(R.id.text_latitude);
                detLat.setText("Latitude :  " + detailLat + "                ");
                TextView detLong = (TextView) findViewById(R.id.text_longitude);
                detLong.setText("Longitude :  " + detailLong);
                TextView detTitle = (TextView) findViewById(R.id.text_address);
                detTitle.setText("Address :  " + detAddress);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setPadding(0, 0, 0, 0);
        googleMap.addMarker(new MarkerOptions()
                .position(detailLoc)
                .title("Marker"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom((detailLoc), 18));
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Pressing Back!!!");
        super.onBackPressed();
    }

    private class GenericTextWatcher implements TextWatcher {
        private View watcherView;

        private GenericTextWatcher(View watcherView) {
            this.watcherView = watcherView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            Log.d(TAG, "beforetextchange");
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.d(TAG, "on text change");
        }

        @Override
        public void afterTextChanged(final Editable s) {
            Log.d(TAG, "aftertextchange");

            if (!initialCountdown) {
                triggerCountdown.cancel();
            }

            triggerCountdown = new CountDownTimer(2000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {

                    ContentValues cv = new ContentValues();
                    switch (watcherView.getId()) {
                        case R.id.buoy_description:
                            cv.put("buoyDescription", s.toString());
                            break;
                        case R.id.text_description:
                            cv.put("buoyDetails", s.toString());
                            break;
                    }
                    if (cv.size() > 0) {
                        db.update("buoyslist", cv, "_ID=?", new String[]{detailIndex});
                        Log.d(TAG, "DATABASE SAVE!");
                    }

                }
            };
            triggerCountdown.start();
            initialCountdown = false;

        }
    }
}
