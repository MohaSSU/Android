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
import com.google.firebase.firestore.DocumentSnapshot;
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

        String currentUserId = auth.getCurrentUser().getUid(); // 현재 사용자 UID

        db.collection("users")
                .whereEqualTo("email", email) // 이메일로 사용자 검색
                .get()
                .addOnSuccessListener(task -> {
                    if (!task.isEmpty()) {
                        DocumentSnapshot document = task.getDocuments().get(0);
                        String friendUserId = document.getId(); // 친구의 UID 가져오기

                        if (currentUserId.equals(friendUserId)) {
                            Toast.makeText(requireContext(), "자기 자신을 친구로 추가할 수 없습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 내 Firestore에 친구 추가 (currentUser -> friendUserId)
                        db.collection("users").document(currentUserId)
                                .collection("friends").document(friendUserId)
                                .get()
                                .addOnSuccessListener(friendDoc -> {
                                    if (friendDoc.exists()) {
                                        Toast.makeText(requireContext(), "이미 친구로 추가된 사용자입니다.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // 내 Firestore에 친구 추가
                                        Map<String, Object> friendData = new HashMap<>();
                                        friendData.put("userId", friendUserId);

                                        db.collection("users").document(currentUserId)
                                                .collection("friends").document(friendUserId)
                                                .set(friendData)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(requireContext(), "내 친구 목록에 추가 완료!", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(requireContext(), "내 친구 추가 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });

                                        // 친구의 Firestore에 나의 UID 추가 (friendUserId -> currentUserId)
                                        Map<String, Object> myData = new HashMap<>();
                                        myData.put("userId", currentUserId);

                                        db.collection("users").document(friendUserId)
                                                .collection("friends").document(currentUserId)
                                                .set(myData)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(requireContext(), "친구의 친구 목록에 추가 완료!", Toast.LENGTH_SHORT).show();
                                                    dismiss(); // 다이얼로그 닫기
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(requireContext(), "친구의 친구 추가 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), "친구 추가 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(requireContext(), "해당 이메일을 가진 사용자가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "친구 추가 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}