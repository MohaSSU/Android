package com.example.mohassu.LoginAndSignUpFragment;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.mohassu.NavigationMainActivity;
import com.example.mohassu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

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

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            String uid = user.getUid();

                            db.collection("users").document(uid)
                                    .get()
                                    .addOnCompleteListener(tasks -> {
                                        if (tasks.isSuccessful()) {
                                            DocumentSnapshot document = tasks.getResult();
                                            if (document.exists()) {
                                                // Firestore에서 사용자 데이터 가져오기
                                                String nickName = document.getString("nickname");
                                                String name = document.getString("name");
                                                String birthDate = document.getString("birthDate");
                                                String photoUrl = document.getString("photoUrl");

                                                Log.d("mohassu:Firestore", "Name: " + name + ", Email: " + email + ", nickName" + nickName +", birthDate" + birthDate);

                                                // SharedPreferences에 이메일과 비밀번호 및 프로필 정보 저장
                                                saveCredentialsToPreferences(uid, email, password, name, nickName, birthDate, photoUrl);

                                                // FCM 토큰 가져오기 및 저장
                                                FirebaseMessaging.getInstance().getToken()
                                                        .addOnCompleteListener(tokenTask -> {
                                                            if (tokenTask.isSuccessful()) {
                                                                String fcmToken = tokenTask.getResult();
                                                                Log.d(TAG, "FCM Token: " + fcmToken);
                                                                saveTokenToFirestore(fcmToken);  // Firestore에 FCM 토큰 저장
                                                            } else {
                                                                Log.w(TAG, "FCM token retrieval failed: " + tokenTask.getException());
                                                            }
                                                        });

                                            } else {
                                                Log.d("Firestore", "No such document!");
                                            }
                                        } else {
                                            Log.w("Firestore", "Error getting document.", task.getException());
                                        }
                                    });

                            // 메인 액티비티로 이동
                            Intent intent = new Intent(getActivity(), NavigationMainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            Log.d("NavigationDebug", "NavigationMainActivity started");
                            requireActivity().finish();
                            Log.d("NavigationDebug", "Current activity finished");
                            Toast.makeText(getActivity(), "로그인 성공!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "이메일 인증을 완료해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "로그인 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveCredentialsToPreferences(String uid, String email, String password, String name, String nickName, String birthDate, String photoUrl) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("uid",uid);
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putString("name", name);
        editor.putString("nickName", nickName);
        editor.putString("birthDate", birthDate);
        editor.putString("photoUrl", photoUrl);
        editor.apply(); // 비동기로 저장
        Log.d(TAG, "이메일과 비밀번호 및 회원프로필 정보가 저장되었습니다.");
    }

    private void saveTokenToFirestore(String fcmToken) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(uid)
                    .update("fcmToken", fcmToken)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "FCM Token 저장 성공");
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "FCM Token 저장 실패: " + e.getMessage());
                    });
        }
    }
}