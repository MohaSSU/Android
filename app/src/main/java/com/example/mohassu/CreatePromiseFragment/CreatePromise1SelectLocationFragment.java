package com.example.mohassu.CreatePromiseFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.mohassu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class CreatePromise1SelectLocationFragment extends Fragment implements OnMapReadyCallback {

    private NaverMap naverMap;
    private Marker marker;
    NavController navController;
    private ImageView centerMarkerIcon;
    String documentId;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_promise1_select_location, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // NavController 초기화
        navController = Navigation.findNavController(view);

        // 뒤로가기 버튼에 클릭 리스너 추가
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            navController.navigateUp();
        });

        // 다음 프레그먼트를 클릭 시 다음 Fragment로 이동
        ImageButton nextButton = view.findViewById(R.id.btnNext);
        nextButton.setFocusable(false);
        nextButton.setOnClickListener(v -> {
            sendLocationToNextFragment();
        });

        centerMarkerIcon = view.findViewById(R.id.center_marker_icon);


        // 새로 지도를 불러오는 코드 (기존과 동일)
        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .add(R.id.fragment_map, mapFragment)
                    .commit();
        }
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        // +- 줌컨트롤 버튼 비활성화
        naverMap.getUiSettings().setZoomControlEnabled(false);

        marker = new Marker();

        loadUserInformationFromFirestore();
    }


    private void loadUserInformationFromFirestore() {

        // XML 레이아웃을 Inflate
        View myMarkerView = LayoutInflater.from(requireContext()).inflate(R.layout.view_marker_my, null);
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
                                myProfile.setImageResource(R.drawable.img_basic_profile); // 기본 이미지
                            }

                            // View를 Bitmap으로 변환
                            //Bitmap myMarkerBitmap = convertViewToBitmap(myMarkerView);

                            marker.setIcon(OverlayImage.fromResource(R.drawable.img_marker_red)); // 마커 이미지 설정
                            marker.setWidth(120); // 마커 크기 조정
                            marker.setHeight(140);
                        }
                    });

            db.collection("users")
                    .document(uid)
                    .collection("location")
                    .document("currentLocation")
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            GeoPoint geopoint = documentSnapshot.getGeoPoint("location");
                            if (geopoint != null) {
                                // 사용자 위치 업데이트
                                LatLng location = new LatLng(geopoint.getLatitude(), geopoint.getLongitude());
                                marker.setPosition(location);

                                // 카메라 위치 선정
                                CameraUpdate update = CameraUpdate.scrollAndZoomTo(location, 20.0)
                                        .animate(CameraAnimation.Easing);
                                naverMap.moveCamera(update);
                            }

                            marker.setMap(naverMap); // 지도에 마커 추가
                        }
                    });
        }
    }

    private void sendLocationToNextFragment() {
        if (naverMap != null) {
            // 현재 카메라 중심 좌표 가져오기
            LatLng centerPosition = naverMap.getCameraPosition().target;
            GeoPoint geoPoint = new GeoPoint(centerPosition.latitude, centerPosition.longitude);


            Bundle bundle = new Bundle();
            bundle.putDouble("latitude", geoPoint.getLatitude());
            bundle.putDouble("longitude", geoPoint.getLongitude());

            navController.navigate(R.id.actionNextToCreatePromise2, bundle);

            /*
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("location", geoPoint);
            locationData.put("timestamp", FieldValue.serverTimestamp());

            // 파이어베이스에 저장
            db.collection("promises")
                    .add(locationData)
                    .addOnSuccessListener(documentReference -> {
                        documentId = documentReference.getId();

                        Bundle bundle = new Bundle();
                        bundle.putString("documentId", documentId);
                        navController.navigate(R.id.actionNextToCreatePromise2, bundle);

                        Toast.makeText(requireContext(), "좌표 저장 성공!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "좌표 저장 실패", Toast.LENGTH_SHORT).show();
                        Log.e("Firebase", "Error adding document", e);
                    });

             */
        }
    }
}