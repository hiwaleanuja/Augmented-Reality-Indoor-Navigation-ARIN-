package com.example.tejas.measureit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private static final String TAG = "ImageAdapter";
    private ImageAdapter imageAdapter;
    private List<Image> imageList;
    private Context mCtx;
    private Bitmap bmp;
    DatabaseHelper myDB;
    private Uri mUri;


    public ImageAdapter(Context mCtx, List<Image> imageList){
        this.mCtx = mCtx;
        this.imageList = imageList;
        this.myDB = new DatabaseHelper(mCtx);
        this.imageAdapter = this;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.image_list_layout_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Image image = imageList.get(position);

        holder.imageTitle.setText(image.getImageTitle());

        try {
            mUri = Uri.parse(image.getImageThumbnailUri());
            bmp = MediaStore.Images.Media.getBitmap(mCtx.getContentResolver(), mUri);

        }
        catch (IOException ex){
            Log.i(TAG, "Could not resolve URI");
        }

        holder.imageThumbnail.setImageBitmap(bmp);

        holder.imageDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(mCtx, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(mCtx);
                }
                builder.setTitle("Remove Measurements!");
                builder.setMessage("Are you sure?");
                builder.setPositiveButton(android.R.string.yes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String title = image.getImageTitle();
                            boolean isRemoved = myDB.deleteImage(title);
                            if(isRemoved){
                                for (Iterator<Image> iter = imageList.listIterator(); iter.hasNext(); ) {
                                    Image temp = iter.next();
                                    if (temp.getImageTitle() == title) {
                                        iter.remove();
                                    }
                                }
                                imageAdapter.notifyDataSetChanged();
                                Toast.makeText(mCtx,
                                        "Image Removed",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(mCtx,
                                        "Failed",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                );
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.show();
            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Card Clicked");
                Intent intent = new Intent(v.getContext(),MeasureImageActivity.class);
                intent.putExtra("Activity", "ImageAdapter");
                intent.putExtra("imageBitmap", image);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageThumbnail;
        TextView imageTitle;
        Button imageDelete;
        CardView cardView;
        public ViewHolder(View itemView) {
            super(itemView);
            imageThumbnail = itemView.findViewById(R.id.imageThumbnail);
            imageTitle = itemView.findViewById(R.id.imageTitle);
            imageDelete = itemView.findViewById(R.id.imageDelete);
            cardView = itemView.findViewById(R.id.imageCards);
        }
    }
}
