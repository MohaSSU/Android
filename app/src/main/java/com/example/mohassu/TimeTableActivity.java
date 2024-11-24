package com.example.mohassu;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.github.tlaabs.timetableview.Schedule;
import com.github.tlaabs.timetableview.Time;
import com.github.tlaabs.timetableview.TimetableView;
import java.util.ArrayList;

public class TimeTableActivity extends AppCompatActivity  {
    private TimetableView timetableView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        // Initialize TimetableView
        timetableView = findViewById(R.id.timetable);

        // Set click listener for schedule items
        timetableView.setOnStickerSelectEventListener(new TimetableView.OnStickerSelectedListener() {
            @Override
            public void OnStickerSelected(int idx, ArrayList<Schedule> schedules) {
                // Handle click event
                // idx can be used for editing or deleting the schedule
            }
        });

        // Add sample schedules
        addSampleSchedules();
    }

    private void addSampleSchedules() {
        ArrayList<Schedule> schedules = new ArrayList<>();

        // Create first schedule
        Schedule schedule1 = new Schedule();
        schedule1.setClassTitle("Android Programming"); // 과목명
        schedule1.setClassPlace("Room 201"); // 강의실
        schedule1.setProfessorName("John Doe"); // 교수명
        schedule1.setDay(0); // 월요일 (0: 월, 1: 화, 2: 수, 3: 목, 4: 금)
        schedule1.setStartTime(new Time(9, 0)); // 시작 시간 (9:00)
        schedule1.setEndTime(new Time(10, 30)); // 종료 시간 (10:30)

        // Create second schedule
        Schedule schedule2 = new Schedule();
        schedule2.setClassTitle("Data Structures");
        schedule2.setClassPlace("Room 405");
        schedule2.setProfessorName("Jane Smith");
        schedule2.setDay(2); // 수요일
        schedule2.setStartTime(new Time(13, 0)); // 13:00
        schedule2.setEndTime(new Time(14, 30)); // 14:30

        // Add schedules to list
        schedules.add(schedule1);
        schedules.add(schedule2);

        // Add schedules to timetable
        timetableView.add(schedules);
    }

    // Example function to save timetable data
    private void saveTimeTable() {
        String savedData = timetableView.createSaveData();
        // You can save this string to SharedPreferences
        // Example:
        // SharedPreferences prefs = getSharedPreferences("MyTimetable", MODE_PRIVATE);
        // prefs.edit().putString("timetable_data", savedData).apply();
    }

    // Example function to load saved timetable data
    private void loadTimeTable() {
        // Example of loading from SharedPreferences:
        // SharedPreferences prefs = getSharedPreferences("MyTimetable", MODE_PRIVATE);
        // String savedData = prefs.getString("timetable_data", "");
        // if (!savedData.isEmpty()) {
        //     timetableView.load(savedData);
        // }
    }

    // Example of schedule editing
    private void editSchedule(int idx, ArrayList<Schedule> schedules) {
        timetableView.edit(idx, schedules);
    }

    // Example of schedule deletion
    private void deleteSchedule(int idx) {
        timetableView.remove(idx);
    }

    // Example of deleting all schedules
    private void deleteAllSchedules() {
        timetableView.removeAll();
    }
}
