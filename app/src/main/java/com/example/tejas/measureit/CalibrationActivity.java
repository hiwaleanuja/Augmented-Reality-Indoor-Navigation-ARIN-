package com.example.tejas.measureit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class CalibrationActivity extends AppCompatActivity {

    private static final String TAG = "CalibrationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        Intent intent = getIntent();
        String value = intent.getStringExtra("key");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Calibration");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
