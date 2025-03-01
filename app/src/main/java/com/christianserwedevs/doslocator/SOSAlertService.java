package com.christianserwedevs.doslocator;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.christianserwedevs.doslocator.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;


public class SOSAlertService extends Service {

    private static final String CHANNEL_ID = "SOSAlertServiceChannel";
    private static final int NOTIFICATION_ID = 102;
    private FirebaseFirestore firestoreDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        firestoreDatabase = FirebaseFirestore.getInstance();
    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        startForeground(NOTIFICATION_ID, getNotification("Listening for SOS alerts..."));
//
//        String userType = intent.getStringExtra("userType");
//        String parentEmail = intent.getStringExtra("parentEmail");
//
//        if ("parents".equals(userType) && parentEmail != null) {
//            listenForSOSAlerts(parentEmail);
//        } else if ("responders".equals(userType)) {
//            listenForSOSAlertsResponder();
//        }
//
//        return START_STICKY;
//    }
@Override
public int onStartCommand(Intent intent, int flags, int startId) {
    // 🔥 Check if the intent is null to prevent crashes
    if (intent == null) {
        Log.e("SOSAlertService", "Received null Intent. Service stopping.");
        stopSelf(); // Stop service since we have no data
        return START_NOT_STICKY;
    }

    // 🔥 Check if the intent has extras before accessing them
    String userType = intent.getStringExtra("userType");
    String parentEmail = intent.getStringExtra("parentEmail");

    if (userType == null || parentEmail == null) {
        Log.e("SOSAlertService", "Missing required intent extras. Stopping service.");
        stopSelf(); // Stop service since we have no data
        return START_NOT_STICKY;
    }

    // 🔥 Start foreground service with notification
    startForeground(NOTIFICATION_ID, getNotification("Listening for SOS alerts..."));

    if ("parents".equals(userType)) {
        listenForSOSAlerts(parentEmail);
    } else if ("responders".equals(userType)) {
        listenForSOSAlertsResponder();
    }

    return START_STICKY;
}

    private Notification getNotification(String contentText) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SOS Alert Service")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.sos_button)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SOS Alert Tracking",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void listenForSOSAlerts(String parentEmail) {
        firestoreDatabase.collection("sos_alerts")
                .whereGreaterThanOrEqualTo(FieldPath.documentId(), parentEmail)
                .whereLessThanOrEqualTo(FieldPath.documentId(), parentEmail + "\uf8ff")
                .whereEqualTo("isActive", true)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("SOSAlertService", "Error listening for SOS alerts: " + e.getMessage());
                        return;
                    }

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String childName = document.getString("childName");
                            String timestamp = document.getString("timestamp");
                            String documentName = document.getId();
                            Double latitude = document.getDouble("latitude");
                            Double longitude = document.getDouble("longitude");

                            sendSOSNotification(childName, timestamp, documentName, latitude, longitude);
                        }
                    }
                });
    }

    private void listenForSOSAlertsResponder() {
        firestoreDatabase.collection("sos_alerts")
                .whereEqualTo("isResponded", false)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("SOSAlertService", "Error listening for SOS alerts: " + e.getMessage());
                        return;
                    }

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String childName = document.getString("childName");
                            String timestamp = document.getString("timestamp");
                            String documentName = document.getId();
                            Double latitude = document.getDouble("latitude");
                            Double longitude = document.getDouble("longitude");

                            sendSOSNotification(childName, timestamp, documentName, latitude, longitude);
                        }
                    }
                });
    }

    private void sendSOSNotification(String childName, String timestamp, String documentName, Double latitude, Double longitude) {
        String message = "SOS Alert from " + childName + " at " + timestamp;

        Intent intent = new Intent(this, MainActivity.class); // Change to your app's main activity
        intent.putExtra("open_map_fragment", true); // Add extra data to identify the SOS click
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SOS Alert")
                .setContentText(message)
                .setSmallIcon(R.drawable.sos_button)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), notification);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
