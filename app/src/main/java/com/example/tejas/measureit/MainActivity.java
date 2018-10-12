package com.example.tejas.measureit;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }


    private static final int REQUEST_TAKE_PHOTO = 1;

    String mCurrentPhotoPath;
    Uri photoURI;

    private static final String TAG = "MainActivity";

    int value = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate");
        if(OpenCVLoader.initDebug()){
            Log.i(TAG, "OpenCV Loaded");

        }
        else{
            Log.i(TAG, "OpenCV Cannot Be loaded");
        }

        Button BTNmyProjects = (Button) findViewById(R.id.BTNmyProjects);
        //Button BTNcalibrate = (Button) findViewById(R.id.BTNcalibrate);
        Button BTNtemplate = (Button) findViewById(R.id.BTNtemplate);

        BTNmyProjects.setOnClickListener((v) -> {
            Intent myIntent = new Intent(MainActivity.this, ProjectListActivity.class);
            myIntent.putExtra("key", value); //Optional parameters
            MainActivity.this.startActivity(myIntent);
        });

        /*BTNcalibrate.setOnClickListener((v) -> {
            Intent myIntent = new Intent(MainActivity.this, CalibrationActivity.class);
            myIntent.putExtra("key", value); //Optional parameters
            MainActivity.this.startActivity(myIntent);
        });*/

        BTNtemplate.setOnClickListener((v) -> {
            dispatchTakePictureIntent();
        });

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileproviderMeasure",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",    /* suffix */
                storageDir      /* directory */
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

}
