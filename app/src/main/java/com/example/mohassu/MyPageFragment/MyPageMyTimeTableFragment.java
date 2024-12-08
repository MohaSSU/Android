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

import com.example.mohassu.R;
import com.github.tlaabs.timetableview.TimetableView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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
//        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        String json = prefs.getString(TIMETABLE_KEY, null);
//        timetable.load(json);
//        Toast.makeText(requireContext(), "저장된 시간표를 불러왔습니다.", Toast.LENGTH_SHORT).show();



        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 현재 로그인한 사용자 ID 가져오기

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // timeTableData 필드 가져오기
                        String timeTableData = documentSnapshot.getString("timetableData");
                        if (timeTableData != null) {
                            timetable.load(timeTableData);
                            Toast.makeText(requireContext(), "저장된 시간표를 불러왔습니다.", Toast.LENGTH_SHORT).show();
                            Log.d("Firestore", "TimeTableData: " + timeTableData);
                            // JSON 데이터 처리 로직 추가 가능
                        } else {
                            Log.d("Firestore", "timeTableData 필드가 없습니다.");
                        }
                    } else {
                        Log.d("Firestore", "해당 문서가 존재하지 않습니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "timeTableData 가져오기 실패: " + e.getMessage());
                });
    }
}
