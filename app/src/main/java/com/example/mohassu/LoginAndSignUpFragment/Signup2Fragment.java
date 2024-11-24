package com.example.mohassu.LoginAndSignUpFragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

public class Signup2Fragment extends Fragment {

    private FirebaseAuth auth; // Firebase Auth 인스턴스
    private EditText etId, etPassword, etConfirmPassword;
    private TextView tvIdError, tvPasswordError;
    private Button btnSignupNext;

    private boolean isIdChecked = true; // 가정: ID 중복 확인 생략
    private static final String TAG = "Signup2Fragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Firebase 초기화
        auth = FirebaseAuth.getInstance();

        // UI 요소 초기화
        etId = view.findViewById(R.id.etId);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        tvIdError = view.findViewById(R.id.tvIdError);
        tvPasswordError = view.findViewById(R.id.tvPasswordError);
        btnSignupNext = view.findViewById(R.id.btn_signup2_next);

        // 회원가입 버튼 클릭
        btnSignupNext.setOnClickListener(v -> {
            Log.d(TAG, "Next button clicked");
            if (validateInputs()) {
                navigateToNextFragment(view);
            } else {
                Log.e(TAG, "Validation failed");
            }
        });
    }

    private boolean validateInputs() {
        String userId = etId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // 유효성 검사
        if (TextUtils.isEmpty(userId)) {
            tvIdError.setText("아이디를 입력해주세요.");
            tvIdError.setVisibility(View.VISIBLE);
            return false;
        }

        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            tvPasswordError.setText("비밀번호를 입력해주세요.");
            tvPasswordError.setVisibility(View.VISIBLE);
            return false;
        }

        if (!password.equals(confirmPassword)) {
            tvPasswordError.setText("비밀번호가 일치하지 않습니다.");
            tvPasswordError.setVisibility(View.VISIBLE);
            return false;
        }

        // 모든 유효성 검사를 통과하면
        tvIdError.setVisibility(View.GONE);
        tvPasswordError.setVisibility(View.GONE);
        return true;
    }

    private void navigateToNextFragment(View view) {
        try {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.Signup3Fragment); // Action ID를 네비게이션 그래프에서 확인
            Log.d(TAG, "Navigating to the next fragment");
        } catch (Exception e) {
            Log.e(TAG, "Navigation failed", e);
            Toast.makeText(requireContext(), "Failed to navigate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}