package com.example.mohassu.CheckAndEditPromiseFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mohassu.R;

public class PromiseCheckFragment extends Fragment {

    private String promiseId;
    private String promiseTitle;
    // 기타 필요한 변수들

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 인자에서 데이터 가져오기
        if (getArguments() != null) {
            promiseId = getArguments().getString("PROMISE_ID");
            promiseTitle = getArguments().getString("PROMISE_TITLE");
            // 기타 데이터도 가져오기
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_promise_check, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Back 버튼 클릭 리스너
        ImageButton backButton = view.findViewById(R.id.btnBack);
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // UI 초기화 및 데이터 표시
        TextView tvPromiseTitle = view.findViewById(R.id.tv_promise_check_title);
        tvPromiseTitle.setText(promiseTitle);

        // 기타 UI 설정 및 데이터 로드
    }
}
