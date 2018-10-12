package com.example.tejas.measureit;

import java.io.Serializable;

public class Image implements Serializable {
    private String imageThumbnailUri;
    private String imageTitle;
    private int id;

    public Image(int id, String imageTitle, String imageThumbnailUri) {
        this.imageThumbnailUri = imageThumbnailUri;
        this.imageTitle = imageTitle;
        this.id = id;
    }

    public String getImageThumbnailUri(){
        return this.imageThumbnailUri;
    }

    public String getImageTitle(){
        return this.imageTitle;
    }

    public int getId(){
        return this.id;
    }

}
