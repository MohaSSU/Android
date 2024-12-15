package com.example.mohassu.Model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class Promise {

    private String id; // 약속 ID
    private DocumentReference host; // 약속 생성자의 참조 (Firestore의 user 문서 참조)
    private GeoPoint location; // 약속 장소 (위도, 경도)
    private Timestamp time; // 약속 시간
    private String description; // 약속 설명
    private String promiseType; // 약속 유형 (밥약속, 술약속, 공부약속 등)
    private List<DocumentReference> participants; // 참여자 목록 (Firestore의 user 문서 참조 리스트)

    // 추가 필드: 호스트의 닉네임과 프로필 이미지 URL
    private String hostNickname;
    private String hostProfileImageUrl;

    // 전체 필드를 포함한 생성자
    public Promise(String id, DocumentReference host, GeoPoint location, Timestamp time, String description, String promiseType, List<DocumentReference> participants) {
        this.id = id;
        this.host = host;
        this.location = location;
        this.time = time;
        this.description = description;
        this.promiseType = promiseType;
        this.participants = participants;
    }

    // 기본 생성자 (Firestore 객체 매핑 시 필요)
    public Promise() {}

    // Getter and Setter methods
    public String getId() {
        return id;
    }

    public void setId(String id) { this.id = id; }

    public DocumentReference getHost() {
        return host;
    }

    public void setHost(DocumentReference host) {
        this.host = host;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) { // **수정: setPlace -> setLocation**
        this.location = location;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPromiseType() {
        return promiseType;
    }

    public void setPromiseType(String promiseType) {
        this.promiseType = promiseType;
    }

    public List<DocumentReference> getParticipants() {
        return participants;
    }

    public void setParticipants(List<DocumentReference> participants) {
        this.participants = participants;
    }

    public String getHostNickname() {
        return hostNickname;
    }

    public void setHostNickname(String hostNickname) {
        this.hostNickname = hostNickname;
    }

    public String getHostProfileImageUrl() {
        return hostProfileImageUrl;
    }

    public void setHostProfileImageUrl(String hostProfileImageUrl) {
        this.hostProfileImageUrl = hostProfileImageUrl;
    }

    @Override
    public String toString() {
        return "Promise{" +
                "id='" + id + '\'' +
                ", host=" + host +
                ", location=" + location + // **수정: place -> location**
                ", time=" + time +
                ", description='" + description + '\'' +
                ", promiseType='" + promiseType + '\'' +
                ", participants=" + participants +
                ", hostNickname='" + hostNickname + '\'' +
                ", hostProfileImageUrl='" + hostProfileImageUrl + '\'' +
                '}';
    }
}
