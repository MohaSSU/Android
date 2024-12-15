package com.example.mohassu.Model;

import com.naver.maps.geometry.LatLng;

public class PlaceInfo {
    private String name;
    private LatLng location;
    private float radius;

    public PlaceInfo(String name, LatLng location, float radius) {
        this.name = name;
        this.location = location;
        this.radius = radius;
    }

    public String getName() {
        return name;
    }

    public LatLng getLocation() {
        return location;
    }

    public float getRadius() {
        return radius;
    }
}