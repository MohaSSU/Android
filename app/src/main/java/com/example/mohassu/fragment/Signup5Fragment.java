package com.example.mohassu.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.mohassu.R;

public class Signup5Fragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fregment_sign_up5, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // NavController 초기화
        NavController navController = Navigation.findNavController(view);

        // EditText를 클릭 시 검색 Fragment로 이동
        Button signupNextButton = view.findViewById(R.id.btn_signup5_next);
        signupNextButton.setFocusable(false);
        signupNextButton.setOnClickListener(v -> {
            navController.navigate(R.id.btn_signup5_next);
        });
    }
}
