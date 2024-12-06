package com.example.mohassu.MainFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mohassu.Adapter.PromiseAdapter;
import com.example.mohassu.Model.Promise;
import com.example.mohassu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainPromiseListFragment extends Fragment {

    private RecyclerView myPromiseRecyclerView;
    private RecyclerView friendPromiseRecyclerView;
    private PromiseAdapter myPromiseAdapter;
    private PromiseAdapter friendPromiseAdapter;
    private List<Promise> myPromiseList = new ArrayList<>();
    private List<Promise> friendPromiseList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_promise_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // NavController 초기화
        NavController navController = Navigation.findNavController(view);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            navController.navigateUp();
        });

        // 약속 RecyclerView 초기화
        myPromiseRecyclerView = view.findViewById(R.id.recycler_my_promise_list);
        myPromiseRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        myPromiseAdapter = new PromiseAdapter(requireContext(), myPromiseList);
        myPromiseRecyclerView.setAdapter(myPromiseAdapter);

        friendPromiseRecyclerView = view.findViewById(R.id.recycler_friend_promise_list);
        friendPromiseRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        friendPromiseAdapter = new PromiseAdapter(requireContext(), friendPromiseList);
        friendPromiseRecyclerView.setAdapter(friendPromiseAdapter);

        // Firebase에서 약속 데이터 가져오기
        fetchPromises();
    }

    private void fetchPromises() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 내 약속 가져오기
        db.collection("promises")
                .whereEqualTo("creatorId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        myPromiseList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            myPromiseList.add(createPromiseFromDocument(document));
                        }
                        myPromiseAdapter.notifyDataSetChanged();
                    } else {
                        // 에러 처리
                    }
                });

        // 친구가 만든 약속 가져오기
        db.collection("promises")
                .whereArrayContains("participantIds", currentUserId)
                .whereNotEqualTo("creatorId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        friendPromiseList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            friendPromiseList.add(createPromiseFromDocument(document));
                        }
                        friendPromiseAdapter.notifyDataSetChanged();
                    } else {
                        // 에러 처리
                    }
                });
    }

    private Promise createPromiseFromDocument(DocumentSnapshot document) {
        String creatorId = document.getString("creatorId");
        String creatorPhotoUrl = document.getString("creatorPhotoUrl");
        List<String> participantIds = (List<String>) document.get("participantIds");
        String location = document.getString("location");
        String dateTime = document.getString("dateTime");
        double latitude = document.getDouble("latitude");
        double longitude = document.getDouble("longitude");

        return new Promise(
                new com.naver.maps.geometry.LatLng(latitude, longitude),
                creatorId,
                creatorPhotoUrl,
                participantIds,
                location,
                dateTime
        );
    }
}
