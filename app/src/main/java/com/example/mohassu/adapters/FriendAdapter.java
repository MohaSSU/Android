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

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {
    private List<Friend> friendList;
    private Context context;

    public FriendAdapter(Context context, List<Friend> friendList) {
        this.context = context;
        this.friendList = friendList;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = friendList.get(position);
        holder.nameTextView.setText(friend.getName());
        holder.emailTextView.setText(friend.getEmail());

        // 이미지 로드 (Glide 사용)
        Glide.with(context)
                .load(friend.getPhotoUrl())
                .placeholder(R.drawable.img_logo) // 로딩 중 대체 이미지
                .error(R.drawable.img_logo) // 로딩 실패 시 대체 이미지
                .into(holder.photoImageView);
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, emailTextView;
        ImageView photoImageView;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.state_text);
            emailTextView = itemView.findViewById(R.id.state_place);
            photoImageView = itemView.findViewById(R.id.profile_image2);
        }
    }
}
