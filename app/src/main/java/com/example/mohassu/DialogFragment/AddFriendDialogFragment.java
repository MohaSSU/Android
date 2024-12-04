package com.example.mohassu.DialogFragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mohassu.R;

public class AddFriendDialogFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_friend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 버튼 클릭 리스너 설정
        view.findViewById(R.id.btnAddFriend).setOnClickListener(v -> {
            // 친구 추가 로직 작성
            dismiss(); // 다이얼로그 닫기
        });

        view.findViewById(R.id.btnCancelFriend).setOnClickListener(v -> {
            dismiss();
        });
    }
}
