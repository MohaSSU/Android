package com.example.mohassu.MyPageFragment;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class MyPageMyTimeTableEditFragment extends Fragment {

    private static final String PREFS_NAME = "TimetablePrefs"; // 로컬 SharedPreferences에 저장
    private static final String TIMETABLE_KEY = "timetable"; // 로컬 SharedPreferences 키

    private TimetableView timetable;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mypage_mytimetable_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        timetable = view.findViewById(R.id.timetable);
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
                if (className.isEmpty() || classPlace.isEmpty() || startHour > endHour || (startHour == endHour && startMinute >= endMinute)) {
                    Toast.makeText(requireContext(), "올바른 수업 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    addScheduleToTimetable(className, classPlace, day, startHour, startMinute, endHour, endMinute);
                }
            });
            classAddDialogFragment.show(requireActivity().getSupportFragmentManager(), "ClassAddDialog");
        });

        timetable.setOnStickerSelectEventListener((idx, schedules) -> {
            ClassEditOrDeleteDialogFragment classEditOrDeleteDialogFragment = ClassEditOrDeleteDialogFragment.newInstance(schedules.get(0));
            classEditOrDeleteDialogFragment.setOnClassEditOrDeleteListener(new ClassEditOrDeleteDialogFragment.OnClassEditOrDeleteListener() {
                @Override
                public void onEdit(Schedule editedSchedule) {
                    ArrayList<Schedule> updatedSchedules = new ArrayList<>();
                    updatedSchedules.add(editedSchedule);
                    timetable.edit(idx, updatedSchedules);
                    Toast.makeText(requireContext(), "수업 정보가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onDelete() {
                    timetable.remove(idx);
                    Toast.makeText(requireContext(), "수업 정보가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                }
            });
            classEditOrDeleteDialogFragment.show(requireActivity().getSupportFragmentManager(), "ClassEditDialog");
        });

        Button timeTableSaveButton = view.findViewById(R.id.btnSave);
        timeTableSaveButton.setOnClickListener(v -> {
            saveTimetable();
            saveTimetableToFirestore();
            navController.navigate(R.id.actionSaveMyClass);

        });
    }

    private void loadTimetable() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(TIMETABLE_KEY, null);
        if (json != null) {
            timetable.load(json);
        }
    }

    private void saveTimetable() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = timetable.createSaveData();
        editor.putString(TIMETABLE_KEY, json);
        editor.apply();
        Toast.makeText(requireContext(), "로컬에 시간표가 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void saveTimetableToFirestore() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String json = timetable.createSaveData();

        // 1️Firestore의 timeTableData 필드 업데이트
        db.collection("users").document(userId)
                .update("timetableData", json)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "timeTableData 필드 업데이트 성공"))
                .addOnFailureListener(e -> Log.e("Firestore", "timeTableData 업데이트 실패: " + e.getMessage()));

        // 2️Firestore의 timeTable 컬렉션 데이터 삭제 후 새로 추가
        db.collection("users")
                .document(userId)
                .collection("timetable")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "기존 timeTable 문서 삭제 성공"))
                                .addOnFailureListener(e -> Log.e("Firestore", "기존 timeTable 문서 삭제 실패: " + e.getMessage()));
                    }

                    ArrayList<Schedule> schedules = timetable.getAllSchedulesInStickers();
                    for (int i = 0; i < schedules.size(); i++) {
                        Schedule schedule = schedules.get(i);
                        HashMap<String, Object> scheduleMap = new HashMap<>();
                        scheduleMap.put("classTitle", schedule.getClassTitle());
                        scheduleMap.put("classPlace", schedule.getClassPlace());
                        scheduleMap.put("professorName", schedule.getProfessorName());
                        scheduleMap.put("day", schedule.getDay());
                        scheduleMap.put("startTime", new HashMap<String, Integer>() {{
                            put("hour", schedule.getStartTime().getHour());
                            put("minute", schedule.getStartTime().getMinute());
                        }});
                        scheduleMap.put("endTime", new HashMap<String, Integer>() {{
                            put("hour", schedule.getEndTime().getHour());
                            put("minute", schedule.getEndTime().getMinute());
                        }});

                        db.collection("users")
                                .document(userId)
                                .collection("timeTable")
                                .add(scheduleMap)
                                .addOnSuccessListener(documentReference -> Log.d("Firestore", "새로운 timeTable 문서 추가 성공"))
                                .addOnFailureListener(e -> Log.e("Firestore", "새로운 timeTable 문서 추가 실패: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "timeTable 문서 삭제 실패: " + e.getMessage()));
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