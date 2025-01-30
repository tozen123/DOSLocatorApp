package com.christianserwedevs.doslocator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.christianserwedevs.doslocator.Model.SOSAlertInfo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SOSAlertsAdapter extends RecyclerView.Adapter<SOSAlertsAdapter.ViewHolder> {
    private ArrayList<SOSAlertInfo> alertsList;
    private GoogleMap googleMap;
    private DialogFragment dialogFragment;

    public SOSAlertsAdapter(ArrayList<SOSAlertInfo> alertsList, GoogleMap googleMap, DialogFragment dialogFragment) {
        this.alertsList = alertsList;
        this.googleMap = googleMap;
        this.dialogFragment = dialogFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sos_alert, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SOSAlertInfo alert = alertsList.get(position);
        holder.childName.setText(alert.childName);
        holder.timestamp.setText("Time: " + alert.timestamp);
        holder.location.setText("Location: " + alert.location);

        holder.trackButton.setOnClickListener(v -> {
            LatLng alertLocation = new LatLng(alert.latitude, alert.longitude);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(alertLocation, 18));
            Toast.makeText(holder.itemView.getContext(), "Tracking " + alert.childName, Toast.LENGTH_SHORT).show();
            dialogFragment.dismiss();
        });

//        holder.acknowledgeButton.setOnClickListener(v -> {
//            acknowledgeSOSAlert(alert.documentId, holder);
//        });
    }

    @Override
    public int getItemCount() {
        return alertsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView childName, timestamp, location;
        //Button trackButton, acknowledgeButton;
        Button trackButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            childName = itemView.findViewById(R.id.child_name);
            timestamp = itemView.findViewById(R.id.timestamp);
            location = itemView.findViewById(R.id.location);
            trackButton = itemView.findViewById(R.id.track_button);
            //acknowledgeButton = itemView.findViewById(R.id.acknowledge_button);
        }
    }

//    private void acknowledgeSOSAlert(String documentId, ViewHolder holder) {
//        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
//        firestore.collection("sos_alerts").document(documentId)
//                .update("isActive", false)
//                .addOnSuccessListener(aVoid -> {
//                    Toast.makeText(holder.itemView.getContext(), "SOS Alert acknowledged!", Toast.LENGTH_SHORT).show();
//                    alertsList.remove(holder.getAdapterPosition());
//                    notifyItemRemoved(holder.getAdapterPosition());
//                })
//                .addOnFailureListener(e -> Toast.makeText(holder.itemView.getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//    }
}
