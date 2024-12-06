package com.example.mohassu.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mohassu.Model.Promise;

import java.util.ArrayList;
import java.util.List;

public class PromiseViewModel extends ViewModel {
    private final MutableLiveData<List<Promise>> promiseList = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Promise>> getPromiseList() {
        return promiseList;
    }

    public void setPromiseList(List<Promise> promises) {
        promiseList.setValue(promises);
    }
}
