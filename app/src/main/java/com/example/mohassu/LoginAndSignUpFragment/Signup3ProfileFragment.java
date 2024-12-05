package com.example.mohassu.LoginAndSignUpFragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.mohassu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Signup3ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageButton addProfileButton;
    private ImageView profileImageView;
    private Button signupNextButton, skipButton;
    private Uri selectedImageUri; // 선택된 이미지의 URI 저장

    private FirebaseStorage storage;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Firebase 초기화
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        // NavController 초기화
        NavController navController = Navigation.findNavController(view);

        // 뒤로가기 버튼에 클릭 리스너 추가
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            navController.navigateUp();
        });

        // UI 요소 초기화
        addProfileButton = view.findViewById(R.id.add_profile_button);
        profileImageView = view.findViewById(R.id.basic_profile);
        signupNextButton = view.findViewById(R.id.btnNext);
        skipButton = view.findViewById(R.id.btnSkip);

        // 프로필 추가 버튼 클릭 리스너
        addProfileButton.setOnClickListener(v -> openImagePicker());

        // 다음 버튼 클릭 리스너
        signupNextButton.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                // 프로필 사진 업로드
                uploadProfileImage(navController);
            } else {
                Toast.makeText(requireContext(), "프로필을 선택하지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 건너뛰기 버튼 클릭 리스너
        skipButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "프로필 추가가 건너뛰어졌습니다.", Toast.LENGTH_SHORT).show();
            navController.navigate(R.id.actionSkipToSignup4);
        });
    }

    // 갤러리에서 이미지 선택을 위한 인텐트 실행
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "프로필 이미지를 선택하세요"), PICK_IMAGE_REQUEST);
    }

    // 선택한 이미지 처리
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                // 이미지 URI를 Bitmap으로 변환하여 ImageView에 표시
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImageUri);
                profileImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "이미지를 로드할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Firebase Storage에 프로필 사진 업로드
    private void uploadProfileImage(NavController navController) {
        if (selectedImageUri != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageRef = storage.getReference().child("profilePictures/" + UUID.randomUUID().toString());

            storageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Firestore에 사진 URL 저장
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("photoUrl", uri.toString());

                        db.collection("users").document(userId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(requireContext(), "프로필 사진 저장 성공!", Toast.LENGTH_SHORT).show();
                                    // 다음 Fragment로 이동
                                    navController.navigate(R.id.actionNextToSignup4);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), "Firestore 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "사진 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}