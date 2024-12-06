package com.example.mohassu.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mohassu.R;
import com.example.mohassu.models.Promise;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;

import java.util.List;

public class PromiseAdapter extends RecyclerView.Adapter<PromiseAdapter.PromiseViewHolder> {

    private List<Promise> promiseList;
    private Context context;

    public PromiseAdapter(List<Promise> promiseList, Context context) {
        this.promiseList = promiseList;
        this.context = context;
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
        holder.tvTitle.setText(promise.getTitle());
        holder.tvLocation.setText(promise.getLocation());
        holder.tvTime.setText(promise.getTime());

        // 네이버 지도 설정
        holder.mapView.onCreate(null);
        holder.mapView.getMapAsync(naverMap -> {
            LatLng location = promise.getLatLng();

            // 마커 추가
            Marker marker = new Marker();
            marker.setPosition(location);
            marker.setMap(naverMap);

            // 카메라 이동
            naverMap.moveCamera(com.naver.maps.map.CameraUpdate.scrollTo(location));
        });
    }

    @Override
    public int getItemCount() {
        return promiseList.size();
    }

    static class PromiseViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {
        MapView mapView;
        TextView tvTitle, tvLocation, tvTime;

        public PromiseViewHolder(@NonNull View itemView) {
            super(itemView);
            mapView = itemView.findViewById(R.id.map_view);
            tvTitle = itemView.findViewById(R.id.tv_promise_title);
            tvLocation = itemView.findViewById(R.id.tv_promise_location);
            tvTime = itemView.findViewById(R.id.tv_promise_time);
        }

        @Override
        public void onMapReady(NaverMap naverMap) {
            // No action required here; handled in onBindViewHolder
        }
    }
}
