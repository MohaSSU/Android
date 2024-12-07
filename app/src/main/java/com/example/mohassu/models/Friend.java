package com.example.mohassu.models;

import java.io.Serializable;

public class Friend implements Serializable {
    private String name;
    private String email;
    private String photoUrl;
    private String uid;
    private String nickname;

    // 생성자
    public Friend(String name, String email, String photoUrl) {
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    public Friend(String uid, String name, String nickname, String email, String photoUrl) {
        this.uid = uid;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.photoUrl = photoUrl;
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
}