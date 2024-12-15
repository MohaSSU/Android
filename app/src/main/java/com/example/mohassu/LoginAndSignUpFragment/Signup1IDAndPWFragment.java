package com.example.mohassu.LoginAndSignUpFragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
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
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Signup1IDAndPWFragment extends Fragment {

    private FirebaseAuth auth; // Firebase Auth 인스턴스

    private EditText etEmail, etPassword, etConfirmPassword;
    private TextView tvEmailError, tvPasswordError;
    private Button btnCheckEmail, btnSignupNext,btnVerifyEmail;

    private boolean isEmailChecked = false; // 이메일 중복 검사 여부
    private ActionCodeSettings actionCodeSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // NavController 초기화
        NavController navController = Navigation.findNavController(view);

        // 뒤로가기 버튼에 클릭 리스너 추가
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            navController.navigateUp();
        });

        // Firebase 초기화
        auth = FirebaseAuth.getInstance();

        // UI 요소 초기화
        etEmail = view.findViewById(R.id.etEmail); // 이메일 입력 필드
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        tvEmailError = view.findViewById(R.id.tvEmaildError);
        tvPasswordError = view.findViewById(R.id.tvPasswordError);


        btnCheckEmail = view.findViewById(R.id.btnCheckEmail);
        btnVerifyEmail = view.findViewById(R.id.btnSendVerification);
        btnSignupNext = view.findViewById(R.id.btnNext);

        // 이메일 중복 및 유효성 검사 버튼 클릭 리스너
        btnCheckEmail.setOnClickListener(v -> checkEmailDuplication());

        // 회원가입 버튼 클릭 리스너
        btnVerifyEmail.setOnClickListener(v -> registerUser());
    }

    private void checkEmailDuplication() {
        String email = etEmail.getText().toString().trim();
        String dummyPassword = "DummyPassword123"; // 테스트용 비밀번호 (중복 검사에만 사용)

        // 이메일 형식 유효성 검사
        if (!isValidEmail(email)) {
            tvEmailError.setText("유효한 이메일 주소를 입력해주세요.");
            tvEmailError.setVisibility(View.VISIBLE);
            return;
        }

        // Firebase Authentication에서 중복 이메일 검사
        auth.createUserWithEmailAndPassword(email, dummyPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 중복되지 않은 이메일 -> 테스트 계정 삭제
                        auth.getCurrentUser().delete()
                                .addOnCompleteListener(deleteTask -> {
                                    if (deleteTask.isSuccessful()) {
                                        tvEmailError.setVisibility(View.GONE);
                                        Toast.makeText(requireContext(), "사용 가능한 이메일입니다.", Toast.LENGTH_SHORT).show();
                                        isEmailChecked = true;
                                    }
                                });
                    } else {
                        // 중복된 이메일
                        if (task.getException() != null && task.getException().getMessage().contains("email address is already in use")) {
                            tvEmailError.setText("이미 사용 중인 이메일입니다.");
                            tvEmailError.setVisibility(View.VISIBLE);
                            isEmailChecked = false;
                        } else {
                            Toast.makeText(requireContext(), "중복 검사 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // 이메일 형식 검사
        if (!isValidEmail(email)) {
            tvEmailError.setText("유효한 이메일 주소를 입력해주세요.");
            tvEmailError.setVisibility(View.VISIBLE);
            return;
        }

        // 이메일 중복 검사 여부 확인
        if (!isEmailChecked) {
            Toast.makeText(requireContext(), "이메일 중복 검사를 진행해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 비밀번호 입력 여부 확인
        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            tvPasswordError.setText("비밀번호를 입력해주세요.");
            tvPasswordError.setVisibility(View.VISIBLE);
            return;
        }

        // 비밀번호 일치 확인
        if (!password.equals(confirmPassword)) {
            tvPasswordError.setText("비밀번호가 일치하지 않습니다.");
            tvPasswordError.setVisibility(View.VISIBLE);
            return;
        }

        tvPasswordError.setVisibility(View.GONE);

        // Firebase Authentication 회원가입
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // 인증 이메일 발송
                        auth.getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(emailTask -> {
                                    if (emailTask.isSuccessful()) {
                                        Toast.makeText(requireContext(), "회원가입 성공! 인증 이메일을 확인해주세요.", Toast.LENGTH_SHORT).show();

//                                        // 인증 확인 버튼 활성화
//                                        Button btnVerifyEmail = requireView().findViewById(R.id.btnVerifyEmail);

                                        // 이메일 인증 확인 버튼 클릭 리스너 설정
                                        btnSignupNext.setOnClickListener(v -> {
                                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                            if (user != null) {
                                                user.reload() // 사용자 정보 새로고침
                                                        .addOnCompleteListener(reloadTask -> {
                                                            NavController navController = Navigation.findNavController(requireView());
                                                            navController.navigate(R.id.actionNextToSignup2); // 적절한 Action ID로 변경
//                                                            if (user.isEmailVerified()) {
//                                                                Toast.makeText(requireContext(), "이메일 인증 완료!", Toast.LENGTH_SHORT).show();
//                                                                // 다음 Fragment로 이동
//                                                                NavController navController = Navigation.findNavController(requireView());
//                                                                navController.navigate(R.id.actionNextToSignup2); // 적절한 Action ID로 변경
//                                                            } else {
//                                                                Toast.makeText(requireContext(), "이메일 인증을 완료해주세요!", Toast.LENGTH_SHORT).show();
//                                                            }
                                                        });
                                            } else {
                                                Toast.makeText(requireContext(), "사용자 정보를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        Toast.makeText(requireContext(), "인증 이메일 발송 실패: " + emailTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(requireContext(), "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 유효한 이메일 형식인지 확인
    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}