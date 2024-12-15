package com.example.mohassu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class SplashActivity extends AppCompatActivity {
    private boolean isReady = false;  // 초기화 완료 상태를 추적하는 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Core Splash Screen API 설정
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 스플래시 화면 유지 조건 설정
        splashScreen.setKeepOnScreenCondition(() -> !isReady);

        // 초기화 작업 시작
        initializeApp();
    }

    private void initializeApp() {
        // 백그라운드 스레드에서 초기화 작업 수행
        new Thread(() -> {
            try {
                // 초기화 작업 시뮬레이션
                Thread.sleep(1000); // 1초 대기

                // UI 스레드에서 다음 화면으로 전환
                runOnUiThread(() -> {
                    isReady = true;  // 초기화 완료 표시
                    navigateToNextActivity();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void navigateToNextActivity() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);
        String password = sharedPreferences.getString("password", null);

        if (email != null && password != null) {
            // 메인 네비게이션 액티비티 실행 -> 자동 로그인
            Intent intent = new Intent(SplashActivity.this, NavigationMainActivity.class);
            startActivity(intent);
        } else {
            // 로그인 및 회원가입 네비게이션 액티비티 실행 -> 로그인 화면으로 이동
            Intent intent = new Intent(SplashActivity.this, NavigationStartLoginAndSignupActivity.class);
            startActivity(intent);
        }
        finish();
    }
}