//package com.example.mohassu.LoginAndSignUpFragment;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.DatePicker;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.navigation.NavController;
//import androidx.navigation.Navigation;
//
//import com.example.mohassu.R;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//public class Signup2DetailFragment extends Fragment {
//
//    private EditText etNickname, etName;
//    private DatePicker dpBirthdate;
//    private Button signupNextButton;
//
//    private FirebaseAuth mAuth;
//    private DatabaseReference databaseReference;
//
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        Setcon
////
////        //androidx.fragment.app.FragmentManager
////        //androidx.fragment.app.FragmentTransaction
////
////    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//
//        return inflater.inflate(R.layout.fragment_sign_up2, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
////        getParentFragmentManager()
////                .beginTransaction()
////                        .replace(R.id.main,new Signup3ProfileFragment())
////                                .commit();
//
//        // NavController 초기화
//        NavController navController = Navigation.findNavController(view);
//
//        // 뒤로가기 버튼에 클릭 리스너 추가
//        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
//            navController.navigateUp();
//        });
//
//        // Firebase 초기화
//        mAuth = FirebaseAuth.getInstance();
//        databaseReference = FirebaseDatabase.getInstance().getReference("users");
//
//        // UI 초기화
//        etNickname = view.findViewById(R.id.etNickname);
//        etName = view.findViewById(R.id.etName);
//        dpBirthdate = view.findViewById(R.id.dpSpinner);
//        signupNextButton = view.findViewById(R.id.btnNext);
//
//        signupNextButton.setOnClickListener(v -> saveUserProfile(view));
//    }
//
//    private void saveUserProfile(View view) {
//        String nickname = etNickname.getText().toString().trim();
//        String name = etName.getText().toString().trim();
//        int day = dpBirthdate.getDayOfMonth();
//        int month = dpBirthdate.getMonth() + 1; // Month is 0-based in DatePicker
//        int year = dpBirthdate.getYear();
//        String birthdate = year + "-" + month + "-" + day;
//
//        // 필드 유효성 검사
//        if (nickname.isEmpty() || name.isEmpty()) {
//            Toast.makeText(requireContext(), "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Firebase Realtime Database에 저장
//        String uid = mAuth.getCurrentUser().getUid();
//        UserProfile userProfile = new UserProfile(nickname, name, birthdate);
//
//        databaseReference.child(uid).setValue(userProfile)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(requireContext(), "프로필 저장 성공!", Toast.LENGTH_SHORT).show();
//
//                        // 다음 Fragment로 이동
//                        NavController navController = Navigation.findNavController(requireView());
//                        navController.navigate(R.id.actionNextToSignup3); // 적절한 Action ID로 변경
//                    } else {
//                        Toast.makeText(requireContext(), "프로필 저장 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    public static class UserProfile {
//        public String nickname;
//        public String name;
//        public String birthdate;
//
//        public UserProfile() {
//            // 기본 생성자
//        }
//
//        public UserProfile(String nickname, String name, String birthdate) {
//            this.nickname = nickname;
//            this.name = name;
//            this.birthdate = birthdate;
//        }
//    }
//}
package com.example.mohassu.LoginAndSignUpFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.mohassu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Signup2DetailFragment extends Fragment {

    private static final String TAG = "Signup2DetailFragment";

    private EditText etNickname, etName;
    private DatePicker dpBirthdate;
    private Button signupNextButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI 초기화
        etNickname = view.findViewById(R.id.etNickname);
        etName = view.findViewById(R.id.etName);
        dpBirthdate = view.findViewById(R.id.dpSpinner);
        signupNextButton = view.findViewById(R.id.btnNext);

        // 다음 버튼 클릭 리스너
        signupNextButton.setOnClickListener(v -> saveUserProfile(view));
    }

    private void saveUserProfile(View view) {
        String nickname = etNickname.getText().toString().trim();
        String name = etName.getText().toString().trim();
        int day = dpBirthdate.getDayOfMonth();
        int month = dpBirthdate.getMonth() + 1; // Month is 0-based in DatePicker
        int year = dpBirthdate.getYear();
        String birthdate = year + "-" + month + "-" + day;

        // 필드 유효성 검사
        if (nickname.isEmpty() || name.isEmpty()) {
            Toast.makeText(requireContext(), "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 현재 사용자 UID 가져오기
        String uid = mAuth.getCurrentUser().getUid();

        // Firestore에 사용자 정보 저장
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("nickname", nickname);
        userProfile.put("name", name);
        userProfile.put("birthDate", birthdate);

        db.collection("users").document(uid)
                .set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "프로필 저장 성공!", Toast.LENGTH_SHORT).show();

                    // 다음 프래그먼트로 이동
                    NavController navController = Navigation.findNavController(view);
                    navController.navigate(R.id.actionNextToSignup3); // 적절한 Action ID로 변경
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Firestore 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Firestore Error", e);
                });
    }
}