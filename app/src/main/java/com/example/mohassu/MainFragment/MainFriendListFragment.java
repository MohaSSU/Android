package com.example.mohassu.MainFragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.mohassu.Adapter.FriendAdapter;
import com.example.mohassu.CheckProfileAndTimeTableFragment.CheckProfileBottomSheetFragment;
import com.example.mohassu.DialogFragment.AddFriendDialogFragment;
import com.example.mohassu.Model.Friend;
import com.example.mohassu.Model.ScheduleClass;
import com.example.mohassu.Model.Time;
import com.example.mohassu.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainFriendListFragment extends Fragment {

    private RecyclerView friendRecyclerView;
    private FriendAdapter friendAdapter;
    private List<Friend> friendList = new ArrayList<>();
    private EditText etSearchFriend;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_friend_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        // 뒤로가기 버튼
        view.findViewById(R.id.btnBack).setOnClickListener(v -> navController.navigateUp());

        // 친구 추가 버튼
        view.findViewById(R.id.add_friend_button).setOnClickListener(v -> {
            AddFriendDialogFragment dialog = new AddFriendDialogFragment();
            dialog.show(getParentFragmentManager(), "AddFriendDialog");
        });

        etSearchFriend = view.findViewById(R.id.etSearchFriend);
        friendRecyclerView = view.findViewById(R.id.friendRecyclerView);
        friendRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        friendAdapter = new FriendAdapter(requireContext(), friendList, this::showCheckProfileBottomSheet);
        friendRecyclerView.setAdapter(friendAdapter);

        fetchFriends();

        etSearchFriend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                friendAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchFriends() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(currentUserId).collection("friends")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        friendList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            String friendUid = document.getId();
                            fetchFriendDetails(friendUid);
                        }
                    } else {
                        Log.e("fetchFriend", "친구 목록을 불러오는 중 오류 발생");
                    }
                });
    }

    private void fetchFriendDetails(String friendUid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(friendUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String nickname = documentSnapshot.getString("nickname");
                        String statusMessage = documentSnapshot.getString("statusMessage");
                        String photoUrl = documentSnapshot.getString("photoUrl");

                        fetchCurrentClass(friendUid, currentClass -> {
                            friendList.add(new Friend(friendUid, name, nickname, email, statusMessage, photoUrl, currentClass));
                            friendAdapter.setData(friendList);
                        });
                    }
                })
                .addOnFailureListener(e -> Log.e("fetchFriendDetails", "프로필 불러오기 실패: " + e.getMessage()));
    }

    private void fetchCurrentClass(String friendUid, CurrentClassCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(friendUid).collection("timeTable")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Calendar calendar = Calendar.getInstance();
                        int today = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int currentMinute = calendar.get(Calendar.MINUTE);

                        ScheduleClass currentClass = null;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            int day = document.getLong("day").intValue();
                            int startHour = document.getLong("startTime.hour").intValue();
                            int startMinute = document.getLong("startTime.minute").intValue();
                            int endHour = document.getLong("endTime.hour").intValue();
                            int endMinute = document.getLong("endTime.minute").intValue();

                            if (today == day &&
                                    (currentHour > startHour || (currentHour == startHour && currentMinute >= startMinute)) &&
                                    (currentHour < endHour || (currentHour == endHour && currentMinute <= endMinute))) {
                                currentClass = new ScheduleClass(
                                        document.getString("classTitle"),
                                        document.getString("classPlace"),
                                        document.getString("professorName"),
                                        day,
                                        new Time(startHour, startMinute),
                                        new Time(endHour, endMinute)
                                );
                                break;
                            }
                        }
                        callback.onClassFetched(currentClass);
                    }
                });
    }

    interface CurrentClassCallback {
        void onClassFetched(ScheduleClass currentClass);
    }

    public void showCheckProfileBottomSheet(Friend friend) {
        CheckProfileBottomSheetFragment bottomSheet = CheckProfileBottomSheetFragment.newInstance(friend);
        bottomSheet.show(getParentFragmentManager(), "CheckProfileBottomSheet");
    }
}