package com.example.mohassu.models;

import com.naver.maps.geometry.LatLng;

public class Promise {
    private String title;
    private String location;
    private String time;
    private LatLng latLng;

    public Promise(String title, String location, String time, LatLng latLng) {
        this.title = title;
        this.location = location;
        this.time = time;
        this.latLng = latLng;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public String getTime() {
        return time;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
