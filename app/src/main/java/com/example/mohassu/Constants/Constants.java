package com.example.mohassu.Constants;

import com.example.mohassu.Model.PlaceInfo;
import com.naver.maps.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Constants {
    public static final List<PlaceInfo> PLACES = new ArrayList<>();

    static {
        PLACES.add(new PlaceInfo("정보과학관", new LatLng(37.494576, 126.959706), 30));
        PLACES.add(new PlaceInfo("한경직기념관", new LatLng(37.495619, 126.957444), 30));
        PLACES.add(new PlaceInfo("중앙도서관", new LatLng(37.496325, 126.958585), 30));
        PLACES.add(new PlaceInfo("전산관", new LatLng(37.495451, 126.959518), 30));
        PLACES.add(new PlaceInfo("형남공학관", new LatLng(37.495732, 126.956152), 30));
        PLACES.add(new PlaceInfo("안익태기념관", new LatLng(37.495753, 126.955012), 30));
        PLACES.add(new PlaceInfo("경상관", new LatLng(37.496549, 126.955030), 30));
        PLACES.add(new PlaceInfo("문화관", new LatLng(37.496576, 126.954271), 30));
        PLACES.add(new PlaceInfo("베어드홀", new LatLng(37.496506, 126.956273), 30));
        PLACES.add(new PlaceInfo("학생회관", new LatLng(37.496873, 126.956308), 30));
        PLACES.add(new PlaceInfo("숭덕경상관", new LatLng(37.496942, 126.954923), 30));
        PLACES.add(new PlaceInfo("백마관", new LatLng(37.497830, 126.956273), 30));
        PLACES.add(new PlaceInfo("벤처관", new LatLng(37.497544, 126.957438), 30));
        PLACES.add(new PlaceInfo("진리관", new LatLng(37.496906, 126.957495), 30));
        PLACES.add(new PlaceInfo("조만식기념관", new LatLng(37.497249, 126.958212), 30));
        PLACES.add(new PlaceInfo("미래관", new LatLng(  37.495610, 126.958487), 30));
        // 필요시 추가
    }
}