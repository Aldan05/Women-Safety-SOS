package com.example.assistant;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import ai.picovoice.porcupine.PorcupineException;
import ai.picovoice.porcupine.PorcupineManager;
import ai.picovoice.porcupine.PorcupineManagerCallback;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class WakeWordService extends android.app.Service {
    private static final String TAG = "WakeWordService";
    public PorcupineManager porcupineManager;
    private Vibrator vibrator;
    private String currentLocation = "";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean isWakeWordDetected = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        startForegroundService();
        initializePorcupine();

        Toast.makeText(getApplicationContext(), "Wake Word Service Started", Toast.LENGTH_SHORT).show();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest.Builder(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLocations() != null && !locationResult.getLocations().isEmpty()) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        currentLocation = "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude();
                        Log.d(TAG, "Location fetched: " + currentLocation);
                        fusedLocationClient.removeLocationUpdates(locationCallback);

                        if (isWakeWordDetected) {
                            String phoneNumber = "+919787887823";
                            sendLiveLocation(phoneNumber);
                        }
                    }
                }
            }
        };

        requestLocationPermissions();
        return START_STICKY;
    }

    private void startForegroundService() {
        String channelId = "wake_word_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Wake Word Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Listening for wake word")
                .setContentText("The service is running in the background")
                .setSmallIcon(R.drawable.tickicon)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(1, notification);
    }

    private void initializePorcupine() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "RECORD_AUDIO permission not granted! Cannot start wake word detection.");
            return;
        }

        String accessKey = "OH+yc52GznJP2wMLo6Vt+7uL7k8846gftSL4Dfg9OFKQY1JvDmQtvQ==";
        try {
            if (porcupineManager != null) {
                Log.e(TAG, "PorcupineManager is already initialized.");
                return;
            }

            String keywordFilePath = "assistant_en_android_v3_0_0.ppn";
            Toast.makeText(getApplicationContext(), "Ready to Detect Wake Word", Toast.LENGTH_SHORT).show();

            porcupineManager = new PorcupineManager.Builder()
                    .setAccessKey(accessKey)
                    .setKeywordPaths(new String[]{keywordFilePath})
                    .setSensitivity(0.7f)
                    .build(getApplicationContext(), new PorcupineManagerCallback() {
                        @Override
                        public void invoke(int keywordIndex) {
                            if (keywordIndex == 0) {
                                Log.d(TAG, "Wake word detected!");

                                // Immediately trigger emergency SOS redirection and location sending
                                startTimerPage(getApplicationContext());
                                performSOSOperations();

                                Intent broadcastIntent = new Intent("com.example.assistant.WAKE_WORD_DETECTED");
                                broadcastIntent.setPackage(getPackageName());
                                sendBroadcast(broadcastIntent);

                                if (vibrator != null) {
                                    vibrator.vibrate(500);
                                }
                                toggleFlashlight(true);
                                new Handler().postDelayed(() -> toggleFlashlight(false), 3000);
                            }
                        }
                    });

            porcupineManager.start();
        } catch (PorcupineException e) {
            Log.e(TAG, "Error initializing Porcupine: " + e.getMessage());
            Toast.makeText(this, "Failed to initialize wake word detection.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startTimerPage(Context context) {
        Intent intent = new Intent(context, FullScreenSosActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void sendLiveLocation(String phoneNumber) {
        if (currentLocation != null && !currentLocation.isEmpty()) {
            String[] locationParts = currentLocation.split(", ");
            if (locationParts.length == 2) {
                String latitude = locationParts[0].replace("Lat: ", "").trim();
                String longitude = locationParts[1].replace("Lon: ", "").trim();

                String message = "My current location: http://maps.google.com/?q=" + latitude + "%2C" + longitude;
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Toast.makeText(getApplicationContext(), "Location message sent.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void performSOSOperations() {
        Toast.makeText(WakeWordService.this, "Activating SOS & Fetching Live Location...", Toast.LENGTH_SHORT).show();
        Intent serviceIntent = new Intent(this, SendLocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
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
            Log.e(TAG, "Error accessing camera: " + e.getMessage());
        }
    }

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission request handled in Activity
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "WakeWordService is being destroyed!");

        try {
            if (porcupineManager != null) {
                porcupineManager.stop();
                porcupineManager = null;
                Log.d(TAG, "PorcupineManager stopped.");
            }
            if (vibrator != null) {
                vibrator.cancel();
                vibrator = null;
            }
        } catch (PorcupineException e) {
            Log.e(TAG, "Porcupine exception during service stop: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception during service stop: " + e.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
