package com.example.mohassu.CheckProfileAndTimeTableFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.mohassu.R;

public class CheckTimeTableFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check_time_table, container, false);

        ImageButton backButton = view.findViewById(R.id.btnBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 현재 프래그먼트를 제거하고 백 스택에서 돌아가기
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack();

                // `BottomSheetCheckProfileFragment` 다시 표시
                BottomSheetCheckProfileFragment bottomSheet = (BottomSheetCheckProfileFragment)
                        requireActivity()
                                .getSupportFragmentManager()
                                .findFragmentByTag("BottomSheetCheckProfile");

                if (bottomSheet != null && bottomSheet.getDialog() != null) {
                    bottomSheet.getDialog().show(); // 숨겨진 다이얼로그 다시 표시
                }
            }
        });

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        Bundle args = getArguments();
        if (args != null) {
            String userName = args.getString("nickName", "Default User");
            tvTitle.setText(getString(R.string.check_time_table_title, userName));
        }

        return view;
    }
}
