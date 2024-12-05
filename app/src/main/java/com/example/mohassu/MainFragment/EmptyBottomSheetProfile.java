package com.example.mohassu.MainFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.mohassu.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class EmptyBottomSheetProfile extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 프로필 레이아웃 Inflate
        return inflater.inflate(R.layout.fragment_bottom_sheet_check_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageButton closeButton = view.findViewById(R.id.btn_close);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> {
                dismiss();
            });
        }
    }
}
