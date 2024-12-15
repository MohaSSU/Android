package com.example.mohassu.CreatePromiseFragment;

import androidx.lifecycle.ViewModel;

import com.example.mohassu.R;

import java.util.ArrayList;

public class PromiseViewModel extends ViewModel {
    public ArrayList<String> selectedNicknames = new ArrayList<>();
    public ArrayList<String> selectedPhotoUrls = new ArrayList<>();
    public String promiseType = "밥약속";
    public int promiseIconRes = R.drawable.ic_promise_rice;
    public String date = "";
    public String time = "";
    public double latitude = 0.0;
    public double longitude = 0.0;
    public String promiseDescription = "";
}