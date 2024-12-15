package com.example.mohassu.MyPageFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.mohassu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MyPageProfileEdit1DetailFragment extends Fragment {

    private EditText etNickname;
    private EditText etName;
    private DatePicker dpSpinner;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static String TAG ="mohassu:mypage_edit_profile";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mypage_profile_edit1, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // NavController 초기화
        NavController navController = Navigation.findNavController(view);

        // 뒤로가기 버튼에 클릭 리스너 추가
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            navController.navigateUp();
        });

        // 뷰 초기화
        etNickname = view.findViewById(R.id.etNickname);
        etName = view.findViewById(R.id.etName);
        dpSpinner = view.findViewById(R.id.dpSpinner);

        // SharedPreferences 인스턴스 가져오기
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // 저장된 데이터 불러오기
        String nickName = sharedPreferences.getString("nickName", ""); // 기본값은 빈 문자열
        String name = sharedPreferences.getString("name", "");
        String birthDate = sharedPreferences.getString("birthDate", "");

        // EditText에 값 설정
        etNickname.setText(nickName);
        etName.setText(name);

        // DatePicker에 값 설정
        if (!birthDate.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                Date date = sdf.parse(birthDate);
                if (date != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH); // 월은 0부터 시작
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    dpSpinner.updateDate(year, month, day);
                }
            } catch (ParseException e) {
                e.printStackTrace();
                // 예외 처리: 기본 날짜 설정 또는 사용자에게 에러 메시지 표시
                // 예: 현재 날짜로 설정
                Calendar calendar = Calendar.getInstance();
                dpSpinner.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            }
        } else {
            // birthDate가 비어 있을 때의 처리: 기본 날짜 설정 등
            Calendar calendar = Calendar.getInstance();
            dpSpinner.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        }

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 다음 프레그먼트를 클릭 시 다음 Fragment로 이동
        Button editProfileNextButton = view.findViewById(R.id.btnNext);
        editProfileNextButton.setFocusable(false);
        editProfileNextButton.setOnClickListener(v -> {
            saveUserProfile(view);
            navController.navigate(R.id.actionNextToEditProfile2);
        });
    }

    private void saveUserProfile(View view) {
        String nickname = etNickname.getText().toString().trim();
        String name = etName.getText().toString().trim();
        int day = dpSpinner.getDayOfMonth();
        int month = dpSpinner.getMonth() + 1; // Month is 0-based in DatePicker
        int year = dpSpinner.getYear();
        String birthDate = year + "-" + month + "-" + day;

        // 필드 유효성 검사
        if (nickname.isEmpty() || name.isEmpty()) {
            Toast.makeText(requireContext(), "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putString("nickName", nickname);
        editor.putString("birthDate", birthDate);
        editor.apply();
        Log.d(TAG, "로컬에 수정 정보 저장 완료");

        // 현재 사용자 UID 가져오기
        String uid = mAuth.getCurrentUser().getUid();
        String email = mAuth.getCurrentUser().getEmail();

        // Firestore에 사용자 정보 저장
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("nickname", nickname);
        userProfile.put("name", name);
        userProfile.put("birthDate", birthDate);

        db.collection("users").document(uid)
                .set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG,"프로필 정보가 업데이트되었습니다.");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Firestore 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Firestore Error", e);
                });
    }
}
