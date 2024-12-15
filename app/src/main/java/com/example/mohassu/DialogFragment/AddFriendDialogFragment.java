package com.example.mohassu.DialogFragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mohassu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class AddFriendDialogFragment extends DialogFragment {

    private EditText emailInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_friend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emailInput = view.findViewById(R.id.emailInput); // 이메일 입력 필드

        // "친구 추가" 버튼 클릭 리스너
        view.findViewById(R.id.btnAddFriend).setOnClickListener(v -> addFriend());

        // "취소" 버튼 클릭 리스너
        view.findViewById(R.id.btnCancelFriend).setOnClickListener(v -> dismiss());
    }

    private void addFriend() {
        String email = emailInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(requireContext(), "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String currentUserId = auth.getCurrentUser().getUid(); // 현재 로그인한 사용자 UID

        // 이메일로 사용자 검색
        //추가한 코드: 다른 데이터(좌표, 이름 등)
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // 검색된 사용자 정보
                        String friendUserId = task.getResult().getDocuments().get(0).getId();
                        String name = task.getResult().getDocuments().get(0).getString("name");
                        String nickname = task.getResult().getDocuments().get(0).getString("nickname");
                        String photoUrl = task.getResult().getDocuments().get(0).getString("photoUrl");
                        String place = task.getResult().getDocuments().get(0).getString("place");
                        String statusMessage = task.getResult().getDocuments().get(0).getString("statusMessage");

                        db.collection("users").document(friendUserId)
                                .collection("location")
                                .document("currentLocation")
                                .get()
                                .addOnSuccessListener(locationDoc -> {
                                    if (locationDoc.exists()) {

                                        // 친구 추가 로직
                                        Map<String, Object> friendData = new HashMap<>();
                                        friendData.put("friendUserId", friendUserId);
                                        friendData.put("email", email);
                                        friendData.put("name", name);

                                        db.collection("users").document(currentUserId)
                                                .collection("friends").document(friendUserId)
                                                .set(friendData)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(requireContext(), "친구 추가 완료!", Toast.LENGTH_SHORT).show();
                                                    dismiss(); // 다이얼로그 닫기
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(requireContext(), "친구 추가 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(requireContext(), "사용자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }
}