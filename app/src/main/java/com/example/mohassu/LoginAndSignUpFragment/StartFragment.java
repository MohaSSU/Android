package com.example.mohassu.LoginAndSignUpFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.mohassu.R;

public class StartFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // NavController 초기화
        NavController navController = Navigation.findNavController(view);

        //Button를 클릭 시 검색 Fragment로 이동
        Button loginButton = view.findViewById(R.id.btn_start_login);
        Button singnButton = view.findViewById(R.id.btn_start_signup);

        loginButton.setFocusable(false);
        loginButton.setOnClickListener(v -> {
            navController.navigate(R.id.btn_start_login);
        });

        singnButton.setFocusable(false);
        singnButton.setOnClickListener(v -> {
            navController.navigate(R.id.btn_start_signup);
        });
    }


}