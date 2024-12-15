package com.example.mohassu.CheckProfileAndTimeTableFragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ViewTarget;
import com.example.mohassu.Model.Friend;
import com.example.mohassu.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CheckProfileBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_FRIEND = "friend";
    private static final String ARG_FRIEND_ID = "friendId";
    private static final String ARG_TIMETABLE = "timetableData";
    private static final String ARG_NICKNAME = "nickname";
    private static final String ARG_NAME = "name";
    private static final String ARG_PROFILE_IMAGE_RES_ID = "profile_image_res_id";

    private static final String TAG = "mohassu:checkProfile";

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();

    // 다양한 데이터 전달 방식을 위한 newInstance 메서드들

    // 1. Friend 객체 전달
    public static CheckProfileBottomSheetFragment newInstance(Friend friend) {
        CheckProfileBottomSheetFragment fragment = new CheckProfileBottomSheetFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FRIEND, friend);
        fragment.setArguments(args);
        return fragment;
    }

    // 2. friendId 전달
    public static CheckProfileBottomSheetFragment newInstanceWithFriendId(String friendId) {
        CheckProfileBottomSheetFragment fragment = new CheckProfileBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FRIEND_ID, friendId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_sheet_check_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton closeButton = view.findViewById(R.id.btn_close);
        ImageView profileImage = view.findViewById(R.id.img_profile);
        TextView nicknameTextView = view.findViewById(R.id.text_nickname);
        TextView nameTextView = view.findViewById(R.id.text_name);

        Button viewTimeTableButton = view.findViewById(R.id.view_time_table_button);

        closeButton.setOnClickListener(v -> dismiss());

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_FRIEND)) {
                // Friend 객체가 전달된 경우
                Friend friend = (Friend) args.getSerializable(ARG_FRIEND);
                if (friend != null) {
                    nicknameTextView.setText(friend.getNickname() != null ? friend.getNickname() : "닉네임 없음");
                    nameTextView.setText(friend.getName() != null ? friend.getName() : "이름 없음");

                    Glide.with(requireContext())
                            .load(friend.getPhotoUrl())
                            .circleCrop()
                            .placeholder(R.drawable.img_basic_profile)
                            .error(R.drawable.img_basic_profile)
                            .into(profileImage);

                    viewTimeTableButton.setOnClickListener(v -> {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(ARG_FRIEND, friend);
                        dismiss();
                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                        navController.navigate(R.id.action_checkProfileToCheckTimeTable, bundle);
                    });
                }
            } else if (args.containsKey(ARG_FRIEND_ID)) {
                // friendId가 전달된 경우 Firestore에서 데이터 조회
                String friendId = args.getString(ARG_FRIEND_ID);
                if (currentUser != null && friendId != null && !friendId.isEmpty()) {
                    db.collection("users").document(friendId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String nickName = documentSnapshot.getString("nickname");
                                    String name = documentSnapshot.getString("name");
                                    String photoUrl = documentSnapshot.getString("photoUrl");

                                    nicknameTextView.setText(nickName != null ? nickName : "닉네임 없음");
                                    nameTextView.setText(name != null ? name : "이름 없음");

                                    ViewTarget<ImageView, Drawable> into = Glide.with(this)
                                            .load(photoUrl)
                                            .circleCrop()
                                            .placeholder(R.drawable.img_basic_profile)
                                            .error(R.drawable.img_basic_profile)
                                            .into(profileImage);

                                    String timeTableJSON = documentSnapshot.getString("timetableData");

                                    Log.d(TAG,"ARG_FRIEND_ID 시간표 데이터 :"+timeTableJSON);

                                    viewTimeTableButton.setOnClickListener(v -> {
                                        Bundle bundle = new Bundle();
                                        bundle.putString(ARG_FRIEND_ID, friendId);
                                        bundle.putString(ARG_NICKNAME, nickName);
                                        bundle.putString(ARG_TIMETABLE, timeTableJSON);
                                        dismiss();
                                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                                        navController.navigate(R.id.action_checkProfileToCheckTimeTable, bundle);
                                    });
                                } else {
                                    Toast.makeText(getContext(), "친구 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "친구 데이터 로드 실패", e);
                                Toast.makeText(getContext(), "친구 데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                            });
                }
            } else if (args.containsKey(ARG_NICKNAME) && args.containsKey(ARG_NAME) && args.containsKey(ARG_PROFILE_IMAGE_RES_ID)) {
                // 개별 필드가 전달된 경우
                String nickNameData = args.getString(ARG_NICKNAME, "Default Nickname");
                String nameData = args.getString(ARG_NAME, "Default Name");
                int profileImageResId = args.getInt(ARG_PROFILE_IMAGE_RES_ID, R.drawable.img_basic_profile);

                nicknameTextView.setText(nickNameData);
                nameTextView.setText(nameData);
                profileImage.setImageResource(profileImageResId);

                viewTimeTableButton.setOnClickListener(v -> {
                    Bundle textData = new Bundle();
                    textData.putString("nickName", nicknameTextView.getText().toString());

                    CheckTimeTableFragment checkTimeTableFragment = new CheckTimeTableFragment();
                    checkTimeTableFragment.setArguments(textData);

                    getDialog().hide(); // 다이얼로그를 숨김 (dismiss()는 호출하지 않음)

                    requireActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(android.R.id.content, checkTimeTableFragment, "CheckTimeTableFragment") // fragment_container 사용
                            .addToBackStack(null)
                            .commit();
                });
            }
        }

    }
}
