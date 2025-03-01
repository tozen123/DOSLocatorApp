package com.christianserwedevs.doslocator.Fragments.MainNavigation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.christianserwedevs.doslocator.ChildInfo;
import com.christianserwedevs.doslocator.ChildInfoAdapter;
import com.christianserwedevs.doslocator.Fragments.ChildrenListDialogFragment;
import com.christianserwedevs.doslocator.Fragments.ParentDetailsBottomSheet;
import com.christianserwedevs.doslocator.Fragments.SOSAlertsDialogFragment;
import com.christianserwedevs.doslocator.Fragments.SOSDialogFragment;
import com.christianserwedevs.doslocator.Fragments.SOSDialogResponderFragment;
import com.christianserwedevs.doslocator.MainActivity;
import com.christianserwedevs.doslocator.Model.SOSAlertInfo;
import com.christianserwedevs.doslocator.Prompts.ConfirmationDialog;
import com.christianserwedevs.doslocator.R;
import com.christianserwedevs.doslocator.SOSAlertService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback, SOSDialogFragment.SOSDialogListener {

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private FirebaseFirestore firestoreDatabase;

    private boolean isFirstUpdate = true;  // To track the first location update
    private boolean isMapBeingMovedByUser = false;  // To track user interaction with the map

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final float LOCATION_CHANGE_THRESHOLD = 10;  // Threshold in meters

    private Location lastKnownLocation;

    private ExtendedFloatingActionButton fabParent, fabChild, fab_sos_alerts,fab_sos_can_still_track;
    private ImageButton fabGoToMyLocation, fabSos;
    private com.google.android.gms.maps.model.Marker userLocationMarker;
    private final HashMap<String, Marker> childMarkers = new HashMap<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);


        fabParent = rootView.findViewById(R.id.fab_parent);
        fab_sos_alerts = rootView.findViewById(R.id.fab_sos_alerts);
        fabChild = rootView.findViewById(R.id.fab_child);
        fabGoToMyLocation = rootView.findViewById(R.id.fab_gotomylocation);
        fabSos = rootView.findViewById(R.id.fab_sos);
        fab_sos_can_still_track = rootView.findViewById(R.id.fab_sos_can_still_track);


        fabGoToMyLocation.setOnClickListener(v -> moveToMyLocation());

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("LoginPrefs", requireContext().MODE_PRIVATE);
        String userType = sharedPreferences.getString("userType", null);
        String parentEmail = sharedPreferences.getString("email", null);

        setFabVisibility(userType);

        fabChild.setOnClickListener(v -> showChildrenListDialog(parentEmail));
        fabParent.setOnClickListener(v -> moveToParentLocation());
        fabSos.setOnClickListener(v -> initiateSOS());
        fab_sos_alerts.setOnClickListener(v -> showAlertsList());
        fab_sos_can_still_track.setOnClickListener(v -> stopTrackingSOS());


        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        firestoreDatabase = FirebaseFirestore.getInstance();



        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("LoginPrefs", requireContext().MODE_PRIVATE);
        String userType = sharedPreferences.getString("userType", null);
        String parentEmail = sharedPreferences.getString("email", null);
        Intent serviceIntent = new Intent(requireContext(), SOSAlertService.class);
        serviceIntent.putExtra("userType", userType);
        serviceIntent.putExtra("parentEmail", parentEmail);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().startForegroundService(serviceIntent);
        } else {
            requireActivity().startService(serviceIntent);
        }
    }
    private void showAlertsList() {
        firestoreDatabase.collection("sos_alerts")
                .whereEqualTo("KeepTracking", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot querySnapshot = task.getResult();
                        ArrayList<SOSAlertInfo> alertsList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String documentId = document.getId();
                            String childName = document.getString("childName");
                            String timestamp = document.getString("timestamp");
                            Double latitude = document.getDouble("latitude");
                            Double longitude = document.getDouble("longitude");

                            String location = getAddressFromLocation(latitude, longitude); // Convert lat/lng to address
                            alertsList.add(new SOSAlertInfo(documentId, childName, timestamp, location, latitude, longitude));
                        }

                        if (!alertsList.isEmpty()) {
                            showSOSAlertsDialog(alertsList);
                        } else {
                            Toast.makeText(requireContext(), "No active SOS alerts found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch SOS alerts.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void showSOSAlertsDialog(ArrayList<SOSAlertInfo> alertsList) {
        SOSAlertsDialogFragment dialogFragment = new SOSAlertsDialogFragment(alertsList, googleMap);
        dialogFragment.show(getChildFragmentManager(), "sos_alerts_list");
    }

    private void initiateSOS() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("LoginPrefs", requireContext().MODE_PRIVATE);
        String userType = sharedPreferences.getString("userType", null);
        String userId = sharedPreferences.getString("userId", null);
        if ("children".equals(userType)) {
            // Get child's details to send the SOS
            firestoreDatabase.collection("children").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String parentEmail = documentSnapshot.getString("parentEmail");
                            String childName = documentSnapshot.getString("firstName") + " " +
                                    (documentSnapshot.getString("middleName") != null ? documentSnapshot.getString("middleName") + " " : "") +
                                    documentSnapshot.getString("lastName");

                            if (parentEmail != null) {
                                sendSOSAlertToParentandResponder(parentEmail, childName, userId);
                            } else {
                                Toast.makeText(requireContext(), "Parent email not found!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "Child document not found!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to initiate SOS: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }



    }

    private void sendSOSAlertToParentandResponder(String parentEmail, String childName, String userId) {
        String timeStamp = new SimpleDateFormat("dd-MM-yy_HH:mm:ss", Locale.getDefault()).format(new Date());
        String documentName = parentEmail + "_" + timeStamp;

        firestoreDatabase.collection("children").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double latitude = documentSnapshot.getDouble("latitude");
                        Double longitude = documentSnapshot.getDouble("longitude");

                        HashMap<String, Object> sosData = new HashMap<>();
                        sosData.put("childName", childName);
                        sosData.put("userId", userId);
                        sosData.put("timestamp", timeStamp);
                        sosData.put("isActive", true);
                        sosData.put("isResponded", false);
                        sosData.put("latitude", latitude);
                        sosData.put("longitude", longitude);
                        sosData.put("KeepTracking", true);

                        firestoreDatabase.collection("sos_alerts").document(documentName)
                                .set(sosData)
                                .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "SOS sent to parent and responders!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to send SOS alert: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(requireContext(), "Child location not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to fetch child location: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }



    private void listenForSOSAlerts(String parentEmail) {
        firestoreDatabase.collection("sos_alerts")
                .whereGreaterThanOrEqualTo(FieldPath.documentId(), parentEmail)
                .whereLessThanOrEqualTo(FieldPath.documentId(), parentEmail + "\uf8ff")
                .whereEqualTo("isActive", true)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(requireContext(), "Error listening for SOS alerts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String childName = document.getString("childName");
                            String timestamp = document.getString("timestamp");
                            String documentName = document.getId();
                            Double latitude = document.getDouble("latitude");
                            Double longitude = document.getDouble("longitude");

                            showSOSAlertDialog(childName, timestamp, parentEmail, documentName, latitude, longitude);
                        }
                    }
                });
    }

    private void listenForSOSAlertsResponder( ) {
        firestoreDatabase.collection("sos_alerts")
                .whereEqualTo("isResponded", false)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(requireContext(), "Error listening for SOS alerts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String childName = document.getString("childName");
                            String timestamp = document.getString("timestamp");
                            String documentName = document.getId();
                            Double latitude = document.getDouble("latitude");
                            Double longitude = document.getDouble("longitude");

                            if (isAdded() && getActivity() != null) {
                                showSOSAlertDialogResponder(childName, timestamp, documentName, latitude, longitude);
                            }
                        }
                    }
                });
    }




    private void showSOSAlertDialog(String childName, String timestamp, String parentEmail, String documentName, Double latitude, Double longitude) {
        if (!isAdded() || getChildFragmentManager().isStateSaved()) {
            return;  // Prevent crash if the fragment is not in a valid state
        }

        SOSDialogFragment dialogFragment = SOSDialogFragment.newInstance(
                childName,
                timestamp,
                parentEmail,
                documentName,
                latitude,
                longitude
        );
        dialogFragment.show(getChildFragmentManager(), "sos_dialog");
    }


    private void showSOSAlertDialogResponder(String childName, String timestamp, String documentName, Double latitude, Double longitude) {
        if (!isAdded() || getChildFragmentManager().isStateSaved()) {
            return;  // Prevent crash if the fragment is not in a valid state
        }

        SOSDialogResponderFragment dialogFragment = SOSDialogResponderFragment.newInstance(
                childName,
                timestamp,
                documentName,
                latitude,
                longitude
        );
        dialogFragment.show(getChildFragmentManager(), "sos_dialog_responder");
    }


    private void moveToParentLocation() {
        if (parentLocationMarker != null) {
            LatLng parentLatLng = parentLocationMarker.getPosition();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(parentLatLng, 15));  // Zoom level 15 for a close view
            Toast.makeText(requireContext(), "Moved to parent's location", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Parent's location is not available yet.", Toast.LENGTH_SHORT).show();
        }
    }




    private HashMap<String, Circle> childBoundaryCircles = new HashMap<>(); // Store circle references

    private void checkBoundaryExpiration(String userId) {
        firestoreDatabase.collection("children").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("boundary_details")) {
                        List<Map<String, Object>> boundaryDetailsList = (List<Map<String, Object>>) documentSnapshot.get("boundary_details");

                        if (boundaryDetailsList != null && !boundaryDetailsList.isEmpty()) {
                            // Get the most recent boundary (last item in the list)
                            Map<String, Object> latestBoundary = boundaryDetailsList.get(boundaryDetailsList.size() - 1);

                            if (latestBoundary.containsKey("startDate") && latestBoundary.containsKey("duration") &&
                                    latestBoundary.containsKey("latitude") && latestBoundary.containsKey("longitude") &&
                                    latestBoundary.containsKey("radius")) {

                                String startDateStr = latestBoundary.get("startDate").toString();
                                String durationStr = latestBoundary.get("duration").toString();
                                double latitude = Double.parseDouble(latestBoundary.get("latitude").toString());
                                double longitude = Double.parseDouble(latestBoundary.get("longitude").toString());
                                double radius = Double.parseDouble(latestBoundary.get("radius").toString());

                                // Convert `startDate` from string to Date
                                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
                                try {
                                    Date startDate = sdf.parse(startDateStr);
                                    long durationMillis = getDurationInMillis(durationStr); // Convert duration to milliseconds
                                    long expirationTime = startDate.getTime() + durationMillis; // Expiration timestamp

                                    long currentTime = System.currentTimeMillis(); // Get current system time

                                    if (currentTime >= expirationTime) {
                                        // 🔥 Boundary has expired - Remove it
                                        removeBoundary(userId);
                                    } else {
                                        // 🔥 Draw the boundary if it's still active
                                        LatLng boundaryCenter = new LatLng(latitude, longitude);

                                        if (childBoundaryCircles.containsKey(userId)) {
                                            // Update existing circle
                                            childBoundaryCircles.get(userId).setCenter(boundaryCenter);
                                            childBoundaryCircles.get(userId).setRadius(radius);
                                        } else {
                                            // Create new circle
                                            Circle newCircle = googleMap.addCircle(new CircleOptions()
                                                    .center(boundaryCenter)
                                                    .radius(radius)  // Radius in meters
                                                    .strokeWidth(2f)
                                                    .fillColor(0x55FFFFFF)); // 🔥 Semi-transparent WHITE fill
                                            childBoundaryCircles.put(userId, newCircle);
                                        }
                                    }

                                } catch (ParseException e) {
                                    Log.e("Boundary", "Error parsing startDate: " + e.getMessage());
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error checking boundary expiration", e));
    }

    private long getDurationInMillis(String durationStr) {
        switch (durationStr) {
            case "30 minutes":
                return 30 * 60 * 1000; // 30 minutes in milliseconds
            case "1 hour":
                return 60 * 60 * 1000; // 1 hour in milliseconds
            case "8 hours":
                return 8 * 60 * 60 * 1000; // 8 hours in milliseconds
            case "24 hours":
                return 24 * 60 * 60 * 1000; // 24 hours in milliseconds
            default:
                return 0; // Default case (should not happen)
        }
    }


    private Handler boundaryHandler = new Handler(Looper.getMainLooper());
    private final long CHECK_INTERVAL = 60 * 1000; // 60 seconds (1 minute)

    private void startBoundaryExpirationChecker(String userId) {
        checkBoundaryExpiration(userId);
        boundaryHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkBoundaryExpiration(userId);
                boundaryHandler.postDelayed(this, CHECK_INTERVAL); // Run again after 1 minute
            }
        }, CHECK_INTERVAL);
    }


    private void removeBoundary(String userId) {
        // Remove the circle from the map
        if (childBoundaryCircles.containsKey(userId)) {
            childBoundaryCircles.get(userId).remove();
            childBoundaryCircles.remove(userId);
            Log.d("Boundary", "Boundary removed for user: " + userId);
        }

        // Delete boundary details from Firestore
        firestoreDatabase.collection("children").document(userId)
                .update("boundary_details", FieldValue.delete()) // Deletes the entire boundary_details array
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Boundary details deleted for user: " + userId))
                .addOnFailureListener(e -> Log.e("Firestore", "Error deleting boundary details", e));
    }


    private HashMap<String, Circle> childCircles = new HashMap<>();



    private void autoTrackAllChildren(String parentEmail) {
        if (parentEmail == null) {
            Toast.makeText(requireContext(), "No email found in preferences.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(requireContext(), "Tracking children in real-time", Toast.LENGTH_SHORT).show();

        firestoreDatabase.collection("children")
                .whereEqualTo("parentEmail", parentEmail)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(requireContext(), "Error listening for updates: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        HashMap<String, LatLng> updatedChildLocations = new HashMap<>();

                        for (QueryDocumentSnapshot document : value) {
                            String userId = document.getId();
                            String firstName = document.getString("firstName");
                            String middleName = document.getString("middleName");
                            String lastName = document.getString("lastName");
                            Double latitude = document.getDouble("latitude");
                            Double longitude = document.getDouble("longitude");

                            if (latitude == null || longitude == null) {
                                continue;  // Skip if no location data is available
                            }

                            String fullName = firstName;
                            LatLng childLocation = new LatLng(latitude, longitude);
                            checkGeofenceViolation(userId, childLocation);
                            // Adjust position if multiple markers have the same location
                            childLocation = adjustOverlappingMarkerPosition(updatedChildLocations, childLocation);

                            updatedChildLocations.put(userId, childLocation);

                            if (childMarkers.containsKey(userId)) {
                                // Update existing marker position
                                Marker existingMarker = childMarkers.get(userId);
                                if (existingMarker != null) {
                                    existingMarker.setPosition(childLocation);
                                }
                            } else {
                                // Add new marker if it doesn't exist
                                Marker newMarker = googleMap.addMarker(new MarkerOptions()
                                        .position(childLocation)
                                        .title(fullName)
                                        .icon(BitmapDescriptorFactory.fromBitmap(
                                                getMarkerBitmapFromView(requireContext(), R.layout.marker_child, fullName)))
                                        .anchor(0.5f, 1.0f)

                                );
                                childMarkers.put(userId, newMarker);
                            }

                            if (document.contains("boundary_details")) {
                                fetchBoundaryAndDrawCircle(userId, childLocation);
                            } else {
                                if (childCircles.containsKey(userId)) {
                                    childCircles.get(userId).remove();
                                    childCircles.remove(userId);
                                }
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "No children associated with this account.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void checkGeofenceViolation(String userId, LatLng childLocation) {
        firestoreDatabase.collection("children").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("boundary_details")) {
                        List<Map<String, Object>> boundaryDetailsList = (List<Map<String, Object>>) documentSnapshot.get("boundary_details");

                        if (boundaryDetailsList != null && !boundaryDetailsList.isEmpty()) {
                            // Get the most recent boundary (last item in the list)
                            Map<String, Object> latestBoundary = boundaryDetailsList.get(boundaryDetailsList.size() - 1);

                            if (latestBoundary.containsKey("latitude") &&
                                    latestBoundary.containsKey("longitude") &&
                                    latestBoundary.containsKey("radius")) {

                                double boundaryLat = Double.parseDouble(latestBoundary.get("latitude").toString());
                                double boundaryLng = Double.parseDouble(latestBoundary.get("longitude").toString());
                                double radius = Double.parseDouble(latestBoundary.get("radius").toString());

                                LatLng boundaryCenter = new LatLng(boundaryLat, boundaryLng);

                                // 🔥 Calculate distance between child's location and boundary center
                                float[] distance = new float[1];
                                Location.distanceBetween(childLocation.latitude, childLocation.longitude,
                                        boundaryCenter.latitude, boundaryCenter.longitude, distance);

                                if (distance[0] > radius) {
                                    // 🔥 Child is OUTSIDE the boundary - Notify parent
                                    triggerGeofenceAlert(userId);
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching boundary details", e));
    }


    private HashSet<String> alertedChildren = new HashSet<>(); // Store already alerted children

    private void triggerGeofenceAlert(String userId) {
        if (alertedChildren.contains(userId)) {
            return; // 🔥 Prevent duplicate alerts
        }

        triggerGeofenceNotification(userId);

        alertedChildren.add(userId);

        // Get child details
        firestoreDatabase.collection("children").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String childName = documentSnapshot.getString("firstName") + " " + documentSnapshot.getString("lastName");

                        // Get current timestamp
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
                        String currentTime = sdf.format(new Date());

                        // Inflate custom dialog layout
                        LayoutInflater inflater = LayoutInflater.from(requireContext());
                        View dialogView = inflater.inflate(R.layout.dialog_geofence_alert, null);

                        // Set child name and timestamp
                        TextView nameTextView = dialogView.findViewById(R.id.child_name);
                        TextView timestampTextView = dialogView.findViewById(R.id.timestamp);
                        nameTextView.setText(childName);
                        timestampTextView.setText("Time: " + currentTime);

                        // Show custom dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                        builder.setView(dialogView);
                        AlertDialog dialog = builder.create();
                        dialog.show();

                        // Handle button click
                        Button okayButton = dialogView.findViewById(R.id.okay_button);
                        okayButton.setOnClickListener(v -> dialog.dismiss());

                        // 🔥 Remove the child from alertedChildren after a delay (e.g., 5 min)
                        new Handler(Looper.getMainLooper()).postDelayed(() -> alertedChildren.remove(userId), 5 * 60 * 1000);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching child details", e));
    }

    private void triggerGeofenceNotification(String userId) {
        firestoreDatabase.collection("children").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String childName = documentSnapshot.getString("firstName") + " " + documentSnapshot.getString("lastName");

                        // Get current timestamp
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
                        String currentTime = sdf.format(new Date());

                        // 🔥 Send notification
                        sendGeofenceNotification(childName, currentTime);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching child details", e));
    }

    private static final String CHANNEL_ID = "GeofenceAlertChannel";

    private void sendGeofenceNotification(String childName, String timestamp) {
        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // 🔥 Create a Notification Channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Geofence Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        // 🔥 Intent to open MainActivity when notification is clicked
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.putExtra("open_map_fragment", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 🔥 Build the notification
        Notification notification = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setContentTitle("Boundary Alert")
                .setContentText(childName + " has exited their boundary at " + timestamp)
                .setSmallIcon(R.drawable.logo_app) // Change to your alert icon
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        // 🔥 Send notification
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), notification);
        }
    }

    private void fetchBoundaryAndDrawCircle(String userId, LatLng childLocation) {
        firestoreDatabase.collection("children").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("boundary_details")) {
                        List<Map<String, Object>> boundaryDetailsList = (List<Map<String, Object>>) documentSnapshot.get("boundary_details");

                        if (boundaryDetailsList != null && !boundaryDetailsList.isEmpty()) {
                            Map<String, Object> latestBoundary = boundaryDetailsList.get(boundaryDetailsList.size() - 1);

                            if (latestBoundary.containsKey("radius")) {
                                double radius = Double.parseDouble(latestBoundary.get("radius").toString());

                                if (childCircles.containsKey(userId)) {
                                    childCircles.get(userId).setCenter(childLocation);
                                    childCircles.get(userId).setRadius(radius);
                                } else {
                                    Circle newCircle = googleMap.addCircle(new CircleOptions()
                                            .center(childLocation)
                                            .radius(radius)
                                            .strokeWidth(2f)
                                            .fillColor(0x55FFFFFF));
                                    childCircles.put(userId, newCircle);
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching boundary details", e));
    }

    public Bitmap getMarkerBitmapFromView(Context context, int layoutResId, String labelText) {
        // Inflate the custom marker layout
        View customMarkerView = LayoutInflater.from(context).inflate(layoutResId, null);

        // Get the text label and set its text
        TextView label = customMarkerView.findViewById(R.id.label);
        if (label != null) {
            label.setText(labelText);
        }

        // **Force a fixed size to prevent unwanted resizing**
        int width = 250;  // Adjust as needed
        int height = 250; // Adjust as needed
        customMarkerView.setLayoutParams(new ViewGroup.LayoutParams(width, height));

        // **Measure and layout the view properly**
        customMarkerView.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        customMarkerView.layout(0, 0, width, height);

        // **Create a Bitmap with the correct dimensions**
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        customMarkerView.draw(canvas);

        return bitmap;
    }



    public Bitmap getUserMarkerBitmap(Context context, int layoutResId) {
        // Inflate the custom marker layout
        View customMarkerView = LayoutInflater.from(context).inflate(layoutResId, null);

        // Measure and layout the custom view
        customMarkerView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());

        // Convert the custom view to a Bitmap
        Bitmap bitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        customMarkerView.draw(canvas);

        return bitmap;
    }

    /**
     * Adjusts the position of overlapping markers by slightly shifting them.
     */
    private LatLng adjustOverlappingMarkerPosition(HashMap<String, LatLng> existingMarkers, LatLng newLocation) {
        double OFFSET_DISTANCE = 0.00100; // Offset distance in latitude/longitude
        boolean isOverlapping = false;

        for (LatLng existingLocation : existingMarkers.values()) {
            if (Math.abs(existingLocation.latitude - newLocation.latitude) < OFFSET_DISTANCE &&
                    Math.abs(existingLocation.longitude - newLocation.longitude) < OFFSET_DISTANCE) {
                isOverlapping = true;
                break;
            }
        }

        if (isOverlapping) {
            double newLat = newLocation.latitude + (Math.random() * OFFSET_DISTANCE - (OFFSET_DISTANCE / 2));
            double newLng = newLocation.longitude + (Math.random() * OFFSET_DISTANCE - (OFFSET_DISTANCE / 2));
            return new LatLng(newLat, newLng);
        }

        return newLocation;
    }

    @SuppressLint("MissingPermission")
    private void moveToMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                    Toast.makeText(requireContext(), "Moved to your location", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to get your current location", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_SHORT).show();
            requestLocationPermission();
        }
    }
    private void showChildrenListDialog(String parentEmail) {
        if (parentEmail == null) {
            Toast.makeText(requireContext(), "No email found in preferences.", Toast.LENGTH_SHORT).show();
            return;
        }

        firestoreDatabase.collection("children")
                .whereEqualTo("parentEmail", parentEmail)  // Query by parentEmail
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot querySnapshot = task.getResult();
                        ArrayList<ChildInfo> childrenInfoList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String userId = document.getId();
                            String firstName = document.getString("firstName");
                            String middleName = document.getString("middleName");
                            String lastName = document.getString("lastName");
                            Double latitude = document.getDouble("latitude");
                            Double longitude = document.getDouble("longitude");

                            String fullName = firstName + " " + (middleName != null ? middleName + " " : "") + lastName;
                            childrenInfoList.add(new ChildInfo(userId, fullName, latitude, longitude));
                        }

                        if (!childrenInfoList.isEmpty()) {
                            showChildrenRecyclerDialog(childrenInfoList);
                        } else {
                            Toast.makeText(requireContext(), "No children associated with this account.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch children data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showChildrenRecyclerDialog(ArrayList<ChildInfo> childrenInfoList) {
        ChildrenListDialogFragment dialogFragment = new ChildrenListDialogFragment(childrenInfoList, googleMap, childMarkers);
        dialogFragment.show(getChildFragmentManager(), "children_list_dialog");
    }



    private void setFabVisibility(String userType) {
        if ("parents".equals(userType)) {
            fabParent.setVisibility(View.GONE);
            fabChild.setVisibility(View.VISIBLE);
            fabSos.setVisibility(View.GONE);
            fab_sos_alerts.setVisibility(View.GONE);
            fab_sos_can_still_track.setVisibility(View.GONE);
        } else if ("children".equals(userType)) {
            fabParent.setVisibility(View.VISIBLE);
            fab_sos_can_still_track.setVisibility(View.VISIBLE);
            fabChild.setVisibility(View.GONE);
            fab_sos_alerts.setVisibility(View.GONE);

        } else {
            fab_sos_alerts.setVisibility(View.VISIBLE);
            fabSos.setVisibility(View.GONE);
            fabParent.setVisibility(View.GONE);
            fabChild.setVisibility(View.GONE);
            fab_sos_can_still_track.setVisibility(View.GONE);

        }
    }
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        try {
            // Load the custom style from the raw resource file
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style));

            if (!success) {
                Toast.makeText(requireContext(), "Map style parsing failed!", Toast.LENGTH_SHORT).show();
            }
        } catch (Resources.NotFoundException e) {
            Toast.makeText(requireContext(), "Map style not found: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();  // Start listening for location updates
        } else {
            requestLocationPermission();
        }

        // Define the bounding area for Camarines Norte, Philippines
        LatLngBounds camarinesNorteBounds = new LatLngBounds(
                new LatLng(13.7840, 122.3153), // Southwest corner
                new LatLng(14.3741, 123.0055)  // Northeast corner
        );

        // Apply the restriction
        googleMap.setLatLngBoundsForCameraTarget(camarinesNorteBounds);


        //        // Listener to detect when the user moves the map manually
        //        googleMap.setOnCameraMoveStartedListener(reason -> {
        //            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
        //                isMapBeingMovedByUser = true;  // User is moving the map
        //            }
        //        });
        //
        //        googleMap.setOnCameraIdleListener(() -> isMapBeingMovedByUser = false);  // Reset when the map stops moving

        // **Set Maximum Zoom Level**
        googleMap.setMaxZoomPreference(21.0f); // Example: Maximum zoom level 18
        googleMap.setMinZoomPreference(15.0f); // Optional: Set a minimum zoom level

        googleMap.setOnMarkerClickListener(marker -> {
            String userId = getChildIdFromMarker(marker);

            if (userId != null) {
                fetchChildDetailsAndShowBottomSheet(userId);
            } else if (marker.equals(parentLocationMarker)) {
                fetchParentDetailsAndShowBottomSheet();
            }

            return true;
        });


        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("LoginPrefs", requireContext().MODE_PRIVATE);
        String userType = sharedPreferences.getString("userType", null);
        String userId = sharedPreferences.getString("userId", null);

        if ("children".equals(userType)) {
            fetchParentEmailAndTrackParent(userId);
            startBoundaryExpirationChecker(userId);
        } else if ("parents".equals(userType)) {
            String parentEmail = sharedPreferences.getString("email", null);

            listenForSOSAlerts(parentEmail);

            if (parentEmail != null) {
                autoTrackAllChildren(parentEmail);
            }

        } else if("responders".equals(userType)) {
            listenForSOSAlertsResponder();
            trackChildrenInSOS();
        }
        checkIfUserCanStopTracking();
    }
    private void checkIfUserCanStopTracking() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("LoginPrefs", requireContext().MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);

        if (userId == null) {
            fab_sos_can_still_track.setVisibility(View.GONE);
            return;
        }

        firestoreDatabase.collection("sos_alerts")
                .whereEqualTo("userId", userId)
                .whereEqualTo("KeepTracking", true)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(requireContext(), "Error checking tracking status: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        fab_sos_can_still_track.setVisibility(View.VISIBLE);
                    } else {
                        fab_sos_can_still_track.setVisibility(View.GONE);
                    }
                });
    }

    private void stopTrackingSOS() {
        ConfirmationDialog.show(requireContext(),
                "Stop Tracking Confirmation",
                "Are you sure you want to stop responders from tracking you? Stopping this action indicates that you're now safe.",
                new ConfirmationDialog.OnDialogClickListener() {
                    @Override
                    public void onConfirm() {
                        executeStopTracking();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(requireContext(), "Tracking remains active. Responders can still tracks you", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void executeStopTracking() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("LoginPrefs", requireContext().MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);

        if (userId == null) {
            Toast.makeText(requireContext(), "User ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        firestoreDatabase.collection("sos_alerts")
                .whereEqualTo("userId", userId)
                .whereEqualTo("KeepTracking", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Create a copy of the document
                            firestoreDatabase.collection("archived_sos_alerts")
                                    .document(document.getId()) // Keep same document ID
                                    .set(document.getData())
                                    .addOnSuccessListener(aVoid -> {
                                        // After successfully copying, delete from sos_alerts
                                        firestoreDatabase.collection("sos_alerts")
                                                .document(document.getId())
                                                .delete()
                                                .addOnSuccessListener(aVoid1 -> {
                                                    Toast.makeText(requireContext(), "Tracking stopped & SOS archived.", Toast.LENGTH_SHORT).show();
                                                    fab_sos_can_still_track.setVisibility(View.GONE); // Hide button
                                                })
                                                .addOnFailureListener(e ->
                                                        Toast.makeText(requireContext(), "Failed to delete SOS: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                                );
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(requireContext(), "Failed to archive SOS: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );
                        }
                    } else {
                        Toast.makeText(requireContext(), "No active SOS tracking found.", Toast.LENGTH_SHORT).show();
                        fab_sos_can_still_track.setVisibility(View.GONE);
                    }
                });
    }


    private final HashMap<String, Marker> trackedChildrenMarkers = new HashMap<>();

    private void trackChildrenInSOS() {
        firestoreDatabase.collection("sos_alerts")
                .whereEqualTo("KeepTracking", true)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(requireContext(), "Error tracking SOS children: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String userId = document.getString("userId");

                            if (userId != null) {
                                trackChildLocation(userId);
                            }
                        }
                    } else {
                        clearTrackedChildrenMarkers(); // If no SOS is active, remove markers
                    }
                });
    }

    private void trackChildLocation(String userId) {
        firestoreDatabase.collection("children").document(userId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(requireContext(), "Error tracking child: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Double latitude = documentSnapshot.getDouble("latitude");
                        Double longitude = documentSnapshot.getDouble("longitude");
                        String firstName = documentSnapshot.getString("firstName");

                        if (latitude != null && longitude != null && firstName != null) {
                            updateChildMarker(userId, firstName, latitude, longitude);
                        }
                    }
                });
    }

    private void updateChildMarker(String userId, String firstName, double latitude, double longitude) {
        if (!isAdded() || getContext() == null) {
            return; // Ensure the fragment is attached before proceeding
        }

        LatLng childLocation = new LatLng(latitude, longitude);

        if (trackedChildrenMarkers.containsKey(userId)) {
            // Update existing marker position
            Marker existingMarker = trackedChildrenMarkers.get(userId);
            if (existingMarker != null) {
                existingMarker.setPosition(childLocation);
            }
        } else {
            // Add a new marker with custom icon and first name
            Bitmap childMarkerBitmap = getMarkerBitmapFromView(requireContext(), R.layout.marker_child, firstName);

            Marker newMarker = googleMap.addMarker(new MarkerOptions()
                    .position(childLocation)
                    .title(firstName)
                    .icon(BitmapDescriptorFactory.fromBitmap(childMarkerBitmap))
                    .anchor(0.5f, 1.0f)
            );

            trackedChildrenMarkers.put(userId, newMarker);
        }
    }


    private void clearTrackedChildrenMarkers() {
        for (Marker marker : trackedChildrenMarkers.values()) {
            if (marker != null) {
                marker.remove();
            }
        }
        trackedChildrenMarkers.clear();
    }


    private void fetchParentDetailsAndShowBottomSheet() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("LoginPrefs", requireContext().MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);  // Get current user's ID (child's user ID)

        firestoreDatabase.collection("children").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String parentEmail = documentSnapshot.getString("parentEmail");
                        if (parentEmail != null) {
                            // Step 2: Fetch the parent's details using parentEmail
                            fetchParentDetailsByEmail(parentEmail);
                        } else {
                            Toast.makeText(requireContext(), "Parent email not found for this child.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Child document not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Error fetching parent email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fetchParentDetailsByEmail(String parentEmail) {
        firestoreDatabase.collection("parents")
                .whereEqualTo("email", parentEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        String fullName = document.getString("firstName") + " " +
                                (document.getString("middleName") != null ? document.getString("middleName") + " " : "") +
                                document.getString("lastName");
                        String birthdate = document.getString("birthdate");
                        String contactNumber = document.getString("contact");
                        String email = document.getString("email");
                        String lastknown = document.getString("lastKnownLocation");

                        // Show bottom sheet with parent details
                        ParentDetailsBottomSheet bottomSheet = ParentDetailsBottomSheet.newInstance(fullName, birthdate, contactNumber, email, lastknown);
                        bottomSheet.show(getChildFragmentManager(), "parent_details_bottom_sheet");
                    } else {
                        Toast.makeText(requireContext(), "Parent document not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Error fetching parent details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void fetchParentEmailAndTrackParent(String userId) {
        firestoreDatabase.collection("children").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String parentEmail = documentSnapshot.getString("parentEmail");
                        if (parentEmail != null) {
                            trackParentLocation(parentEmail);  // Start tracking the parent's location
                        } else {
                            Toast.makeText(requireContext(), "Parent email not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Child document not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Error fetching parent email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private Marker parentLocationMarker;

    private void trackParentLocation(String parentEmail) {
        firestoreDatabase.collection("parents")
                .whereEqualTo("email", parentEmail)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(requireContext(), "Error tracking parent location: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Double latitude = document.getDouble("latitude");
                            Double longitude = document.getDouble("longitude");

                            if (latitude != null && longitude != null) {
                                LatLng parentLocation = new LatLng(latitude, longitude);
                                updateParentLocationMarker(parentLocation);
                            }
                        }
                    }
                });
    }

    private void updateParentLocationMarker(LatLng location) {
        if (parentLocationMarker != null) {
            parentLocationMarker.remove();
        }

        Bitmap parentMarkerBitmap = getMarkerBitmapFromView(requireContext(), R.layout.marker_parent, "Parent's Location");

        parentLocationMarker = googleMap.addMarker(new MarkerOptions()
                .position(location)
                .title("Your Parent's Location")
                .icon(BitmapDescriptorFactory.fromBitmap(parentMarkerBitmap))
        );

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));  // Zoom into the parent's location
    }



    private void fetchChildDetailsAndShowBottomSheet(String userId) {
        firestoreDatabase.collection("children").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String uid = userId;
                        String fullName = documentSnapshot.getString("firstName") + " " +
                                (documentSnapshot.getString("middleName") != null ? documentSnapshot.getString("middleName") + " " : "") +
                                documentSnapshot.getString("lastName");
                        String birthdate = documentSnapshot.getString("birthdate");
                        String contactNumber = documentSnapshot.getString("contact");
                        String lastKnown = documentSnapshot.getString("lastKnownLocation");

                        // Show bottom sheet with child details
                        com.christianserwedevs.doslocator.Fragments.MainNavigation.ChildDetailsBottomSheet bottomSheet = com.christianserwedevs.doslocator.Fragments.MainNavigation.ChildDetailsBottomSheet.newInstance(fullName, birthdate, contactNumber, lastKnown, uid);
                        bottomSheet.show(getChildFragmentManager(), "child_details_bottom_sheet");
                    } else {
                        Toast.makeText(requireContext(), "No details found for this child", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Error fetching child details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private String getChildIdFromMarker(com.google.android.gms.maps.model.Marker marker) {
        for (String userId : childMarkers.keySet()) {
            if (childMarkers.get(userId).equals(marker)) {
                return userId;  // Return the userId associated with this marker
            }
        }
        return null;  // Not a child marker
    }
    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);  // Update every 5 seconds
        locationRequest.setFastestInterval(3000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null && hasLocationSignificantlyChanged(location)) {
                    updateMapWithCurrentLocation(location);
                    updateLocationInFirestore(location);  // Update location in Firestore
                    lastKnownLocation = location;  // Save the last known location
                }
            }
        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, requireActivity().getMainLooper());
    }


    private void updateMapWithCurrentLocation(Location location) {
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        // Remove the previous user marker if it exists
        if (userLocationMarker != null) {
            userLocationMarker.remove();
        }

        Bitmap userMarkerBitmap = getUserMarkerBitmap(requireContext(), R.layout.marker_user);
        userLocationMarker = googleMap.addMarker(new MarkerOptions()
                .position(currentLatLng)
                .title("You are here")
                .icon(BitmapDescriptorFactory.fromBitmap(userMarkerBitmap))
        );

        // Only move the camera on the first update or if the user is not interacting with the map
        if (isFirstUpdate || !isMapBeingMovedByUser) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));  // Zoom level 15
            isFirstUpdate = false;  // After the first update, don't keep resetting the camera
        }
    }


    private void updateLocationInFirestore(Location location) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("LoginPrefs", requireContext().MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);
        String userType = sharedPreferences.getString("userType", null);
        String addressText = getAddressFromLocation(location.getLatitude(), location.getLongitude());
        if (userId != null && userType != null) {
            firestoreDatabase.collection(userType).document(userId)
                    .update("latitude", location.getLatitude(),
                            "longitude", location.getLongitude(),
                            "lastKnownLocation", addressText)

                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to update location: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
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





    private boolean hasLocationSignificantlyChanged(Location newLocation) {
        if (lastKnownLocation == null) {
            return true;  // First update
        }

        float distance = lastKnownLocation.distanceTo(newLocation);
        return distance > LOCATION_CHANGE_THRESHOLD;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);  // Stop location updates when the view is destroyed
        }
    }

    @Override
    public void onChildLocationRequested(double latitude, double longitude) {
        Toast.makeText(requireContext(), "latitude " + latitude + " longitude " + longitude, Toast.LENGTH_SHORT).show();

        if (googleMap != null) {
            LatLng childLocation = new LatLng(latitude, longitude);


            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(childLocation, 18));

            Toast.makeText(requireContext(), "Moved to the child's location!", Toast.LENGTH_SHORT).show();
        }
    }
}
