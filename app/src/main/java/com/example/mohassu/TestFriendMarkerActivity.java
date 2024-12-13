package com.example.mohassu;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mohassu.MainFragment.MainHomeFriendTestFragment;

public class TestFriendMarkerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        if (savedInstanceState == null) { // Activity가 처음 실행된 경우
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, new MainHomeFriendTestFragment()) // MainHomeFragment로 전환
                    .commit();
        }
    }
}