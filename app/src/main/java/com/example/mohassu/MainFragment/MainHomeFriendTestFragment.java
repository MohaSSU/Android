package com.example.mohassu.MainFragment;

import static com.naver.maps.map.CameraUpdate.REASON_GESTURE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.mohassu.CheckProfileAndTimeTableFragment.EmptyBottomSheetProfile;
import com.example.mohassu.Constants.Constants;
import com.example.mohassu.Model.PlaceInfo;
import com.example.mohassu.R;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
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

public class MainHomeFriendTestFragment extends Fragment implements OnMapReadyCallback {

    private NaverMap naverMap;
    private FusedLocationSource locationSource;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private Marker locationMarker; // Marker 객체 선언
    private boolean isCameraMovedByUser = false;
    private boolean isMarkerClicked = false; // 마커 클릭 상태 추적 변수

    ImageButton notificationButton;
    ImageButton promiseListButton;
    ImageButton signupNextButton;
    ImageButton createPromiseButton;
    ImageButton myPageButton;
    ImageButton myLocationButton;
    TextView tvBuildingName;
    boolean focusMode = false;

    private GeofencingClient geofencingClient;

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
        //NavController navController = Navigation.findNavController(view);

        // 다음 프레그먼트를 클릭 시 다음 Fragment로 이동
        // 알림 페이지 이동
        notificationButton = view.findViewById(R.id.btnNotification);
        notificationButton.setFocusable(false);
        notificationButton.setOnClickListener(v -> {
            //navController.navigate(R.id.actionNotification);
        });
        // 약속 리스트 페이지 이동
        promiseListButton = view.findViewById(R.id.btnPromiseList);
        promiseListButton.setFocusable(false);
        promiseListButton.setOnClickListener(v -> {
            //navController.navigate(R.id.actionPromiseList);
        });
        // 친구 리스트 페이지 이동
        signupNextButton = view.findViewById(R.id.btnFriendList);
        signupNextButton.setFocusable(false);
        signupNextButton.setOnClickListener(v -> {
            //navController.navigate(R.id.actionFriendList);
        });
        //약속 추가 페이지 이동
        createPromiseButton = view.findViewById(R.id.btnAddPlan);
        createPromiseButton.setFocusable(false);
        createPromiseButton.setOnClickListener(v -> {
            //navController.navigate(R.id.actionAddPlan);
        });
        // 마이페이지 이동
        myPageButton = view.findViewById(R.id.btnMyPage);
        myPageButton.setFocusable(false);
        myPageButton.setOnClickListener(v -> {
            //navController.navigate(R.id.actionMyPage);
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

        // 지도 이동 이벤트 설정
        naverMap.addOnCameraChangeListener((reason, animated) -> {
            if (reason == REASON_GESTURE) {
                isCameraMovedByUser = true; // 사용자가 화면을 이동했을 때 플래그 설정
                isMarkerClicked = false; // 사용자가 화면을 이동하면 마커 클릭 상태 해제
                if (focusMode) {
                    resetMarkerFocusMode();
                }
                FrameLayout mapContainer = requireActivity().findViewById(R.id.fragment_map);
                View balloonView = mapContainer.findViewById(R.id.dialog_text_message); // ID로 찾기
                if (balloonView != null) {
                    mapContainer.removeView(balloonView); // 말풍선 제거
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

        // 위치 요청 수락 시 트래킹모드 가동, 거부 시 다시 묻기
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);// 트래킹 모드 설정 후에도 오버레이 비활성화
            naverMap.addOnLocationChangeListener(location -> {
                locationOverlay.setVisible(false); // 계속해서 오버레이를 비활성화
            });
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // Marker 초기화
        initializeLocationMarker();

        // 지도 클릭 이벤트 설정 (말풍선 닫기)
        naverMap.setOnMapClickListener((point, coord) -> {
            if (focusMode) {
                resetMarkerFocusMode();
            }
            resetMarkerFocusMode();
            FrameLayout mapContainer = requireActivity().findViewById(R.id.fragment_map);
            View balloonView = mapContainer.findViewById(R.id.dialog_text_message); // ID로 찾기
            if (balloonView != null) {
                mapContainer.removeView(balloonView); // 말풍선 제거
            }
            View bannerView = mapContainer.findViewById(R.id.fragment_status_banner); // ID로 찾기
            if (bannerView != null) {
                mapContainer.removeView(bannerView); // 말풍선 제거
            }
            View profileButton = mapContainer.findViewById(R.id.dialog_show_profile); // ID로 찾기
            if (profileButton != null) {
                mapContainer.removeView(profileButton); // 말풍선 제거
            }
            isMarkerClicked = false; // 마커 클릭 상태 해제
        });


        // 위치 변화 업데이트
        naverMap.addOnLocationChangeListener(location -> {
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            locationMarker.setPosition(currentLocation);

            // 마커 클릭 상태 또는 초기 화면에서 카메라 이동
            if (isMarkerClicked) {
                CameraUpdate update = CameraUpdate.scrollTo(currentLocation)
                        .animate(CameraAnimation.Easing); // 줌 레벨 17.0
                naverMap.moveCamera(update);
            } else if (!isCameraMovedByUser) {
                CameraUpdate update = CameraUpdate.scrollAndZoomTo(currentLocation, 17.0)
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
                    if (!focusMode) {
                        tvBuildingName.setVisibility(View.VISIBLE);
                    }
                    return; // 반경 내 첫 번째 장소를 찾으면 종료
                }
            }
            tvBuildingName.setVisibility(View.GONE); // 반경 내 장소가 없을 경우
        });
    }

    // Marker 초기화 메서드
    private void initializeLocationMarker() {
        // Marker 객체 생성
        locationMarker = new Marker();
        LatLng defaultPosition = new LatLng(0, 0); // 최초 좌표
        locationMarker.setPosition(defaultPosition);
        locationMarker.setIcon(OverlayImage.fromResource(R.drawable.img_marker_blue)); // 마커 이미지 설정
        locationMarker.setWidth(120); // 마커 크기 조정
        locationMarker.setHeight(140);
        locationMarker.setMap(naverMap); // 지도에 마커 추가

        // 클릭 이벤트 설정
        locationMarker.setOnClickListener(overlay -> {
            if (naverMap == null || getView() == null) {
                // 지도 초기화가 완료되지 않은 경우
                Toast.makeText(requireContext(), "지도가 아직 초기화되지 않았습니다.", Toast.LENGTH_SHORT).show();
                return true; // 이벤트 소비
            }

            //다른 버튼 안 보이게
            showMarkerFocusMode();

            // 현재 위치 가져오기
                LocationOverlay locationOverlay = naverMap.getLocationOverlay();
                LatLng currentLocation = locationOverlay.getPosition(); // 현재 위치 좌표 가져오기
                CameraUpdate update = CameraUpdate.scrollAndZoomTo(currentLocation, 20.0)
                        .animate(CameraAnimation.Easing);// 줌 레벨 17.0
                naverMap.moveCamera(update);
                isMarkerClicked = true;

            FrameLayout mapContainer = requireActivity().findViewById(R.id.fragment_map);

            // 말풍선 View 인플레이트
            View balloonView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_text_message, mapContainer, false);
            //balloonView.setId(R.id.dialog_text_message); // ID 설정
            mapContainer.addView(balloonView); // 말풍선 추가

            // 배너 View 인플레이트
            View bannerView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_status_banner, mapContainer, false);
            //bannerView.setId(R.id.fragment_status_banner);
            mapContainer.addView(bannerView);

            // 프로필버튼 View 인플레이트
            View profileButton = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_show_profile, mapContainer, false);
            //profileButton.setId(R.id.dialog_show_profile);
            mapContainer.addView(profileButton);
            // 클릭 이벤트 설정
            // 프로필 보기 버튼 클릭 이벤트
            profileButton.findViewById(R.id.showProfileButton).setOnClickListener(v -> {
                // BottomSheetDialogFragment 호출
                EmptyBottomSheetProfile bottomSheet = new EmptyBottomSheetProfile();
                bottomSheet.show(getParentFragmentManager(), "ProfileBottomSheet");
            });

            return true; // 클릭 이벤트 소비
        });

    }

    private void showMarkerFocusMode() {
        focusMode = true;
        // 버튼 숨기기
        notificationButton.setVisibility(View.GONE);
        promiseListButton.setVisibility(View.GONE);
        signupNextButton.setVisibility(View.GONE);
        createPromiseButton.setVisibility(View.GONE);
        myPageButton.setVisibility(View.GONE);
        myLocationButton.setVisibility(View.GONE);
        tvBuildingName.setVisibility(View.GONE);
    }


    private void resetMarkerFocusMode() {
        focusMode = false;
        // 버튼 다시 표시
        notificationButton.setVisibility(View.VISIBLE);
        promiseListButton.setVisibility(View.VISIBLE);
        signupNextButton.setVisibility(View.VISIBLE);
        createPromiseButton.setVisibility(View.VISIBLE);
        myPageButton.setVisibility(View.VISIBLE);
        myLocationButton.setVisibility(View.VISIBLE);
        tvBuildingName.setVisibility(View.VISIBLE);
    }
}
