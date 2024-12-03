package com.example.mohassu;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mohassu.CheckProfileAndTimeTableFragment.BottomSheetCheckProfileFragment;

public class TestViewProfileButtonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test_button);

        Button testButton = findViewById(R.id.testBtn);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 데이터 전달용 Bundle 생성
                Bundle bundle = new Bundle();
                bundle.putString("nickName", "JDoe"); // 닉네임 데이터 <- 친구 닉네임 넣어주면 됨
                bundle.putString("name", "John Doe");   // 이름 데이터 <- 친구 이름 넣어주면 됨
                bundle.putInt("profileImage", R.drawable.img_my_profile); // 프로필 이미지 <- 친구 이미지 넣어주면 됨

                // BottomSheetCheckProfileFragment 생성 및 데이터 설정
                BottomSheetCheckProfileFragment bottomSheet = new BottomSheetCheckProfileFragment();
                bottomSheet.setArguments(bundle);

                // 바텀시트 호출
                bottomSheet.show(getSupportFragmentManager(), "BottomSheetCheckProfile");
            }
        });
    }
}