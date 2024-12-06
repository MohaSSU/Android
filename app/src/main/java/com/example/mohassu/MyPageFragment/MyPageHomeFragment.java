package com.example.mohassu.MyPageFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
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

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();

            db.collection("users").document(uid)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Firestore에서 사용자 데이터 가져오기
                                String nickName = document.getString("nickname");
                                String name = document.getString("name");
                                String email = document.getString("email");
                                String birthDate = document.getString("birthDate");
                                String photoUrl = document.getString("photoUrl");

                                Log.d("Firestore", "Name: " + name + ", Email: " + email + ", nickName" + nickName +", birthDate" + birthDate);

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
                                            .placeholder(R.drawable.img_logo) // 로딩 중 대체 이미지
                                            .error(R.drawable.img_logo) // 로딩 실패 시 대체 이미지
                                            .into(profileImageView);
                                } else {
                                    profileImageView.setImageResource(R.drawable.img_logo); // 기본 이미지
                                }


                                greetingTextView.setText(nickName +"님 반갑습니다!");
                                userIdView.setText(email);
                                userNickNameView.setText(nickName);
                                userNameView.setText(name);
                                userBirthView.setText(birthDate);
                            } else {
                                Log.d("Firestore", "No such document!");
                            }
                        } else {
                            Log.w("Firestore", "Error getting document.", task.getException());
                        }
                    });
        } else {
            Log.d("FirebaseAuth", "No user is logged in");
        }

        // NavController 초기화
        NavController navController = Navigation.findNavController(view);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            navController.navigateUp();
        });

        TextView logoutText = view.findViewById(R.id.logoutText);
        logoutText.setOnClickListener(v -> {
            auth.signOut(); // Firebase 인증 로그아웃
//            navController.navigate(R.id.actionLogout); // 로그인 화면으로 이동
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
