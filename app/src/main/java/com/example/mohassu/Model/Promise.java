package com.example.mohassu.Model;

import com.naver.maps.geometry.LatLng;
import java.util.List;

public class Promise {
    private LatLng latLng; // 약속 장소의 좌표
    private String creatorId; // 약속을 만든 사람의 ID (이메일)
    private String creatorPhotoUrl; // 약속을 만든 사람의 프로필 사진 URL
    private List<String> participantIds; // 약속에 참여하는 사람들의 ID (이메일)
    private String location; // 약속 장소의 이름
    private String dateTime; // 약속 날짜와 시간 (예: "2023-12-05 15:00")

    public Promise(LatLng latLng, String creatorId, String creatorPhotoUrl, List<String> participantIds, String location, String dateTime) {
        this.latLng = latLng;
        this.creatorId = creatorId;
        this.creatorPhotoUrl = creatorPhotoUrl;
        this.participantIds = participantIds;
        this.location = location;
        this.dateTime = dateTime;
    }

    // 약속 장소의 좌표 가져오기
    public LatLng getLatLng() {
        return latLng;
    }

    // 약속 생성자의 ID(이메일) 가져오기
    public String getCreatorId() {
        return creatorId;
    }

    // 약속 생성자의 프로필 사진 URL 가져오기
    public String getCreatorPhotoUrl() {
        return creatorPhotoUrl;
    }

    // 약속 참여자들의 ID(이메일) 가져오기
    public List<String> getParticipantIds() {
        return participantIds;
    }

    // 약속 장소 가져오기
    public String getLocation() {
        return location;
    }

    // 약속 날짜와 시간 가져오기
    public String getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return "Promise{" +
                "latLng=" + latLng +
                ", creatorId='" + creatorId + '\'' +
                ", creatorPhotoUrl='" + creatorPhotoUrl + '\'' +
                ", participantIds=" + participantIds +
                ", location='" + location + '\'' +
                ", dateTime='" + dateTime + '\'' +
                '}';
    }
}