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
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class Signup1Fragment extends Fragment {

    private EditText etPhoneNumber, etAuthCode;
    private Button btnRequestCode, btnVerifyCode, btnNext;
    private TextView tvAuthMessage;
    private FirebaseAuth mAuth;
    private String verificationId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fregment_sign_up1, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // NavController 초기화
        NavController navController = Navigation.findNavController(view);

        // FirebaseAuth 초기화
        mAuth = FirebaseAuth.getInstance();

        // UI 요소 초기화
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber);
        etAuthCode = view.findViewById(R.id.etAuthCode);
        btnRequestCode = view.findViewById(R.id.btnRequestCode);
        btnVerifyCode = view.findViewById(R.id.btnVerifyCode);
        btnNext = view.findViewById(R.id.btn_signup1_next);
        tvAuthMessage = view.findViewById(R.id.tvAuthMessage);

        // 인증번호 요청 버튼 리스너
        btnRequestCode.setOnClickListener(v -> sendVerificationCode());

        // 인증번호 확인 버튼 리스너
        btnVerifyCode.setOnClickListener(v -> verifyCode());

        // 다음 버튼 리스너
        btnNext.setOnClickListener(v -> {
            if (verificationId != null) {
                // 인증 성공 시 다음 Fragment로 이동
                navController.navigate(R.id.btn_signup1_next);
            } else {
                Toast.makeText(getActivity(), "인증을 완료해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendVerificationCode() {
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(getActivity(), "전화번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber) // 입력된 전화번호
                .setTimeout(60L, TimeUnit.SECONDS) // 타임아웃 설정
                .setActivity(requireActivity()) // 현재 Activity
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        tvAuthMessage.setText("인증 완료");
                        tvAuthMessage.setVisibility(View.VISIBLE);
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(getActivity(), "인증 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = s; // 인증 ID 저장
                        Toast.makeText(getActivity(), "인증번호가 전송되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyCode() {
        String code = etAuthCode.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(getActivity(), "인증번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                tvAuthMessage.setText("인증 완료");
                tvAuthMessage.setVisibility(View.VISIBLE);
            } else {
                tvAuthMessage.setText("인증 실패");
                tvAuthMessage.setVisibility(View.VISIBLE);
            }
        });
    }
}