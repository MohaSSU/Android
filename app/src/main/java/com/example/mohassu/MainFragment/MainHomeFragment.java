package com.example.mohassu.MainFragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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

import com.example.mohassu.Constants;
import com.example.mohassu.PlaceInfo;
import com.example.mohassu.R;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.naver.maps.geometry.LatLng;
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
    //private FusedLocationSource locationSource;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private Marker currentLocationMarker; // 현재 위치를 표시할 마커
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

        // 위치 아이콘 활성화
        //LocationOverlay locationOverlay = naverMap.getLocationOverlay();
        //locationOverlay.setVisible(true); // Make LocationOverlay visible


        // 첫 번째 위치 업데이트 여부를 판단하는 플래그
        final boolean[] isFirstUpdate = {true};

        // 위치 요청 수락 시 트래킹모드 가동, 거부 시 다시 묻기
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initializeCurrentLocationMarker(); // 현재 위치 마커 초기화
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        //updateLocationOverlay();

        /*
        // 위치 변화 업데이트
        naverMap.addOnLocationChangeListener(location -> {
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            locationOverlay.setPosition(currentLocation);
            locationOverlay.setBearing(0);

            // 처음 위치 갱신 시에만 카메라 이동
            if (isFirstUpdate[0]) {
                CameraUpdate update = CameraUpdate.scrollAndZoomTo(currentLocation, 17.0); // 줌 레벨 17.0
                naverMap.moveCamera(update);
                isFirstUpdate[0] = false; // 플래그 업데이트
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

    private void initializeCurrentLocationMarker() {
        // 현재 위치 마커 초기화
        currentLocationMarker = new Marker();
        LatLng initialPosition = new LatLng(0, 0); // 바다
        currentLocationMarker.setPosition(initialPosition);
        currentLocationMarker.setMap(naverMap);

        // 마커 클릭 이벤트 추가
        currentLocationMarker.setOnClickListener(overlay -> {
            Toast.makeText(requireContext(), "현재 위치 클릭됨!", Toast.LENGTH_SHORT).show();
            return true; // 이벤트 소비
        });

        // 위치 추적 모드 활성화
        //naverMap.setLocationTrackingMode(LocationTrackingMode.None); // LocationOverlay 사용 안 함

        // 위치 업데이트 리스너 등록
        naverMap.addOnLocationChangeListener(location -> updateMarkerLocation(location));
    }

    private void updateMarkerLocation(@NonNull Location location) {
        if (currentLocationMarker != null) {
            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            // 마커 위치 업데이트
            currentLocationMarker.setPosition(currentLatLng);

            // 처음 위치 업데이트 시 지도 카메라 이동
            if (naverMap.getCameraPosition().target.latitude == 0 && naverMap.getCameraPosition().target.longitude == 0) {
                CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(currentLatLng, 15.0); // 줌 레벨 15
                naverMap.moveCamera(cameraUpdate);
            }

            // 마커 아이콘 커스텀
            Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_marker_red);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 120, 140, true);
            currentLocationMarker.setIcon(OverlayImage.fromBitmap(scaledBitmap));
            // 카메라 이동
            naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(currentLatLng, 15.0));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // naverMap이 초기화된 경우 마커 업데이트
        if (naverMap != null) {
            if (currentLocationMarker != null) {
                LatLng markerPosition = currentLocationMarker.getPosition();
                if (markerPosition != null) {
                    naverMap.moveCamera(CameraUpdate.scrollTo(markerPosition));
                }
            }
        }
    }

}
