package com.example.mohassu.CheckProfileAndTimeTableFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mohassu.R;
import com.example.mohassu.models.Friend;
import com.github.tlaabs.timetableview.TimetableView;
import com.google.firebase.firestore.FirebaseFirestore;

public class CheckTimeTableFragment extends Fragment {
    private TimetableView timetableView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_check_time_table, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 🔥 Back 버튼 클릭 리스너
        ImageButton backButton = view.findViewById(R.id.btnBack);
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // 🔥 TimetableView 초기화
        timetableView = view.findViewById(R.id.timetable);

        // 🔥 타이틀 설정
        TextView tvTitle = view.findViewById(R.id.tvTitle);

        // 🔥 Bundle에서 친구 정보 가져오기
        Bundle args = getArguments();
        if (args != null) {
            Friend friend = (Friend) args.getSerializable("friend");
            if (friend != null) {
                // 🔥 친구 닉네임으로 타이틀 설정
                tvTitle.setText(getString(R.string.check_time_table_title, friend.getNickname()));

                // 🔥 친구의 시간표 데이터 로드
                loadFriendTimeTable(friend.getUid());
            } else {
                Log.e("CheckTimeTableFragment", "Friend 객체가 null입니다.");
            }
        } else {
            Log.e("CheckTimeTableFragment", "전달된 Bundle이 null입니다.");
        }
    }

    private void loadFriendTimeTable(String friendUid) {
        if (friendUid == null || friendUid.isEmpty()) {
            Log.e("CheckTimeTableFragment", "friendUid가 null이거나 빈 문자열입니다.");
            Toast.makeText(requireContext(), "친구의 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("CheckTimeTableFragment", "로드할 친구 UID: " + friendUid);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(friendUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // 🔥 시간표 데이터 가져오기
                        String timetableData = documentSnapshot.getString("timetableData");

                        if (timetableData != null && !timetableData.isEmpty()) {
                            Log.d("CheckTimeTableFragment", "시간표 데이터: " + timetableData);

                            // 🔥 TimetableView에 데이터 로드
                            try {
                                timetableView.load(timetableData);
                                Toast.makeText(requireContext(), "시간표를 불러왔습니다.", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.e("CheckTimeTableFragment", "시간표 로드 중 오류 발생", e);
                                Toast.makeText(requireContext(), "시간표를 로드할 수 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "시간표 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("CheckTimeTableFragment", "친구의 프로필을 찾을 수 없습니다.");
                        Toast.makeText(requireContext(), "프로필을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CheckTimeTableFragment", "시간표 불러오기 실패: " + e.getMessage(), e);
                    Toast.makeText(requireContext(), "시간표 불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // 🔥 상단 타이틀 숨기기
        if (requireActivity().getActionBar() != null) {
            requireActivity().getActionBar().hide();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 🔥 상단 타이틀 복원
        if (requireActivity().getActionBar() != null) {
            requireActivity().getActionBar().show();
        }
    }
}