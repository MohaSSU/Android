package com.example.mohassu.CreatePromiseFragment;

import androidx.lifecycle.ViewModel;
import java.util.ArrayList;

public class PromiseViewModel extends ViewModel {
    public ArrayList<String> selectedNicknames = new ArrayList<>();
    public ArrayList<String> selectedPhotoUrls = new ArrayList<>();
    public String promiseType = "";
    public String date = "";
    public String time = "";
}