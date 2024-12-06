package com.example.mohassu.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mohassu.Model.Promise;
import com.example.mohassu.R;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;

import java.util.List;

public class PromiseAdapter extends RecyclerView.Adapter<PromiseAdapter.PromiseViewHolder> {
    private List<Promise> promiseList;
    private Context context;

    public PromiseAdapter(Context context, List<Promise> promiseList) {
        this.context = context;
        this.promiseList = promiseList;
    }

    @NonNull
    @Override
    public PromiseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_promise, parent, false);
        return new PromiseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromiseViewHolder holder, int position) {
        Promise promise = promiseList.get(position);

        // 약속 제목 동적 설정
        String title;
        if (promise.getParticipantIds().size() > 1) {
            int otherParticipants = promise.getParticipantIds().size() - 1;
            title = promise.getCreatorId() + "님 외 " + otherParticipants + "명과의 약속";
        } else {
            title = promise.getCreatorId() + "님과의 약속";
        }
        holder.tvTitle.setText(title);

        // 장소 정보 동적 설정
        String locationText = promise.getLocation() != null ? promise.getLocation() + "에서" : "위 장소에서";
        holder.tvLocation.setText(locationText);

        // 날짜 및 시간 동적 설정
        holder.tvTime.setText(formatDateTime(promise.getDateTime()));

        // 프로필 사진 로드
        Glide.with(context)
                .load(promise.getCreatorPhotoUrl())
                .placeholder(R.drawable.pic_basic_profile)
                .error(R.drawable.pic_basic_profile)
                .into((ImageView) holder.imgProfile);

        // 지도 설정
        holder.mapView.onCreate(null);
        holder.mapView.getMapAsync(naverMap -> {
            LatLng location = promise.getLatLng();

            Marker marker = new Marker();
            marker.setPosition(location);
            marker.setMap(naverMap);
            naverMap.moveCamera(com.naver.maps.map.CameraUpdate.scrollTo(location));
        });
    }

    // 날짜 및 시간 포맷 함수
    private String formatDateTime(String dateTime) {
        // 날짜를 분석해 오늘, 내일, 특정 날짜로 표시
        // 예시: "오늘 12:00에 만나요"
        // 이 함수의 세부 구현은 간단히 날짜를 비교하고 원하는 텍스트 형식으로 반환하는 코드로 작성
        return dateTime; // 임시 반환 (구현 필요)
    }

    @Override
    public int getItemCount() {
        return promiseList.size();
    }

    static class PromiseViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {
        MapView mapView;
        TextView tvTitle, tvLocation, tvTime;
        ImageView imgProfile;

        public PromiseViewHolder(@NonNull View itemView) {
            super(itemView);
            mapView = itemView.findViewById(R.id.map_view);
            tvTitle = itemView.findViewById(R.id.tv_promise_title);
            tvLocation = itemView.findViewById(R.id.tv_promise_location);
            tvTime = itemView.findViewById(R.id.tv_promise_time);
            imgProfile = itemView.findViewById(R.id.img_profile);
        }

        @Override
        public void onMapReady(NaverMap naverMap) {
            // MapView 로드 시 처리
        }
    }
}
