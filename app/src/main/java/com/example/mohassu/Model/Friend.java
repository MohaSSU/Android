package com.example.mohassu.Model;

public class Friend {
    private String name;
    private String email;
    private String photoUrl;

    public Friend(String name, String email, String photoUrl) {
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}