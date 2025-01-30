package com.christianserwedevs.doslocator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.List;

public class ChildInfoAdapter extends RecyclerView.Adapter<ChildInfoAdapter.ChildViewHolder> {

    private final Context context;
    private final List<ChildInfo> childrenList;
    private final GoogleMap googleMap;
    private final HashMap<String, Marker> childMarkers;  // Reference to childMarkers from MapFragment

    public ChildInfoAdapter(Context context, List<ChildInfo> childrenList, GoogleMap googleMap, HashMap<String, Marker> childMarkers) {
        this.context = context;
        this.childrenList = childrenList;
        this.googleMap = googleMap;
        this.childMarkers = childMarkers;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_child_info, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        ChildInfo child = childrenList.get(position);

        holder.textViewChildName.setText(child.getFullName());
        holder.textViewChildId.setText(child.getUserId());

        holder.buttonTrackChild.setOnClickListener(v -> {
            String userId = child.getUserId();  // Get child ID
            if (childMarkers.containsKey(userId)) {
                // Focus on the existing marker if available
                Marker existingMarker = childMarkers.get(userId);
                if (existingMarker != null) {
                    LatLng childLocation = existingMarker.getPosition();
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(childLocation, 15));
                    Toast.makeText(context, "Focusing on " + child.getFullName(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Location marker not found for " + child.getFullName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return childrenList.size();
    }

    public static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView textViewChildName, textViewChildId;
        Button buttonTrackChild;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewChildName = itemView.findViewById(R.id.textViewChildName);
            textViewChildId = itemView.findViewById(R.id.textViewChildId);
            buttonTrackChild = itemView.findViewById(R.id.buttonTrackChild);
        }
    }
}
