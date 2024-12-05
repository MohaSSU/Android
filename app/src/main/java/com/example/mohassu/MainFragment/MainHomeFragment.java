package com.example.mohassu.MainFragment;

import static com.naver.maps.map.CameraUpdate.REASON_GESTURE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.mohassu.Constants;
import com.example.mohassu.PlaceInfo;
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
import com.naver.maps.map.Projection;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

public class MainHomeFragment extends Fragment implements OnMapReadyCallback {

    private NaverMap naverMap;
    //private FusedLocationSource locationSource;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private Marker locationMarker; // Marker 객체 선언
    private boolean isCameraMovedByUser = false;
    private boolean isMarkerClicked = false; // 마커 클릭 상태 추적 변수

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
        //locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        geofencingClient = LocationServices.getGeofencingClient(requireContext());

        // Custom button to center on current location
        ImageButton myLocationButton = view.findViewById(R.id.btnNowLocation);
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
        ImageButton notificationButton = view.findViewById(R.id.btnNotification);
        notificationButton.setFocusable(false);
        notificationButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionNotification);
        });
        // 약속 리스트 페이지 이동
        ImageButton promiseListButton = view.findViewById(R.id.btnPromiseList);
        promiseListButton.setFocusable(false);
        promiseListButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionPromiseList);
        });
        // 친구 리스트 페이지 이동
        ImageButton signupNextButton = view.findViewById(R.id.btnFriendList);
        signupNextButton.setFocusable(false);
        signupNextButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionFriendList);
        });
        //약속 추가 페이지 이동
        ImageButton createPromiseButton = view.findViewById(R.id.btnAddPlan);
        createPromiseButton.setFocusable(false);
        createPromiseButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionAddPlan);
        });
        // 마이페이지 이동
        ImageButton myPageButton = view.findViewById(R.id.btnMyPage);
        myPageButton.setFocusable(false);
        myPageButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionMyPage);
        });
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        // 초기 좌표를 보이지 않는 위치로 설정 (예: 바다 위의 좌표)
        LatLng defaultPosition = new LatLng(0, 0); // 서울시청
        naverMap.moveCamera(CameraUpdate.scrollTo(defaultPosition));

        // 위치 정보 가져오기
        //naverMap.setLocationSource(locationSource);

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
            FrameLayout mapContainer = requireActivity().findViewById(R.id.fragment_map);
            View balloonView = mapContainer.findViewById(R.id.dialog_edit_message); // ID로 찾기
            if (balloonView != null) {
                mapContainer.removeView(balloonView); // 말풍선 제거
            }
            isMarkerClicked = false; // 마커 클릭 상태 해제
        });


        /*
        // 위치 변화 업데이트
        naverMap.addOnLocationChangeListener(location -> {
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            locationMarker.setPosition(currentLocation);
            //locationOverlay.setBearing(0);

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
            TextView tvBuildingName = view.findViewById(R.id.tvBuildingName);

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
                    tvBuildingName.setVisibility(View.VISIBLE);
                    return; // 반경 내 첫 번째 장소를 찾으면 종료
                }
            }
            tvBuildingName.setVisibility(View.GONE); // 반경 내 장소가 없을 경우
        });
         */
    }

    // Marker 초기화 메서드
    private void initializeLocationMarker() {
        // Marker 객체 생성
        locationMarker = new Marker();
        LatLng defaultPosition = new LatLng(0, 0); // 최초 좌표
        locationMarker.setPosition(defaultPosition);
        locationMarker.setIcon(OverlayImage.fromResource(R.drawable.img_marker_red)); // 마커 이미지 설정
        locationMarker.setWidth(120); // 마커 크기 조정
        locationMarker.setHeight(140);
        locationMarker.setMap(naverMap); // 지도에 마커 추가

        // 클릭 이벤트 설정
        locationMarker.setOnClickListener(overlay -> {

            // 현재 위치 가져오기
            LocationOverlay locationOverlay = naverMap.getLocationOverlay();
            LatLng currentLocation = locationOverlay.getPosition(); // 현재 위치 좌표 가져오기
            CameraUpdate update = CameraUpdate.scrollTo(currentLocation)
                    .animate(CameraAnimation.Easing);// 줌 레벨 17.0
            naverMap.moveCamera(update);
            isMarkerClicked = true;



            FrameLayout mapContainer = requireActivity().findViewById(R.id.fragment_map);
            // 기존 말풍선 제거
            View existingBalloon = mapContainer.findViewById(R.id.dialog_edit_message);
            if (existingBalloon != null) {
                mapContainer.removeView(existingBalloon);
            }

            // 말풍선 View 인플레이트
            View balloonView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_message, mapContainer, false);
            balloonView.setId(R.id.dialog_edit_message); // ID 설정
            mapContainer.addView(balloonView); // 말풍선 추가

            // 말풍선 위치 조정
            // Marker의 화면 좌표 계산
            Projection projection = naverMap.getProjection();
            PointF markerPositionF = projection.toScreenLocation(currentLocation);
            Point markerPosition = new Point((int) markerPositionF.x, (int) markerPositionF.y); // Point로 변환

            // 말풍선 View의 레이아웃 설정
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) balloonView.getLayoutParams();
            params.leftMargin = markerPosition.x - (balloonView.getWidth() / 2); // 말풍선 중앙 정렬
            params.topMargin = markerPosition.y - balloonView.getHeight() - 100; // 마커 위에 배치
            balloonView.setLayoutParams(params);

            // 지도 이동/줌 변경 시 말풍선 위치 업데이트
            naverMap.addOnCameraIdleListener(() -> {
                PointF updatedPositionF = projection.toScreenLocation(currentLocation);
                Point updatedPosition = new Point((int) updatedPositionF.x, (int) updatedPositionF.y);

                balloonView.post(() -> {
                    params.leftMargin = updatedPosition.x - (balloonView.getWidth() / 2);
                    params.topMargin = updatedPosition.y - balloonView.getHeight() - 100;
                    balloonView.setLayoutParams(params);
                });
            });

            return true; // 클릭 이벤트 소비
        });

    }
}
