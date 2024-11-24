package com.example.mohassu;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.mohassu.databinding.ActivityNavigationStartLoginAndSignupBinding;
public class StartLoginAndSignupActivity extends AppCompatActivity {

    private NavController navController;
    private ActivityNavigationStartLoginAndSignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNavigationStartLoginAndSignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar를 ActionBar로 설정
        setSupportActionBar(binding.toolbar);

        // NavController 가져오기
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // NavigationUI를 사용하여 ActionBar와 NavController 연결
        if (navController != null) {
            NavigationUI.setupActionBarWithNavController(this, navController);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
    }
}
