package com.example.tejas.measureit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;

public class DatabaseHelper extends SQLiteOpenHelper{

    private static final String TAG = "ProjectAdapter";

    private static final String DATABASE_NAME = "Projects.db";

    private static final String PROJECT_TABLE = "Project_table";
    private static final String PROJECT_ID = "ID";
    private static final String PROJECT_TITLE = "TITLE";
    private static final String PROJECT_DESCRIPTION = "DESCRIPTION";
    private static final String PROJECT_MEASUREMENTS = "MEASUREMENTS";

    private static final String IMAGE_TABLE = "Image_table";
    private static final String IMAGE_THUMBNAIL = "THUMBNAIL";
    private static final String IMAGE_ID = "ID";
    private static final String IMAGE_TITLE = "TITLE";

    private static DatabaseHelper databaseHelper;

    public static synchronized DatabaseHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(context.getApplicationContext());
        }
        return databaseHelper;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+PROJECT_TABLE+"(" +
                PROJECT_ID           +" INTEGER PRIMARY KEY AUTOINCREMENT," +
                PROJECT_TITLE        +" TEXT," +
                PROJECT_DESCRIPTION  +" TEXT," +
                PROJECT_MEASUREMENTS +" INTEGER)"
        );

        db.execSQL("CREATE TABLE "+IMAGE_TABLE+"(" +
                IMAGE_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT ," +
                IMAGE_TITLE     + " TEXT UNIQUE NOT NULL," +
                IMAGE_THUMBNAIL + " TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+PROJECT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+IMAGE_TABLE);
        onCreate(db);
    }

    public boolean insertData(String title, String description, int measurement){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PROJECT_TITLE, title);
        contentValues.put(PROJECT_DESCRIPTION, description);
        contentValues.put(PROJECT_MEASUREMENTS, measurement);

        long result = db.insert(PROJECT_TABLE, null, contentValues);

        if(result == -1){
            return false;
        } else {
            return true;
        }
    }

    public boolean insertImage(String imageTitle, Uri imageThumbnail){
        Log.i(TAG, "imageThumbnail 1 = "+imageThumbnail.toString());
        SQLiteDatabase db = this.getWritableDatabase();
        Log.i(TAG, "imageThumbnail 2 = "+imageThumbnail.toString());
        ContentValues contentValues = new ContentValues();

        contentValues.put(IMAGE_TITLE, imageTitle);
        Log.i(TAG, imageThumbnail.toString());
        if(imageThumbnail == null){
            Log.i(TAG, "Not null");
        }
        else{
            Log.i(TAG, "Bitmap Null");
        }

        String imageURI = imageThumbnail.toString();

        contentValues.put(IMAGE_THUMBNAIL, imageURI);

        long result = db.insert(IMAGE_TABLE, null, contentValues);

        if(result == -1){
            return false;
        } else {
            return true;
        }
    }

    public Cursor selectData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM "+PROJECT_TABLE, null);
        return res;
    }

    public Cursor selectImage(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM "+IMAGE_TABLE, null);
        return res;
    }

    public boolean deleteRow(String title) {
        SQLiteDatabase db = this.getWritableDatabase();

        long result = db.delete(PROJECT_TABLE, "TITLE" + "=" + '"'+title+'"', null);

        if(result == -1){
            return false;
        } else {
            return true;
        }

    }

    public boolean deleteImage (String title) {
        SQLiteDatabase db = this.getWritableDatabase();

        long result = db.delete(IMAGE_TABLE, "TITLE" + "=" + '"'+title+'"', null);

        if(result == -1){
            return false;
        } else {
            return true;
        }

    }
}
