package com.example.mohassu.CheckProfileAndTimeTableFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mohassu.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetCheckProfileFragment extends BottomSheetDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_check_profile, container, false);
        //firebase 연동해서 json파일로 받아야함
        ImageButton closeButton = view.findViewById(R.id.btn_close);
        ImageView profileImage = view.findViewById(R.id.img_profile);
        TextView nickName = view.findViewById(R.id.text_nickname);
        TextView name = view.findViewById(R.id.text_name);
        Button viewTimeTableButton = view.findViewById(R.id.view_time_table_button);

        closeButton.setOnClickListener(v -> dismiss());

        Bundle bundle = getArguments();
        if (bundle != null) {
            String nickNameData = bundle.getString("nickName", "Default Nickname");
            String nameData = bundle.getString("name", "Default Name");
            int profileImageResId = bundle.getInt("profileImage", R.drawable.img_basic_profile); // Default image

            nickName.setText(nickNameData);
            name.setText(nameData);
            profileImage.setImageResource(profileImageResId);
        }

        viewTimeTableButton.setOnClickListener(view1 -> {
            Bundle textData = new Bundle();
            textData.putString("nickName", nickName.getText().toString());

            CheckTimeTableFragment checkTimeTableFragment = new CheckTimeTableFragment();
            checkTimeTableFragment.setArguments(textData);

            getDialog().hide(); // 다이얼로그를 숨김 (dismiss()는 호출하지 않음)

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, checkTimeTableFragment, "CheckTimeTableFragment") // fragment_container 사용
                    //android.R.id.content는 임시임. 사용하는 화면에서 frameLayout으로 전환할 화면 부분을 정의해주어야함
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
