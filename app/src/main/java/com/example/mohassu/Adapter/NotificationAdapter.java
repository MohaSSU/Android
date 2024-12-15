package com.example.mohassu.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mohassu.Notification.NotificationItem;
import com.example.mohassu.R;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationItem> notificationList;
    private Context context;

    public NotificationAdapter(Context context, List<NotificationItem> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 아이템 뷰를 인플레이트하여 뷰 홀더 반환
        View view = LayoutInflater.from(context).inflate(R.layout.view_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem notification = notificationList.get(position);

        String userName = notification.getUserName();
        // 알림 데이터 바인딩
        if (notification.getActionType().equals("addFr")) {
            holder.message.setText(userName + "과 친구가 되었어요!");
        } else if (notification.getActionType().equals("newPr")) {
            holder.message.setText(userName + "와 새로운 약속이 생성되었어요");
        }

        long currentTime = System.currentTimeMillis();
        holder.timeAgo.setText(getTimeAgo(currentTime - notification.getTimeAgo()));

        // 프로필 이미지 로딩 (Glide 사용)
        Glide.with(context)
                .load(notification.getProfileImageUrl()) // URL에서 이미지 로드
                .circleCrop()  // 원형으로 잘라서 표시
                .placeholder(R.drawable.img_basic_profile) // 기본 이미지
                .error(R.drawable.img_logo) // 오류시 기본 이미지
                .into(holder.profileImage);  // ImageView에 이미지 설정
    }

    public String getTimeAgo(long timeDifference) {

        // 시간 차이를 분으로 변환
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference);
        long hours = TimeUnit.MILLISECONDS.toHours(timeDifference);
        long days = TimeUnit.MILLISECONDS.toDays(timeDifference);

        if (days > 0) {
            return days + "일 전";
        } else if (hours > 0) {
            return hours + "시간 전";
        } else if (minutes > 0) {
            return minutes + "분 전";
        } else {
            return "방금 전";
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    // 새 알림 추가 메서드
    public void addNotification(NotificationItem notificationItem) {
        notificationList.add(0, notificationItem); // 새 알림을 리스트의 맨 앞에 추가
        notifyItemInserted(0); // RecyclerView 갱신
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView  message, timeAgo;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            // 레이아웃에서 필요한 뷰 연결
            profileImage = itemView.findViewById(R.id.profile_image2);
            message = itemView.findViewById(R.id.notification_text);
            timeAgo = itemView.findViewById(R.id.timestamp);
        }
    }
}