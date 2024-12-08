package com.example.mohassu.models;

import java.io.Serializable;

public class ScheduleClass implements Serializable {
    private String classTitle;
    private String classPlace;
    private String professorName;
    private int day; // 0 = 일요일, 1 = 월요일, ...
    private Time startTime;
    private Time endTime;

    public ScheduleClass(String classTitle, String classPlace, String professorName, int day, Time startTime, Time endTime) {
        this.classTitle = classTitle;
        this.classPlace = classPlace;
        this.professorName = professorName;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getter and Setter methods
    public String getClassTitle() {
        return classTitle;
    }

    public void setClassTitle(String classTitle) {
        this.classTitle = classTitle;
    }

    public String getClassPlace() {
        return classPlace;
    }

    public void setClassPlace(String classPlace) {
        this.classPlace = classPlace;
    }

    public String getProfessorName() {
        return professorName;
    }

    public void setProfessorName(String professorName) {
        this.professorName = professorName;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }
}