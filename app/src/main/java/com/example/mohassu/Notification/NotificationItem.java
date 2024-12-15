package com.example.mohassu.Notification;

public class NotificationItem {
    private String profileImageUrl; // 프로필 이미지 URL
    private String userName;        // 사용자 이름
    private String message;         // 알림 메시지
    private int timeAgo;         // 몇 분 전
    private String actionType;      // 액션 타입 (예: 약속, 친구 요청)

    // 생성자
    public NotificationItem(String profileImageUrl, String userName, String message, int timeAgo, String actionButtonText, String actionType) {
        this.profileImageUrl = profileImageUrl;
        this.userName = userName;
        this.message = message;
        this.timeAgo = timeAgo;
        this.actionType = actionType;
    }

    // Getter와 Setter
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getMessage() {
        return message;
    }

    public int getTimeAgo() {
        return timeAgo;
    }

    public String getActionType() {
        return actionType;
    }
}
