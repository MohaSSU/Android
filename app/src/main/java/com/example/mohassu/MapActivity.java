package com.example.mohassu;

import android.content.pm.PackageManager;
import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private NaverMap naverMap;
    private FusedLocationSource locationSource;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_promise_edit);

        // Initialize MapView
        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Initialize LocationSource
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        // Set Location Source
        naverMap.setLocationSource(locationSource);

        // Remove zoom control buttons
        naverMap.getUiSettings().setZoomControlEnabled(false);

        // Create the listener as a variable
        NaverMap.OnLocationChangeListener locationChangeListener = new NaverMap.OnLocationChangeListener() {
            @Override
            public void onLocationChange(@NonNull Location location) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                // Move camera to current location with a higher zoom level
                CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(currentLocation, 17.0); // Zoom level 17
                naverMap.moveCamera(cameraUpdate);

                // Remove listener after initial camera setup
                naverMap.removeOnLocationChangeListener(this);
            }
        };

        // Check and request location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

            // Add the listener
            naverMap.addOnLocationChangeListener(locationChangeListener);

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Customize LocationOverlay
        LocationOverlay locationOverlay = naverMap.getLocationOverlay();
        locationOverlay.setVisible(true); // Make LocationOverlay visible

        // Load the custom marker image
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_marker_red);

        // Scale the bitmap to reduce the size
        int newWidth = 120; // 원하는 너비 (픽셀)
        int newHeight = 140; // 원하는 높이 (픽셀)
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);

        // Set the scaled icon to the LocationOverlay
        locationOverlay.setIcon(OverlayImage.fromBitmap(scaledBitmap));


        // Fix the direction to avoid rotation
        locationOverlay.setBearing(0); // Prevent rotation based on device orientation

        // Listen for location changes
        naverMap.addOnLocationChangeListener(location -> {
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            locationOverlay.setPosition(currentLocation); // Update the position to the current location
            locationOverlay.setBearing(0); // Reapply the fixed bearing
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}