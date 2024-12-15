package com.example.mohassu.Notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mohassu.Adapter.NotificationAdapter;
import com.example.mohassu.NavigationStartLoginAndSignupActivity;
import com.example.mohassu.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 레이아웃 인플레이트
        return inflater.inflate(R.layout.fragment_main_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // RecyclerView 초기화
        recyclerView = view.findViewById(R.id.notification_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 초기 데이터 리스트
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(requireContext(), notificationList);
        recyclerView.setAdapter(adapter);

        // Adapter를 FirebaseMessagingService에 설정
        NavigationStartLoginAndSignupActivity.MyFirebaseMessagingService.setNotificationAdapter(adapter);
    }
}