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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
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
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            navController.navigateUp();
        });

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
                        System.out.println("친구 목록을 불러오는 중 오류 발생");
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

                        friendList.add(new Friend(friendUid, name, nickname, email, statusMessage, photoUrl));
                        friendAdapter.notifyDataSetChanged();
                    } else {
                        System.out.println("친구 프로필을 찾을 수 없습니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println("프로필 불러오기 실패: " + e.getMessage());
                });
    }

    public void showCheckProfileBottomSheet(Friend friend) {
        CheckProfileBottomSheetFragment bottomSheet = CheckProfileBottomSheetFragment.newInstance(friend);
        bottomSheet.show(getParentFragmentManager(), "CheckProfileBottomSheet");
    }
}