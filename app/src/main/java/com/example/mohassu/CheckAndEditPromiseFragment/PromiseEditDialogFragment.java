package com.example.mohassu.CheckAndEditPromiseFragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.mohassu.CreatePromiseFragment.CreatePromise3ChooseFriendsFragment;
import com.example.mohassu.CreatePromiseFragment.PromiseViewModel;
import com.example.mohassu.R;
import com.example.mohassu.Adapter.MenuWithIconAdapter;
import com.example.mohassu.Model.Friend;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PromiseEditDialogFragment extends BottomSheetDialogFragment {

    private String promiseId;
    private View rootView;
    private PromiseViewModel promiseViewModel;
    int year, month, day, hour, minute;

    // 전역변수 선언
    private GeoPoint geoPoint;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ViewModel 초기화
        promiseViewModel = new ViewModelProvider(requireActivity()).get(PromiseViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_promise_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.rootView = view;

        // **promiseId 초기화 (Arguments로부터 가져옴)**
        Bundle arguments = getArguments();
        if (arguments != null) {
            promiseId = arguments.getString("promiseId", null);
            if (promiseId == null) {
                Log.e("PromiseEditDialog", "promiseId is null. Firestore update will fail.");
            }
            else Log.e("PromiseEditDialog", promiseId);
        } else {
            Log.e("PromiseEditDialog", "Arguments are null. Firestore update will fail.");
        }

        loadFromFirestore();

        loadParticipantsFromFirestore();

        // 뒤로가기 버튼에 클릭 리스너 추가
        view.findViewById(R.id.btnBack).setOnClickListener(v -> dismiss());

        LinearLayout addFriendsButton = view.findViewById(R.id.btnAddFriends);
        if (addFriendsButton != null) {
            addFriendsButton.setOnClickListener(v -> {
                saveDataToViewModel(); // ViewModel에 데이터 저장 추가
                dismiss();
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.actionAddFriends);
            });
        } else {
            Log.e("CreatePromise2Detail", "btnAddFriends is null");
        }

        // 다음 프레그먼트를 클릭 시 다음 Fragment로 이동
        Button saveButton = view.findViewById(R.id.btnSave);
        saveButton.setFocusable(false);
        saveButton.setOnClickListener(v -> {
            saveToFirestore();
            dismiss();
        });


        LinearLayout btnSelectPromiseType = view.findViewById(R.id.btnSelectPromiseType);
        ImageView ivIcon = btnSelectPromiseType.findViewById(R.id.ivIcon);
        TextView tvText = btnSelectPromiseType.findViewById(R.id.tvText);
        LinearLayout dateButton = view.findViewById(R.id.btnSelectPromiseDate);
        LinearLayout timeButton = view.findViewById(R.id.btnSelectPromiseTime);

        // 클릭 리스너 설정
        btnSelectPromiseType.setOnClickListener(v -> showPromiseTypeDialog(ivIcon, tvText));

        //datePicker
        dateButton.setOnClickListener(v -> showDatePickerDialog());

        //timePicker
        timeButton.setOnClickListener(v -> showTimePickerDialog());

    }

    private void showPromiseTypeDialog(ImageView ivIcon, TextView tvText) {
        // 약속 종류 데이터
        String[] promiseTypes = {"밥약속", "술약속", "공부약속", "기타"};
        int[] promiseIcons = {
                R.drawable.ic_promise_rice,
                R.drawable.ic_promise_drink,
                R.drawable.ic_promise_study,
                R.drawable.ic_promise_etc
        };

        // 어댑터 설정
        MenuWithIconAdapter adapter = new MenuWithIconAdapter(requireContext(), promiseTypes, promiseIcons);

        // 다이얼로그 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("약속 종류 선택");
        builder.setAdapter(adapter, (dialog, which) -> {
            // 선택된 항목 처리
            ivIcon.setImageResource(promiseIcons[which]);
            ivIcon.setTag(promiseIcons[which]); // 아이콘 리소스를 태그에 저장
            tvText.setText(promiseTypes[which]);

            // ViewModel에 선택한 약속 종류와 아이콘 리소스 저장
            promiseViewModel.promiseType = promiseTypes[which];
            promiseViewModel.promiseIconRes = promiseIcons[which];

            Toast.makeText(requireContext(), promiseTypes[which] + " 선택됨", Toast.LENGTH_SHORT).show();
        });

        // 다이얼로그 표시
        builder.show();
    }

    private void showDatePickerDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_promise_date_picker, null);
        DatePicker datePicker = dialogView.findViewById(R.id.datePicker);
        TextView tvDate = rootView.findViewById(R.id.tvSelectedDate);

        // AlertDialog 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("날짜 선택");
        builder.setView(dialogView);
        builder.setPositiveButton("확인", (dialog, which) -> {
            // 날짜와 시간 가져오기
            year = datePicker.getYear();
            month = datePicker.getMonth();
            day = datePicker.getDayOfMonth();

            // 선택한 날짜와 시간을 표시하거나 저장
            String selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day);
            tvDate.setText(selectedDate);
            tvDate.setTextSize(14);
        });
        builder.setNegativeButton("취소", null);
        builder.show();
    }

    private void showTimePickerDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_promise_time_picker, null);
        TimePicker startTimePicker = dialogView.findViewById(R.id.startTimePicker);
        TextView tvTime = rootView.findViewById(R.id.tvSelectedTime);

        // AlertDialog 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("시간 선택");
        builder.setView(dialogView);
        builder.setPositiveButton("확인", (dialog, which) -> {
            // 날짜와 시간 가져오기
            hour = startTimePicker.getHour();
            minute = startTimePicker.getMinute();

            // 선택한 날짜와 시간을 표시하거나 저장
            String selectedTime = String.format("%02d:%02d", hour, minute);
            tvTime.setText(selectedTime);
            tvTime.setTextSize(14);
        });
        builder.setNegativeButton("취소", null);
        builder.show();
    }

    private void saveToFirestore() {
        // 2에서 입력한 데이터 가져오기
        String description = ((TextView) rootView.findViewById(R.id.promise_description)).getText().toString();
        String promiseTypes = ((TextView) rootView.findViewById(R.id.tvText)).getText().toString();

        String date = ((TextView) rootView.findViewById(R.id.tvSelectedDate)).getText().toString();
        String time = ((TextView) rootView.findViewById(R.id.tvSelectedTime)).getText().toString();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference myId = db.collection("users").document(userId);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

// 특정 날짜와 시간 설정 (예: 2024년 12월 25일 14시 30분 45초)
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0); // 월은 0부터 시작 (12월 = Calendar.DECEMBER)
        Date specificDate = calendar.getTime();

        // 파이어베이스에 저장할 데이터 생성
        Map<String, Object> promiseData = new HashMap<>();
        promiseData.put("location", geoPoint);
        promiseData.put("host", myId);
        promiseData.put("description", description);
        promiseData.put("promiseType", promiseTypes);
        promiseData.put("time", specificDate);
        promiseData.put("splitted_date", date);
        promiseData.put("splitted_time", time);
        //promiseData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("promises")
                .add(promiseData)
                .addOnSuccessListener(documentReference -> {
                    String promiseId = documentReference.getId();
                    Log.d("Firestore", "약속이 성공적으로 추가되었습니다: " + promiseId);
                    addParticipantsToFirestore(promiseId);


                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "약속 추가에 실패했습니다.", e);
                    Toast.makeText(requireContext(), "약속 추가에 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadFromFirestore() {

        db.collection("promises").document(promiseId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Firestore에서 가져온 데이터
                        promiseViewModel.promiseDescription = documentSnapshot.getString("description");
                        promiseViewModel.date = documentSnapshot.getString("splitted_date");
                        promiseViewModel.time = documentSnapshot.getString("splitted_time");

                        Log.d("Firestore", "약속 데이터를 성공적으로 불러왔습니다: " + promiseId);
                    } else {
                        Log.w("Firestore", "해당 약속 문서를 찾을 수 없습니다: " + promiseId);
                    }
                    // 데이터 복원
                    updateUIFromViewModel();
                })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "약속 데이터를 불러오는 중 오류가 발생했습니다.", e);
                            Toast.makeText(requireContext(), "약속 데이터를 불러오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        });
                    }

    private void loadParticipantsFromFirestore() {
        db.collection("promises").document(promiseId).collection("participants")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<String> nicknames = new ArrayList<>();
                    ArrayList<String> photoUrls = new ArrayList<>();

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String nickname = document.getString("nickname");
                        String photoUrl = document.getString("photoUrl");

                        if (nickname != null) {
                            nicknames.add(nickname);
                            photoUrls.add(photoUrl);
                            Log.d("Firestore", "참여자 추가됨 - 닉네임: " + nickname + ", 사진 URL: " + photoUrl);
                        } else {
                            Log.w("Firestore", "닉네임 또는 사진 URL이 누락되었습니다. Document: " + document.getId());
                        }
                    }

                    // 가져온 데이터를 UI에 업데이트
                    updateFriendListUI(nicknames, photoUrls);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "참여자 데이터를 불러오는 중 오류가 발생했습니다.", e);
                    Toast.makeText(requireContext(), "참여자 데이터를 불러오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    private void addParticipantsToFirestore(String promiseId) {
        if (!promiseViewModel.selectedNicknames.isEmpty() && !promiseViewModel.selectedPhotoUrls.isEmpty()) {
            if (promiseViewModel.selectedNicknames.size() != promiseViewModel.selectedPhotoUrls.size()) {
                Log.e("Firestore", "닉네임과 사진 URL의 수가 일치하지 않습니다.");
                return;
            }

            for (int i = 0; i < promiseViewModel.selectedNicknames.size(); i++) {
                String friendNickname = promiseViewModel.selectedNicknames.get(i);
                String friendPhotoUrl = promiseViewModel.selectedPhotoUrls.get(i);

                // Firestore에 추가할 데이터 생성
                Map<String, Object> participantData = new HashMap<>();
                participantData.put("nickname", friendNickname);
                participantData.put("photoUrl", friendPhotoUrl);

                // Firestore에 participants 컬렉션에 추가
                db.collection("promises")
                        .document(promiseId)
                        .collection("participants")
                        .add(participantData)
                        .addOnSuccessListener(participantRef ->
                                Log.d("Firestore", "참여자 추가됨: " + friendNickname)
                        )
                        .addOnFailureListener(e ->
                                Log.e("Firestore", "참여자 추가 실패: " + friendNickname, e)
                        );
            }
        } else {
            Log.w("Firestore", "선택된 친구가 없습니다.");
        }
    }

    private void updateFriendListUI(ArrayList<String> nicknames, ArrayList<String> photoUrls) {
        LinearLayout friendListContainer = rootView.findViewById(R.id.friendListContainer);

        // **visibility를 VISIBLE로 변경**
        friendListContainer.setVisibility(View.VISIBLE);

        // LayoutInflater 준비
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        // 선택한 친구 목록을 기반으로 프로필 동적 추가
        for (int i = 0; i < nicknames.size(); i++) {
            //Log.e("Promise2", );
            View profileView = inflater.inflate(R.layout.view_promise_profile, friendListContainer, false);

            // 프로필 이미지와 이름 설정
            ImageView profileImage = profileView.findViewById(R.id.img_in_promise);
            TextView profileName = profileView.findViewById(R.id.name_in_promise);

            profileName.setText(nicknames.get(i));
            // Glide를 사용해 이미지 로드
            Glide.with(requireContext())
                    .load(photoUrls.get(i))
                    .placeholder(R.drawable.img_basic_profile)
                    .error(R.drawable.img_basic_profile)
                    .into(profileImage);

            // friendListContainer에 추가
            friendListContainer.addView(profileView);
        }

    }

    private void resetViewModel() {
        promiseViewModel.promiseDescription = "";
        promiseViewModel.date = "";
        promiseViewModel.time = "";
        promiseViewModel.promiseType = "";
        promiseViewModel.promiseIconRes = 0;
        promiseViewModel.latitude = 0.0;
        promiseViewModel.longitude = 0.0;
        promiseViewModel.selectedNicknames = new ArrayList<>();
        promiseViewModel.selectedPhotoUrls = new ArrayList<>();
    }


    private void updateUIFromViewModel() {

        EditText descriptionEditText = rootView.findViewById(R.id.promise_description);
        TextView tvDate = rootView.findViewById(R.id.tvSelectedDate);
        TextView tvTime = rootView.findViewById(R.id.tvSelectedTime);
        TextView tvText = rootView.findViewById(R.id.tvText);
        ImageView ivIcon = rootView.findViewById(R.id.ivIcon);


        if (promiseViewModel.promiseDescription != null && !promiseViewModel.promiseDescription.isEmpty()) {
            descriptionEditText.setText(promiseViewModel.promiseDescription);
        } else {
            Log.w("CreatePromise2Detail", "promiseDescription is not set in ViewModel.");
        }

        if (promiseViewModel.date != null && !promiseViewModel.date.isEmpty()) {
            tvDate.setText(promiseViewModel.date);
        } else {
            Log.w("CreatePromise2Detail", "Date is not set in ViewModel.");
        }

        if (promiseViewModel.time != null && !promiseViewModel.time.isEmpty()) {
            tvTime.setText(promiseViewModel.time);
        } else {
            Log.w("CreatePromise2Detail", "Time is not set in ViewModel.");
        }

        if (promiseViewModel.promiseType != null && !promiseViewModel.promiseType.isEmpty()) {
            tvText.setText(promiseViewModel.promiseType);
        } else {
            Log.w("CreatePromise2Detail", "Promise type is not set in ViewModel.");
        }

        if (promiseViewModel.promiseIconRes != 0) {
            ivIcon.setImageResource(promiseViewModel.promiseIconRes);
            ivIcon.setTag(promiseViewModel.promiseIconRes);
        } else {
            Log.w("CreatePromise2Detail", "Promise icon is not set in ViewModel.");
        }
    }

    private void saveDataToViewModel() {
        EditText descriptionEditText = rootView.findViewById(R.id.promise_description);
        TextView tvDate = rootView.findViewById(R.id.tvSelectedDate);
        TextView tvTime = rootView.findViewById(R.id.tvSelectedTime);
        TextView tvText = rootView.findViewById(R.id.tvText);
        ImageView ivIcon = rootView.findViewById(R.id.ivIcon);

        promiseViewModel.promiseDescription = descriptionEditText.getText().toString();
        promiseViewModel.date = tvDate.getText().toString();
        promiseViewModel.time = tvTime.getText().toString();
        promiseViewModel.promiseType = tvText.getText().toString();

        Object iconTag = ivIcon.getTag();
        if (iconTag != null && iconTag instanceof Integer) {
            promiseViewModel.promiseIconRes = (Integer) iconTag;
        } else {
            Log.w("CreatePromise2Detail", "Failed to get icon resource ID from ImageView tag.");
        }
    }

}
