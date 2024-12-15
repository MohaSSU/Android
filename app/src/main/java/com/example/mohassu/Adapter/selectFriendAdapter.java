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
import com.example.mohassu.R;
import com.example.mohassu.Model.Friend;

import java.util.List;

public class selectFriendAdapter extends RecyclerView.Adapter<selectFriendAdapter.FriendViewHolder> {
    private List<Friend> friendList;
    private Context context;
    private OnFriendClickListener onFriendClickListener;

    // 생성자에 OnFriendClickListener 추가
    public selectFriendAdapter(Context context, List<Friend> friendList, OnFriendClickListener listener) {
        this.context = context;
        this.friendList = friendList;
        this.onFriendClickListener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_select_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = friendList.get(position);
        holder.nicknameTextView.setText(friend.getNickname());

        if(friend.getStatusMessage() != null && !friend.getStatusMessage().isEmpty())
            holder.statusTextView.setText(friend.getStatusMessage());
        else{
            holder.statusTextView.setText("상태 메시지가 없어요!");
        }

        if(friend.getCurrentClass() != null){
            holder.placeTextView.setText(friend.getCurrentClass().getClassPlace() + "에서 " +friend.getCurrentClass().getClassTitle() + "수업 중!!!");
            holder.timeTextView.setText(friend.getCurrentClass().getStartTime().getHour() + "시 " + friend.getCurrentClass().getStartTime().getMinute() +"분 부터 " + friend.getCurrentClass().getEndTime().getHour() + "시 "+friend.getCurrentClass().getEndTime().getMinute() + "분 까지");
        }
        else{
            holder.placeTextView.setText("지금은 수업 중이 아닌디요??");
            holder.timeTextView.setText("친구 한테 연락해봐요!");
        }

        // 이미지 로드 (Glide 사용)
        Glide.with(context)
                .load(friend.getPhotoUrl())
                .placeholder(R.drawable.img_logo) // 로딩 중 대체 이미지
                .error(R.drawable.img_logo) // 로딩 실패 시 대체 이미지
                .into(holder.photoImageView);

        // 클릭 리스너 연결
        holder.itemView.setOnClickListener(v -> {
            if (onFriendClickListener != null) {
                onFriendClickListener.onFriendClick(friend); // Friend 객체 전달
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView nicknameTextView, statusTextView, placeTextView, timeTextView;
        ImageView photoImageView;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            nicknameTextView = itemView.findViewById(R.id.nickname_text);
            statusTextView = itemView.findViewById(R.id.state_text);
            placeTextView = itemView.findViewById(R.id.state_place);
            timeTextView = itemView.findViewById(R.id.state_time);
            photoImageView = itemView.findViewById(R.id.profile_image2);
        }
    }

    // 클릭 리스너 인터페이스
    public interface OnFriendClickListener {
        void onFriendClick(Friend friend); // Friend 객체 전달
    }
}