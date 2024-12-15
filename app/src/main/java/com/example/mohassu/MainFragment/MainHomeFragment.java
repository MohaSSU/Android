package com.example.mohassu.MainFragment;

import static com.naver.maps.map.CameraUpdate.REASON_GESTURE;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.mohassu.CheckAndEditPromiseFragment.PromiseEditDialogFragment;
import com.example.mohassu.CheckProfileAndTimeTableFragment.EmptyBottomSheetProfile;
import com.example.mohassu.Constants.Constants;
import com.example.mohassu.Model.PlaceInfo;
import com.example.mohassu.R;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.HashMap;
import java.util.Map;

public class MainHomeFragment extends Fragment implements OnMapReadyCallback {

    private NaverMap naverMap;
    private FusedLocationSource locationSource;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private boolean isCameraMovedByUser = false;
    private boolean isMyMarkerClicked = false; // 마커 클릭 상태 추적 변수
    private boolean isFriendMarkerClicked = false;
    private boolean isFocusMode = false;
    private boolean isEditTextClicked = false;
    private boolean isPlaceFound = false;
    ImageButton notificationButton;
    ImageButton promiseListButton;
    ImageButton signupNextButton;
    ImageButton createPromiseButton;
    ImageButton myPageButton;
    ImageButton myLocationButton;
    TextView tvBuildingName;
    Marker locationMarker;

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

