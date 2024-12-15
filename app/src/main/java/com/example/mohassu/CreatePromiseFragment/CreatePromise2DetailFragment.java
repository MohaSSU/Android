package com.example.mohassu.CreatePromiseFragment;

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
import com.example.mohassu.R;

import com.example.mohassu.Adapter.MenuWithIconAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
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

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreatePromise2DetailFragment extends Fragment implements OnMapReadyCallback {

    private View rootView;
    private NaverMap naverMap;
    private Marker marker;
    private PromiseViewModel promiseViewModel;
    int year, month, day, hour, minute;

    // 전역변수 선언
    private GeoPoint geoPoint;


    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        promiseViewModel = new ViewModelProvider(requireActivity()).get(PromiseViewModel.class);

        // 새로운 약속 생성 시 ViewModel 초기화
        if (getArguments() == null || !getArguments().containsKey("promiseId")) {
            resetViewModel();
        }

        // *** 리스너 등록 ***
        Log.d("CreatePromise2Detail", "리스너 등록 시작");
        // *** Fragment가 생성될 때부터 리스너 대기 ***
        getParentFragmentManager().setFragmentResultListener("requestKey", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                Log.d("CreatePromise2Detail", "리스너 작동 - requestKey: " + requestKey);
                promiseViewModel.selectedNicknames = result.getStringArrayList("selectedNicknames");
                promiseViewModel.selectedPhotoUrls = result.getStringArrayList("selectedPhotoUrls");

                // UI 업데이트 추가
                updateFriendListUI(promiseViewModel.selectedNicknames, promiseViewModel.selectedPhotoUrls);

                if (promiseViewModel.selectedNicknames != null && promiseViewModel.selectedPhotoUrls != null) {
                    Log.d("CreatePromise2Detail", "받은 친구 수: " + promiseViewModel.selectedNicknames.size());
                    for (int i = 0; i < promiseViewModel.selectedNicknames.size(); i++) {
                        Log.d("CreatePromise2Detail", "Nickname: " + promiseViewModel.selectedNicknames.get(i));
                        Log.d("CreatePromise2Detail", "Photo URL: " + promiseViewModel.selectedPhotoUrls.get(i));
                    }
                } else {
                    Log.w("CreatePromise2Detail", "받은 데이터가 없습니다.");
                }

                Log.d("CreatePromise2Detail", "리스너 등록 완료");

            }
        });
        // ViewModel 초기화
        promiseViewModel = new ViewModelProvider(requireActivity()).get(PromiseViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_promise2_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.rootView = view;

        // NavController 초기화
        NavController navController = Navigation.findNavController(view);

        // 뒤로가기 버튼에 클릭 리스너 추가
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            navController.navigateUp();
        });

        LinearLayout addFriendsButton = view.findViewById(R.id.btnAddFriends);
        if (addFriendsButton != null) {
            addFriendsButton.setOnClickListener(v -> {
                saveDataToViewModel(); // ViewModel에 데이터 저장 추가
                navController.navigate(R.id.actionNextToCreatePromise3);
            });
        } else {
            Log.e("CreatePromise2Detail", "btnAddFriends is null");
        }

        // 다음 프레그먼트를 클릭 시 다음 Fragment로 이동
        Button saveButton = view.findViewById(R.id.btnSave);
        saveButton.setOnClickListener(v -> {
            saveToFirestore();
            navController.navigate(R.id.actionSavePromise);
        });

        // 새로 지도를 불러오는 코드 (기존과 동일)
        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .add(R.id.fragment_map, mapFragment)
                    .commit();
        }
        mapFragment.getMapAsync(this);

        LinearLayout btnSelectPromiseType = view.findViewById(R.id.btnSelectPromiseType);
        ImageView ivIcon = btnSelectPromiseType.findViewById(R.id.ivIcon);
        TextView tvText = btnSelectPromiseType.findViewById(R.id.tvText);
        LinearLayout dateButton = view.findViewById(R.id.btnSelectPromiseDate);
        LinearLayout timeButton = view.findViewById(R.id.btnSelectPromiseTime);

        // 기본값 설정
        ivIcon.setImageResource(R.drawable.ic_promise_rice);
        tvText.setText("밥약속");

        // 클릭 리스너 설정
        btnSelectPromiseType.setOnClickListener(v -> showPromiseTypeDialog(ivIcon, tvText));

        //datePicker
        dateButton.setOnClickListener(v -> showDatePickerDialog());

        //timePicker
        timeButton.setOnClickListener(v -> showTimePickerDialog());
    }


    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        // +- 줌컨트롤 버튼 비활성화
        naverMap.getUiSettings().setZoomControlEnabled(false);

        // 축척 바 제거 (0___2m 같은 정보)
        naverMap.getUiSettings().setScaleBarEnabled(false);

        marker = new Marker();
        marker.setIcon(OverlayImage.fromResource(R.drawable.ic_promise_marker)); // 마커 이미지 설정
        marker.setWidth(120); // 마커 크기 조정
        marker.setHeight(140);

        loadUserInformationFromBundle();

        // 데이터 복원
        updateUIFromViewModel();
    }

    private void loadUserInformationFromBundle() {

        if (promiseViewModel.latitude != 0 && promiseViewModel.longitude != 0) {
            Log.d("CreatePromise2Detail", "ViewModel에 저장된 위도: " + promiseViewModel.latitude + ", 경도: " + promiseViewModel.longitude);
        } else {
            Bundle args = getArguments();
            if (args != null) {
                double latitude = args.getDouble("latitude", 0);
                double longitude = args.getDouble("longitude", 0);
                Log.d("CreatePromise2Detail", "받은 위도: " + latitude + ", 경도: " + longitude);

                // ViewModel에 위도와 경도 저장
                promiseViewModel.latitude = latitude;
                promiseViewModel.longitude = longitude;

                geoPoint = new GeoPoint(latitude, longitude);
                // 사용자 위치 업데이트
                LatLng location = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                marker.setPosition(location);

                // 카메라 위치 선정
                CameraUpdate update = CameraUpdate.scrollAndZoomTo(location, 20.0)
                        .animate(CameraAnimation.Easing);
                naverMap.moveCamera(update);
            } else {
                Log.e("CreatePromise2Detail", "Bundle에 데이터가 없습니다.");
            }

            marker.setMap(naverMap); // 지도에 마커 추가
        }
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

    private void addParticipantsToFirestore(String promiseId) {
        if (!promiseViewModel.selectedNicknames.isEmpty() && !promiseViewModel.selectedPhotoUrls.isEmpty()) {

            // 사용자 ID를 저장할 리스트 생성 (for 루프 밖으로 이동)
            List<DocumentReference> participantReferences = new ArrayList<>();

            // 모든 Firestore 비동기 작업을 추적하기 위한 Task 리스트
            List<com.google.android.gms.tasks.Task<?>> tasks = new ArrayList<>();

            for (int i = 0; i < promiseViewModel.selectedNicknames.size(); i++) {
                String friendNickname = promiseViewModel.selectedNicknames.get(i);
                String friendPhotoUrl = promiseViewModel.selectedPhotoUrls.get(i);

                // Firestore에서 닉네임에 맞는 사용자 ID 조회
                com.google.android.gms.tasks.Task<?> task = db.collection("users")
                        .whereEqualTo("nickname", friendNickname) // 닉네임으로 해당 사용자 조회
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!querySnapshot.isEmpty()) {
                                // 첫 번째 일치하는 사용자 문서 가져오기
                                String userId = querySnapshot.getDocuments().get(0).getId();

                                // Firestore에 추가할 데이터 생성
                                Map<String, Object> participantData = new HashMap<>();
                                participantData.put("nickname", friendNickname);
                                participantData.put("photoUrl", friendPhotoUrl);

                                // Firestore의 users/{userId}/participants에 데이터 추가
                                db.collection("users")
                                        .document(userId)
                                        .collection("participants")
                                        .add(participantData)
                                        .addOnSuccessListener(participantRef ->
                                                Log.d("Firestore", "참여자 추가됨: " + friendNickname)
                                        )
                                        .addOnFailureListener(e ->
                                                Log.e("Firestore", "참여자 추가 실패: " + friendNickname, e)
                                        );

                                DocumentReference reference = db.collection("users").document(userId);
                                participantReferences.add(reference);

                            } else {
                                Log.e("Firestore", "닉네임에 해당하는 사용자를 찾을 수 없습니다: " + friendNickname);
                            }
                        })
                        .addOnFailureListener(e ->
                                Log.e("Firestore", "사용자 조회 실패: " + friendNickname, e)
                        );

                // Firestore의 비동기 작업을 추적하기 위해 Task를 추가
                tasks.add(task);
            }

            // 모든 Firestore 작업이 완료된 후에 participants 필드에 DocumentReference 추가
            com.google.android.gms.tasks.Tasks.whenAll(tasks)
                    .addOnSuccessListener(aVoid -> {
                        // Firestore의 promises/{promiseId} 문서에 participants 필드를 추가
                        db.collection("promises")
                                .document(promiseId)
                                .update("participants", participantReferences) // 약속 문서의 participants 필드에 DocumentReference 추가
                                .addOnSuccessListener(aVoid2 ->
                                        Log.d("Firestore", "participants 필드가 Firestore에 참조 배열로 추가되었습니다.")
                                )
                                .addOnFailureListener(e ->
                                        Log.e("Firestore", "participants 필드 추가 실패", e)
                                );
                    })
                    .addOnFailureListener(e ->
                            Log.e("Firestore", "작업 전체 실패", e)
                    );
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
        if (promiseViewModel.latitude != 0.0 && promiseViewModel.longitude != 0.0) {
            geoPoint = new GeoPoint(promiseViewModel.latitude, promiseViewModel.longitude);
            LatLng location = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
            if (naverMap != null) {
                marker.setPosition(location);
                marker.setMap(naverMap);

                CameraUpdate update = CameraUpdate.scrollAndZoomTo(location, 20.0).animate(CameraAnimation.Easing);
                naverMap.moveCamera(update);
            } else {
                Log.w("CreatePromise2Detail", "NaverMap is not ready yet.");
            }
        } else {
            Log.w("CreatePromise2Detail", "Latitude or Longitude is not set in ViewModel.");
        }

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

        promiseViewModel.promiseDescription = descriptionEditText.getText().toString();
        promiseViewModel.date = tvDate.getText().toString();
        promiseViewModel.time = tvTime.getText().toString();

        if (geoPoint != null) {
            promiseViewModel.latitude = geoPoint.getLatitude();
            promiseViewModel.longitude = geoPoint.getLongitude();
        } else {
            Log.w("CreatePromise2Detail", "GeoPoint is null, cannot save latitude and longitude to ViewModel.");
        }
    }

}