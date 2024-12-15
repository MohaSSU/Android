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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreatePromise2DetailFragment extends Fragment implements OnMapReadyCallback {

    private View rootView;
    private NaverMap naverMap;
    private Marker marker;
    private PromiseViewModel promiseViewModel;

    // 전역변수 선언
    private GeoPoint geoPoint;
    private ArrayList<String> selectedNicknames = new ArrayList<>();
    private ArrayList<String> selectedPhotoUrls = new ArrayList<>();
    private String promiseType;
    private String date;
    private String time;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // *** 리스너 등록 ***
        Log.d("CreatePromise2Detail", "리스너 등록 시작");
        // *** Fragment가 생성될 때부터 리스너 대기 ***
        getParentFragmentManager().setFragmentResultListener("requestKey", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                Log.d("CreatePromise2Detail", "리스너 작동 - requestKey: " + requestKey);
                promiseViewModel.selectedNicknames = result.getStringArrayList("selectedNicknames");
                promiseViewModel.selectedPhotoUrls = result.getStringArrayList("selectedPhotoUrls");

                if (selectedNicknames != null && selectedPhotoUrls != null) {
                    Log.d("CreatePromise2Detail", "받은 친구 수: " + selectedNicknames.size());
                    for (int i = 0; i < selectedNicknames.size(); i++) {
                        Log.d("CreatePromise2Detail", "Nickname: " + selectedNicknames.get(i));
                        Log.d("CreatePromise2Detail", "Photo URL: " + selectedPhotoUrls.get(i));
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

        // 데이터 복원
        updateUIFromViewModel();

        // 약속 타입 변경 이벤트
        LinearLayout btnSelectPromiseType = view.findViewById(R.id.btnSelectPromiseType);
        ImageView ivIcon = btnSelectPromiseType.findViewById(R.id.ivIcon);
        TextView tvText = btnSelectPromiseType.findViewById(R.id.tvText);

        ivIcon.setImageResource(R.drawable.ic_promise_rice);
        tvText.setText(promiseViewModel.promiseType); // ViewModel에 저장된 값으로 복원

        btnSelectPromiseType.setOnClickListener(v -> showPromiseTypeDialog(ivIcon, tvText));

        // 날짜 선택 이벤트
        LinearLayout dateButton = view.findViewById(R.id.btnSelectPromiseDate);
        dateButton.setOnClickListener(v -> showDatePickerDialog());

        // 시간 선택 이벤트
        LinearLayout timeButton = view.findViewById(R.id.btnSelectPromiseTime);
        timeButton.setOnClickListener(v -> showTimePickerDialog());

       //////////////////////


        // NavController 초기화
        NavController navController = Navigation.findNavController(view);

        // 뒤로가기 버튼에 클릭 리스너 추가
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            navController.navigateUp();
        });

        LinearLayout addFriendsButton = view.findViewById(R.id.btnAddFriends);
        if (addFriendsButton != null) {
            addFriendsButton.setOnClickListener(v -> {
                navController.navigate(R.id.actionNextToCreatePromise3);
            });
        } else {
            Log.e("CreatePromise2Detail", "btnAddFriends is null");
        }

        // 다음 프레그먼트를 클릭 시 다음 Fragment로 이동
        Button saveButton = view.findViewById(R.id.btnSave);
        saveButton.setFocusable(false);
        saveButton.setOnClickListener(v -> {
            saveDataToViewModel();
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

        loadUserInformationFromFirestore();
    }

    private void loadUserInformationFromFirestore() {

        // 전달받은 Bundle에서 documentId 추출
        Bundle args = getArguments();
        if (args != null) {
            double latitude = args.getDouble("latitude", 0);
            double longitude = args.getDouble("longitude", 0);
            geoPoint = new GeoPoint(latitude, longitude);
            // 사용자 위치 업데이트
            LatLng location = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
            marker.setPosition(location);

            // 카메라 위치 선정
            CameraUpdate update = CameraUpdate.scrollAndZoomTo(location, 20.0)
                    .animate(CameraAnimation.Easing);
            naverMap.moveCamera(update);
        }

        marker.setMap(naverMap); // 지도에 마커 추가
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
            tvText.setText(promiseTypes[which]);
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
            int year = datePicker.getYear();
            int month = datePicker.getMonth();
            int day = datePicker.getDayOfMonth();

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
            int hour = startTimePicker.getHour();
            int minute = startTimePicker.getMinute();

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
        promiseType = ((TextView) rootView.findViewById(R.id.tvText)).getText().toString();
        date = ((TextView) rootView.findViewById(R.id.tvSelectedDate)).getText().toString();
        time = ((TextView) rootView.findViewById(R.id.tvSelectedTime)).getText().toString();

        // 파이어베이스에 저장할 데이터 생성
        Map<String, Object> promiseData = new HashMap<>();
        promiseData.put("location", geoPoint);
        promiseData.put("promiseType", promiseType);
        promiseData.put("date", date);
        promiseData.put("time", time);
        promiseData.put("createdAt", FieldValue.serverTimestamp());

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
        if (!selectedNicknames.isEmpty() && !selectedPhotoUrls.isEmpty()) {
            if (selectedNicknames.size() != selectedPhotoUrls.size()) {
                Log.e("Firestore", "닉네임과 사진 URL의 수가 일치하지 않습니다.");
                return;
            }

            for (int i = 0; i < selectedNicknames.size(); i++) {
                String friendNickname = selectedNicknames.get(i);
                String friendPhotoUrl = selectedPhotoUrls.get(i);

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

        // 기존 View 초기화 (중복 추가 방지)
        friendListContainer.removeAllViews();

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

        // 추가 버튼 추가
        LinearLayout addFriendsButton = rootView.findViewById(R.id.btnAddFriends);
        friendListContainer.addView(addFriendsButton);
    }

    private void loadDataFromSharedPreferences() {
        Log.d("CreatePromise2Detail", "SharedPreference 시작");
        // SharedPreferences에서 데이터 가져오기
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("PromiseData", Context.MODE_PRIVATE);

        String nicknamesCsv = sharedPreferences.getString("selectedNicknames", "");
        String photoUrlsCsv = sharedPreferences.getString("selectedPhotoUrls", "");

        // CSV 문자열을 다시 ArrayList로 변환
        if (!nicknamesCsv.isEmpty()) {
            selectedNicknames = new ArrayList<>(List.of(nicknamesCsv.split(",")));
        }
        if (!photoUrlsCsv.isEmpty()) {
            selectedPhotoUrls = new ArrayList<>(List.of(photoUrlsCsv.split(",")));
        }

        Log.d("CreatePromise2Detail", "SharedPreferences에서 불러온 닉네임 수: " + selectedNicknames.size());
        for (int i = 0; i < selectedNicknames.size(); i++) {
            Log.d("CreatePromise2Detail", "Nickname: " + selectedNicknames.get(i));
            Log.d("CreatePromise2Detail", "Photo URL: " + selectedPhotoUrls.get(i));
        }

    }

    private void updateUIFromViewModel() {
        TextView tvDate = rootView.findViewById(R.id.tvSelectedDate);
        TextView tvTime = rootView.findViewById(R.id.tvSelectedTime);

        if (!promiseViewModel.date.isEmpty()) {
            tvDate.setText(promiseViewModel.date);
        }
        if (!promiseViewModel.time.isEmpty()) {
            tvTime.setText(promiseViewModel.time);
        }
    }

    private void saveDataToViewModel() {
        promiseViewModel.date = ((TextView) rootView.findViewById(R.id.tvSelectedDate)).getText().toString();
        promiseViewModel.time = ((TextView) rootView.findViewById(R.id.tvSelectedTime)).getText().toString();
        promiseViewModel.promiseType = ((TextView) rootView.findViewById(R.id.tvText)).getText().toString();
    }

}