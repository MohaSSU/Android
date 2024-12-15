package com.example.mohassu.MyPageFragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
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

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.mohassu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MyPageProfileEdit2ImageFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageButton editProfileButton;
    private ImageView profileImageView;
    private Uri selectedImageUri; // 선택된 이미지의 URI 저장
    private Bitmap circularBitmapToUpload; // 업로드할 원형 Bitmap을 저장할 변수 추가
    private Button saveButton;

    private FirebaseStorage storage;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static String TAG ="mohassu:mypage_edit_profile";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mypage_profile_edit2, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // NavController 초기화
        NavController navController = Navigation.findNavController(view);

        // 뒤로가기 버튼에 클릭 리스너 추가
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            navController.navigateUp();
        });

        // UI 요소 초기화
        editProfileButton = view.findViewById(R.id.edit_profile_button);
        profileImageView = view.findViewById(R.id.profile_image);
        saveButton = view.findViewById(R.id.btnSave);

        // Firebase 초기화
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        // SharedPreferences 인스턴스 가져오기
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // 저장된 데이터 불러오기
        String photoUrl = sharedPreferences.getString("photoUrl", ""); // 기본값은 빈 문자열

        if (!photoUrl.isEmpty()) {
            Uri photoUri = Uri.parse(photoUrl);
            Glide.with(requireActivity())
                    .load(photoUri)
                    .circleCrop()
                    .placeholder(R.drawable.img_basic_profile) // 로딩 중 표시할 이미지
                    .error(R.drawable.img_basic_profile) // 로딩 실패 시 표시할 이미지
                    .into(profileImageView);
        } else {
            // photoUrl이 비어 있을 때의 처리: 기본 이미지 설정
            profileImageView.setImageResource(R.drawable.img_basic_profile);
        }

        editProfileButton.setOnClickListener(v -> openImagePicker());

        // 마이페이지 메인으로 이동
        saveButton.setFocusable(false);
        saveButton.setOnClickListener(v -> {
            if (circularBitmapToUpload != null) {
                // 프로필 사진 업로드
                uploadCircularImage(circularBitmapToUpload, navController);
            } else {
                Toast.makeText(requireContext(), "프로필을 변경하지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
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
                // 이미지 URI를 Bitmap으로 변환
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImageUri);
                // 원형 Bitmap으로 변환
                Bitmap circularBitmap = getCircularBitmap(bitmap);
                // ImageView에 설정
                profileImageView.setImageBitmap(circularBitmap);
                // Firebase에 업로드를 위한 변수에 저장
                this.circularBitmapToUpload = circularBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "이미지를 로드할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Firebase Storage에 프로필 사진 업로드
    private void uploadCircularImage(Bitmap circularBitmap, NavController navController) {
        if (circularBitmap == null) {
            Toast.makeText(getContext(), "이미지가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageRef = storage.getReference().child("profilePictures/" + UUID.randomUUID().toString());

            // Bitmap을 바이트 배열로 변환
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            circularBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            // 업로드
            UploadTask uploadTask = storageRef.putBytes(data);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Toast.makeText(getContext(), "프로필 사진을 업로드 중 입니다.", Toast.LENGTH_SHORT).show();
                    String photoUrl = uri.toString();
                    // Firestore에 photoUrl 저장
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("photoUrl", photoUrl);

                    db.collection("users").document(userId)
                            .set(updates, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "프로필 사진이 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                                // 로컬 SharedPreferences에 저장
                                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("photoUrl", photoUrl);
                                editor.apply();
                                navController.navigate(R.id.actionSaveProfile);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Firestore 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }).addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "다운로드 URL 가져오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {

                Toast.makeText(getContext(), "이미지 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int min = Math.min(width, height);

        Bitmap output = Bitmap.createBitmap(min, min, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, min, min);

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        // 투명 배경
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));

        // 원 그리기
        canvas.drawCircle(min / 2, min / 2, min / 2, paint);

        // SRC_IN 모드로 설정하여 원 내부만 남기기
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        // 원형 비트맵 그리기
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}

