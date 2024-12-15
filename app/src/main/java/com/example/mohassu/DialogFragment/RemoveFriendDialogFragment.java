package com.example.mohassu.DialogFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mohassu.CheckProfileAndTimeTableFragment.CheckProfileBottomSheetFragment;
import com.example.mohassu.R;

public class RemoveFriendDialogFragment extends DialogFragment {

    // 버튼 클릭 결과를 부모 Fragment에 전달하는 메서드
    private RemoveFriendDialogListener listener;

    // 콜백 인터페이스 정의
    public interface RemoveFriendDialogListener {
        void onFriendRemoved();
        void onFriendRemovalCancelled();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_remove_friend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnAddFriend = view.findViewById(R.id.btnAddFriend);
        Button btnCancelFriend = view.findViewById(R.id.btnCancelFriend);

        // "확인" 버튼 클릭 리스너
        btnAddFriend.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFriendRemoved(); // 친구 삭제가 확인되었을 때
            }
        });

        // "취소" 버튼 클릭 리스너
        btnCancelFriend.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFriendRemovalCancelled(); // 친구 삭제 취소
            }
            dismiss();  // 다이얼로그 닫기
        });
    }

    // 부모 Fragment에서 콜백 인터페이스를 설정
    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (context instanceof RemoveFriendDialogListener) {
            listener = (RemoveFriendDialogListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement RemoveFriendDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;  // 콜백 인터페이스를 null로 설정하여 메모리 누수 방지
    }
}