package com.example.mohassu.MyPageFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.mohassu.R;

public class MyPageHomeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mypage_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // NavController 초기화
        NavController navController = Navigation.findNavController(view);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            navController.navigateUp();
        });

        // 다음 프레그먼트를 클릭 시 다음 Fragment로 이동
        ImageButton profileEditButton = view.findViewById(R.id.btnProfileEdit);
        profileEditButton.setFocusable(false);
        profileEditButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionProfileEdit);
        });

        // 다음 프레그먼트를 클릭 시 다음 Fragment로 이동
        ImageButton timeTableViewButton = view.findViewById(R.id.btnSchedule);
        timeTableViewButton.setFocusable(false);
        timeTableViewButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionSchedule);
        });

        // 다음 프레그먼트를 클릭 시 다음 Fragment로 이동
        ImageButton notificationButton = view.findViewById(R.id.btnNotification);
        notificationButton.setFocusable(false);
        notificationButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionSettingNotification);
        });
    }
}
