package com.example.mohassu.DialogFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mohassu.R;
import com.github.tlaabs.timetableview.Schedule;

public class ClassEditDialogFragment extends DialogFragment {
    private Schedule schedule;
    private OnClassEditListener listener;

    private TextView selectedLocationButton = null;

    public interface OnClassEdittedListener {
        void onClassAdded(String className, String classPlace, int day, int startHour, int startMinute, int endHour, int endMinute);
    }

    private ClassAddDialogFragment.OnClassAddedListener listener;
    private String selectedClassPlace = ""; // 선택된 강의 장소를 저장하는 변수

    public void setOnClassAddedListener(ClassAddDialogFragment.OnClassAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public int getTheme() {
        return R.style.CustomDialogStyle;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 다이얼로그의 크기 조정
        if (getDialog() != null && getDialog().getWindow() != null) {
            // 가로 크기를 화면 전체로 설정하고 세로는 WRAP_CONTENT
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT, // 가로를 화면 크기로 설정
                    ViewGroup.LayoutParams.WRAP_CONTENT // 세로는 내용에 맞춤
            );
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_class_add, container, false);

        ImageButton closeButton = view.findViewById(R.id.btn_close);
        EditText inputClassName = view.findViewById(R.id.input_class_name);
        Spinner spinnerDay = view.findViewById(R.id.spinner_day);
        Spinner spinnerStartTime = view.findViewById(R.id.start_time);
        Spinner spinnerEndTime = view.findViewById(R.id.end_time);
        EditText inputClassPlace = view.findViewById(R.id.input_class_place);
        LinearLayout horizontalContainer = view.findViewById(R.id.horizontal_spinner_container);

        closeButton.setOnClickListener(v -> dismiss());

        // strings.xml에서 강의 장소 배열 가져오기
        String[] locations = getResources().getStringArray(R.array.locations);

        // 동적으로 버튼 추가
        for (String location : locations) {
            TextView locationButton = new TextView(requireContext());
            locationButton.setText(location);
            locationButton.setBackgroundResource(R.drawable.background_banner_white); // 기본 배경
            locationButton.setPadding(16, 8, 16, 8);
            locationButton.setTextColor(getResources().getColor(android.R.color.black));
            locationButton.setGravity(View.TEXT_ALIGNMENT_CENTER);

            // 레이아웃 매개변수 설정
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0); // 버튼 간격
            locationButton.setLayoutParams(params);

            // 버튼 클릭 이벤트
            locationButton.setOnClickListener(v -> {
                // 이전에 선택된 버튼이 있는 경우, 상태 해제
                if (selectedLocationButton != null && selectedLocationButton != locationButton) {
                    selectedLocationButton.setBackgroundResource(R.drawable.background_banner_white); // 기본 배경
                    selectedLocationButton.setTextColor(getResources().getColor(android.R.color.black)); // 기본 텍스트 색상
                }

                // 현재 버튼을 선택 상태로 설정
                selectedLocationButton = locationButton;
                selectedLocationButton.setBackgroundResource(R.drawable.background_banner_selected); // 선택된 배경
                selectedLocationButton.setTextColor(getResources().getColor(android.R.color.white)); // 선택된 텍스트 색상

                // 선택된 장소를 저장
                selectedClassPlace = location;
                inputClassPlace.setText(location); // EditText에 설정
            });

            horizontalContainer.addView(locationButton);
        }

        Button saveButton = view.findViewById(R.id.btn_save);
        saveButton.setOnClickListener(v -> {
            String className = inputClassName.getText().toString();
            String classPlace = inputClassPlace.getText().toString();
            int day = spinnerDay.getSelectedItemPosition(); // 0: Monday, 1: Tuesday, ...
            int startHour = Integer.parseInt(spinnerStartTime.getSelectedItem().toString().split(":")[0]);
            int startMinute = Integer.parseInt(spinnerStartTime.getSelectedItem().toString().split(":")[1]);
            int endHour = Integer.parseInt(spinnerEndTime.getSelectedItem().toString().split(":")[0]);
            int endMinute = Integer.parseInt(spinnerEndTime.getSelectedItem().toString().split(":")[1]);

            if (listener != null) {
                listener.onClassAdded(className, classPlace, day, startHour, startMinute, endHour, endMinute);
            }
            dismiss();
        });

        return view;
    }
}
