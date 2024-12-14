package com.example.mohassu;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.mohassu.Model.Friend;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CheckProfileBottomSheetFragment extends BottomSheetDialogFragment {

    // Friend 객체를 전달받는 newInstance 메서드
    public static CheckProfileBottomSheetFragment newInstance(Friend friend) {
        CheckProfileBottomSheetFragment fragment = new CheckProfileBottomSheetFragment();
        Bundle args = new Bundle();
        args.putSerializable("friend", friend);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_sheet_check_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 🔥 Friend 객체 가져오기
        Friend friend = (Friend) getArguments().getSerializable("friend");

        if (friend == null) {
            return;
        }

        // 📌 UI 요소 초기화
        TextView nicknameTextView = view.findViewById(R.id.text_nickname);
        TextView nameTextView = view.findViewById(R.id.text_name);
        ImageView photoImageView = view.findViewById(R.id.img_profile);
        Button viewTimeTableButton = view.findViewById(R.id.view_time_table_button); // 🔥 추가된 버튼

        // 🔥 데이터 바인딩
        nicknameTextView.setText(friend.getNickname() != null ? friend.getNickname() : "닉네임 없음");
        nameTextView.setText(friend.getName() != null ? friend.getName() : "이름 없음");

        // 🔥 Glide를 활용하여 이미지 로드
        Glide.with(requireContext())
                .load(friend.getPhotoUrl())
                .placeholder(R.drawable.img_logo) // 로딩 중 표시할 이미지
                .error(R.drawable.img_logo) // 로딩 실패 시 표시할 이미지
                .into(photoImageView);

        // 🔥 "시간표 보기" 버튼 클릭 리스너 추가
        viewTimeTableButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("friend", friend);

            try {
                dismiss();
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                Log.d("CheckProfileBottomSheetFragment", "NavController 찾기 성공");
                navController.navigate(R.id.action_checkProfileToCheckTimeTable, bundle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}