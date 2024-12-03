package com.example.mohassu.MyPageFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.mohassu.R;
import com.github.tlaabs.timetableview.TimetableView;

public class MyPageMyTimeTableFragment extends Fragment {

    private static final String PREFS_NAME = "TimetablePrefs"; //파이어베이스상 이메일을 넣어주면 될 듯
    private static final String TIMETABLE_KEY = "timetable";

    private TimetableView timetable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mypage_mytimetable, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        timetable=view.findViewById(R.id.timetable);
        loadTimetable();

        // NavController 초기화
        NavController navController = Navigation.findNavController(view);

        // 뒤로가기 버튼에 클릭 리스너 추가
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            navController.navigateUp();
        });

        // 다음 프레그먼트를 클릭 시 다음 Fragment로 이동
        Button editClassButton = view.findViewById(R.id.btnEditClass);
        editClassButton.setFocusable(false);
        editClassButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionEditClass);
        });
    }

    private void loadTimetable() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(TIMETABLE_KEY, null);

        if (json != null) {
            timetable.load(json);
            //Toast.makeText(requireContext(), "저장된 시간표를 불러왔습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
