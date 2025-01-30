package com.christianserwedevs.doslocator.Fragments;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import com.christianserwedevs.doslocator.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class SOSDialogFragment extends DialogFragment {

    private static final String ARG_CHILD_NAME = "childName";
    private static final String ARG_TIMESTAMP = "timestamp";
    private static final String ARG_PARENT_EMAIL = "parentEmail";
    private static final String ARG_DOCUMENT_NAME = "documentName";  // Unique document name
    private static final String ARG_LATITUDE = "latitude";
    private static final String ARG_LONGITUDE = "longitude";

    private FirebaseFirestore firestoreDatabase;
    private Vibrator vibrator;
    private Ringtone ringtone;

    private double childLatitude;
    private double childLongitude;

    public static SOSDialogFragment newInstance(String childName, String timestamp, String parentEmail, String documentName, double latitude, double longitude) {
        SOSDialogFragment fragment = new SOSDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHILD_NAME, childName);
        args.putString(ARG_TIMESTAMP, timestamp);
        args.putString(ARG_PARENT_EMAIL, parentEmail);
        args.putString(ARG_DOCUMENT_NAME, documentName);
        args.putDouble(ARG_LATITUDE, latitude);
        args.putDouble(ARG_LONGITUDE, longitude);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_sos_alert, container, false);
        firestoreDatabase = FirebaseFirestore.getInstance();  // Initialize Firestore

        TextView childNameTextView = view.findViewById(R.id.child_name);
        TextView timestampTextView = view.findViewById(R.id.timestamp);
        Button acknowledgeButton = view.findViewById(R.id.acknowledge_button);

        if (getArguments() != null) {
            String childName = getArguments().getString(ARG_CHILD_NAME);
            String timestamp = getArguments().getString(ARG_TIMESTAMP);
            childLatitude = getArguments().getDouble(ARG_LATITUDE);
            childLongitude = getArguments().getDouble(ARG_LONGITUDE);

            childNameTextView.setText(childName);
            timestampTextView.setText("Time of Send: " + timestamp);
        }
        startVibrationAndSound();

        acknowledgeButton.setOnClickListener(v -> {
            stopVibrationAndSound();
            acknowledgeSOSAlert();
            moveCameraToChildLocation();  // Move the Google Map camera
        });

        return view;
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
                    .update("isActive", false)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "SOS alert acknowledged!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to acknowledge SOS alert: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void moveCameraToChildLocation() {
        if (getActivity() instanceof SOSDialogListener) {
            ((SOSDialogListener) getActivity()).onChildLocationRequested(childLatitude, childLongitude);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopVibrationAndSound();
    }

    public interface SOSDialogListener {
        void onChildLocationRequested(double latitude, double longitude);
    }
}
