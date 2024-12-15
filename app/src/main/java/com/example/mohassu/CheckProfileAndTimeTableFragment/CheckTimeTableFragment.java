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

import com.example.mohassu.Model.Friend;
import com.example.mohassu.R;
import com.github.tlaabs.timetableview.TimetableView;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * CheckTimeTableFragment는 Friend 객체 또는 friendId를 전달받아
 * 해당 친구의 닉네임과 시간표 데이터를 Firestore에서 조회하고 UI를 설정합니다.
 */
public class CheckTimeTableFragment extends Fragment {
    private TimetableView timetableView;
    private TextView tvTitle;
    private ImageButton backButton;

    private static final String ARG_FRIEND = "friend";
    private static final String ARG_FRIEND_ID = "friendId";
    private static final String ARG_TIMETABLE = "timetableData";
    private static final String ARG_NICKNAME = "nickname";

    private static final String TAG = "mohassu:checkTimeTable";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_check_time_table, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // UI 요소 초기화
        backButton = view.findViewById(R.id.btnBack);
        timetableView = view.findViewById(R.id.timetable);
        tvTitle = view.findViewById(R.id.tvTitle);

        // 🔥 Back 버튼 클릭 리스너
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // 🔥 Bundle에서 데이터 가져오기
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_FRIEND)) {
                // Friend 객체가 전달된 경우
                Friend friend = (Friend) args.getSerializable(ARG_FRIEND);
                if (friend != null) {
                    setupUI(friend.getNickname(), friend.getTimeTableJSON());
                } else {
                    Log.e(TAG, "Friend 객체가 null입니다.");
                    Toast.makeText(requireContext(), "친구 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else if (args.containsKey(ARG_FRIEND_ID)) {
                // friendId가 전달된 경우
                String friendId = args.getString(ARG_FRIEND_ID);
                if (friendId != null && !friendId.isEmpty()) {
                    setupUI(args.getString(ARG_NICKNAME), args.getString(ARG_TIMETABLE));
                } else {
                    Log.e(TAG, "friendId가 null이거나 빈 문자열입니다.");
                    Toast.makeText(requireContext(), "유효하지 않은 친구 ID입니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "전달된 Bundle에 Friend 객체나 friendId가 없습니다.");
                Toast.makeText(requireContext(), "필요한 데이터가 전달되지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "전달된 Bundle이 null입니다.");
            Toast.makeText(requireContext(), "필요한 데이터가 전달되지 않았습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 닉네임과 시간표 데이터를 사용하여 UI를 설정합니다.
     *
     * @param nickname      친구의 닉네임
     * @param timetableData 친구의 시간표 데이터 (JSON 형식 등)
     */
    private void setupUI(String nickname, String timetableData) {
        // 🔥 친구 닉네임으로 타이틀 설정
        String displayNickname = (nickname != null && !nickname.isEmpty()) ? nickname : "닉네임 없음";
        tvTitle.setText(getString(R.string.check_time_table_title, displayNickname));

        // 🔥 시간표 데이터 로드
        if (timetableData != null && !timetableData.isEmpty()) {
            Log.d(TAG, "시간표 데이터: " + timetableData);
            try {
                timetableView.load(timetableData);
                Toast.makeText(requireContext(), "시간표를 불러왔습니다.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "시간표 로드 중 오류 발생", e);
                Toast.makeText(requireContext(), "시간표를 로드할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "로드 실패; 시간표 데이터: " + timetableData);
            Toast.makeText(requireContext(), "친구가 시간표를 등록하지 않았습니다!", Toast.LENGTH_SHORT).show();
        }
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
