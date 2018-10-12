package com.example.tejas.measureit;

import java.io.Serializable;

public class Project implements Serializable {
    private int id;
    private String title;
    private String desc;
    private String measurements;
    private int image;

    public Project(int id, String title, String desc, String measurements, int image){
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.measurements = measurements;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getMeasurements() {
        return measurements;
    }

    public int getImage() {
        return image;
    }
}
