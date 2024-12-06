package com.example.mohassu.MainFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mohassu.R;
import com.example.mohassu.adapters.PromiseAdapter;
import com.example.mohassu.models.Promise;
import com.naver.maps.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainPromiseListFragment extends Fragment {
    private List<Promise> promiseList = new ArrayList<>();
    private LatLng latLng;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 약속이 있는 경우와 없는 경우의 레이아웃 선택
        if (promiseList.isEmpty()) {
            return inflater.inflate(R.layout.fragment_main_promise_list_with_no_promise, container, false);
        } else {
            return inflater.inflate(R.layout.fragment_main_promise_list, container, false);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // NavController 초기화
        NavController navController = Navigation.findNavController(view);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            navController.navigateUp();
        });

        // 약속 리스트가 있는 경우 RecyclerView 초기화
        if (!promiseList.isEmpty()) {
            RecyclerView recyclerView = view.findViewById(R.id.grid_my_promise_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            //PromiseAdapter adapter = new PromiseAdapter(promiseList);
            //recyclerView.setAdapter(adapter);
        }
    }

    // 더미 데이터 생성 (실제 데이터로 대체 가능)
    private void loadDummyData() {
        promiseList.add(new Promise("원석평 외 2명과의 약속", "정보과학관", "오늘 12:00", latLng));
        promiseList.add(new Promise("정유진 외 1명과의 약속", "학교 정문", "내일 15:00", latLng));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadDummyData(); // 더미 데이터 로드
    }
}
