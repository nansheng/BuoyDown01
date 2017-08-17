package com.example.lonejourneyman.buoydownnofragment;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

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

    String detailDesc;
    String detailLong;
    String detailLat;
    LatLng detailLoc;
    private String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        detailDesc = intent.getStringExtra("Description");
        detailLong = intent.getStringExtra("Longitude");
        detailLat = intent.getStringExtra("Latitude");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().
                findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

                Log.d(TAG, "ADD : " +
                        detAddress + " " +
                        detCity + " " +
                        detState + " " +
                        detCountry + " " +
                        detPostal);

                TextView detTitle = (TextView) findViewById(R.id.text_address);
                detTitle.setText(detAddress);


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
}
