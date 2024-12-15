package com.example.mohassu.MyPageFragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.mohassu.NavigationMainActivity;
import com.example.mohassu.NavigationStartLoginAndSignupActivity;
import com.example.mohassu.R;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyPageHomeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mypage_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String photoUrl = sharedPreferences.getString("photoUrl",null);
        String nickName = sharedPreferences.getString("nickName",null);
        String email = sharedPreferences.getString("email",null);
        String name = sharedPreferences.getString("name",null);
        String birthDate = sharedPreferences.getString("birthDate",null);

        // UI 업데이트
        TextView greetingTextView = view.findViewById(R.id.greetingText);
        TextView userIdView = view.findViewById(R.id.userId);
        TextView userNickNameView = view.findViewById(R.id.usernickName);
        TextView userNameView = view.findViewById(R.id.userName);
        TextView userBirthView = view.findViewById(R.id.userBirth);
        ImageView profileImageView = view.findViewById(R.id.profileImage);

        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .circleCrop() // 원형으로 자르기
                    .placeholder(R.drawable.img_basic_profile) // 로딩 중 대체 이미지
                    .error(R.drawable.img_basic_profile) // 로딩 실패 시 대체 이미지
                    .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.img_basic_profile); // 기본 이미지
        }

        greetingTextView.setText(nickName +"님 반갑습니다!");
        userIdView.setText(email);
        userNickNameView.setText(nickName);
        userNameView.setText(name);
        userBirthView.setText(birthDate);

        // NavController 초기화
        NavController navController = Navigation.findNavController(view);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            navController.navigateUp();
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();
        view.findViewById(R.id.logoutText).setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            auth.signOut(); // Firebase 인증 로그아웃
            // 메인 액티비티로 이동
            Intent intent = new Intent(getActivity(), NavigationStartLoginAndSignupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Log.d("NavigationDebug", "NavigationStartLoginAndSignupActivity started");
            requireActivity().finish();
            Log.d("NavigationDebug", "Current activity finished");
            Toast.makeText(getActivity(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show(); // 로그인 화면으로 이동
        });

        // 다음 프레그먼트를 클릭 시 다음 Fragment로 이동
        ImageButton profileEditButton = view.findViewById(R.id.btnProfileEdit);
        profileEditButton.setFocusable(false);
        profileEditButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionProfileEdit);
        });

        // 다음 프레그먼트를 클릭 시 다음 Fragment로 이동
        ImageButton timeTableViewButton = view.findViewById(R.id.btnSchedule);
        timeTableViewButton.setFocusable(false);
        timeTableViewButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionSchedule);
        });

        // 다음 프레그먼트를 클릭 시 다음 Fragment로 이동
        ImageButton notificationButton = view.findViewById(R.id.btnNotification);
        notificationButton.setFocusable(false);
        notificationButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionSettingNotification);
        });
    }

}
