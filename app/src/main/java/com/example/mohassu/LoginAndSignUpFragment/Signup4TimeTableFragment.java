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

public class Signup4TimeTableFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up4, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // NavController 초기화
        NavController navController = Navigation.findNavController(view);

        // 뒤로가기 버튼에 클릭 리스너 추가
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            navController.navigateUp();
        });

        // 다음 프레그먼트를 클릭 시 다음 Fragment로 이동
        Button signupNextButton = view.findViewById(R.id.btnNext);
        signupNextButton.setFocusable(false);
        signupNextButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionMoveToLogin);
        });

        Button signupSkipButton = view.findViewById(R.id.btnSkip);
        signupSkipButton.setFocusable(false);
        signupSkipButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionMoveToLogin);
        });
    }
}
