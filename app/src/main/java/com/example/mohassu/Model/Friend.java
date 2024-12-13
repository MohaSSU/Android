package com.example.mohassu.Model;

import java.io.Serializable;

public class Friend implements Serializable {
    private String name;
    private String email;
    private String photoUrl;
    private String uid;
    private String nickname;
    private String statusMessage;
    private ScheduleClass currentScheduleClass; // 현재 수업 정보 추가

    public Friend(String uid, String name, String nickname, String email, String statusMessage, String photoUrl, ScheduleClass currentScheduleClass) {
        this.uid = uid;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.statusMessage = statusMessage;
        this.photoUrl = photoUrl;
        this.currentScheduleClass = currentScheduleClass;
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

    public ScheduleClass getCurrentClass() {
        return currentScheduleClass;
    }

    public void setCurrentClass(ScheduleClass currentScheduleClass) {
        this.currentScheduleClass = currentScheduleClass;
    }
}