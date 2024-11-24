package com.example.mohassu.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.mohassu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Signup2Fragment extends Fragment {

    private FirebaseAuth auth; // Firebase Auth 인스턴스
    private DatabaseReference databaseReference; // Firebase Database Reference

    private EditText etId, etPassword, etConfirmPassword;
    private TextView tvIdError, tvPasswordError;
    private Button btnCheckId, btnSignupNext;

    private boolean isIdChecked = false; // 아이디 중복 검사 여부

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fregment_sign_up2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Firebase 초기화
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users"); // Firebase Database 경로

        // UI 요소 초기화
        etId = view.findViewById(R.id.etId);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        tvIdError = view.findViewById(R.id.tvIdError);
        tvPasswordError = view.findViewById(R.id.tvPasswordError);
        btnCheckId = view.findViewById(R.id.btnCheckId);
        btnSignupNext = view.findViewById(R.id.btn_signup2_next);

        // 아이디 중복 검사
        btnCheckId.setOnClickListener(v -> checkIdDuplication());

        // 회원가입 버튼 클릭
        btnSignupNext.setOnClickListener(v -> registerUser());
    }

    private void checkIdDuplication() {
        String userId = etId.getText().toString().trim();

        if (TextUtils.isEmpty(userId)) {
            tvIdError.setText("아이디를 입력해주세요.");
            tvIdError.setVisibility(View.VISIBLE);
            return;
        }

        databaseReference.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();
                if (dataSnapshot.exists()) {
                    tvIdError.setText("이미 사용 중인 아이디입니다.");
                    tvIdError.setVisibility(View.VISIBLE);
                    isIdChecked = false;
                } else {
                    tvIdError.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
                    isIdChecked = true;
                }
            } else {
                Toast.makeText(requireContext(), "아이디 중복 검사 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser() {
        String userId = etId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // 유효성 검사
        if (!isIdChecked) {
            Toast.makeText(requireContext(), "아이디 중복 검사를 진행해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            tvPasswordError.setText("비밀번호를 입력해주세요.");
            tvPasswordError.setVisibility(View.VISIBLE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            tvPasswordError.setText("비밀번호가 일치하지 않습니다.");
            tvPasswordError.setVisibility(View.VISIBLE);
            return;
        }

        tvPasswordError.setVisibility(View.GONE);

        // Firebase Authentication 회원가입
        auth.createUserWithEmailAndPassword(userId + "@gmail.com", password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // 이메일 인증 메일 전송
                        auth.getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(emailTask -> {
                                    if (emailTask.isSuccessful()) {
                                        Toast.makeText(requireContext(), "회원가입 성공! 이메일 인증을 완료해주세요.", Toast.LENGTH_SHORT).show();

                                        // Firebase Realtime Database에 사용자 데이터 저장
                                        databaseReference.child(userId).setValue(userId)
                                                .addOnCompleteListener(dbTask -> {
                                                    if (dbTask.isSuccessful()) {
                                                        // 이메일 인증 안내 화면 또는 다음 Fragment로 이동
                                                        NavController navController = Navigation.findNavController(requireView());
                                                        navController.navigate(R.id.Signup3Fragment); // 적절한 Action ID로 변경
                                                    } else {
                                                        Toast.makeText(requireContext(), "데이터 저장 실패: " + dbTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(requireContext(), "인증 이메일 전송 실패: " + emailTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(requireContext(), "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}