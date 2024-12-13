// com.example.mohassu.Adapter.PromiseAdapter.java

package com.example.mohassu.Adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log; // 추가
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // 추가
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
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

public class PromiseAdapter extends RecyclerView.Adapter<PromiseAdapter.PromiseViewHolder> {

    private static final String TAG = "promise"; // 로그 태그 추가

    // 생성자에 리스너 추가
    private List<Promise> promiseList;
    private Context context;
    private String currentUserId;

    // 클릭 리스너 인터페이스 정의
    public interface OnEditClickListener {
        void onEditClick(Promise promise);
    }

    private OnEditClickListener editClickListener;

    // 생성자에 리스너 및 currentUserId 추가
    public PromiseAdapter(Context context, List<Promise> promiseList, OnEditClickListener editClickListener, String currentUserId) {
        this.context = context;
        this.promiseList = promiseList;
        this.editClickListener = editClickListener;
        this.currentUserId = currentUserId;
        Log.d(TAG, "PromiseAdapter created with user ID: " + currentUserId); // 로그 추가
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
        Log.d(TAG, "Binding Promise at position " + position + ": " + promise.getId()); // 로그 추가

        // 프로필 이미지 설정
        Glide.with(context)
                .load(promise.getHostProfileImageUrl())
                .placeholder(R.drawable.img_basic_profile)
                .error(R.drawable.img_basic_profile)
                .into(holder.imgProfile);
        Log.d(TAG, "Loaded profile image for Promise ID: " + promise.getId()); // 로그 추가

        // 약속 제목 설정
        if (promise.getHost().getId().equals(currentUserId)) {
            holder.tvTitle.setText("내가 만든 약속");
            Log.d(TAG, "Set title as '내가 만든 약속' for Promise ID: " + promise.getId()); // 로그 추가
        } else {
            int participantCount = promise.getParticipants() != null ? promise.getParticipants().size() : 0;
            if (participantCount <= 1) {
                holder.tvTitle.setText(promise.getHostNickname() + "님과의 약속");
                Log.d(TAG, "Set title as '" + promise.getHostNickname() + "님과의 약속' for Promise ID: " + promise.getId()); // 로그 추가
            } else {
                holder.tvTitle.setText(promise.getHostNickname() + "님 외 " + (participantCount - 1) + "명과의 약속");
                Log.d(TAG, "Set title as '" + promise.getHostNickname() + "님 외 " + (participantCount - 1) + "명과의 약속' for Promise ID: " + promise.getId()); // 로그 추가
            }
        }

        // 약속 장소 설정 (지오펜싱)
        String geofenceName = getGeofenceName(promise.getPlace());
        if (geofenceName != null) {
            holder.tvLocation.setText(geofenceName + "에서");
            Log.d(TAG, "Set location as '" + geofenceName + "에서' for Promise ID: " + promise.getId()); // 로그 추가
        } else {
            holder.tvLocation.setText("지도 위 마커 장소에서");
            Log.d(TAG, "Set location as '지도 위 마커 장소에서' for Promise ID: " + promise.getId()); // 로그 추가
        }

        // 시간 포맷팅
        String formattedTime = formatTimestamp(promise.getTime());
        holder.tvTime.setText(formattedTime);
        Log.d(TAG, "Set time as '" + formattedTime + "' for Promise ID: " + promise.getId()); // 로그 추가

        // 정적인 지도 이미지 로드
        String staticMapUrl = generateStaticMapUrl(promise.getPlace(), promise.getPromiseType());
        Glide.with(context)
                .load(staticMapUrl)
                .placeholder(R.drawable.img_basic_profile) // 임시 이미지 설정 (원하는 이미지로 변경 가능)
                .error(R.drawable.img_basic_profile)
                .into(holder.staticMapImage);
        Log.d(TAG, "Loaded static map image for Promise ID: " + promise.getId()); // 로그 추가

        // edit_promise_button 설정
        if (promise.getHost().getId().equals(currentUserId)) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.editButton.setOnClickListener(v -> {
                if (editClickListener != null) {
                    Log.d(TAG, "Edit button clicked for Promise ID: " + promise.getId()); // 로그 추가
                    editClickListener.onEditClick(promise);
                }
            });
            Log.d(TAG, "Edit button visible for Promise ID: " + promise.getId()); // 로그 추가
        } else {
            holder.editButton.setVisibility(View.GONE);
            holder.editButton.setOnClickListener(null);
            Log.d(TAG, "Edit button hidden for Promise ID: " + promise.getId()); // 로그 추가
        }
    }

    /**
     * Naver Static Maps API URL 생성
     *
     * @param place        GeoPoint 객체 (위도, 경도)
     * @param promiseType  약속 유형 (밥약속, 술약속, 공부약속 등)
     * @return 생성된 Static Maps URL
     */
    private String generateStaticMapUrl(GeoPoint place, String promiseType) {
        double latitude = place.getLatitude();
        double longitude = place.getLongitude();
        int zoom = 16;
        int width = 400;
        int height = 300;
        String markerIcon = getMarkerIconUrl(promiseType);

        // URL 인코딩 필요 시 추가
        String url = "https://naveropenapi.apigw.ntruss.com/map-static/v2/raster?center=" + longitude + "," + latitude +
                "&level=" + zoom +
                "&size=" + width + "," + height +
                "&markers=" + markerIcon + "," + longitude + "," + latitude +
                "&X-NCP-APIGW-API-KEY-ID=" + "YOUR_NAVER_CLIENT_ID" +
                "&X-NCP-APIGW-API-KEY=" + "YOUR_NAVER_CLIENT_SECRET";
        Log.d(TAG, "Generated static map URL: " + url); // 로그 추가
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
                url = "https://firebasestorage.googleapis.com/v0/b/mohassu-98a30.firebasestorage.app/o/marker_icons%2Fic_promise_meal_marker.png?alt=media&token=1487e418-3e68-43f8-ac66-44ece7afb3d5"; // 실제 URL로 교체
                break;
            case "DRINK":
                url = "https://firebasestorage.googleapis.com/v0/b/mohassu-98a30.firebasestorage.app/o/marker_icons%2Fic_promise_drink_marker.png?alt=media&token=1b55929a-9db4-489c-8eca-67f554820bf6"; // 실제 URL로 교체
                break;
            case "STUDY":
                url = "https://firebasestorage.googleapis.com/v0/b/mohassu-98a30.firebasestorage.app/o/marker_icons%2Fic_promise_study_marker.png?alt=media&token=2a595f5e-84ba-4c14-8ab5-36c23c2f1d77"; // 실제 URL로 교체
                break;
            case "ETC":
                url = "https://firebasestorage.googleapis.com/v0/b/mohassu-98a30.firebasestorage.app/o/marker_icons%2Fic_promise_etc_marker.png?alt=media&token=4f0fa4a3-906c-40ed-8357-823c5c788b1c"; // 실제 URL로 교체
                break;
            default:
                url = "https://firebasestorage.googleapis.com/v0/b/mohassu-98a30.firebasestorage.app/o/marker_icons%2Fic_promise_marker.png?alt=media&token=ac6771b1-ea80-447e-9bc5-2e98559fa676"; // 실제 URL로 교체
                break;
        }
        Log.d(TAG, "Marker Icon URL for promise type '" + promiseType + "': " + url); // 로그 추가
        return url;
    }

    // 지오펜싱 로직 예시
    private String getGeofenceName(GeoPoint place) {
        double lat1 = place.getLatitude();
        double lon1 = place.getLongitude();
        for (PlaceInfo placeInfo : Constants.PLACES) {
            double lat2 = placeInfo.getLocation().latitude;
            double lon2 = placeInfo.getLocation().longitude;
            float[] results = new float[1];
            android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results);
            if (results[0] <= placeInfo.getRadius()) {
                Log.d(TAG, "Geofence matched: " + placeInfo.getName() + " for Promise location."); // 로그 추가
                return placeInfo.getName();
            }
        }
        Log.d(TAG, "No Geofence matched for Promise location."); // 로그 추가
        return null;
    }

    /**
     * 타임스탬프를 조건에 맞게 포맷팅
     *
     * @param timestamp Firestore 타임스탬프
     * @return 포맷팅된 시간 문자열
     */
    private String formatTimestamp(Timestamp timestamp) {
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
        Log.d(TAG, "Formatted time: " + formattedTime); // 로그 추가
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
        Log.d(TAG, "Is Tomorrow: " + isTomorrow); // 로그 추가
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
        ImageButton editButton; // 추가

        public PromiseViewHolder(@NonNull View itemView) {
            super(itemView);
            staticMapImage = itemView.findViewById(R.id.map_view);
            tvTitle = itemView.findViewById(R.id.tv_promise_title);
            tvLocation = itemView.findViewById(R.id.tv_promise_location);
            tvTime = itemView.findViewById(R.id.tv_promise_time);
            imgProfile = itemView.findViewById(R.id.img_profile);
            editButton = itemView.findViewById(R.id.edit_promise_button); // 추가
        }
    }
}
