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
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mohassu.R;
import com.github.tlaabs.timetableview.Schedule;
import com.github.tlaabs.timetableview.Time;

public class ClassEditOrDeleteDialogFragment extends DialogFragment {

    private TextView selectedLocationButton = null; // 강의 장소 선택 로직을 위해서 정의
    private String selectedClassPlace = ""; // 선택된 강의 장소를 저장하는 변수

    public interface OnClassEditOrDeleteListener {
        void onEdit(Schedule editedSchedule);
        void onDelete();
    }

    private OnClassEditOrDeleteListener listener;

    public void setOnClassEditOrDeleteListener(OnClassEditOrDeleteListener listener) {
        this.listener = listener;
    }

    @Override
    public int getTheme() {
        return R.style.CustomDialogStyle;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    public static ClassEditOrDeleteDialogFragment newInstance(Schedule existingSchedule) {
        ClassEditOrDeleteDialogFragment fragment = new ClassEditOrDeleteDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("schedule", existingSchedule);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_class_edit_or_delete, container, false);

        EditText classTitle = view.findViewById(R.id.input_class_name);
        Spinner spinnerDay = view.findViewById(R.id.spinner_day);
        Spinner spinnerStartTime = view.findViewById(R.id.start_time);
        Spinner spinnerEndTime = view.findViewById(R.id.end_time);
        EditText inputClassPlace = view.findViewById(R.id.input_class_place);
        Button editSaveButton = view.findViewById(R.id.btn_edit_save);
        Button deleteButton = view.findViewById(R.id.btn_delete);
        ImageButton closeButton = view.findViewById(R.id.btn_close);
        LinearLayout horizontalContainer = view.findViewById(R.id.horizontal_spinner_container);

        Schedule existingSchedule = new Schedule();
        existingSchedule = (Schedule) getArguments().getSerializable("schedule");

        // 닫기 버튼 동작
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

            // 이전에 저장된 장소 선택
            if (existingSchedule != null && location.equals(existingSchedule.getClassPlace())) {
                selectedLocationButton = locationButton;
                selectedLocationButton.setBackgroundResource(R.drawable.background_banner_selected);
                selectedLocationButton.setTextColor(getResources().getColor(android.R.color.white));
            }

            horizontalContainer.addView(locationButton);
        }

        // 기존 데이터 설정
        if (existingSchedule != null) {
            classTitle.setText(existingSchedule.getClassTitle());
            inputClassPlace.setText(existingSchedule.getClassPlace());

            // Day 스피너 설정 (0: Monday, 1: Tuesday, ...)
            spinnerDay.setSelection(existingSchedule.getDay());

            // Start Time 설정
            String startTime = String.format("%02d:%02d", existingSchedule.getStartTime().getHour(), existingSchedule.getStartTime().getMinute());
            setSpinnerSelection(spinnerStartTime, startTime);

            // End Time 설정
            String endTime = String.format("%02d:%02d", existingSchedule.getEndTime().getHour(), existingSchedule.getEndTime().getMinute());
            setSpinnerSelection(spinnerEndTime, endTime);
        }

        // 수정 버튼 동작
        editSaveButton.setOnClickListener(v -> {
            if (listener != null) {
                String classTitleText = classTitle.getText().toString();
                String classPlaceText = inputClassPlace.getText().toString();
                int day = spinnerDay.getSelectedItemPosition();
                int startHour = Integer.parseInt(spinnerStartTime.getSelectedItem().toString().split(":")[0]);
                int startMinute = Integer.parseInt(spinnerStartTime.getSelectedItem().toString().split(":")[1]);
                int endHour = Integer.parseInt(spinnerEndTime.getSelectedItem().toString().split(":")[0]);
                int endMinute = Integer.parseInt(spinnerEndTime.getSelectedItem().toString().split(":")[1]);

                // 수정된 Schedule 객체 생성
                Schedule editedSchedule = new Schedule();
                editedSchedule.setClassTitle(classTitleText);
                editedSchedule.setClassPlace(classPlaceText);
                editedSchedule.setDay(day);
                editedSchedule.setStartTime(new Time(startHour, startMinute));
                editedSchedule.setEndTime(new Time(endHour, endMinute));

                // 수정된 데이터를 listener를 통해 전달
                listener.onEdit(editedSchedule);
            }
            dismiss();
        });

        // 삭제 버튼 동작
        deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete();
            }
            dismiss();
        });

        return view;
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}
