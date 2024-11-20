package com.example.mohassu;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.mohassu.databinding.ActivityBinding;

public class Activity extends AppCompatActivity {

    private NavController navController;
    private ActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initBottomNavigation();
    }

    private void initBottomNavigation() {
        // 네비게이션 호스트 프래그먼트를 찾아 NavController를 가져온다.
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

    }
}
