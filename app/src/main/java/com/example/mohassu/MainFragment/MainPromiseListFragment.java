package com.example.mohassu.MainFragment;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mohassu.Adapter.PromiseAdapter;
import com.example.mohassu.CheckAndEditPromiseFragment.PromiseCheckFragment;
import com.example.mohassu.CheckAndEditPromiseFragment.PromiseEditFragment;
import com.example.mohassu.Model.Promise;
import com.example.mohassu.R;
import com.example.mohassu.ViewModel.UserProfileViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainPromiseListFragment extends Fragment implements PromiseAdapter.OnEditClickListener {

    private static final String TAG = "mohassu:promise";

    private RecyclerView myPromiseRecyclerView;
    private RecyclerView friendPromiseRecyclerView;
    private PromiseAdapter myPromiseAdapter;
    private PromiseAdapter friendPromiseAdapter;
    private List<Promise> myPromiseList = new ArrayList<>();
    private List<Promise> friendPromiseList = new ArrayList<>();

    private View rootView;

    private FirebaseFirestore db;
    private String currentUserId;

    // UserProfileViewModel 추가
    private UserProfileViewModel userProfileViewModel;

    // 콜백 인터페이스 정의
    public interface OnFetchOtherHostsCompleteListener {
        void onFetchComplete();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 기본 레이아웃 인플레이션
        rootView = inflater.inflate(R.layout.fragment_main_promise_list, container, false);
        myPromiseRecyclerView = rootView.findViewById(R.id.recycler_my_promise_list);
        friendPromiseRecyclerView = rootView.findViewById(R.id.recycler_friend_promise_list);

        // RecyclerView 설정
        myPromiseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        myPromiseAdapter = new PromiseAdapter(
                getContext(),
                myPromiseList,
                promise -> {
                    // OnEditClickListener 구현 (생략)
                },
                promise -> {
                    // OnItemClickListener 구현
                    openPromiseCheckFragment(promise);
                },
                currentUserId
        );
        myPromiseRecyclerView.setAdapter(myPromiseAdapter);

        // RecyclerView 설정
        friendPromiseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        friendPromiseAdapter = new PromiseAdapter(
                getContext(),
                friendPromiseList,
                promise -> {
                    // OnEditClickListener 구현 하지 않음
                },
                promise -> {
                    // OnItemClickListener 구현
                    openPromiseCheckFragment(promise);
                },
                currentUserId
        );
        friendPromiseRecyclerView.setAdapter(friendPromiseAdapter);

        return rootView;
    }

    /**
     * PromiseCheckFragment 열기
     *
     * @param promise 클릭된 Promise 객체
     */
    private void openPromiseCheckFragment(Promise promise) {
        // Promise 데이터를 Bundle에 담기
        Bundle bundle = new Bundle();
        bundle.putString("PROMISE_ID", promise.getId());
        bundle.putString("PROMISE_TITLE", promise.getDescription());
        // 필요한 다른 데이터들도 추가

        // PromiseCheckFragment 인스턴스 생성 및 인자 전달
        PromiseCheckFragment promiseCheckFragment = new PromiseCheckFragment();
        promiseCheckFragment.setArguments(bundle);

        // FragmentTransaction을 통해 프래그먼트 교체
        FragmentManager fragmentManager = getParentFragmentManager(); // 또는 getActivity().getSupportFragmentManager()
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, promiseCheckFragment ,"PromiseCheckFragment") // R.id.fragment_container는 프래그먼트를 담을 컨테이너의 ID
                .addToBackStack(null) // 뒤로가기 스택에 추가
                .commit();

        Log.d(TAG, "Opened PromiseCheckFragment for Promise ID: " + promise.getId());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        //임시
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        currentUserId = currentUser.getUid();
        Log.d(TAG, "Current User ID: " + currentUserId);

        if (currentUserId == null) {
            Log.e(TAG, "Current User ID is null. Cannot proceed.");
            Toast.makeText(getContext(), "사용자 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
            // 필요에 따라 로그인 화면으로 이동
            return;
        }

        // UserProfileViewModel 초기화 및 현재 사용자의 프로필 데이터 관찰
        userProfileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        observeUserProfile();

        NavController navController = Navigation.findNavController(view);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> navController.navigateUp());

        fetchMyPromises();
    }

    /**
     * UserProfileViewModel을 통해 현재 사용자의 프로필 정보를 관찰하고 업데이트합니다.
     */
    private void observeUserProfile() {
        userProfileViewModel.getNickname().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String nickname) {
                Log.d(TAG, "User Nickname Changed: " + nickname);
                // 현재 사용자의 닉네임을 필요에 따라 처리
            }
        });

        userProfileViewModel.getPhotoUri().observe(getViewLifecycleOwner(), new Observer<Uri>() {
            @Override
            public void onChanged(Uri uri) {
                Log.d(TAG, "User Photo URI Changed: " + uri);
                // 현재 사용자의 프로필 이미지 URI를 필요에 따라 처리
            }
        });

        // 현재 사용자의 프로필 정보를 Firestore에서 가져와 ViewModel에 설정
        fetchCurrentUserProfile();
    }

    /**
     * 현재 사용자의 프로필 정보를 Firestore에서 가져와 UserProfileViewModel에 설정합니다.
     */
    private void fetchCurrentUserProfile() {
        DocumentReference currentUserRef = db.collection("users").document(currentUserId);
        currentUserRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String nickname = documentSnapshot.getString("nickname");
                String photoUrl = documentSnapshot.getString("photoUrl");
                Log.d(TAG, "Fetched User Profile - Nickname: " + nickname + ", Photo URL: " + photoUrl);
                userProfileViewModel.setNickname(nickname);
                userProfileViewModel.setPhotoUri(photoUrl != null ? Uri.parse(photoUrl) : null);
            } else {
                Log.d(TAG, "User profile does not exist.");
                Toast.makeText(getContext(), "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to fetch user profile.", e);
            Toast.makeText(getContext(), "프로필 정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Firestore에서 호스트인 약속과 참가자인 약속을 조회하고, 호스트 데이터를 설정한 후 RecyclerView를 설정합니다.
     */
    private void fetchMyPromises() {
        rootView.findViewById(R.id.promise_list_container).setVisibility(View.GONE); // Optionally manage visibility without progressBar
        Log.d(TAG, "Fetching promises...");

        CollectionReference promiseRef = db.collection("promises");

        // 쿼리 1: host가 현재 사용자
        Query hostQuery = promiseRef.whereEqualTo("host", db.collection("users").document(currentUserId));

        // 쿼리 2: participants에 현재 사용자 포함
        Query participantQuery = promiseRef.whereArrayContains("participants", db.collection("users").document(currentUserId));

        // 호스트인 약속 조회
        hostQuery.get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                List<Promise> hostPromises = new ArrayList<>();
                Log.d(TAG, "Host Query Success. Documents fetched: " + task1.getResult().size());

                for (DocumentSnapshot document : task1.getResult()) {
                    Promise promise = parsePromise(document);
                    if (promise != null) {
                        // 현재 사용자의 프로필 정보를 ViewModel에서 가져와 설정
                        promise.setHostNickname(userProfileViewModel.getNickname().getValue());
                        Uri photoUri = userProfileViewModel.getPhotoUri().getValue();
                        promise.setHostProfileImageUrl(photoUri != null ? photoUri.toString() : null);
                        hostPromises.add(promise);
                        Log.d(TAG, "Host Promise Added: " + promise.getId());
                    }
                }

                // 참가자인 약속 조회
                participantQuery.get().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        List<Promise> participantPromises = new ArrayList<>();
                        Set<DocumentReference> hostRefs = new HashSet<>();
                        Log.d(TAG, "Participant Query Success. Documents fetched: " + task2.getResult().size());

                        for (DocumentSnapshot document : task2.getResult()) {
                            DocumentReference hostRef = document.getDocumentReference("host");
                            if (hostRef == null) {
                                Log.w(TAG, "Promise ID: " + document.getId() + " has null host reference.");
                                continue; // hostRef가 null인 경우 건너뜁니다.
                            }
                            // 호스트가 현재 사용자인 경우 중복 방지
                            if (!hostRef.getId().equals(currentUserId)) {
                                Promise promise = parsePromise(document);
                                if (promise != null) {
                                    participantPromises.add(promise);
                                    hostRefs.add(hostRef);
                                    Log.d(TAG, "Participant Promise Added: " + promise.getId() + ", Host ID: " + hostRef.getId());
                                }
                            }
                        }

                        // 다른 호스트들의 프로필 데이터를 가져와 Promise 객체에 설정
                        fetchOtherHostsPromises(hostRefs, participantPromises, new OnFetchOtherHostsCompleteListener() {
                            @Override
                            public void onFetchComplete() {
                                Log.d(TAG, "Completed fetching other hosts' data.");
                                // 호스트인 약속과 참가자인 약속을 RecyclerView에 표시
                                displayPromises(hostPromises, participantPromises);
                            }
                        });

                    } else {
                        // 참가자 쿼리 실패 처리
                        Log.e(TAG, "Participant Query Failed.", task2.getException());
                        Toast.makeText(getContext(), "데이터를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        showNoPromises();
                    }
                });

            } else {
                // 호스트 쿼리 실패 처리
                Log.e(TAG, "Host Query Failed.", task1.getException());
                Toast.makeText(getContext(), "데이터를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                showNoPromises();
            }
        });
    }

    /**
     * 다른 호스트들의 프로필 데이터를 Firestore에서 조회하여 Promise 객체에 설정합니다.
     *
     * @param hostRefs           다른 호스트들의 DocumentReference 집합
     * @param participantPromises 참가자인 약속 리스트
     * @param listener           모든 호스트 데이터 조회가 완료된 후 호출될 콜백
     */
    private void fetchOtherHostsPromises(Set<DocumentReference> hostRefs, List<Promise> participantPromises, OnFetchOtherHostsCompleteListener listener) {
        if (hostRefs.isEmpty()) {
            Log.d(TAG, "No other hosts to fetch.");
            listener.onFetchComplete();
            return;
        }

        List<DocumentReference> hostRefList = new ArrayList<>(hostRefs);
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

        for (DocumentReference hostRef : hostRefList) {
            if (hostRef != null) {
                tasks.add(hostRef.get());
                Log.d(TAG, "Added task for host ID: " + hostRef.getId());
            } else {
                Log.w(TAG, "Encountered null DocumentReference in hostRefs.");
            }
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
            @Override
            public void onSuccess(List<Object> results) {
                Log.d(TAG, "All host tasks succeeded. Processing results.");
                for (Object result : results) {
                    if (result instanceof DocumentSnapshot) {
                        DocumentSnapshot userDoc = (DocumentSnapshot) result;
                        if (userDoc.exists()) {
                            String nickname = userDoc.getString("nickname");
                            String profileImageUrl = userDoc.getString("profileImageUrl");
                            String userId = userDoc.getId();
                            Log.d(TAG, "Fetched Host Data - ID: " + userId + ", Nickname: " + nickname + ", Profile URL: " + profileImageUrl);

                            for (Promise promise : participantPromises) {
                                if (promise.getHost() != null && promise.getHost().getId().equals(userId)) {
                                    promise.setHostNickname(nickname);
                                    promise.setHostProfileImageUrl(profileImageUrl);
                                    Log.d(TAG, "Updated Promise: " + promise.getId() + " with Host Nickname: " + nickname);
                                }
                            }
                        } else {
                            Log.d(TAG, "Host document does not exist.");
                        }
                    }
                }
                // 모든 데이터 로드가 완료된 후 콜백 호출
                listener.onFetchComplete();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to fetch other hosts' data.", e);
                Toast.makeText(getContext(), "호스트 데이터를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                showNoPromises();
                listener.onFetchComplete();
            }
        });
    }

    /**
     * Promise 리스트를 RecyclerView에 표시
     *
     * @param hostPromises        호스트인 약속 리스트
     * @param participantPromises 참가자인 약속 리스트
     */
    private void displayPromises(List<Promise> hostPromises, List<Promise> participantPromises) {
        Log.d(TAG, "Displaying Promises - Hosts: " + hostPromises.size() + ", Participants: " + participantPromises.size());
        if (!hostPromises.isEmpty() || !participantPromises.isEmpty()) {
            // 약속 리스트 컨테이너 표시
            rootView.findViewById(R.id.promise_list_container).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.no_promise_list_container).setVisibility(View.GONE);
            Log.d(TAG, "Promise list container is visible.");

            // 리스트에 추가
            myPromiseList.clear();
            myPromiseList.addAll(hostPromises);
            Log.d(TAG, "Added " + hostPromises.size() + " host promises.");

            friendPromiseList.clear();
            friendPromiseList.addAll(participantPromises);
            Log.d(TAG, "Added " + participantPromises.size() + " participant promises.");

            // RecyclerView 설정
            setupRecyclerViews();
        } else {
            // 약속 없음 컨테이너 표시
            Log.d(TAG, "No promises found.");
            showNoPromises();
        }
    }

    /**
     * 약속 없음 레이아웃 표시
     */
    private void showNoPromises() {
        rootView.findViewById(R.id.promise_list_container).setVisibility(View.GONE);
        rootView.findViewById(R.id.no_promise_list_container).setVisibility(View.VISIBLE);
        Log.d(TAG, "No promise container is visible.");
    }

    /**
     * Firestore 문서를 Promise 객체로 변환
     *
     * @param document Firestore 문서
     * @return Promise 객체 또는 null
     */
    private Promise parsePromise(DocumentSnapshot document) {
        try {
            String id = document.getId();
            DocumentReference hostRef = document.getDocumentReference("host");
            GeoPoint location = document.getGeoPoint("location");
            Timestamp time = document.getTimestamp("time");
            String description = document.getString("description");
            String promiseType = document.getString("promiseType");
            List<DocumentReference> participants = (List<DocumentReference>) document.get("participants");

            if (hostRef == null) {
                Log.w(TAG, "Promise ID: " + id + " has null host reference. Skipping.");
                return null;
            }

            if (location == null) {
                Log.w(TAG, "Promise ID: " + id + " has null location. Skipping.");
                return null;
            }

            Promise promise = new Promise(id, hostRef, location, time, description, promiseType, participants);
            Log.d(TAG, "Parsed Promise: " + id);
            return promise;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing Promise document: " + document.getId(), e);
            return null;
        }
    }

    /**
     * RecyclerView 설정
     */
    private void setupRecyclerViews() {
        Log.d(TAG, "Setting up RecyclerViews.");
        // 내 약속 RecyclerView 설정 (host가 본인인 약속)
        myPromiseRecyclerView = rootView.findViewById(R.id.recycler_my_promise_list);
        myPromiseRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        myPromiseAdapter = new PromiseAdapter(requireContext(), myPromiseList, this, this::onItemClick,currentUserId);
        myPromiseRecyclerView.setAdapter(myPromiseAdapter);
        Log.d(TAG, "My Promise RecyclerView set.");

        // 친구 약속 RecyclerView 설정 (host가 본인이 아닌 약속)
        friendPromiseRecyclerView = rootView.findViewById(R.id.recycler_friend_promise_list);
        friendPromiseRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        friendPromiseAdapter = new PromiseAdapter(requireContext(), friendPromiseList, this, this::onItemClick, currentUserId);
        friendPromiseRecyclerView.setAdapter(friendPromiseAdapter);
        Log.d(TAG, "Friend Promise RecyclerView set.");
    }

    /**
     * 편집 버튼 클릭 시 호출되는 메소드
     *
     * @param promise 편집할 약속 객체
     */
    @Override
    public void onEditClick(Promise promise) {
        Log.d(TAG, "Edit button clicked for Promise ID: " + promise.getId());
        // PromiseEditFragment으로 이동하면서 약속 ID 전달
        Bundle bundle = new Bundle();
        bundle.putString("promiseId", promise.getId()); // 약속 ID 전달

        // PromiseEditFragment 인스턴스 생성 및 인자 전달
        PromiseEditFragment promiseEditFragment = new PromiseEditFragment();
        promiseEditFragment.setArguments(bundle);

        // FragmentTransaction을 통해 프래그먼트 교체
        FragmentManager fragmentManager = getParentFragmentManager(); // 또는 getActivity().getSupportFragmentManager()
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, promiseEditFragment, "PromiseEditFragment") // R.id.fragment_container는 Activity 레이아웃의 프래그먼트 컨테이너 ID
                .addToBackStack(null) // 뒤로가기 스택에 추가
                .commit();

        Log.d(TAG, "Opened PromiseEditFragment for Promise ID: " + promise.getId());
    }

    public void onItemClick(Promise promise) {
        Log.d(TAG, "Item clicked for Promise ID: " + promise.getId());
        // PromiseCheckFragment으로 이동하면서 약속 ID 전달
        Bundle bundle = new Bundle();
        bundle.putString("promiseId", promise.getId()); // 약속 ID 전달

        // PromiseCheckFragment 인스턴스 생성 및 인자 전달
        PromiseCheckFragment promiseCheckFragment = new PromiseCheckFragment();
        promiseCheckFragment.setArguments(bundle);

        // FragmentTransaction을 통해 프래그먼트 교체
        FragmentManager fragmentManager = getParentFragmentManager(); // 또는 getActivity().getSupportFragmentManager()
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, promiseCheckFragment, "PromiseCheckFragment") // R.id.fragment_container는 Activity 레이아웃의 프래그먼트 컨테이너 ID
                .addToBackStack(null) // 뒤로가기 스택에 추가
                .commit();

        Log.d(TAG, "Opened PromiseCheckFragment for Promise ID: " + promise.getId());
    }
}
