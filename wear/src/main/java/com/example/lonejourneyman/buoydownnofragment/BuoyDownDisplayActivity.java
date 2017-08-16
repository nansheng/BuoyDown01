package com.example.lonejourneyman.buoydownnofragment;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class BuoyDownDisplayActivity extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        mTextView = (TextView) findViewById(R.id.text);
    }
}
