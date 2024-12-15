package com.example.mohassu.MainFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mohassu.Adapter.NotificationAdapter;
import com.example.mohassu.Notification.NotificationItem;
import com.example.mohassu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainNotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 레이아웃 인플레이트
        return inflater.inflate(R.layout.fragment_main_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Firebase Firestore 인스턴스 초기화
        db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // RecyclerView 초기화
        recyclerView = view.findViewById(R.id.notification_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 초기 데이터 리스트
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(requireContext(), notificationList);
        recyclerView.setAdapter(adapter);

        // Firebase에서 데이터 불러오기
        fetchNotificationDetails(currentUserId);
    }

    private void fetchNotificationDetails(String userUid) {

        db.collection("users").document(userUid)
                .collection("notification") // notification 서브 컬렉션
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            // Firestore에서 필드 가져오기
                            String nickname = documentSnapshot.getString("nickname");
                            String profileImageUrl = documentSnapshot.getString("photoUrl");
                            String actionType = documentSnapshot.getString("actionType");
                            int createdTime = documentSnapshot.getLong("createdTime").intValue();

                            // NotificationItem 객체 생성
                            NotificationItem notificationItem = new NotificationItem(nickname, profileImageUrl,actionType, createdTime, 1);
                            // 알림 리스트에 추가
                            notificationList.add(notificationItem);
                        }

                        // 어댑터에 데이터 갱신
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Log.e("fetchNotificationDetails", "알림 불러오기 실패: " + e.getMessage()));
    }
}