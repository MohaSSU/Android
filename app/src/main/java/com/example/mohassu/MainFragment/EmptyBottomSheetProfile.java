package com.example.mohassu.MainFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.mohassu.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class EmptyBottomSheetProfile extends BottomSheetDialogFragment {

    private static final String ARG_FRIEND_ID = "friend_id";
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();

    public static EmptyBottomSheetProfile newInstance(String friendId) {
        EmptyBottomSheetProfile fragment = new EmptyBottomSheetProfile();
        Bundle args = new Bundle();
        args.putString(ARG_FRIEND_ID, friendId);  // 데이터를 Bundle에 넣어서 전달
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_sheet_check_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageButton closeButton = view.findViewById(R.id.btn_close);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> dismiss());
        }

        String friendId = getArguments() != null ? getArguments().getString(ARG_FRIEND_ID) : null;

        if (currentUser != null) {
            String uid = currentUser.getUid();

            db.collection("users").document(uid)
                    .collection("friends").document(friendId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nickName = documentSnapshot.getString("nickname");
                            String name = documentSnapshot.getString("name");
                            String photoUrl = documentSnapshot.getString("photoUrl");

                            // UI 업데이트
                            TextView textNickname = view.findViewById(R.id.text_nickname);
                            TextView textName = view.findViewById(R.id.text_name);
                            ImageView profileImage = view.findViewById(R.id.img_profile);

                            textNickname.setText(nickName != null ? nickName : "#nickname");
                            textName.setText(name != null ? name : "#name");

                            // 프로필 사진 로드
                            if (photoUrl != null) {
                                Glide.with(this)
                                        .load(photoUrl)
                                        .placeholder(R.drawable.img_default)
                                        .error(R.drawable.img_default)
                                        .into(profileImage);
                            } else {
                                profileImage.setImageResource(R.drawable.pic_basic_profile);
                            }
                        } else {
                            Toast.makeText(getContext(), "친구 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}