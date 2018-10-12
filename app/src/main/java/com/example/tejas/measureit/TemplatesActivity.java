package com.example.tejas.measureit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class TemplatesActivity extends AppCompatActivity {

    private static final String TAG = "TemplatesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_templates);

        Intent intent = getIntent();
        String value = intent.getStringExtra("key");

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Templates");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
