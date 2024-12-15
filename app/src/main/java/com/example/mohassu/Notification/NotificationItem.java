package com.example.mohassu.Notification;

public class NotificationItem {
    private String profileImageUrl; // 프로필 이미지 URL
    private String userName;        // 사용자 이름
    private int status;         // 알림 메시지
    private long timeAgo;         // 몇 분 전
    private String actionType;      // 액션 타입 (예: 약속, 친구 요청)

    public NotificationItem(String userName, String profileImageUrl, String actionType, long timeAgo, int status) {
        this.userName = userName;
        this.profileImageUrl = profileImageUrl;
        this.actionType = actionType;
        this.timeAgo = timeAgo;
        this.status = status;
    }

    // Getter와 Setter
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getUserName() {
        return userName;
    }



    public long getTimeAgo() {
        return timeAgo;
    }

    public String getActionType() {
        return actionType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
