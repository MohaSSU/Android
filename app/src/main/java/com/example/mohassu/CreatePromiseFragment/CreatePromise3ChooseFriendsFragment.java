package com.example.mohassu.CreatePromiseFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mohassu.CheckProfileBottomSheetFragment;
import com.example.mohassu.MainFragment.MainFriendListFragment;
import com.example.mohassu.R;
import com.example.mohassu.adapters.selectFriendAdapter;
import com.example.mohassu.models.Friend;
import com.example.mohassu.models.ScheduleClass;
import com.example.mohassu.models.Time;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreatePromise3ChooseFriendsFragment extends Fragment {

    private RecyclerView selectFriendRecyclerView;
    private selectFriendAdapter selectFriendAdapter;
    private List<Friend> friendList = new ArrayList<>();
    ArrayList<String> selectedNicknames = new ArrayList<>();
    ArrayList<String> selectedPhotoUrls = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_promise3_choose_friends, container, false);
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

        // 다음 프레그먼트를 클릭 시 다음 Fragment로 이동
        Button nextButton = view.findViewById(R.id.btnAdd);
        nextButton.setFocusable(false);
        nextButton.setOnClickListener(v -> {
            // 체크된 친구들의 닉네임을 가져옴
            for (Friend friend : friendList) {
                if (friend.isChecked()) {
                    selectedNicknames.add(friend.getNickname()); // 닉네임 추가
                    selectedPhotoUrls.add(friend.getPhotoUrl()); // 이미지 URL 추가
                }
            }
            if (!selectedNicknames.isEmpty()) {
                Log.d("CreatePromise3", "전송하는 닉네임 수: " + selectedNicknames.size());
                for (int i = 0; i < selectedNicknames.size(); i++) {
                    Log.d("CreatePromise3", "Nickname: " + selectedNicknames.get(i));
                    Log.d("CreatePromise3", "Photo URL: " + selectedPhotoUrls.get(i));
                }

                Bundle result = new Bundle();
                result.putStringArrayList("selectedNicknames", selectedNicknames);
                result.putStringArrayList("selectedPhotoUrls", selectedPhotoUrls);
                // **데이터 전송 시 로그 출력**
                Log.d("CreatePromise3", "setFragmentResult 호출 직전 - 데이터 전송 시작");
                getParentFragmentManager().setFragmentResult("requestKey", result);
                // **데이터 전송 후 로그 출력**
                Log.d("CreatePromise3", "setFragmentResult 호출 완료 - 데이터 전송 완료");

                navController.navigate(R.id.actionSaveFriendsToPromise);
            } else {
                Log.w("CreatePromise3", "선택된 친구가 없습니다.");
            }

            /*
            if (!selectedFriends.isEmpty()) {
                // Firestore에 추가할 데이터 생성
                Map<String, Object> promiseData = new HashMap<>();
                promiseData.put("createdAt", FieldValue.serverTimestamp());

                // Firestore에 promise 추가
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("promises")
                        .add(promiseData)
                        .addOnSuccessListener(documentReference -> {
                            Log.d("Firestore", "약속이 성공적으로 추가되었습니다: " + documentReference.getId());
                            String promiseId = documentReference.getId(); // 약속 문서의 ID

                            // 약속 문서의 하위에 participants 컬렉션에 친구 추가
                            for (Friend friend : selectedFriends) {
                                Map<String, Object> participantData = new HashMap<>();
                                participantData.put("nickname", friend.getNickname());

                                db.collection("promises")
                                        .document(promiseId)
                                        .collection("participants")
                                        .add(participantData)
                                        .addOnSuccessListener(participantRef ->
                                                Log.d("Firestore", "참여자 추가됨: " + friend.getNickname())
                                        )
                                        .addOnFailureListener(e ->
                                                Log.e("Firestore", "참여자 추가 실패: " + friend.getNickname(), e)
                                        );
                            }

                            // 약속 추가 성공 시 다른 화면으로 이동
                            navController.navigate(R.id.actionSaveFriendsToPromise);
                        })
                        .addOnFailureListener(e -> Log.e("Firestore", "약속 추가에 실패했습니다.", e));
            } else {
                Log.w("Firestore", "선택된 친구가 없습니다.");
            } */
        });

        // RecyclerView 초기화
        selectFriendRecyclerView = view.findViewById(R.id.selectFriendRecyclerView);
        selectFriendRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 어댑터에 클릭 리스너 추가
        selectFriendAdapter = new selectFriendAdapter(requireContext(), friendList, this::checkFriends);
        selectFriendRecyclerView.setAdapter(selectFriendAdapter);

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
                        Log.e("fetchFriend","친구 목록을 불러오는 중 오류 발생");
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
                            selectFriendAdapter.notifyDataSetChanged();
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

    public void checkFriends(Friend friend) {
        // 선택한 친구의 position 찾기
        int position = friendList.indexOf(friend);

        // RecyclerView에서 ViewHolder 가져오기
        RecyclerView.ViewHolder viewHolder = selectFriendRecyclerView.findViewHolderForAdapterPosition(position);

        if (viewHolder != null) {
            // ViewHolder의 itemView에서 icToken에 접근
            ImageView icToken = viewHolder.itemView.findViewById(R.id.icToken);

            // **기존의 태그를 가져오고, 만약 null이라면 기본값으로 R.drawable.ic_token을 사용**
            boolean currentCheckStatus = friend.isChecked();
            if (currentCheckStatus) {
                // 체크 해제
                icToken.setImageResource(R.drawable.ic_token);
                friend.setChecked(false);
            } else {
                // 체크 상태로 변경
                icToken.setImageResource(R.drawable.ic_checked_token);
                friend.setChecked(true);
            }
        }
    }

}
