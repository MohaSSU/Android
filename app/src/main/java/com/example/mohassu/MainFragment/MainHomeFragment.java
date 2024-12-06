package com.example.mohassu.MainFragment;

import static com.naver.maps.map.CameraUpdate.REASON_GESTURE;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.mohassu.Constants;
import com.example.mohassu.PlaceInfo;
import com.example.mohassu.R;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

public class MainHomeFragment extends Fragment implements OnMapReadyCallback {

    private NaverMap naverMap;
    private FusedLocationSource locationSource;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private boolean isCameraMovedByUser = false;
    private boolean isMyMarkerClicked = false; // 마커 클릭 상태 추적 변수
    private boolean isFriendMarkerClicked = false;
    private boolean isFocusMode = false;
    private boolean isEditTextClicked = false;

    ImageButton notificationButton;
    ImageButton promiseListButton;
    ImageButton signupNextButton;
    ImageButton createPromiseButton;
    ImageButton myPageButton;
    ImageButton myLocationButton;
    TextView tvBuildingName;

    Marker locationMarker;

    private GeofencingClient geofencingClient;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();

    // ActivityResultLauncher for permission requests
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    if (naverMap != null) {
                        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
                    }
                } else {
                    Toast.makeText(requireContext(), "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize MapFragment
        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getChildFragmentManager().beginTransaction().add(R.id.fragment_map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        // Initialize LocationSource
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        geofencingClient = LocationServices.getGeofencingClient(requireContext());

        // Custom button to center on current location
        myLocationButton = view.findViewById(R.id.btnNowLocation);
        if (myLocationButton != null) {
            myLocationButton.setOnClickListener(v -> {
                isCameraMovedByUser = false; // 자동 중심 이동 다시 활성화
                LatLng currentPosition = naverMap.getLocationOverlay().getPosition();
                if (currentPosition != null) {
                    // Move camera to the current position
                    naverMap.moveCamera(CameraUpdate.scrollTo(currentPosition));
                } else { // 예외처리 생략 가능
                    Toast.makeText(requireContext(), "현재 위치를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // NavController 초기화
        NavController navController = Navigation.findNavController(view);

        // 다음 프레그먼트를 클릭 시 다음 Fragment로 이동
        // 알림 페이지 이동
        notificationButton = view.findViewById(R.id.btnNotification);
        notificationButton.setFocusable(false);
        notificationButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionNotification);
        });
        // 약속 리스트 페이지 이동
        promiseListButton = view.findViewById(R.id.btnPromiseList);
        promiseListButton.setFocusable(false);
        promiseListButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionPromiseList);
        });
        // 친구 리스트 페이지 이동
        signupNextButton = view.findViewById(R.id.btnFriendList);
        signupNextButton.setFocusable(false);
        signupNextButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionFriendList);
        });
        //약속 추가 페이지 이동
        createPromiseButton = view.findViewById(R.id.btnAddPlan);
        createPromiseButton.setFocusable(false);
        createPromiseButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionAddPlan);
        });
        // 마이페이지 이동
        myPageButton = view.findViewById(R.id.btnMyPage);
        myPageButton.setFocusable(false);
        myPageButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionMyPage);
        });
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        // 초기 좌표를 보이지 않는 위치로 설정 (예: 바다 위의 좌표)
        CameraUpdate initialUpdate = CameraUpdate.scrollTo(new LatLng(0, 0));
        naverMap.moveCamera(initialUpdate);

        // 위치 정보 가져오기
        naverMap.setLocationSource(locationSource);

        // +- 줌컨트롤 버튼 비활성화
        naverMap.getUiSettings().setZoomControlEnabled(false);

        // 오버레이 위치 아이콘 비활성화
        LocationOverlay locationOverlay = naverMap.getLocationOverlay();
        locationOverlay.setVisible(false);


        // 위치 요청 수락 시 트래킹모드 가동, 거부 시 다시 묻기
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);// 트래킹 모드 설정 후에도 오버레이 비활성화
            naverMap.addOnLocationChangeListener(location -> {
                locationOverlay.setVisible(false); // 계속해서 오버레이를 비활성화
            });
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // 지도 이동 이벤트 설정
        naverMap.addOnCameraChangeListener((reason, animated) -> {
            if (reason == REASON_GESTURE) {
                isCameraMovedByUser = true; // 사용자가 화면을 이동했을 때 플래그 설정
                isMyMarkerClicked = false; // 사용자가 화면을 이동하면 마커 클릭 상태 해제
                isFriendMarkerClicked = false;

                if (isFocusMode) {
                    resetMarkerFocusMode();
                }
                FrameLayout mapContainer = requireActivity().findViewById(R.id.fragment_map);
                View myBalloonView = mapContainer.findViewById(R.id.dialog_edit_message); // ID로 찾기
                if (myBalloonView != null) {
                    mapContainer.removeView(myBalloonView); // 말풍선 제거
                }
                View friendBalloonView = mapContainer.findViewById(R.id.dialog_text_message); // ID로 찾기
                if (friendBalloonView != null) {
                    mapContainer.removeView(friendBalloonView); // 말풍선 제거
                }
                View bannerView = mapContainer.findViewById(R.id.fragment_status_banner); // ID로 찾기
                if (bannerView != null) {
                    mapContainer.removeView(bannerView); // 말풍선 제거
                }
                View profileButton = mapContainer.findViewById(R.id.dialog_show_profile); // ID로 찾기
                if (profileButton != null) {
                    mapContainer.removeView(profileButton); // 말풍선 제거
                }
            }
        });


        // 내 위치 Marker 초기화
        loadMyMarker();

        // Firestore에서 친구 마커 로드
        loadFriendMarkers();

        // 지도 클릭 이벤트 설정 (말풍선 닫기)
        naverMap.setOnMapClickListener((point, coord) -> {
            if (!isEditTextClicked) {
                isMyMarkerClicked = false; // 사용자가 화면을 클릭하면 마커 클릭 상태 해제
                isFriendMarkerClicked = false;
                if (isFocusMode) {
                    resetMarkerFocusMode();
                }
                FrameLayout mapContainer = requireActivity().findViewById(R.id.fragment_map);
                View myBalloonView = mapContainer.findViewById(R.id.dialog_edit_message); // ID로 찾기
                if (myBalloonView != null) {
                    mapContainer.removeView(myBalloonView); // 말풍선 제거
                }
                View friendBalloonView = mapContainer.findViewById(R.id.dialog_text_message); // ID로 찾기
                if (friendBalloonView != null) {
                    mapContainer.removeView(myBalloonView); // 말풍선 제거
                }
                View bannerView = mapContainer.findViewById(R.id.fragment_status_banner); // ID로 찾기
                if (bannerView != null) {
                    mapContainer.removeView(bannerView); // 말풍선 제거
                }
                View profileButton = mapContainer.findViewById(R.id.dialog_show_profile); // ID로 찾기
                if (profileButton != null) {
                    mapContainer.removeView(profileButton); // 말풍선 제거
                }
            }
        });


        // 위치 변화 업데이트
        naverMap.addOnLocationChangeListener(location -> {
            if (locationMarker == null) {
                // locationMarker가 초기화되지 않았다면 초기화가 완료되길 기다립니다.
                return;
            }
            LatLng currentNewLocation = new LatLng(location.getLatitude(), location.getLongitude());
            locationMarker.setPosition(currentNewLocation);

            // 마커 클릭 상태 또는 초기 화면에서 카메라 이동
            if (isMyMarkerClicked) {
                CameraUpdate update = CameraUpdate.scrollTo(currentNewLocation)
                        .animate(CameraAnimation.Easing); // 줌 레벨 17.0
                naverMap.moveCamera(update);
            } else if (!isCameraMovedByUser && !isFriendMarkerClicked) {
                CameraUpdate update = CameraUpdate.scrollAndZoomTo(currentNewLocation, 17.0)
                        .animate(CameraAnimation.Easing);
                naverMap.moveCamera(update);
            }


            View view = getView();
            tvBuildingName = view.findViewById(R.id.tvBuildingName);

            for (PlaceInfo place : Constants.PLACES) {
                float[] results = new float[1];
                Location.distanceBetween(
                        location.getLatitude(), location.getLongitude(),
                        place.getLocation().latitude, place.getLocation().longitude,
                        results
                );

                if (results[0] <= place.getRadius()) {
                    String buildingName = place.getName();
                    tvBuildingName.setText(buildingName + "에 있어요.");
                    if (!isFocusMode) {
                        tvBuildingName.setVisibility(View.VISIBLE);
                    }
                    return; // 반경 내 첫 번째 장소를 찾으면 종료
                }
            }
            tvBuildingName.setVisibility(View.GONE); // 반경 내 장소가 없을 경우
        });
    }

    private void showMarkerFocusMode() {
        // 버튼 숨기기
        isFocusMode = true;
        notificationButton.setVisibility(View.GONE);
        promiseListButton.setVisibility(View.GONE);
        signupNextButton.setVisibility(View.GONE);
        createPromiseButton.setVisibility(View.GONE);
        myPageButton.setVisibility(View.GONE);
        myLocationButton.setVisibility(View.GONE);
        tvBuildingName.setVisibility(View.GONE);
    }


    private void resetMarkerFocusMode() {
        // 버튼 다시 표시
        isFocusMode = false;
        notificationButton.setVisibility(View.VISIBLE);
        promiseListButton.setVisibility(View.VISIBLE);
        signupNextButton.setVisibility(View.VISIBLE);
        createPromiseButton.setVisibility(View.VISIBLE);
        myPageButton.setVisibility(View.VISIBLE);
        myLocationButton.setVisibility(View.VISIBLE);
        tvBuildingName.setVisibility(View.VISIBLE);
    }

    private void loadMyMarker() {

        // XML 레이아웃을 Inflate
        View myMarkerView = LayoutInflater.from(requireContext()).inflate(R.layout.my_marker, null);
        ImageView myProfile = myMarkerView.findViewById(R.id.my_marker_image);

        if (currentUser != null) {
            String uid = currentUser.getUid();

            // Firestore에서 사용자 데이터를 가져옴
            db.collection("users").document(currentUser.getUid()) // 예: 사용자 ID를 문서 ID로 사용
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String photoUrl = documentSnapshot.getString("photoUrl");

                            if (photoUrl != null) {
                                // Glide를 사용하여 이미지 로드
                                Glide.with(this)
                                        .load(photoUrl)
                                        .placeholder(R.drawable.img_default)
                                        .error(R.drawable.img_default)
                                        .into(myProfile);

                            } else {
                                myProfile.setImageResource(R.drawable.pic_basic_profile); // 기본 이미지
                            }
                            // View를 Bitmap으로 변환
                            //Bitmap myMarkerBitmap = convertViewToBitmap(myMarkerView);
// Marker 객체 생성
                            locationMarker = new Marker();

                            // 마커 초기화
                            LatLng defaultPosition = new LatLng(0, 0); // 최초 좌표
                            locationMarker.setPosition(defaultPosition);
                            locationMarker.setIcon(OverlayImage.fromResource(R.drawable.img_marker_red)); // 마커 이미지 설정
                            locationMarker.setWidth(120); // 마커 크기 조정
                            locationMarker.setHeight(140);
                            locationMarker.setMap(naverMap); // 지도에 마커 추가

                            locationMarker.setOnClickListener(overlay -> {

                                if (naverMap == null) { // 테스트 필요
                                    Toast.makeText(requireContext(), "지도를 불러오는 중입니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show();
                                    return true;
                                }

                                //다른 버튼 안 보이게
                                showMarkerFocusMode();

                                // 현재 위치 가져오기
                                LocationOverlay locationOverlay = naverMap.getLocationOverlay();
                                LatLng currentNewLocation = locationOverlay.getPosition(); // 현재 위치 좌표 가져오기
                                CameraUpdate update = CameraUpdate.scrollAndZoomTo(currentNewLocation, 20.0)
                                        .animate(CameraAnimation.Easing);
                                naverMap.moveCamera(update);
                                isMyMarkerClicked = true;
                                isFriendMarkerClicked = false;


                                FrameLayout mapContainer = requireActivity().findViewById(R.id.fragment_map);

                                // 말풍선 View 인플레이트
                                View myBalloonView = LayoutInflater.from(requireContext())
                                        .inflate(R.layout.dialog_edit_message, mapContainer, false);
                                mapContainer.addView(myBalloonView); // 말풍선 추가

                                // EditText 참조 가져오기
                                EditText markerMessageEditText = myBalloonView.findViewById(R.id.markerMyMessage);

                                db.collection("users")
                                        .document(currentUser.getUid()) // 사용자 ID
                                        .get()
                                        .addOnSuccessListener(statusMsg -> {
                                            if (statusMsg.exists()) {
                                                // 상태 메시지가 이미 저장되어 있으면 EditText에 띄우기
                                                String storedMessage = statusMsg.getString("statusMessage");
                                                if (storedMessage != null && !storedMessage.isEmpty()) {
                                                    markerMessageEditText.setText(storedMessage); // 기존 메시지 띄우기
                                                }
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.w("TAG", "Error getting document", e);
                                        });

                                markerMessageEditText.setOnEditorActionListener((v, actionId, event) -> {
                                    isEditTextClicked= true;
                                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                                        String statusMessage = markerMessageEditText.getText().toString().trim();

                                        if (!statusMessage.isEmpty()) {
                                            db.collection("users")
                                                    .document(currentUser.getUid()) // 사용자 ID
                                                    .update("statusMessage", statusMessage)
                                                    .addOnSuccessListener(aVoid -> {
                                                        // 저장 성공 시 처리 (예: 메시지 표시)
                                                        Toast.makeText(requireContext(), "상태 메시지가 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                                                    });
                                        }


                                        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                        if (imm != null) {
                                            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                                            markerMessageEditText.clearFocus(); // 포커스 해제하여 깜빡임 끄기
                                        }
                                        return true;
                                    }
                                    return false;
                                });
                                return true; // 클릭 이벤트 소비
                            });
                        }
                    });
        }
    }

    // Firestore에서 친구 데이터 가져오기
    private void loadFriendMarkers() {

        // XML 레이아웃을 Inflate
        View friendMarkerView = LayoutInflater.from(requireContext()).inflate(R.layout.your_marker, null);
        ImageView friendProfile = friendMarkerView.findViewById(R.id.your_marker_image);

        if (currentUser != null) {
            String uid = currentUser.getUid();

            db.collection("users").document(uid)
                    .collection("friends")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (querySnapshot.isEmpty()) {
                            // 친구 데이터가 없을 경우 처리
                            Toast.makeText(requireContext(), "친구를 추가해보세요!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String name = document.getString("name");
                            String nickname = document.getString("nickname");
                            String class_name = document.getString("class_name");
                            String place = document.getString("place");
                            String startTime = document.getString("startTime");
                            String endTime = document.getString("endTime");
                            String photoUrl = document.getString("photoUrl");
                            GeoPoint location = document.getGeoPoint("location");
                            // 마커 클릭 시 친구 ID 전달
                            String friendId = document.getId();

                            if (photoUrl != null) {
                                // Glide를 사용하여 이미지 로드
                                Glide.with(this)
                                        .load(photoUrl)
                                        .placeholder(R.drawable.img_default)
                                        .error(R.drawable.img_default)
                                        .into(friendProfile);
                            } else {
                                friendProfile.setImageResource(R.drawable.pic_basic_profile); // 기본 이미지
                            }

                            // View를 Bitmap으로 변환
                            Bitmap friendMarkerBitmap = convertViewToBitmap(friendMarkerView);

                            // 친구 위치를 기반으로 마커 추가

                            LatLng friendLocation = new LatLng(location.getLatitude(), location.getLongitude());

                            Marker friendMarker = new Marker();
                            friendMarker.setPosition(friendLocation);
                            friendMarker.setIcon(OverlayImage.fromBitmap(friendMarkerBitmap)); // 마커 이미지
                            friendMarker.setWidth(120);
                            friendMarker.setHeight(140);
                            friendMarker.setMap(naverMap);

                            // 마커 클릭 이벤트
                            friendMarker.setOnClickListener(overlay -> { // 아직 테스트
                                // 클릭 이벤트 설정
                                if (naverMap == null || getView() == null) {
                                    // 지도 초기화가 완료되지 않은 경우
                                    Toast.makeText(requireContext(), "지도가 아직 초기화되지 않았습니다.", Toast.LENGTH_SHORT).show();
                                    return true; // 이벤트 소비
                                }

                                //다른 버튼 안 보이게
                                showMarkerFocusMode();

                                //친구 위치로 카메라 업데이트
                                CameraUpdate update = CameraUpdate.scrollAndZoomTo(friendLocation, 20.0)
                                        .animate(CameraAnimation.Easing);
                                naverMap.moveCamera(update);
                                isFriendMarkerClicked = true;
                                isMyMarkerClicked = false;

                                FrameLayout mapContainer = requireActivity().findViewById(R.id.fragment_map);

                                // 말풍선 View 인플레이트
                                View friendBalloonView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_text_message, mapContainer, false);
                                mapContainer.addView(friendBalloonView); // 말풍선 추가

                                // 배너 View 인플레이트
                                View bannerView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_status_banner, mapContainer, false);
                                mapContainer.addView(bannerView);

                                // UI 업데이트
                                updateStatusBanner(place, class_name, startTime, endTime);

                                // 프로필버튼 View 인플레이트
                                View profileButton = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_show_profile, mapContainer, false);
                                mapContainer.addView(profileButton);
                                // 클릭 이벤트 설정
                                // 프로필 보기 버튼 클릭 이벤트
                                profileButton.findViewById(R.id.showProfileButton).setOnClickListener(v -> {
                                    // BottomSheetDialogFragment 호출
                                    EmptyBottomSheetProfile bottomSheet = EmptyBottomSheetProfile.newInstance(friendId);
                                    bottomSheet.show(getParentFragmentManager(), "ProfileBottomSheet");
                                });

                                return true; // 클릭 이벤트 소비
                            });
                        }
                    });
        }
    }

    // 상태 배너 업데이트 메서드
    private void updateStatusBanner(String place, String class_name, String startTime, String endTime) {
        View view = getView();
        if (view == null) return;

        TextView placeInfo = view.findViewById(R.id.placeInfo);
        TextView classInfo = view.findViewById(R.id.classInfo);
        TextView stTimeInfo = view.findViewById(R.id.startTimeInfo);
        TextView endTimeInfo = view.findViewById(R.id.endTimeInfo);

        // Firestore 데이터로 텍스트 업데이트
        placeInfo.setText(place != null ? place : "#PLACE");
        classInfo.setText(class_name != null ? class_name : "#CLASS");
        stTimeInfo.setText(startTime != null ? startTime : "#st_time");
        endTimeInfo.setText(endTime != null ? endTime : "#end_time");
    }

    private Bitmap convertViewToBitmap(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

}