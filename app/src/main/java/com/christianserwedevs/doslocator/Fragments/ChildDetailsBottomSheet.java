package com.christianserwedevs.doslocator.Fragments.MainNavigation;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.christianserwedevs.doslocator.MainActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.christianserwedevs.doslocator.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class ChildDetailsBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_NAME = "name";
    private static final String ARG_BIRTHDATE = "birthdate";
    private static final String ARG_CONTACT_NUMBER = "contactNumber";
    private static final String ARG_LAST_KNOWN = "lastKnown";
    private static final String ARG_USER_ID = "null";
    private ImageButton openMessaging, openGeoBoundary;

    private FirebaseFirestore firestore;
    public static ChildDetailsBottomSheet newInstance(String name, String birthdate, String contactNumber, String lastKnown, String userId) {
        ChildDetailsBottomSheet fragment = new ChildDetailsBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_BIRTHDATE, birthdate);
        args.putString(ARG_CONTACT_NUMBER, contactNumber);
        args.putString(ARG_LAST_KNOWN, lastKnown);
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_child_details, container, false);

        TextView nameTextView = view.findViewById(R.id.child_name);
        TextView birthdateTextView = view.findViewById(R.id.child_birthdate);
        TextView contactNumberTextView = view.findViewById(R.id.child_contact);
        TextView lastKnown = view.findViewById(R.id.child_lastknown_location);
        openMessaging = view.findViewById(R.id.openMessaging);
        openGeoBoundary = view.findViewById(R.id.openGeoBoundary);

        firestore = FirebaseFirestore.getInstance();


        openMessaging.setOnClickListener(v -> openMessagingFragment());
        if (getArguments() != null) {
            nameTextView.setText(getArguments().getString(ARG_NAME));
            birthdateTextView.setText(getArguments().getString(ARG_BIRTHDATE));
            contactNumberTextView.setText(getArguments().getString(ARG_CONTACT_NUMBER));
            lastKnown.setText(getArguments().getString(ARG_LAST_KNOWN));
        }


        openGeoBoundary.setOnClickListener(v -> openBoundarySetup());



        return view;
    }
    private void openMessagingFragment() {
        FragmentActivity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).switchFragment(((MainActivity) activity).messagesFragment);
        }
        dismiss(); // Close the BottomSheet
    }

    private void openBoundarySetup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_boundary_setup, null);
        builder.setView(dialogView);

        EditText radiusInput = dialogView.findViewById(R.id.radiusInput);
        Spinner durationSpinner = dialogView.findViewById(R.id.durationSpinner);
        Button saveButton = dialogView.findViewById(R.id.saveBoundary);
        Button cancelButton = dialogView.findViewById(R.id.cancelBoundary);

        // Set up the Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.boundary_durations,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            String radiusStr = radiusInput.getText().toString();
            String duration = durationSpinner.getSelectedItem().toString();


            if (radiusStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a radius", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double radius = Double.parseDouble(radiusStr); // Convert string to double

                if (radius < 80) {  // Corrected comparison
                    Toast.makeText(requireContext(), "Value too low", Toast.LENGTH_SHORT).show();
                    return;  // Stop execution if the radius is too low
                }

                // Continue with saving the boundary if valid
                String userId = getArguments().getString(ARG_USER_ID);
                fetchCurrentLocationAndSaveBoundary(userId, radius, durationSpinner.getSelectedItem().toString());

            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid input. Please enter a valid number.", Toast.LENGTH_SHORT).show();
            }

            double radius = Double.parseDouble(radiusStr);
            String userId = getArguments().getString(ARG_USER_ID);
            fetchCurrentLocationAndSaveBoundary(userId, radius, duration);

            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void fetchCurrentLocationAndSaveBoundary(String userId, double radius, String duration) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "User ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userRef = firestore.collection("children").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                double latitude = documentSnapshot.getDouble("latitude");
                double longitude = documentSnapshot.getDouble("longitude");

                Log.d("Firestore", "Fetched location: Lat " + latitude + ", Lng " + longitude);
                saveBoundaryToFirestore(userId, radius, duration, latitude, longitude);
            } else {
                Toast.makeText(requireContext(), "Location data not found!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Failed to fetch location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Firestore", "Error fetching location", e);
        });
    }

    private void saveBoundaryToFirestore(String userId, double radius, String duration, double latitude, double longitude) {
        String boundaryId = UUID.randomUUID().toString();
        String startDate = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> boundaryDetails = new HashMap<>();
        boundaryDetails.put("boundary_details_id", boundaryId);
        boundaryDetails.put("latitude", latitude);
        boundaryDetails.put("longitude", longitude);
        boundaryDetails.put("radius", radius);
        boundaryDetails.put("duration", duration);
        boundaryDetails.put("startDate", startDate);

        firestore.collection("children").document(userId)
                .update("boundary_details", com.google.firebase.firestore.FieldValue.arrayUnion(boundaryDetails))
                .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Boundary saved successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to save boundary: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error saving boundary", e);
                });
    }
}
