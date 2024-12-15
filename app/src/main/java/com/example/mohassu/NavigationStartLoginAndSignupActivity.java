package com.example.mohassu;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.mohassu.Adapter.NotificationAdapter;
import com.example.mohassu.Notification.NotificationItem;
import com.example.mohassu.databinding.ActivityNavigationStartLoginAndSignupBinding;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class NavigationStartLoginAndSignupActivity extends AppCompatActivity {

    private NavController navController;
    private ActivityNavigationStartLoginAndSignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNavigationStartLoginAndSignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar를 ActionBar로 설정
        setSupportActionBar(binding.toolbar);

        // NavController 가져오기
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // NavigationUI를 사용하여 ActionBar와 NavController 연결
        if (navController != null) {
            NavigationUI.setupActionBarWithNavController(this, navController);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
    }

    public static class MyFirebaseMessagingService extends FirebaseMessagingService {

        private static NotificationAdapter notificationAdapter;

        // RecyclerView Adapter를 설정하는 메서드
        public static void setNotificationAdapter(NotificationAdapter adapter) {
            notificationAdapter = adapter;
        }

        @Override
        public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("message");
            String userName = remoteMessage.getData().get("userName");
            String profileImageUrl = remoteMessage.getData().get("profileImageUrl");
            String actionType = remoteMessage.getData().get("actionType");

            // 현재 시간 계산
            int timeAgo = 1;

            // NotificationItem 생성
            NotificationItem notificationItem = new NotificationItem(
                    profileImageUrl,
                    userName,
                    message,
                    timeAgo,
                    actionType != null ? "수락" : null,
                    actionType
            );

            // RecyclerView에 알림 추가
            if (notificationAdapter != null) {
                notificationAdapter.addNotification(notificationItem);
            }
        }
    }
}
