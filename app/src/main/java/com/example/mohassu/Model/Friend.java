package com.example.mohassu.Model;

import java.io.Serializable;

public class Friend implements Serializable {
    private String name;
    private String email;
    private String photoUrl;
    private String uid;
    private String nickname;
    private String statusMessage;
    private String timeTableJSON;
    private ScheduleClass currentScheduleClass; // 현재 수업 정보 추가
    private boolean isChecked = false; // 선택 여부 추가

    public Friend(String uid, String name, String nickname, String email, String statusMessage, String timeTableJSON,String photoUrl, ScheduleClass currentScheduleClass) {
        this.uid = uid;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.statusMessage = statusMessage;
        this.timeTableJSON = timeTableJSON;
        this.photoUrl = photoUrl;
        this.currentScheduleClass = currentScheduleClass;
        this.isChecked = false; // 기본값으로 false 설정
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

    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

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

    public String getTimeTableJSON() {
        return timeTableJSON;
    }

    public void setTimeTableJSON(String timeTableJSON) {
        this.timeTableJSON = timeTableJSON;
    }

    public ScheduleClass getCurrentClass() {
        return currentScheduleClass;
    }

    public void setCurrentClass(ScheduleClass currentScheduleClass) {
        this.currentScheduleClass = currentScheduleClass;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

}