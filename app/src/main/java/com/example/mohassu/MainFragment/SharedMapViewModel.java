package com.example.mohassu.MainFragment;

import androidx.lifecycle.ViewModel;
import com.naver.maps.map.NaverMap;

public class SharedMapViewModel extends ViewModel {
    private NaverMap naverMap;

    public void setNaverMap(NaverMap map) {
        this.naverMap = map;
    }

    public NaverMap getNaverMap() {
        return naverMap;
    }
}