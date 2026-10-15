package com.example.assistant;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class SendLocationService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "send_location_service_channel";
    private static final String TAG = "SendLocationService";

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean locationSent = false;

    private SharedPreferences sharedPreferences;
    private List<String> allContacts = new ArrayList<>();

    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private int currentCallIndex = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        toggleFlashlight(true);

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        loadEmergencyContacts();

        Log.d(TAG, "Starting Emergency Service with " + allContacts.size() + " contacts.");

        // 1. Setup Sequential Auto-Dialing for calls
        setupSequentialCalling();

        // 2. Fetch Real GPS Location and send SMS to ALL contacts
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fetchRealGpsLocationAndSend();
    }

    private void loadEmergencyContacts() {
        List<String> contactsList = new ArrayList<>();
        SharedPreferences myPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences userData = getSharedPreferences("UserData", MODE_PRIVATE);

        for (int i = 1; i <= 20; i++) {
            String phone = myPrefs.getString("contact" + i + "Mobile", "");
            if (phone.isEmpty() || phone.equals("No Number")) {
                phone = userData.getString("contact" + i + "Mobile", "");
            }
            if (!phone.isEmpty() && !phone.equals("No Number") && !contactsList.contains(phone)) {
                contactsList.add(phone);
            }
        }

        if (contactsList.isEmpty()) {
            String c1 = myPrefs.getString("contact1Mobile", "+919787887823");
            if (!c1.isEmpty() && !c1.equals("No Number")) contactsList.add(c1);
        }

        allContacts = contactsList;
        Log.d(TAG, "Loaded emergency contacts count: " + allContacts.size());
    }

    private void fetchRealGpsLocationAndSend() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            sendSmsToAllContacts("🚨 EMERGENCY! I need immediate help! (Location permission missing)");
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                processAndSendLocation(location);
            } else {
                requestFreshGpsLocation();
            }
        }).addOnFailureListener(e -> requestFreshGpsLocation());
    }

    private void requestFreshGpsLocation() {
        LocationRequest locationRequest = new LocationRequest.Builder(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 500)
                .setMinUpdateIntervalMillis(200)
                .setWaitForAccurateLocation(true)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && !locationResult.getLocations().isEmpty() && !locationSent) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        processAndSendLocation(location);
                        fusedLocationClient.removeLocationUpdates(locationCallback);
                    }
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException in location updates", e);
            sendSmsToAllContacts("🚨 EMERGENCY! I need immediate help!");
        }
    }

    private void processAndSendLocation(Location location) {
        if (locationSent) return;
        locationSent = true;

        double lat = location.getLatitude();
        double lon = location.getLongitude();
        String timestamp = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()).toString();
        String mapsLink = "https://maps.google.com/?q=" + lat + "," + lon;

        String customMsg = sharedPreferences.getString("customSOSMessage", "🚨 EMERGENCY! I need immediate help!");
        String message = customMsg + "\n\n📍 Real Live GPS Location:\n" +
                "• Latitude: " + lat + "\n" +
                "• Longitude: " + lon + "\n" +
                "• Time: " + timestamp + "\n\n" +
                "🗺️ Open Map: " + mapsLink;

        sendSmsToAllContacts(message);
    }

    private void sendSmsToAllContacts(String messageText) {
        if (allContacts.isEmpty()) {
            Log.e(TAG, "No emergency contacts found to send SMS.");
            return;
        }

        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> partsList = smsManager.divideMessage(messageText);

        for (String phone : allContacts) {
            try {
                smsManager.sendMultipartTextMessage(phone, null, partsList, null, null);
                Log.d(TAG, "Successfully sent emergency SMS to: " + phone);
            } catch (Exception e) {
                Log.e(TAG, "Failed to send SMS to " + phone, e);
            }
        }
    }

    private void setupSequentialCalling() {
        if (allContacts.isEmpty()) return;

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            phoneStateListener = new PhoneStateListener() {
                private boolean isCallActive = false;

                @Override
                public void onCallStateChanged(int state, String phoneNumber) {
                    super.onCallStateChanged(state, phoneNumber);
                    if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                        isCallActive = true;
                    } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                        if (isCallActive) {
                            isCallActive = false;
                        } else {
                            Log.d(TAG, "Call missed/declined. Calling next contact...");
                            handlerNextCall();
                        }
                    }
                }
            };
            try {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            } catch (Exception e) {
                Log.e(TAG, "Error registering phone state listener", e);
            }
        }

        currentCallIndex = 0;
        makePhoneCall(allContacts.get(currentCallIndex));
    }

    private void handlerNextCall() {
        currentCallIndex++;
        if (currentCallIndex < allContacts.size()) {
            Log.d(TAG, "Auto-dialing next contact index: " + currentCallIndex);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                makePhoneCall(allContacts.get(currentCallIndex));
            }, 2000);
        } else {
            Log.d(TAG, "All emergency contacts called.");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Emergency SOS Active")
                .setContentText("Sending real GPS location & auto-dialing contacts.")
                .setSmallIcon(R.drawable.tickicon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Send Location Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void makePhoneCall(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.equals("No Number")) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            startActivity(callIntent);
        }
    }

    private void toggleFlashlight(boolean enable) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (cameraManager != null) {
                String[] cameraIds = cameraManager.getCameraIdList();
                if (cameraIds != null && cameraIds.length > 0) {
                    cameraManager.setTorchMode(cameraIds[0], enable);
                }
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error accessing flashlight", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (telephonyManager != null && phoneStateListener != null) {
            try {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            } catch (Exception ignored) {
            }
        }
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
