package com.example.mohassu.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mohassu.Notification.NotificationItem;
import com.example.mohassu.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationItem> notificationList;
    private Context context;

    public NotificationAdapter(

            Context context, List<NotificationItem> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem notification = notificationList.get(position);
        holder.userName.setText(notification.getUserName());
        holder.message.setText(notification.getMessage());
        holder.timeAgo.setText(notification.getTimeAgo());

//        Glide.with(context)
//                .load(notification.getProfileImageUrl())
//                .circleCrop()
//                .placeholder(R.drawable.img_default_profile)
//                .error(R.drawable.img_default_profile)
//                .into(holder.profileImage);

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
        TextView userName, message, timeAgo;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
//            userName = itemView.findViewById(R.id.user_name);
//            message = itemView.findViewById(R.id.message);
//            timeAgo = itemView.findViewById(R.id.time_ago);
        }
    }
}