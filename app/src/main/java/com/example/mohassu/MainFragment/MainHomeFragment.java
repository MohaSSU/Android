package com.example.mohassu.MainFragment;

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

public class MainHomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // NavController 초기화
        NavController navController = Navigation.findNavController(view);

        // 다음 프레그먼트를 클릭 시 다음 Fragment로 이동
        // 알림 페이지 이동
        ImageButton notificationButton = view.findViewById(R.id.btnNotification);
        notificationButton.setFocusable(false);
        notificationButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionNotification);
        });
        // 약속 리스트 페이지 이동
        ImageButton promiseListButton = view.findViewById(R.id.btnPromiseList);
        promiseListButton.setFocusable(false);
        promiseListButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionPromiseList);
        });
        // 친구 리스트 페이지 이동
        ImageButton signupNextButton = view.findViewById(R.id.btnFriendList);
        signupNextButton.setFocusable(false);
        signupNextButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionFriendList);
        });
        //약속 추가 페이지 이동
        ImageButton createPromiseButton = view.findViewById(R.id.btnAddPlan);
        createPromiseButton.setFocusable(false);
        createPromiseButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionAddPlan);
        });
        // 마이페이지 이동
        ImageButton myPageButton = view.findViewById(R.id.btnMyPage);
        myPageButton.setFocusable(false);
        myPageButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionMyPage);
        });
    }
}
