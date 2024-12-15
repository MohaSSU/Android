package com.example.mohassu.CreatePromiseFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.mohassu.R;

public class CreatePromise2DetailFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_promise2_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // NavController 초기화
        NavController navController = Navigation.findNavController(view);

        // 뒤로가기 버튼에 클릭 리스너 추가
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            navController.navigateUp();
        });

        LinearLayout addFriendsButton = view.findViewById(R.id.btnAddFriends);
        if (addFriendsButton != null) {
            addFriendsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navController.navigate(R.id.actionNextToCreatePromise3);
                }
            });
        } else {
            Log.e("CreatePromise2Detail", "btnAddFriends is null");
        }

        // 다음 프레그먼트를 클릭 시 다음 Fragment로 이동
        Button saveButton = view.findViewById(R.id.btnSave);
        saveButton.setFocusable(false);
        saveButton.setOnClickListener(v -> {
            navController.navigate(R.id.actionSavePromise);
        });
    }
}
