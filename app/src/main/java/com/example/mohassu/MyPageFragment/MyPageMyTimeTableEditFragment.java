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

import com.example.mohassu.DialogFragment.ClassAddDialogFragment;
import com.example.mohassu.R;
import com.github.tlaabs.timetableview.Schedule;
import com.github.tlaabs.timetableview.Time;
import com.github.tlaabs.timetableview.TimetableView;

import java.util.ArrayList;

public class MyPageMyTimeTableEditFragment extends Fragment {

    private static final String PREFS_NAME = "TimetablePrefs"; //파이어베이스상 이메일을 넣어주면 될 듯
    private static final String TIMETABLE_KEY = "timetable";

    private TimetableView timetable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mypage_mytimetable_edit, container, false);
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

        // 수업 추가하기 dialog
        view.findViewById(R.id.btnAddClass).setOnClickListener(v -> {
            ClassAddDialogFragment classAddDialogFragment = new ClassAddDialogFragment();
            classAddDialogFragment.setOnClassAddedListener((className, classPlace, day, startHour, startMinute, endHour, endMinute) -> {
                // Validate inputs
                if (className.isEmpty() || classPlace.isEmpty() || startHour > endHour || (startHour == endHour && startMinute >= endMinute)) {
                    if (className.isEmpty() || classPlace.isEmpty()) {
                        Toast.makeText(requireContext(), "전부 입력해주세요!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "시작 시간이 종료 시간보다 빠르거나 같아야 합니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    addScheduleToTimetable(className, classPlace, day, startHour, startMinute, endHour, endMinute);
                    saveTimetable();
                }
            });
            classAddDialogFragment.show(requireActivity().getSupportFragmentManager(), "ClassAddDialog");
        });

        // 다음 프레그먼트를 클릭 시 다음 Fragment로 이동
        Button timeTableSaveButton = view.findViewById(R.id.btnSave);
        timeTableSaveButton.setFocusable(false);
        timeTableSaveButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionSaveMyClass);
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

    private void saveTimetable() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String json = timetable.createSaveData();
        editor.putString(TIMETABLE_KEY, json);
        editor.apply();

        Toast.makeText(requireContext(), "시간표가 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void addScheduleToTimetable(String className, String classPlace, int day, int startHour, int startMinute, int endHour, int endMinute) {
        ArrayList<Schedule> schedules = new ArrayList<>();

        Schedule schedule = new Schedule();
        schedule.setClassTitle(className);
        schedule.setClassPlace(classPlace);
        schedule.setDay(day);
        schedule.setStartTime(new Time(startHour, startMinute));
        schedule.setEndTime(new Time(endHour, endMinute));

        schedules.add(schedule);

        timetable.add(schedules);
    }
}
