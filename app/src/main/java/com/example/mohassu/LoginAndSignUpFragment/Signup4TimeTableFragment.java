package com.example.mohassu.LoginAndSignUpFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.mohassu.DialogFragment.ClassAddDialogFragment;
import com.example.mohassu.DialogFragment.ClassEditOrDeleteDialogFragment;
import com.example.mohassu.R;
import com.github.tlaabs.timetableview.Schedule;
import com.github.tlaabs.timetableview.Time;
import com.github.tlaabs.timetableview.TimetableView;

import java.util.ArrayList;

public class Signup4TimeTableFragment extends Fragment {

    private static final String PREFS_NAME = "TimetablePrefs"; //회원가입1에서 받은 이메일을 넣어주면 될 듯 -> 지금은 그냥 휴대폰의 아무유저나 다 동일하게 되어있음..
    private static final String TIMETABLE_KEY = "timetable";

    private TimetableView timetable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up4, container, false);
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
                }
            });
            classAddDialogFragment.show(requireActivity().getSupportFragmentManager(), "ClassAddDialog");
        });

        timetable.setOnStickerSelectEventListener(new TimetableView.OnStickerSelectedListener() {
            @Override
            public void OnStickerSelected(int idx, ArrayList<Schedule> schedules) {
                // 다이얼로그 프래그먼트 호출
                ClassEditOrDeleteDialogFragment classEditOrDeleteDialogFragment = ClassEditOrDeleteDialogFragment.newInstance(schedules.get(0));
                Log.d("StickerSelectEvent","schedule idx : "+idx);
                classEditOrDeleteDialogFragment.setOnClassEditOrDeleteListener(new ClassEditOrDeleteDialogFragment.OnClassEditOrDeleteListener() {
                    @Override
                    public void onEdit(Schedule editedSchedule) {
                        // 기존 스티커 수정
                        ArrayList<Schedule> updatedSchedules = new ArrayList<>();
                        updatedSchedules.add(editedSchedule);
                        timetable.edit(idx, updatedSchedules);
                        Toast.makeText(requireContext(), "수업 정보가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDelete() {
                        // 스티커 삭제
                        timetable.remove(idx);
                        Toast.makeText(requireContext(), "수업 정보가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                classEditOrDeleteDialogFragment.show(requireActivity().getSupportFragmentManager(), "ClassEditDialog");
            }
        });

        // 다음 프레그먼트를 클릭 시 다음 Fragment로 이동
        Button signupNextButton = view.findViewById(R.id.btnNext);
        signupNextButton.setFocusable(false);
        signupNextButton.setOnClickListener(v -> {
            saveTimetable();
            navController.navigate(R.id.actionNextToSignupDone);
        });

        Button signupSkipButton = view.findViewById(R.id.btnSkip);
        signupSkipButton.setFocusable(false);
        signupSkipButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionSkipToSignupDone);
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
