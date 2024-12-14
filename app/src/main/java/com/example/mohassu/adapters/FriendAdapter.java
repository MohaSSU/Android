package com.example.mohassu.adapters;

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
import com.example.mohassu.models.Friend;

import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {
    private List<Friend> friendList; // 전체 친구 목록
    private List<Friend> filteredList; // 필터링된 친구 목록
    private Context context;
    private OnFriendClickListener onFriendClickListener;

    public FriendAdapter(Context context, List<Friend> friendList, OnFriendClickListener listener) {
        this.context = context;
        this.friendList = new ArrayList<>(friendList);
        this.filteredList = new ArrayList<>(friendList);
        this.onFriendClickListener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = filteredList.get(position);
        holder.nicknameTextView.setText(friend.getNickname());

        if (friend.getStatusMessage() != null && !friend.getStatusMessage().isEmpty())
            holder.statusTextView.setText(friend.getStatusMessage());
        else
            holder.statusTextView.setText("상태 메시지가 없어요!");

        if(friend.getCurrentClass() != null){
            holder.placeTextView.setText(friend.getCurrentClass().getClassPlace() + "에서 " +friend.getCurrentClass().getClassTitle() + "수업 중!!!");
            holder.timeTextView.setText(friend.getCurrentClass().getStartTime().getHour() + "시 " + friend.getCurrentClass().getStartTime().getMinute() +"분 부터 " + friend.getCurrentClass().getEndTime().getHour() + "시 "+friend.getCurrentClass().getEndTime().getMinute() + "분 까지");
        }
        else{
            holder.placeTextView.setText("지금은 수업 중이 아닌디요??");
            holder.timeTextView.setText("친구 한테 연락해봐요!");
        }
        Glide.with(context)
                .load(friend.getPhotoUrl())
                .placeholder(R.drawable.img_logo)
                .error(R.drawable.img_logo)
                .into(holder.photoImageView);

        holder.itemView.setOnClickListener(v -> {
            if (onFriendClickListener != null) {
                onFriendClickListener.onFriendClick(friend);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    // 검색 기능 추가
    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(friendList);
        } else {
            for (Friend friend : friendList) {
                if (friend.getNickname().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(friend);
                }
            }
        }
        notifyDataSetChanged();
    }

    // 🔥 setData() 메서드 추가 (오류 수정)
    public void setData(List<Friend> newFriendList) {
        this.friendList.clear();
        this.friendList.addAll(newFriendList);

        this.filteredList.clear();
        this.filteredList.addAll(newFriendList);

        notifyDataSetChanged();
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

    public interface OnFriendClickListener {
        void onFriendClick(Friend friend);
    }
}