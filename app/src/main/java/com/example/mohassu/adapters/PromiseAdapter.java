import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mohassu.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

        // 지도 설정
        holder.mapView.onCreate(null);
        holder.mapView.getMapAsync(googleMap -> {
            LatLng location = promise.getLatLng();
            googleMap.addMarker(new MarkerOptions().position(location).title(promise.getLocation()));
            googleMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(location, 15));
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
        public void onMapReady(GoogleMap googleMap) {
            // No action required here; handled in onBindViewHolder
        }
    }
}