        Toast.makeText(requireContext(), "지도를 불러오는 중입니다... \n화면을 누르지 마십시오", Toast.LENGTH_LONG).show();
        // Initialize MapFragment
        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getChildFragmentManager().beginTransaction().add(R.id.fragment_map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        // Initialize LocationSource
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        GeofencingClient geofencingClient = LocationServices.getGeofencingClient(requireContext());

        // Custom button to center on current location
        myLocationButton = view.findViewById(R.id.btnNowLocation);
        if (myLocationButton != null) {
            myLocationButton.setOnClickListener(v -> {
                isCameraMovedByUser = false; // 자동 중심 이동 다시 활성화
                LatLng currentPosition = naverMap.getLocationOverlay().getPosition();
                if (currentPosition != null) {
                    // Move camera to the current position
                    naverMap.moveCamera(CameraUpdate.scrollTo(currentPosition));
                    Log.d("","");// 추가
                } else { // 예외처리 생략 가능
                    Toast.makeText(requireContext(), "현재 위치를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();

                }
            });
        }

        // NavController 초기화
        NavController navController = Navigation.findNavController(requireView());

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

        // 현재 위치 불러오기 전 초기 화면을 보이지 않는 위치(바다)로 설정
        CameraUpdate initialUpdate = CameraUpdate.scrollTo(new LatLng(0, 0));
        naverMap.moveCamera(initialUpdate);

        // 위치 정보 가져오기
        naverMap.setLocationSource(locationSource);

        // +- 줌컨트롤 버튼 비활성화
        naverMap.getUiSettings().setZoomControlEnabled(false);


        // 위치 요청 수락 시 트래킹모드 가동, 거부 시 다시 묻기
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);// 트래킹 모드 설정 후 나중에 오버레이 비활성화
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // 지도 드래그 이벤트 설정
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
                    mapContainer.removeView(myBalloonView); // 내 말풍선 제거
                }
                View friendBalloonView = mapContainer.findViewById(R.id.dialog_text_message); // ID로 찾기
                if (friendBalloonView != null) {
                    mapContainer.removeView(friendBalloonView); // 친구 말풍선 제거
                }
                View bannerView = mapContainer.findViewById(R.id.fragment_status_banner); // ID로 찾기
                if (bannerView != null) {
                    mapContainer.removeView(bannerView); // 친구 상태 배너 제거
                }
                View profileButton = mapContainer.findViewById(R.id.dialog_show_profile); // ID로 찾기
                if (profileButton != null) {
                    mapContainer.removeView(profileButton); // 친구 프로필 확인 버튼 제거
                }
            }

        });

        // 내 Marker 초기화
        initializeMyMarker();

        // 친구 Marker 초기화 및 위치 갱신
        loadFriendMarkers();

        // 약속 Marker 갱신
        loadPromisesFromFirestore();

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
                // locationMarker가 초기화되지 않았다면 초기화 기다리기
                return;
            }
            updateUserLocationToFirestore(location);
            loadUserLocationFromFirestore();
            naverMap.getLocationOverlay().setVisible(false); // 오버레이 비활성화
        });
    }

    private void initializeMyMarker() {
        CameraUpdate hide = CameraUpdate.scrollAndZoomTo(new LatLng(0,0), 20.0)
                .animate(CameraAnimation.Easing);
        naverMap.moveCamera(hide); // 가끔씩 naverMap 로딩 버그로 인해 위치 업데이트 이후 보이게 설정

        // XML 레이아웃을 Inflate
        View myMarkerView = LayoutInflater.from(requireContext()).inflate(R.layout.view_marker_my, null);
        ImageView myProfile = myMarkerView.findViewById(R.id.my_marker_image);

        // SharedPreferences 인스턴스 가져오기
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // 저장된 데이터 불러오기
        String photoUrl = sharedPreferences.getString("photoUrl", ""); // 기본값은 빈 문자열
        Log.d("mohassu:marker","마커에 쓰일 프로필 url : " + photoUrl);

        if (photoUrl != null && !photoUrl.isEmpty()) {
            // Glide를 사용하여 이미지 로드
            Glide.with(this)
                    .load(Uri.parse(photoUrl)) // 프로필 이미지 URI
                    .circleCrop()
                    .placeholder(R.drawable.img_basic_profile)
                    .error(R.drawable.img_basic_profile)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            myProfile.setImageDrawable(resource);
                            Log.d("mohassu:marker", "마커에 쓰일 프로필 uri 로드 완료: " + Uri.parse(photoUrl));

                            // 이미지가 로드된 후 Bitmap 변환
                            Bitmap myMarkerBitmap = convertViewToBitmap(myMarkerView);
                            if (myMarkerBitmap != null) {
                                // Marker 객체 생성
                                locationMarker = new Marker();
                                locationMarker.setPosition(naverMap.getLocationOverlay().getPosition());
                                locationMarker.setIcon(OverlayImage.fromBitmap(myMarkerBitmap)); // 마커 이미지 설정
                                locationMarker.setWidth(dpToPx(60)); // 마커 크기 조정 (dp를 px로 변환)
                                locationMarker.setHeight(dpToPx(70));
                                locationMarker.setMap(naverMap); // 지도에 마커 추가

                                // Marker 클릭 리스너 설정
                                locationMarker.setOnClickListener(overlay -> {
                                    if (naverMap == null) { // 테스트 필요
                                        Toast.makeText(requireContext(), "지도를 불러오는 중입니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show();
                                        return true;
                                    }

                                    // 다른 버튼 안 보이게
                                    showMarkerFocusMode();

                                    LatLng location = locationMarker.getPosition();

                                    // 현재 위치 가져오기
                                    CameraUpdate update = CameraUpdate.scrollAndZoomTo(location, 20.0)
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
                                                Log.w("mohassu:marker", "Error getting document", e);
                                            });

                                    markerMessageEditText.setOnEditorActionListener((v, actionId, event) -> {
                                        isEditTextClicked = true;
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
                            } else {
                                Log.e("mohassu:marker", "Bitmap 변환 실패.");
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // 필요한 경우 처리
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            myProfile.setImageDrawable(errorDrawable);
                            Log.d("mohassu:marker", "마커에 쓰일 프로필 이미지 로드 실패");

                            // Bitmap 변환 및 마커 생성 (에러 이미지 사용)
                            Bitmap myMarkerBitmap = convertViewToBitmap(myMarkerView);
                            if (myMarkerBitmap != null) {
                                locationMarker = new Marker();
                                locationMarker.setPosition(naverMap.getLocationOverlay().getPosition());
                                locationMarker.setIcon(OverlayImage.fromBitmap(myMarkerBitmap)); // 마커 이미지 설정
                                locationMarker.setWidth(dpToPx(60)); // 마커 크기 조정
                                locationMarker.setHeight(dpToPx(70));
                                locationMarker.setMap(naverMap); // 지도에 마커 추가
                                Log.d("mohassu:marker", "내 프로필 사진 불러오지 못함");
                            } else {
                                Log.e("mohassu:marker", "Bitmap 변환 실패 (onLoadFailed).");
                            }
                        }
                    });
        } else {
            // photoUrl이 비어 있을 때의 처리: 기본 이미지 설정
            myProfile.setImageResource(R.drawable.img_basic_profile);
            Log.d("mohassu:marker", "내 프로필 사진 불러오지 못함");

            // Bitmap 변환 및 마커 생성 (기본 이미지 사용)
            Bitmap myMarkerBitmap = convertViewToBitmap(myMarkerView);
            if (myMarkerBitmap != null) {
                locationMarker = new Marker();
                locationMarker.setPosition(naverMap.getLocationOverlay().getPosition());
                locationMarker.setIcon(OverlayImage.fromBitmap(myMarkerBitmap)); // 마커 이미지 설정
                locationMarker.setWidth(dpToPx(60)); // 마커 크기 조정
                locationMarker.setHeight(dpToPx(70));
                locationMarker.setMap(naverMap); // 지도에 마커 추가

                locationMarker.setOnClickListener(overlay -> {
                    if (naverMap == null) { // 테스트 필요
                        Toast.makeText(requireContext(), "지도를 불러오는 중입니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    // 다른 버튼 안 보이게
                    showMarkerFocusMode();

                    LatLng location = locationMarker.getPosition();

                    // 현재 위치 가져오기
                    CameraUpdate update = CameraUpdate.scrollAndZoomTo(location, 20.0)
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
                                Log.w("mohassu:marker", "Error getting document", e);
                            });

                    markerMessageEditText.setOnEditorActionListener((v, actionId, event) -> {
                        isEditTextClicked = true;
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
            } else {
                Log.e("mohassu:marker", "Bitmap 변환 실패 (기본 이미지).");
            }
        }
    }

    // dp를 px로 변환하는 메서드 추가
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void updateUserLocationToFirestore(Location location) {
        if (currentUser == null) return; // 사용자 인증되지 않은 경우

        String uid = currentUser.getUid(); // 사용자 고유 ID (UID)

        //GeoPoint로 location 저장
        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

        // Firestore에 저장할 데이터
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("location", geoPoint);
        locationData.put("timestamp", FieldValue.serverTimestamp()); // 서버의 타임스탬프 추가

        db.collection("users")
                .document(uid)
                .collection("location")
                .document("currentLocation")
                .set(locationData) // 🔥 Firestore에 데이터 저장
                .addOnSuccessListener(aVoid -> Log.d("TAG", "Location updated in Firestore"))
                .addOnFailureListener(e -> Log.w("TAG", "Failed to update location", e));
    }

    private void loadUserLocationFromFirestore() {
        if (currentUser == null) return; // 사용자 인증되지 않은 경우

        String uid = currentUser.getUid();

        db.collection("users")
                .document(uid)
                .collection("location")
                .document("currentLocation")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.w("TAG", "Listen failed.", error);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        GeoPoint newGeopoint = snapshot.getGeoPoint("location");

                        LatLng newLocation = new LatLng(newGeopoint.getLatitude(), newGeopoint.getLongitude());

                        // 사용자 위치 업데이트
                        locationMarker.setPosition(newLocation);

                        // 마커 클릭 상태 또는 초기 화면에서 카메라 이동
                        if (isMyMarkerClicked) {
                            CameraUpdate update = CameraUpdate.scrollTo(newLocation)
                                    .animate(CameraAnimation.Easing); // 줌 레벨 17.0
                            naverMap.moveCamera(update);
                        } else if (!isCameraMovedByUser && !isFriendMarkerClicked) {
                            CameraUpdate update = CameraUpdate.scrollAndZoomTo(newLocation, 17.0)
                                    .animate(CameraAnimation.Easing);
                            naverMap.moveCamera(update);
                        }


                        View view = getView();
                        //tvBuildingName = view.findViewById(R.id.tvBuildingName);

                        //loadFromGeofencing(newLocation);
                    }
                });
    }


    private void loadFromGeofencing(LatLng location) {
        for (PlaceInfo place : Constants.PLACES) {
            float[] results = new float[1];
            Location.distanceBetween(
                    location.latitude, location.longitude,
                    place.getLocation().latitude, place.getLocation().longitude,
                    results
            );

            if (results[0] <= place.getRadius()) {
                String buildingName = place.getName();
                //tvBuildingName.setText(buildingName + "에 있어요.");
                if (!isFocusMode) {
                    //tvBuildingName.setVisibility(View.VISIBLE);
                }
                db.collection("users")
                        .document(currentUser.getUid())
                        .update("place", buildingName) // Firestore에 장소명 저장
                        .addOnSuccessListener(aVoid -> Log.d("TAG", "Location updated in Firestore"))
                        .addOnFailureListener(e -> Log.w("TAG", "Failed to update location", e));
                break; // 반경 내 첫 번째 장소를 찾으면 종료
            }
        }
        //tvBuildingName.setVisibility(View.GONE); // 반경 내 장소가 없을 경우

        if (!isPlaceFound) {
            db.collection("users")
                    .document(currentUser.getUid())
                    .update("place", "지도 위 장소")
                    .addOnSuccessListener(aVoid -> Log.d("TAG", "No place found, updated to '건물없음'"))
                    .addOnFailureListener(e -> Log.w("TAG", "Failed to update location to '건물없음'", e));
        }
    }

    // Firestore에서 친구 데이터 가져오기
    private void loadFriendMarkers() {

        // XML 레이아웃을 Inflate
        View friendMarkerView = LayoutInflater.from(requireContext()).inflate(R.layout.view_marker_friend, null);
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
                            String statusMessage = document.getString("statusMessage");

                            // 마커 클릭 시 친구 ID 전달
                            String friendId = document.getId();

                            if (location == null) {
                                Log.e("mohassu:marker", "친구의 위치 정보가 없습니다: " + friendId);
                                continue; // 위치 정보가 없는 친구는 건너뜀
                            }

                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                // Glide를 사용하여 이미지 로드
                                Glide.with(this)
                                        .load(Uri.parse(photoUrl)) // 프로필 이미지 URI
                                        .circleCrop()
                                        .placeholder(R.drawable.img_basic_profile)
                                        .error(R.drawable.img_basic_profile)
                                        .into(new CustomTarget<Drawable>() {
                                            @Override
                                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                                friendProfile.setImageDrawable(resource);
                                                Log.d("mohassu:marker", "친구 마커 이미지 로드 완료: " + Uri.parse(photoUrl));

                                                // 이미지가 로드된 후 Bitmap 변환
                                                Bitmap friendMarkerBitmap = convertViewToBitmap(friendMarkerView);
                                                if (friendMarkerBitmap != null) {
                                                    // Marker 객체 생성
                                                    Marker friendMarker = new Marker();
                                                    friendMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                                                    friendMarker.setIcon(OverlayImage.fromBitmap(friendMarkerBitmap)); // 마커 이미지 설정
                                                    friendMarker.setWidth(dpToPx(60)); // 마커 크기 조정 (dp를 px로 변환)
                                                    friendMarker.setHeight(dpToPx(70));
                                                    friendMarker.setMap(naverMap); // 지도에 마커 추가

                                                    // 마커 클릭 이벤트 설정
                                                    friendMarker.setOnClickListener(overlay -> {
                                                        if (naverMap == null || getView() == null) {
                                                            Toast.makeText(requireContext(), "지도가 아직 초기화되지 않았습니다.", Toast.LENGTH_SHORT).show();
                                                            return true; // 이벤트 소비
                                                        }

                                                        // 다른 버튼 안 보이게
                                                        showMarkerFocusMode();

                                                        // 친구 위치로 카메라 업데이트
                                                        CameraUpdate update = CameraUpdate.scrollAndZoomTo(friendMarker.getPosition(), 20.0)
                                                                .animate(CameraAnimation.Easing);
                                                        naverMap.moveCamera(update);
                                                        isFriendMarkerClicked = true;
                                                        isMyMarkerClicked = false;

                                                        FrameLayout mapContainer = requireActivity().findViewById(R.id.fragment_map);

                                                        // 말풍선 View 인플레이트
                                                        View friendBalloonView = LayoutInflater.from(requireContext())
                                                                .inflate(R.layout.dialog_text_message, mapContainer, false);
                                                        TextView friendBalloonText = friendBalloonView.findViewById(R.id.markerFriendMessage);
                                                        friendBalloonText.setText(statusMessage != null ? statusMessage : "상태 메시지 없음");
                                                        mapContainer.addView(friendBalloonView); // 말풍선 추가

                                                        // 배너 View 인플레이트
                                                        View bannerView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_status_banner, mapContainer, false);
                                                        mapContainer.addView(bannerView);

                                                        // UI 업데이트 (구현 필요)
                                                        updateStatusBanner(place, class_name, startTime, endTime);

                                                        // 프로필버튼 View 인플레이트
                                                        View profileButton = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_show_profile, mapContainer, false);
                                                        mapContainer.addView(profileButton);

                                                        // 클릭 이벤트 설정
                                                        // 프로필 보기 버튼 클릭 이벤트
                                                        profileButton.findViewById(R.id.showProfileButton).setOnClickListener(v -> {
                                                            // CheckProfileBottomSheetFragment 호출
                                                            CheckProfileBottomSheetFragment bottomSheet = CheckProfileBottomSheetFragment.newInstanceWithFriendId(friendId);
                                                            bottomSheet.show(getParentFragmentManager(), "CheckProfileBottomSheetFragment");
                                                        });

                                                        return true; // 클릭 이벤트 소비
                                                    });
                                                } else {
                                                    Log.e("mohassu:marker", "Bitmap 변환 실패 for friend: " + friendId);
                                                }
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                                // 필요한 경우 처리
                                            }

                                            @Override
                                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                                super.onLoadFailed(errorDrawable);
                                                friendProfile.setImageDrawable(errorDrawable);
                                                Log.d("mohassu:marker", "친구 마커 이미지 로드 실패: " + friendId);

                                                // Bitmap 변환 및 마커 생성 (에러 이미지 사용)
                                                Bitmap friendMarkerBitmap = convertViewToBitmap(friendMarkerView);
                                                if (friendMarkerBitmap != null) {
                                                    Marker friendMarker = new Marker();
                                                    friendMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                                                    friendMarker.setIcon(OverlayImage.fromBitmap(friendMarkerBitmap)); // 마커 이미지 설정
                                                    friendMarker.setWidth(dpToPx(60)); // 마커 크기 조정
                                                    friendMarker.setHeight(dpToPx(70));
                                                    friendMarker.setMap(naverMap); // 지도에 마커 추가
                                                    Log.d("mohassu:marker", "친구 마커 생성 완료 (에러 이미지 사용): " + friendId);

                                                    // 마커 클릭 이벤트 설정
                                                    friendMarker.setOnClickListener(overlay -> {
                                                        if (naverMap == null || getView() == null) {
                                                            Toast.makeText(requireContext(), "지도가 아직 초기화되지 않았습니다.", Toast.LENGTH_SHORT).show();
                                                            return true; // 이벤트 소비
                                                        }

                                                        // 다른 버튼 안 보이게
                                                        showMarkerFocusMode();

                                                        // 친구 위치로 카메라 업데이트
                                                        CameraUpdate update = CameraUpdate.scrollAndZoomTo(friendMarker.getPosition(), 20.0)
                                                                .animate(CameraAnimation.Easing);
                                                        naverMap.moveCamera(update);
                                                        isFriendMarkerClicked = true;
                                                        isMyMarkerClicked = false;

                                                        FrameLayout mapContainer = requireActivity().findViewById(R.id.fragment_map);

                                                        // 말풍선 View 인플레이트
                                                        View friendBalloonView = LayoutInflater.from(requireContext())
                                                                .inflate(R.layout.dialog_text_message, mapContainer, false);
                                                        TextView friendBalloonText = friendBalloonView.findViewById(R.id.markerFriendMessage);
                                                        friendBalloonText.setText(statusMessage != null ? statusMessage : "상태 메시지 없음");
                                                        mapContainer.addView(friendBalloonView); // 말풍선 추가

                                                        // 배너 View 인플레이트
                                                        View bannerView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_status_banner, mapContainer, false);
                                                        mapContainer.addView(bannerView);

                                                        // UI 업데이트 (구현 필요)
                                                        updateStatusBanner(place, class_name, startTime, endTime);

                                                        // 프로필버튼 View 인플레이트
                                                        View profileButton = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_show_profile, mapContainer, false);
                                                        mapContainer.addView(profileButton);

                                                        // 클릭 이벤트 설정
                                                        // 프로필 보기 버튼 클릭 이벤트
                                                        profileButton.findViewById(R.id.showProfileButton).setOnClickListener(v -> {
                                                            // CheckProfileBottomSheetFragment 호출
                                                            CheckProfileBottomSheetFragment bottomSheet = CheckProfileBottomSheetFragment.newInstanceWithFriendId(friendId);
                                                            bottomSheet.show(getParentFragmentManager(), "CheckProfileBottomSheetFragment");
                                                        });

                                                        return true; // 클릭 이벤트 소비
                                                    });
                                                } else {
                                                    Log.e("mohassu:marker", "Bitmap 변환 실패 (onLoadFailed) for friend: " + friendId);
                                                }
                                            }
                                        });
                            } else {
                                Log.e("mohassu:marker", "Bitmap 변환 실패 for friend: " + friendId);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("mohassu:marker", "친구 데이터 로드 실패", e);
                        Toast.makeText(requireContext(), "친구 데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
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
        //classInfo.setText(class_name != null ? class_name : "#CLASS");
        //stTimeInfo.setText(startTime != null ? startTime : "#st_time");
        //endTimeInfo.setText(endTime != null ? endTime : "#end_time");
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
        //tvBuildingName.setVisibility(View.GONE);
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
        //tvBuildingName.setVisibility(View.VISIBLE);
    }

    // convertViewToBitmap 메서드 (기존과 동일)
    private Bitmap convertViewToBitmap(View view) {
        // dp 단위를 픽셀 단위로 변환하여 설정
        int width = dpToPx(120); // RelativeLayout의 너비
        int height = dpToPx(140); // RelativeLayout의 높이
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }


    private void loadPromisesFromFirestore() {
        db.collection("promises")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        GeoPoint geoPoint = document.getGeoPoint("location");
                        if (geoPoint != null) {
                            double latitude = geoPoint.getLatitude();
                            double longitude = geoPoint.getLongitude();
                            String promiseId = document.getId();
                            addMarkerOnMap(promiseId, latitude, longitude);
                        } else {
                            Log.w("HomeFragment", "GeoPoint가 null입니다. Document ID: " + document.getId());
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("HomeFragment", "Firestore에서 약속 정보를 가져오는 데 실패했습니다.", e));
    }

    private void addMarkerOnMap(String promiseId, double latitude, double longitude) {
        Marker marker = new Marker();
        marker.setPosition(new LatLng(latitude, longitude));
        marker.setIcon(OverlayImage.fromResource(R.drawable.ic_promise_marker)); // 마커 이미지 설정
        marker.setWidth(120); // 마커 크기 조정
        marker.setHeight(140);

        // 마커에 tag로 promiseId 저장
        marker.setTag(promiseId);

        // 마커 클릭 리스너 추가
        marker.setOnClickListener(overlay -> {
            String promiseIdClicked = (String) marker.getTag();
            if (promiseIdClicked != null)
                Log.d("Promise2", promiseIdClicked);
            else
                Log.d("Promise2", "fuc");
            Bundle args = new Bundle();
            args.putString("promiseId", promiseIdClicked);

            PromiseEditDialogFragment bottomSheetFragment = new PromiseEditDialogFragment();
            bottomSheetFragment.setArguments(args);
            bottomSheetFragment.show(getParentFragmentManager(), bottomSheetFragment.getTag());

            return true; // 클릭 이벤트 소비
        });

        marker.setMap(naverMap);


    }

}