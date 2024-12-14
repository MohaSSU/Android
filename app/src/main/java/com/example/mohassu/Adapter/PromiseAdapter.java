// com.example.mohassu.Adapter.PromiseAdapter.java

package com.example.mohassu.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.example.mohassu.Constants.Constants;
import com.example.mohassu.Model.PlaceInfo;
import com.example.mohassu.Model.Promise;
import com.example.mohassu.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PromiseAdapter extends RecyclerView.Adapter<PromiseAdapter.PromiseViewHolder> {

    private static final String TAG = "mohassu:promise";

    private List<Promise> promiseList;
    private Context context;
    private String currentUserId;

    // 클릭 리스너 인터페이스 정의
    public interface OnEditClickListener {
        void onEditClick(Promise promise);
    }

    // 새로 추가할 OnItemClickListener 인터페이스
    public interface OnItemClickListener {
        void onItemClick(Promise promise);
    }

    private OnEditClickListener editClickListener;
    private OnItemClickListener itemClickListener;

    // 생성자에 리스너 및 currentUserId 추가
    public PromiseAdapter(Context context, List<Promise> promiseList, OnEditClickListener editClickListener, OnItemClickListener itemClickListener, String currentUserId) {
        this.context = context;
        this.promiseList = promiseList;
        this.editClickListener = editClickListener;
        this.itemClickListener = itemClickListener;
        this.currentUserId = currentUserId;
        Log.d(TAG, "PromiseAdapter created with user ID: " + currentUserId);
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
        Log.d(TAG, "Binding Promise at position " + position + ": " + promise.getId());

        // 프로필 이미지 URL 로그 출력
        String profileImageUrl = promise.getHostProfileImageUrl();
        Log.d(TAG, "Profile Image URL: " + profileImageUrl);

        // 프로필 이미지 설정
        Glide.with(context)
                .load(promise.getHostProfileImageUrl())
                .placeholder(R.drawable.img_basic_profile)
                .error(R.drawable.img_basic_profile)
                .into(holder.imgProfile);
        Log.d(TAG, "Loaded profile image for Promise ID: " + promise.getId());

        // 약속 제목 설정
        if (promise.getHost() != null && promise.getHost().getId().equals(currentUserId)) {
            holder.tvTitle.setText("내가 만든 약속");
            Log.d(TAG, "Set title as '내가 만든 약속' for Promise ID: " + promise.getId());
        } else {
            int participantCount = promise.getParticipants() != null ? promise.getParticipants().size() : 0;
            if (participantCount <= 1) {
                holder.tvTitle.setText(promise.getHostNickname() + "님과의 약속");
                Log.d(TAG, "Set title as '" + promise.getHostNickname() + "님과의 약속' for Promise ID: " + promise.getId());
            } else {
                holder.tvTitle.setText(promise.getHostNickname() + "님 외 " + (participantCount - 1) + "명과의 약속");
                Log.d(TAG, "Set title as '" + promise.getHostNickname() + "님 외 " + (participantCount - 1) + "명과의 약속' for Promise ID: " + promise.getId());
            }
        }

        // 약속 장소 설정 (지오펜싱)
        GeoPoint location = promise.getLocation(); // 'getPlace()' 대신 'getLocation()' 사용
        Log.d(TAG, "Promise ID: " + promise.getId() + ", location: " + location);
        if (location != null) {
            String geofenceName = getGeofenceName(location);
            if (geofenceName != null) {
                holder.tvLocation.setText(geofenceName + "에서");
                Log.d(TAG, "Set location as '" + geofenceName + "에서' for Promise ID: " + promise.getId());
            } else {
                holder.tvLocation.setText("지도 위 마커 장소에서");
                Log.d(TAG, "Set location as '지도 위 마커 장소에서' for Promise ID: " + promise.getId());
            }
        } else {
            holder.tvLocation.setText("지도 위 마커 장소에서");
            Log.w(TAG, "Promise ID: " + promise.getId() + " has null GeoPoint.");
        }

        // 시간 포맷팅
        String formattedTime = formatTimestamp(promise.getTime());
        holder.tvTime.setText(formattedTime);
        Log.d(TAG, "Set time as '" + formattedTime + "' for Promise ID: " + promise.getId());

        // 정적인 지도 이미지 로드
        String staticMapUrl = generateStaticMapUrl(promise.getLocation(), promise.getPromiseType());

        if (!staticMapUrl.isEmpty()) {
//            Log.d(TAG, "Client ID: " + BuildConfig.NAVER_STATIC_MAP_CLIENT_ID);
//            Log.d(TAG, "Client Secret: " + BuildConfig.NAVER_STATIC_MAP_CLIENT_SECRET);
//            // 헤더 추가
            LazyHeaders headers = new LazyHeaders.Builder()
                    .addHeader("X-NCP-APIGW-API-KEY-ID", "fs9rplnpjv")
                    .addHeader("X-NCP-APIGW-API-KEY", "nkBj5b39jqUnW0OVh6IKylxZvBqpd0XUz8GvlkeC")
                    .build();
            GlideUrl glideUrl = new GlideUrl(staticMapUrl, headers);

            // 헤더 로그 출력
            Map<String, String> headerMap = glideUrl.getHeaders();
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                Log.d(TAG, "Header: " + entry.getKey() + " = " + entry.getValue());
            }
            Glide.with(context)
                    .load(glideUrl)
                    .into(holder.staticMapImage);
            Log.d(TAG, "Loaded static map image for Promise ID: " + promise.getId());
            Log.d(TAG, "Static Map glideURL: " + glideUrl);
        } else {
            holder.staticMapImage.setImageResource(R.drawable.background_banner_white);
            Log.w(TAG, "Static map URL is empty. Set default image for Promise ID: " + promise.getId());
        }

        // 아이템 클릭 리스너 설정
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                Log.d(TAG, "Item clicked for Promise ID: " + promise.getId());
                itemClickListener.onItemClick(promise);
            }
        });

        // edit_promise_button 설정
        if (promise.getHost() != null && promise.getHost().getId().equals(currentUserId)) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.editButton.setOnClickListener(v -> {
                if (editClickListener != null) {
                    Log.d(TAG, "Edit button clicked for Promise ID: " + promise.getId());
                    editClickListener.onEditClick(promise);
                }
            });
            Log.d(TAG, "Edit button visible for Promise ID: " + promise.getId());
        } else {
            holder.editButton.setVisibility(View.GONE);
            holder.editButton.setOnClickListener(null);
            Log.d(TAG, "Edit button hidden for Promise ID: " + promise.getId());
        }
    }

    /**
     * Naver Static Maps API URL 생성
     *
     * @param location        GeoPoint 객체 (위도, 경도)
     * @param promiseType  약속 유형 (밥약속, 술약속, 공부약속 등)
     * @return 생성된 Static Maps URL
     */
    private String generateStaticMapUrl(GeoPoint location, String promiseType) {
        if (location == null) {
            Log.w(TAG, "GeoPoint is null. Cannot generate static map URL.");
            return "";
        }

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        int zoom = 16;
        int width = 400;
        int height = 300;
        String markerIcon = getMarkerIconUrl(promiseType);

        // 인증 파라미터 제거
        String url = "https://naveropenapi.apigw.ntruss.com/map-static/v2/raster?" +
                "center=" + longitude + "," + latitude +
                "&level=" + zoom +
                "&w=" + width + "&h=" + height +
                "&markers=type:e|anchor=center|" + markerIcon + "|pos:" + longitude + "%" + latitude;

        Log.d(TAG, "Generated static map URL: " + url);
        return url;
    }

    /**
     * 약속 유형에 따라 마커 아이콘 URL 반환
     *
     * @param promiseType 약속 유형
     * @return 마커 아이콘 URL
     */
    private String getMarkerIconUrl(String promiseType) {
        String url;
        switch (promiseType) {
            case "MEAL":
                url = "icon:https://firebasestorage.googleapis.com/v0/b/mohassu-98a30.firebasestorage.app/o/marker_icons%2Fic_promise_meal_marker.png?alt=media&token=1487e418-3e68-43f8-ac66-44ece7afb3d5";
                break;
            case "DRINK":
                url = "icon:https://firebasestorage.googleapis.com/v0/b/mohassu-98a30.firebasestorage.app/o/marker_icons%2Fic_promise_drink_marker.png?alt=media&token=1b55929a-9db4-489c-8eca-67f554820bf6";
                break;
            case "STUDY":
                url = "icon:https://firebasestorage.googleapis.com/v0/b/mohassu-98a30.firebasestorage.app/o/marker_icons%2Fic_promise_study_marker.png?alt=media&token=2a595f5e-84ba-4c14-8ab5-36c23c2f1d77";
                break;
            case "ETC":
                url = "icon:https://firebasestorage.googleapis.com/v0/b/mohassu-98a30.firebasestorage.app/o/marker_icons%2Fic_promise_etc_marker.png?alt=media&token=4f0fa4a3-906c-40ed-8357-823c5c788b1c";
                break;
            default:
                url = "icon:https://firebasestorage.googleapis.com/v0/b/mohassu-98a30.firebasestorage.app/o/marker_icons%2Fic_promise_marker.png?alt=media&token=ac6771b1-ea80-447e-9bc5-2e98559fa676";
                break;
        }
        Log.d(TAG, "Marker Icon URL for promise type '" + promiseType + "': " + url);
        return url;
    }

    /**
     * 지오펜싱 로직 예시
     */
    private String getGeofenceName(GeoPoint place) {
        if (place == null) {
            Log.w(TAG, "GeoPoint is null. Cannot determine geofence name.");
            return null;
        }

        double lat1 = place.getLatitude();
        double lon1 = place.getLongitude();
        for (PlaceInfo placeInfo : Constants.PLACES) {
            if (placeInfo.getLocation() == null) {
                Log.w(TAG, "PlaceInfo '" + placeInfo.getName() + "' has null location.");
                continue; // null location을 가진 PlaceInfo는 건너뜁니다.
            }

            double lat2 = placeInfo.getLocation().latitude;
            double lon2 = placeInfo.getLocation().longitude;
            float[] results = new float[1];
            android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results);
            if (results[0] <= placeInfo.getRadius()) {
                Log.d(TAG, "Geofence matched: " + placeInfo.getName() + " for Promise location.");
                return placeInfo.getName();
            }
        }
        Log.d(TAG, "No Geofence matched for Promise location.");
        return null;
    }

    /**
     * 타임스탬프를 조건에 맞게 포맷팅
     *
     * @param timestamp Firestore 타임스탬프
     * @return 포맷팅된 시간 문자열
     */
    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            Log.w(TAG, "Timestamp is null. Cannot format time.");
            return "시간 정보 없음";
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date date = timestamp.toDate();
        Date now = new Date();

        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dayFormat.format(now);
        String promiseDay = dayFormat.format(date);

        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MM월 dd일 HH:mm", Locale.getDefault());

        String formattedTime;
        if (today.equals(promiseDay)) {
            formattedTime = "오늘 " + timeFormat.format(date) + "에 만나요";
        } else if (isTomorrow(now, date)) {
            formattedTime = "내일 " + timeFormat.format(date) + "에 만나요";
        } else {
            formattedTime = monthDayFormat.format(date) + "에 만나요";
        }
        Log.d(TAG, "Formatted time: " + formattedTime);
        return formattedTime;
    }

    /**
     * 내일인지 확인
     *
     * @param now  현재 날짜
     * @param date 비교할 날짜
     * @return 내일이면 true, 아니면 false
     */
    private boolean isTomorrow(Date now, Date date) {
        java.util.Calendar calNow = java.util.Calendar.getInstance();
        calNow.setTime(now);
        calNow.add(java.util.Calendar.DAY_OF_YEAR, 1);

        java.util.Calendar calDate = java.util.Calendar.getInstance();
        calDate.setTime(date);

        boolean isTomorrow = calNow.get(java.util.Calendar.YEAR) == calDate.get(java.util.Calendar.YEAR) &&
                calNow.get(java.util.Calendar.DAY_OF_YEAR) == calDate.get(java.util.Calendar.DAY_OF_YEAR);
        Log.d(TAG, "Is Tomorrow: " + isTomorrow);
        return isTomorrow;
    }

    @Override
    public int getItemCount() {
        return promiseList.size();
    }

    /**
     * ViewHolder 클래스
     */
    static class PromiseViewHolder extends RecyclerView.ViewHolder {
        ImageView staticMapImage;
        TextView tvTitle, tvLocation, tvTime;
        ImageView imgProfile;
        ImageButton editButton;

        public PromiseViewHolder(@NonNull View itemView) {
            super(itemView);
            staticMapImage = itemView.findViewById(R.id.map_view);
            tvTitle = itemView.findViewById(R.id.tv_promise_title);
            tvLocation = itemView.findViewById(R.id.tv_promise_location);
            tvTime = itemView.findViewById(R.id.tv_promise_time);
            imgProfile = itemView.findViewById(R.id.img_profile);
            editButton = itemView.findViewById(R.id.edit_promise_button);
        }
    }
}
