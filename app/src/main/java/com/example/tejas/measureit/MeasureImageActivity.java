package com.example.tejas.measureit;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class MeasureImageActivity extends AppCompatActivity {

    //Multiplying the RATIO = (LENGTH/BREADTH) with calculated area
    //will give us Length of Parallel marker in Pixels in image.
    private static final double RATIO = 1.5;

    private static final String TAG = "MeasureImageActivity";

    //Mat to store the processed image.
    Mat inputMat = new Mat();
    //Mat on which OpenCV operations will be performed
    Mat processedMat = new Mat();
    //Mat to store the transformation Matrix
    Mat M = new Mat();
    //To store Uri of the captured/stored image.
    Uri myUri;


    public static Bitmap orgBitmap = null;      //Load the Captured/Stored bitmap in this variable.
    public static Bitmap bitmapImage;           //Store the processed Mat to Bitmap.
    public static Bitmap resizedBitmap;
    DrawableImageView drawableImageView;

    private double parallelLENGTH = 0;
    private double perspectiveLENGTH = 0;

    public int[] imgResolution = {4096, 2304};
    public int[] scrResolution = {1920, 1080};

    private boolean parallelMarkerPresent = true;
    private boolean perspectiveMarkerPresent = true;

    ImageView editImage;
    Button saveImage;
    EditText newTitle;
    private int dataCount;

    private int IMAGE_ID;
    private String IMAGE_TITLE;
    private byte[] IMAGE_THUMBNAIL;

    DatabaseHelper myDB;

    static MeasureImageActivity measureImageActivity;
    Processing processing = new Processing();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_image);

        //Create an object to be referenced from DrawableImageView.
        measureImageActivity = this;

        myDB = new DatabaseHelper(this);

        drawableImageView = findViewById(R.id.measureImageView);
        saveImage = findViewById(R.id.saveButton);
        newTitle  = findViewById(R.id.newTitle);

        Intent intent = getIntent();

        //As this activity can be opened from two Classes, use the switch case to check.
        switch (intent.getStringExtra("Activity")){
            case "ImageListActivity":
                myUri = Uri.parse(intent.getStringExtra("imageUri"));

                try {
                    orgBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), myUri);
                }
                catch (IOException ex){
                    Log.i(TAG, "Could not resolve URI");
                }

                break;
            case "ImageAdapter":
                Image image = (Image) intent.getSerializableExtra("imageBitmap");
                try {
                    myUri = Uri.parse(image.getImageThumbnailUri());
                    orgBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), myUri);
                }
                catch (IOException ex){
                    Log.i(TAG, "Could not resolve URI");
                }
                break;
        }

        orgBitmap = orgBitmap.copy(Bitmap.Config.ARGB_8888, true);
        orgBitmap = rotateImageIfRequired(orgBitmap);

        Utils.bitmapToMat(orgBitmap, inputMat);
        bitmapImage = Bitmap.createBitmap(
                inputMat.cols(),
                inputMat.rows(),
                Bitmap.Config.ARGB_8888
        );

        boolean parallelMarkerPresent = markerParallel();
        boolean perspectiveMarkerPresent = markerPerspective();
        //Conditions to handle Presence/Absence of markers in the image.
        if(!parallelMarkerPresent && !perspectiveMarkerPresent){
            Toast.makeText(
                    MeasureImageActivity.this,
                    "No Marker Found",
                    Toast.LENGTH_LONG
            ).show();

        }else if(parallelMarkerPresent && !perspectiveMarkerPresent){
            Toast.makeText(
                    MeasureImageActivity.this,
                    "Perspective Marker Not Found, Measurements will be available only in Parallel View",
                    Toast.LENGTH_LONG
            ).show();

        }else if(!parallelMarkerPresent && perspectiveMarkerPresent){
            Toast.makeText(
                    MeasureImageActivity.this,
                    "Parallel Marker Not Found, Measurements will be available only in Perspective View",
                    Toast.LENGTH_LONG
            ).show();
        } else if(parallelMarkerPresent && perspectiveMarkerPresent){
            Toast.makeText(
                    MeasureImageActivity.this,
                    "Markers Detected",
                    Toast.LENGTH_LONG
            ).show();
        }

        Utils.matToBitmap(processedMat, bitmapImage);

        //Resized the image to match the screen resolution for experimenting with the zoomed view.
        resizedBitmap = Bitmap.createScaledBitmap(bitmapImage, 1080, 1920, false);

        drawableImageView.setImageBitmap(bitmapImage);
        int temp = 0;
        Log.i(TAG, Integer.toString(temp));

        saveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Save Clicked");
                newTitle.setVisibility(View.VISIBLE);
                String title = newTitle.getText().toString();
                Log.i(TAG, "Edit = "+title);
                addImage(title, myUri);
                //newTitle.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void addImage(String title, Uri myUri) {
        IMAGE_TITLE = title;
        /*Cursor res = myDB.selectImage();
        dataCount = res.getCount();*/
        Log.i(TAG, "orgBitmap 1 = "+orgBitmap.toString());
        boolean isInserted = myDB.insertImage(IMAGE_TITLE, myUri);
        Log.i(TAG, "isInserted = "+isInserted);
        Log.i(TAG, "orgBitmap 2 = "+orgBitmap.toString());
        if(isInserted){
            //addToImageList(++dataCount, IMAGE_TITLE, bitmap);
            Toast.makeText(MeasureImageActivity.this,
                    "New Project Added",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MeasureImageActivity.this,
                    "Failed",
                    Toast.LENGTH_LONG).show();
        }
    }

    public static MeasureImageActivity getInstance(){
        return measureImageActivity;
    }

    public boolean markerPerspective(){     //Detects marker in perspective

        Mat Mask = processing.findROI(inputMat, processing.RED);    //Find Region Of Interest

        try {
            MatOfPoint contour = processing.findROIContours(Mask);  //Find Contours of the ROI
            Point[] Rect = processing.getRectangle(contour, processing.SKEWED); //Get the skewed marker which is in perspective
            //Draw the rectangle.
            for (int j = 0; j < 4; j++){
                Imgproc.line(inputMat, Rect[j], Rect[(j+1)%4], new Scalar(0,255,0), 7);
            }

            //Calculate the transfomation Matrix.
            M = processing.getTransformation(Rect);
            //Get the length of rectangular marker in perspective.
            perspectiveLENGTH = processing.getMarkerLength(Rect);
            Imgproc.putText(
                    inputMat,
                    Double.toString(Math.round(perspectiveLENGTH))+" px",
                    new Point(Rect[3].x, (Rect[3].y)+50),
                    Core.FONT_HERSHEY_PLAIN,
                    3.0,
                    new Scalar(128,128,128),
                    7
            );
        } catch (Exception e) {
            //If marker not found, set the flag to false
            perspectiveMarkerPresent = false;
            e.printStackTrace();
            return false;
        }finally {
            processedMat = inputMat;
            Log.i(TAG, "perspectiveLENGTH = " + perspectiveLENGTH);
        }

        return true;

    }

    public boolean markerParallel(){

        Mat Mask = processing.findROI(inputMat, processing.GREEN);

        double AREA = 0;
        try {
            MatOfPoint contour = processing.findROIContours(Mask);
            Point[] Rect = processing.getRectangle(contour, processing.ROTATED);
            for (int j = 0; j < 4; j++){
                Imgproc.line(inputMat, Rect[j], Rect[(j+1)%4], new Scalar(0,255,0), 7);
            }

            AREA = Imgproc.contourArea(contour);
            parallelLENGTH = Math.sqrt(AREA * RATIO);
            Imgproc.putText(
                    inputMat,
                    Double.toString(Math.round(parallelLENGTH))+" px",
                    new Point(Rect[2].x, (Rect[2].y)-10),
                    Core.FONT_HERSHEY_PLAIN,
                    3.0,
                    new Scalar(128,128,128),
                    7
            );

        } catch (Exception e) {
            e.printStackTrace();

            parallelMarkerPresent = false;

            return false;

        } finally {

            processedMat = inputMat;
            Log.i(TAG, "Area = " + AREA);
            Log.i(TAG, "Length = " + Math.sqrt(AREA * RATIO));

        }

        return true;

    }

    public int getPerspectiveLineLength(Point[] points){
        if(perspectiveMarkerPresent){
            Point[] convertedPoints = processing.transformPoints(points, this.M);
            int lineLength = (int)processing.eucDistance(convertedPoints[0], convertedPoints[1]);
            return lineLength;
        }
        else{
            return -1;
        }

    }

    public double getParallelLength(){
        if(parallelMarkerPresent){
            return this.parallelLENGTH;
        }
        else{
            return -1;
        }
    }

    public double getPerspectiveLength(){
        if(perspectiveMarkerPresent){
            return this.perspectiveLENGTH;
        }
        else{
            return -1;
        }
    }

    public int[] getImgResolution(){
        imgResolution[0] = orgBitmap.getWidth();
        imgResolution[1] = orgBitmap.getHeight();
        return this.imgResolution;
    }

    public int[] getScrResolution(){
        scrResolution[0] = Resources.getSystem().getDisplayMetrics().widthPixels;
        scrResolution[1] = Resources.getSystem().getDisplayMetrics().heightPixels;
        Log.i(TAG, "scrRes width = "+scrResolution[0]);
        Log.i(TAG, "scrRes height = "+scrResolution[1]);
        return this.scrResolution;
    }

    public Bitmap getImage() {
        return this.bitmapImage;
    }

    private Bitmap rotateImageIfRequired(Bitmap bmp){
        int height = bmp.getHeight();
        int width = bmp.getWidth();

        if(height > width){
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Log.i(TAG, "Potrait, Height="+height+", Width="+width);
            return Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);

        }
        else{
            return bmp;
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
}
