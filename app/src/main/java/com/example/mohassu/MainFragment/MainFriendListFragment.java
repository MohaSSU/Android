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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mohassu.CheckProfileBottomSheetFragment;
import com.example.mohassu.DialogFragment.AddFriendDialogFragment;
import com.example.mohassu.R;
import com.example.mohassu.adapters.FriendAdapter;
import com.example.mohassu.models.Friend;
import com.example.mohassu.models.ScheduleClass;
import com.example.mohassu.models.Time;
import android.util.Log;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_friend_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // NavController 초기화
        NavController navController = Navigation.findNavController(view);

        // 뒤로가기 버튼에 클릭 리스너 추가
        view.findViewById(R.id.btnBack).setOnClickListener(v -> navController.navigateUp());

        // 친구 추가 버튼 클릭 리스너
        view.findViewById(R.id.add_friend_button).setOnClickListener(v -> {
            AddFriendDialogFragment dialog = new AddFriendDialogFragment();
            dialog.show(getParentFragmentManager(), "AddFriendDialog");
        });

        // RecyclerView 초기화
        friendRecyclerView = view.findViewById(R.id.friendRecyclerView);
        friendRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 어댑터에 클릭 리스너 추가
        friendAdapter = new FriendAdapter(requireContext(), friendList, this::showCheckProfileBottomSheet);
        friendRecyclerView.setAdapter(friendAdapter);

        // Firestore에서 친구 데이터 가져오기
        fetchFriends();
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
                        Log.e("fetchFriend","친구 몫록을 불러오는 중 오류 발생");
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
                            // Friend 객체 생성 및 추가
                            friendList.add(new Friend(friendUid, name, nickname, email, statusMessage, photoUrl, currentClass));
                            friendAdapter.notifyDataSetChanged();
                            Log.d("fetchFriendDetails", "Friend added: " + name);
                        });
                    } else {
                        Log.e("fetchFriendDetails", "친구 프로필을 찾을 수 없습니다.");
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

                        // 현재 요일 (0 = 일요일, 1 = 월요일, ...)
                        int today = calendar.get(Calendar.DAY_OF_WEEK) - 1;

//                        int today = 0;
                        // 현재 시간
                        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int currentMinute = calendar.get(Calendar.MINUTE);

                        System.out.println("Current Time: " + currentHour + "시 " + currentMinute + "분");
//
//                        int currentHour = 9;
//                        int currentMinute = 45;

                        // 현재 진행 중인 수업 탐색
                        ScheduleClass currentClass = null;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            int day = document.getLong("day").intValue();
                            int startHour = document.getLong("startTime.hour").intValue();
                            int startMinute = document.getLong("startTime.minute").intValue();
                            int endHour = document.getLong("endTime.hour").intValue();
                            int endMinute = document.getLong("endTime.minute").intValue();

                            // 현재 시간 확인
                            if (today == day &&
                                    (currentHour > startHour || (currentHour == startHour && currentMinute >= startMinute)) &&
                                    (currentHour < endHour || (currentHour == endHour && currentMinute <= endMinute))) {
                                // Class 객체 생성
                                currentClass = new ScheduleClass(
                                        document.getString("classTitle"),
                                        document.getString("classPlace"),
                                        document.getString("professorName"),
                                        day,
                                        new Time(startHour, startMinute),
                                        new Time(endHour, endMinute)

                                );
                                Log.d("fetchCurrentClass", "현재 진행 중인 수업: " + currentClass.getClassTitle());
                                break;
                            }

                            else Log.d("fetchCurrentClass", "현재 진행 중인 수업이 없습니다.");
                        }

                        // 결과 콜백
                        callback.onClassFetched(currentClass);
                    } else {
                        Log.e("fetchCurrentClass", "시간표 데이터를 불러오지 못했습니다: " + task.getException().getMessage());
                        callback.onClassFetched(null);
                    }
                });
    }

    /**
     * 현재 수업 정보를 가져오기 위한 콜백 인터페이스
     */
    interface CurrentClassCallback {
        void onClassFetched(ScheduleClass currentClass);
    }

    public void showCheckProfileBottomSheet(Friend friend) {
        CheckProfileBottomSheetFragment bottomSheet = CheckProfileBottomSheetFragment.newInstance(friend);
        bottomSheet.show(getParentFragmentManager(), "CheckProfileBottomSheet");
    }
}