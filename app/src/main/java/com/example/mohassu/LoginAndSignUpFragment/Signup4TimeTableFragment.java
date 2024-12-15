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

import androidx.annotation.NonNull;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class Signup4TimeTableFragment extends Fragment {

    private static final String PREFS_NAME = "TimetablePrefs"; //회원가입1에서 받은 이메일을 넣어주면 될 듯 -> 지금은 그냥 휴대폰의 아무유저나 다 동일하게 되어있음..
    private static final String TIMETABLE_KEY = "timetable";

    private TimetableView timetable;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up4, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        timetable = view.findViewById(R.id.timetable);

        // Firestore에서 시간표 불러오기
        loadTimetableFromFirestoreStructured();

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

        timetable.setOnStickerSelectEventListener((idx, schedules) -> {
            // 다이얼로그 프래그먼트 호출
            ClassEditOrDeleteDialogFragment classEditOrDeleteDialogFragment = ClassEditOrDeleteDialogFragment.newInstance(schedules.get(0));
            Log.d("StickerSelectEvent", "schedule idx : " + idx);
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
        });

        // 다음 버튼
        Button signupNextButton = view.findViewById(R.id.btnNext);
        signupNextButton.setFocusable(false);
        signupNextButton.setOnClickListener(v -> {
            saveTimetableToFirestoreStructured();
            saveTimetable();
            navController.navigate(R.id.actionNextToSignupDone);
        });

        // 건너뛰기 버튼
        Button signupSkipButton = view.findViewById(R.id.btnSkip);
        signupSkipButton.setFocusable(false);
        signupSkipButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionSkipToSignupDone);
        });
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

    private void saveTimetable() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String json = timetable.createSaveData();
        editor.putString(TIMETABLE_KEY, json);
        editor.apply();

        Toast.makeText(requireContext(), "시간표가 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void saveTimetableToFirestoreStructured() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String json = timetable.createSaveData();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Firestore에 저장
        db.collection("users").document(userId)
                .update(new HashMap<String, Object>() {{
                    put("timetableData", json);
                }})
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Firestore에 시간표가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "시간표 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // 시간표 데이터를 가져오기
        ArrayList<Schedule> schedules = timetable.getAllSchedulesInStickers();

        for (Schedule schedule : schedules) {
            HashMap<String, Object> scheduleMap = new HashMap<>();
            scheduleMap.put("classTitle", schedule.getClassTitle());
            scheduleMap.put("classPlace", schedule.getClassPlace());
            scheduleMap.put("professorName", schedule.getProfessorName());
            scheduleMap.put("day", schedule.getDay());
            scheduleMap.put("startTime", new HashMap<String, Object>() {{
                put("hour", schedule.getStartTime().getHour());
                put("minute", schedule.getStartTime().getMinute());
            }});
            scheduleMap.put("endTime", new HashMap<String, Object>() {{
                put("hour", schedule.getEndTime().getHour());
                put("minute", schedule.getEndTime().getMinute());
            }});

            // Firestore에 저장
            db.collection("users")
                    .document(userId)
                    .collection("timetable")
                    .add(scheduleMap)
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "수업 저장 성공"))
                    .addOnFailureListener(e -> Log.e("Firestore", "수업 저장 실패: " + e.getMessage()));
        }

        Toast.makeText(requireContext(), "Firestore에 시간표가 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void loadTimetableFromFirestoreStructured() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//
//        db에서 파싱한 data를 다시 json형식으로 합쳐서 표현하는 방법
//        db.collection("users")
//                .document(userId)
//                .collection("timetable")
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    ArrayList<Schedule> schedules = new ArrayList<>();
//                    querySnapshot.forEach(document -> {
//                        HashMap<String, Object> data = (HashMap<String, Object>) document.getData();
//
//                        Schedule schedule = new Schedule();
//                        schedule.setClassTitle((String) data.get("classTitle"));
//                        schedule.setClassPlace((String) data.get("classPlace"));
//                        schedule.setProfessorName((String) data.get("professorName"));
//                        schedule.setDay(((Long) data.get("day")).intValue());
//
//                        HashMap<String, Object> startTime = (HashMap<String, Object>) data.get("startTime");
//                        schedule.setStartTime(new Time(((Long) startTime.get("hour")).intValue(), ((Long) startTime.get("minute")).intValue()));
//
//                        HashMap<String, Object> endTime = (HashMap<String, Object>) data.get("endTime");
//                        schedule.setEndTime(new Time(((Long) endTime.get("hour")).intValue(), ((Long) endTime.get("minute")).intValue()));
//
//                        schedules.add(schedule);
//                    });
//
//                    // 시간표에 로드
//                    timetable.add(schedules);
//                    Toast.makeText(requireContext(), "Firestore에서 시간표를 불러왔습니다.", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(requireContext(), "시간표 불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });

//        db에 json을 그냥 불러와서 사용하는 방법
//        db.collection("users")
//                .document(userId)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        // 특정 필드 값 가져오기
//                        String json = documentSnapshot.getString("timetableData"); // "name" 필드
//                        // 사용
//                        if (json != null) {
//                            timetable.load(json);
//                            //Toast.makeText(requireContext(), "저장된 시간표를 불러왔습니다.", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        Log.d("Firestore", "Document does not exist.");
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("Firestore", "Error fetching document", e);
//                });
//        local에 저장하고 사용하는 방법
//        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        String json = prefs.getString(TIMETABLE_KEY, null);
//
//        if (json != null) {
//            timetable.load(json);
//            //Toast.makeText(requireContext(), "저장된 시간표를 불러왔습니다.", Toast.LENGTH_SHORT).show();
//        }
    }
}