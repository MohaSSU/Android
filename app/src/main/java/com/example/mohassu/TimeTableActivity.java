package com.example.mohassu;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mohassu.databinding.ActivityCheckTimeTableBinding;
import com.github.tlaabs.timetableview.Schedule;
import com.github.tlaabs.timetableview.Time;
import com.github.tlaabs.timetableview.TimetableView;

import java.util.ArrayList;

public class TimeTableActivity extends AppCompatActivity {
    private ActivityCheckTimeTableBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityCheckTimeTableBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Access the TimetableView from the binding
        TimetableView timetableView = binding.viewTimeTable.timetable;

        // Set listener for TimetableView
        timetableView.setOnStickerSelectEventListener((idx, schedules) -> {
            // Handle sticker selection
        });

        // Add sample schedules
        addSampleSchedules(timetableView);
    }

    private void addSampleSchedules(TimetableView timetableView) {
        ArrayList<Schedule> schedules = new ArrayList<>();

        // Sample Schedule 1
        Schedule schedule1 = new Schedule();
        schedule1.setClassTitle("Android Programming");
        schedule1.setClassPlace("Room 201");
        schedule1.setProfessorName("John Doe");
        schedule1.setDay(0); // Monday
        schedule1.setStartTime(new Time(9, 0));
        schedule1.setEndTime(new Time(10, 30));

        // Sample Schedule 2
        Schedule schedule2 = new Schedule();
        schedule2.setClassTitle("Data Structures");
        schedule2.setClassPlace("Room 405");
        schedule2.setProfessorName("Jane Smith");
        schedule2.setDay(2); // Wednesday
        schedule2.setStartTime(new Time(13, 0));
        schedule2.setEndTime(new Time(14, 30));

        schedules.add(schedule1);
        schedules.add(schedule2);

        // Add schedules to TimetableView
        timetableView.add(schedules);
    }
}
