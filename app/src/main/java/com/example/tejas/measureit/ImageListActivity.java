package com.example.tejas.measureit;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImageListActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private static final int SELECT_PICTURE = 100;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = "ImageListActivity";

    String mCurrentPhotoPath;
    Uri photoURI;

    DatabaseHelper myDB;

    private int IMAGE_ID;
    private String IMAGE_TITLE;
    private String IMAGE_THUMBNAIL;

    private int dataCount;

    List<Image> imageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list);

        Project project = (Project) getIntent().getSerializableExtra("Project");
        Log.i(TAG, "Project = " + project.getTitle());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(project.getTitle());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageList = new ArrayList<>();
        myDB = DatabaseHelper.getInstance(this);

        RecyclerView recyclerView = findViewById(R.id.imageRecycler);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        Cursor res = myDB.selectImage();
        Log.i(TAG, "RES = "+res.getCount());
        if (!(res.moveToFirst()) || res.getCount() > 0) {
            Log.i(TAG, "res done");
            //res.moveToFirst();
            dataCount = res.getCount();
            Log.i(TAG, "dataCount = "+dataCount);
            if (dataCount > 0) {
                while (res.moveToNext()) {
                    Log.i(TAG, "While");
                    IMAGE_ID = res.getInt(0);
                    IMAGE_TITLE = res.getString(1);
                    IMAGE_THUMBNAIL = res.getString(2);
                    Log.i(TAG, "IMAGE_TITLE = " + IMAGE_TITLE);


                    addToImageList(IMAGE_ID, IMAGE_TITLE, IMAGE_THUMBNAIL);
                }
            }
            res.close();
        }
        myDB.close();

        RecyclerView.Adapter mAdapter = new ImageAdapter(this, imageList);
        recyclerView.setAdapter(mAdapter);

    }

    private void addToImageList(int id, String imageTitle, String imageURI){
        Uri mUri = Uri.parse(imageURI);
        String imageThumbnailUri;
        imageThumbnailUri = mUri.toString();

        Image temp = new Image(
                id,
                imageTitle,
                imageThumbnailUri
        );

        imageList.add(temp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.getImageFromGallery){
            openImageChooser();
        }
        if(id == R.id.getImageFromCamera){
            dispatchTakePictureIntent();
        }

        return super.onOptionsItemSelected(item);
    }

    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Intent intent = new Intent(ImageListActivity.this, MeasureImageActivity.class);
            intent.putExtra("Activity", "ImageListActivity");
            intent.putExtra("imageUri", photoURI.toString());
            startActivity(intent);
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {

                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {

                    String filePath = getPathFromURI(selectedImageUri);
                    //Log.i(TAG, "Image Path : " + filePath);

                    Intent intent = new Intent(ImageListActivity.this, MeasureImageActivity.class);
                    intent.putExtra("Activity", "ImageListActivity");
                    intent.putExtra("imageUri", selectedImageUri.toString());
                    startActivity(intent);

                }
            }
        }
    }

    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] Proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, Proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
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
