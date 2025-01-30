package com.christianserwedevs.doslocator.Fragments;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.christianserwedevs.doslocator.Fragments.MainNavigation.MapFragment;
import com.christianserwedevs.doslocator.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SOSDialogResponderFragment extends DialogFragment {

    private static final String ARG_CHILD_NAME = "childName";
    private static final String ARG_TIMESTAMP = "timestamp";
    private static final String ARG_DOCUMENT_NAME = "documentName";
    private static final String ARG_LATITUDE = "latitude";
    private static final String ARG_LONGITUDE = "longitude";

    private FirebaseFirestore firestoreDatabase;
    private Vibrator vibrator;
    private Ringtone ringtone;

    private double childLatitude;
    private double childLongitude;
    private String childAddress = "Retrieving...";

    public static SOSDialogResponderFragment newInstance(String childName, String timestamp, String documentName, double latitude, double longitude) {
        SOSDialogResponderFragment fragment = new SOSDialogResponderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHILD_NAME, childName);
        args.putString(ARG_TIMESTAMP, timestamp);
        args.putString(ARG_DOCUMENT_NAME, documentName);
        args.putDouble(ARG_LATITUDE, latitude);
        args.putDouble(ARG_LONGITUDE, longitude);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_sos_alert_responder, container, false);
        firestoreDatabase = FirebaseFirestore.getInstance();

        TextView childNameTextView = view.findViewById(R.id.child_name);
        TextView timestampTextView = view.findViewById(R.id.timestamp);
        TextView locationInfoTextView = view.findViewById(R.id.location_info);
        Button acknowledgeButton = view.findViewById(R.id.acknowledge_button);

        if (getArguments() != null) {
            String childName = getArguments().getString(ARG_CHILD_NAME);
            String timestamp = getArguments().getString(ARG_TIMESTAMP);
            childLatitude = getArguments().getDouble(ARG_LATITUDE);
            childLongitude = getArguments().getDouble(ARG_LONGITUDE);

            childNameTextView.setText(childName);
            timestampTextView.setText("Time of Emergency: " + timestamp);
        }

        // **Retrieve Address and Update UI**
        locationInfoTextView.setText("Location: " + childLatitude + ", " + childLongitude + " | " + getAddressFromLocation(childLatitude, childLongitude));

        startVibrationAndSound();

        acknowledgeButton.setText("Respond to SOS and Track");
        acknowledgeButton.setOnClickListener(v -> {
            stopVibrationAndSound();
            acknowledgeSOSAlert();
            moveCameraToChildLocation();
        });

        return view;
    }
    public String getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        String addressText = "Unknown Location";

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                String streetAddress = address.getThoroughfare();  // Street/Road
                String subLocality = address.getSubLocality();  // Neighborhood/Barangay
                String locality = address.getLocality();  // Town/City
                String subAdminArea = address.getSubAdminArea();  // Province
                String adminArea = address.getAdminArea();  // Region
                String postalCode = address.getPostalCode();  // Postal Code
                String country = address.getCountryName();  // Country

                StringBuilder fullAddress = new StringBuilder();

                // Append each part only if it's not null, adding a comma only when necessary
                if (streetAddress != null) {
                    fullAddress.append(streetAddress);
                }
                if (subLocality != null) {
                    if (fullAddress.length() > 0) fullAddress.append(", ");
                    fullAddress.append(subLocality);
                }
                if (locality != null) {
                    if (fullAddress.length() > 0) fullAddress.append(", ");
                    fullAddress.append(locality);
                }
                if (subAdminArea != null) {
                    if (fullAddress.length() > 0) fullAddress.append(", ");
                    fullAddress.append(subAdminArea);  // Province
                }
                if (adminArea != null) {
                    if (fullAddress.length() > 0) fullAddress.append(", ");
                    fullAddress.append(adminArea);  // Region
                }
                if (postalCode != null) {
                    if (fullAddress.length() > 0) fullAddress.append(", ");
                    fullAddress.append(postalCode);
                }
                if (country != null) {
                    if (fullAddress.length() > 0) fullAddress.append(", ");
                    fullAddress.append(country);
                }

                addressText = fullAddress.toString().trim();  // Final full address
            }
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Failed to get address: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return addressText;
    }


    private void startVibrationAndSound() {
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            VibrationEffect vibrationEffect = VibrationEffect.createWaveform(
                    new long[]{0, 500, 1000, 500},
                    0
            );
            vibrator.vibrate(vibrationEffect);
        }

        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        ringtone = RingtoneManager.getRingtone(requireContext(), alarmUri);
        ringtone.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build());
        ringtone.play();
    }

    private void stopVibrationAndSound() {
        if (vibrator != null) {
            vibrator.cancel();
        }

        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    private void acknowledgeSOSAlert() {
        if (getArguments() != null) {
            String documentName = getArguments().getString(ARG_DOCUMENT_NAME);

            firestoreDatabase.collection("sos_alerts").document(documentName)
                    .update("isResponded", true)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "You are now responding to the SOS alert!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to accept SOS alert: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void moveCameraToChildLocation() {
        if (getActivity() instanceof SOSDialogResponderListener) {
            ((SOSDialogResponderListener) getActivity()).onResponderLocationRequested(childLatitude, childLongitude);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopVibrationAndSound();
    }

    public interface SOSDialogResponderListener {
        void onResponderLocationRequested(double latitude, double longitude);
    }
}
