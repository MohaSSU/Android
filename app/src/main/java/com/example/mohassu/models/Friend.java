package com.example.mohassu.models;

import java.io.Serializable;

public class Friend implements Serializable {
    private String name;
    private String email;
    private String photoUrl;
    private String uid;
    private String nickname;
    private String statusMessage;


    public Friend(String uid, String name, String nickname, String email, String statusMessage, String photoUrl) {
        this.uid = uid;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.photoUrl = photoUrl;
        this.statusMessage = statusMessage;
    }

    // Getter & Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}