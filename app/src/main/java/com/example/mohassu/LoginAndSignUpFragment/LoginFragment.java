package com.example.mohassu.LoginAndSignUpFragment;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.mohassu.NavigationMainActivity;
import com.example.mohassu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {

    private static final String TAG = "mohassu:login";

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_login, container, false);

        // FirebaseAuth 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();

        view.findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavController navController = Navigation.findNavController(view);
                navController.popBackStack(); // 네비게이션 스택에서 이전 프래그먼트로 이동
            }
        });

        // UI 요소 초기화
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        buttonLogin = view.findViewById(R.id.btnLogin);

        editTextEmail.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                editTextPassword.requestFocus(); // 비밀번호 입력 필드로 포커스 이동
                return true;
            }
            return false;
        });

        // 로그인 버튼 클릭 리스너
        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(getActivity(), "이메일과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });

        return view;
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()){
//
                            Toast.makeText(getActivity(), "로그인 성공!", Toast.LENGTH_SHORT).show();

                            // SharedPreferences에 이메일과 비밀번호 저장
                            saveCredentialsToPreferences(email, password);

                            // 메인 액티비티로 이동
                            Intent intent = new Intent(getActivity(), NavigationMainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            Log.d("NavigationDebug", "NavigationMainActivity started");
                            requireActivity().finish();
                            Log.d("NavigationDebug", "Current activity finished");
                        } else {
                            Toast.makeText(getActivity(), "이메일 인증을 완료해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "로그인 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void saveCredentialsToPreferences(String email, String password) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("uid",currentUserId);
        editor.putString("email", email);
        editor.putString("password", password);
        editor.apply(); // 비동기로 저장
        Log.d(TAG, "이메일과 비밀번호가 저장되었습니다.");
    }
}