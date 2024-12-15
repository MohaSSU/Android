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

import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder>;

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


import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {
    private List<Friend> friendList;
    private List<Friend> filteredList;
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


