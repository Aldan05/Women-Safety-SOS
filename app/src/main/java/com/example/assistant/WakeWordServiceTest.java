
package com.example.assistant;

import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;
import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.content.IntentSender;


import ai.picovoice.rhino.*;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ComponentName;


import androidx.appcompat.app.AppCompatActivity;
import android.location.Location;
import android.os.Vibrator;
import android.os.Handler;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;
import ai.picovoice.porcupine.Porcupine;
import ai.picovoice.porcupine.PorcupineException;
import ai.picovoice.porcupine.PorcupineManager;
import ai.picovoice.porcupine.PorcupineManagerCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.location.SettingsClient;

import java.io.IOException;

public class WakeWordServiceTest extends android.app.Service {
    private static final String TAG = "WakeWordService";
    public PorcupineManager porcupineManager;
    private Vibrator vibrator;
    private String currentLocation = "";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean isWakeWordDetected = false; // Flag to track wake word detection


    private RhinoManager rhinoManager;

    private static final String ACCESS_KEY = "zKLOSDDCARhQnUSs0+F22Zf2fAW3VCBn7OFMLtzS0sh21/c6puYTtQ==";  // Replace with your actual Picovoice access key
    private static final String CONTEXT_FILE_PATH = "call-police_en_android_v3_0_0.rhn"; // Path to the custom context file in assets folder
    private static final int REQUEST_CODE_RECORD_AUDIO = 100; // For runtime permissions


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        startForegroundService();
        initializePorcupine();

        Toast.makeText(getApplicationContext(), "Wake Word Service Started", Toast.LENGTH_SHORT).show();


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);



        // Initialize location request
        locationRequest = new LocationRequest.Builder(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .build();

        // Initialize location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLocations() != null && !locationResult.getLocations().isEmpty()) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        currentLocation = "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude();
                        Log.d(TAG, "Location fetched: " + currentLocation);

                        // Once the location is retrieved, stop location updates
                        fusedLocationClient.removeLocationUpdates(locationCallback);

                        // Send live location after fetching it, only if the wake word was detected
                        if (isWakeWordDetected) {
                            String phoneNumber = "+919787887823"; // Replace with the number
                            sendLiveLocation(phoneNumber);
                        }
                    }
                }
            }
        };

        // Request location permissions
        requestLocationPermissions();

        //  ServiceUtils.scheduleServiceRestart(getApplicationContext());
        return START_STICKY;

    }


    private void initializeRhinoManager() {
        // Define the callback for inference
        RhinoManagerCallback inferenceCallback = new RhinoManagerCallback() {
            @Override
            public void invoke(RhinoInference inference) {
                if (inference.getIsUnderstood()) {
                    String intent = inference.getIntent();
                    Map<String, String> slots = inference.getSlots();
                    // Take action based on inferred intent and slot values
                    handleInferenceResult(intent, slots);
                    Toast.makeText(getApplicationContext(), "Command detected", Toast.LENGTH_SHORT).show();

                    //   startSendLocationService();
                    //   startSosTriggeredByAssistant();


                    // Send a broadcast to update the TextView color in WakeWordTestActivity
                    Intent broadcastIntent = new Intent("com.example.assistant.COMMAND_DETECTED");
                    broadcastIntent.setPackage(getPackageName());
                    sendBroadcast(broadcastIntent);  // Send the broadcast with the custom action


                    // Vibrate the phone when the context is detected
                    if (vibrator != null) {
                        // Vibrate for 500 milliseconds (0.5 seconds)
                        vibrator.vibrate(500);
                    }
                } else {
                    // Handle unsupported commands
                    Log.e(TAG, "Command not understood.");
                }
            }
        };

        try {
            // Initialize RhinoManager
            rhinoManager = new RhinoManager.Builder()
                    .setAccessKey(ACCESS_KEY)
                    .setContextPath(CONTEXT_FILE_PATH)  // Context file path in the assets folder
                    .setSensitivity(0.7f)
                    .build(getApplicationContext(), inferenceCallback);

            Log.d(TAG, "RhinoManager initialized successfully.");

            // Immediately start processing audio for commands after initialization
            rhinoManager.process();  // This activates listening right away


        } catch (Exception e) {
            Log.e(TAG, "Error initializing RhinoManager: " + e.getMessage());
            e.printStackTrace();  // Log the full stack trace
            Toast.makeText(this, "Failed to initialize speech processing.", Toast.LENGTH_SHORT).show();
        }
    }


    private void handleInferenceResult(String intent, Map<String, String> slots) {
        // Process intent and slots based on your custom context
        Log.d(TAG, "Intent: " + intent);
        Log.d(TAG, "Slots: " + slots);

        // Example: Handle an action based on the inferred intent
        if ("play_music".equals(intent)) {
            String song = slots.get("song");
            Log.d(TAG, "Playing song: " + song);
            // Implement your action, e.g., play the song using an audio player
        } else {
            Log.d(TAG, "Unsupported intent.");
        }
    }

    private void startForegroundService() {
        String channelId = "wake_word_channel";

        // Create NotificationChannel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Wake Word Service",
                    NotificationManager.IMPORTANCE_LOW // Use IMPORTANCE_LOW to avoid unnecessary interruptions
            );
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Create Notification (Use NotificationCompat for backward compatibility)
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Listening for wake word")
                .setContentText("The service is running in the background")
                .setSmallIcon(R.drawable.tickicon)
                .setOngoing(true) // Keeps the notification persistent
                .setPriority(NotificationCompat.PRIORITY_LOW) // Matches IMPORTANCE_LOW
                .build();

        // Start the service in foreground mode
        startForeground(1, notification);
    }


    private void initializePorcupine() {
        String accessKey = "zKLOSDDCARhQnUSs0+F22Zf2fAW3VCBn7OFMLtzS0sh21/c6puYTtQ=="; // Replace with your actual access key
        try {
            if (porcupineManager != null) {
                Log.e(TAG, "PorcupineManager is already initialized.");
                return;
            }

            // Set the path to the .ppn files in the assets folder
            String keywordFilePath = "hey-siri_en_android_v3_0_0.ppn";  // Path for "two-thousand"
            // Path for "alexa"

            // Create a keyword file array
            Toast.makeText(getApplicationContext(), "Ready to Detect Wake Word", Toast.LENGTH_SHORT).show();


            porcupineManager = new PorcupineManager.Builder()
                    .setAccessKey(accessKey)
                    .setKeywordPaths(new String[]{keywordFilePath})  // Provide the relative path to the keyword file

                    .setSensitivity(0.7f)  // Adjust sensitivity as needed
                    .build(getApplicationContext(), new PorcupineManagerCallback() {
                        @Override
                        public void invoke(int keywordIndex) {

                            if (keywordIndex == 0) {
                                Log.d(TAG, "Wake word detected!");
                                initializeRhinoManager();

                                // Send a broadcast to update the TextView color in WakeWordTestActivity
                                Intent broadcastIntent = new Intent("com.example.assistant.WAKE_WORD_DETECTED");
                                broadcastIntent.setPackage(getPackageName());
                                sendBroadcast(broadcastIntent);  // Send the broadcast with the custom action


                            }
                        }
                    });

            porcupineManager.start();  // Start the Porcupine manager
        } catch (PorcupineException e) {
            Log.e(TAG, "Error initializing Porcupine: " + e.getMessage());
            Toast.makeText(this, "Failed to initialize wake word detection.", Toast.LENGTH_SHORT).show();
        }
    }


    private void sendLiveLocation(String phoneNumber) {
        // Ensure the location is available
        if (currentLocation != null && !currentLocation.isEmpty()) {
            String[] locationParts = currentLocation.split(", ");
            if (locationParts.length == 2) {
                String latitude = locationParts[0].replace("Lat: ", "").trim();
                String longitude = locationParts[1].replace("Lon: ", "").trim();

                String message = "My current location: http://maps.google.com/?q=" + latitude + "%2C" + longitude;

                // Send the location via SMS
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Toast.makeText(getApplicationContext(), "Location message sent.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(WakeWordServiceTest.this, "Location format is incorrect.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(WakeWordServiceTest.this, "Location not available. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void makeCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(android.net.Uri.parse("tel:" + phoneNumber));
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(callIntent);
        } else {
            Toast.makeText(this, "Call permission is not granted", Toast.LENGTH_SHORT).show();
        }
    }

    public void performSOSOperations() {
        String phoneNumber = "+919787887823";

        if (!phoneNumber.isEmpty()) {
            // Notify the user that location fetching is in progress
            Toast.makeText(WakeWordServiceTest.this, "Fetching your location, please wait...", Toast.LENGTH_SHORT).show();

            // Send a hello message immediately
            sendHelloMessage(phoneNumber);  // Sends a hello message

            // After location is fetched, send live location
            sendLiveLocation(phoneNumber);  // Sends live location after it's fetched
        } else {
            // If no phone number entered, show a prompt
            Toast.makeText(WakeWordServiceTest.this, "Please enter a mobile number", Toast.LENGTH_SHORT).show();
        }

        // Emergency number
        // Emergency contact number
        //  makeCall(phoneNumber);  // Make the emergency call
    }


    private void sendHelloMessage(String phoneNumber) {
        String helloMessage = "Hi, I'm here";
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, helloMessage, null, null);
        Log.d(TAG, "Hello message sent.");
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
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // Permission request logic goes here (handled in Activity)
        } else {
            //  getLocation();
        }
    }




    private void startSosTriggeredByAssistant() {
        Intent intent = new Intent(WakeWordServiceTest.this, SosTriggeredByAssistant.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  // Add this line to set the new task flag
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "WakeWordService is being destroyed!");

        try {
            // If you are using Picovoice's PorcupineManager, use the stop() method to stop detection
            if (porcupineManager != null) {
                porcupineManager.stop();  // stop listening for wake word
                porcupineManager = null;
                Log.d(TAG, "PorcupineManager stopped.");
            }

            //    if (rhinoManager != null) {
            //        rhinoManager.release();  // Release resources and stop any active processing
            //        rhinoManager = null;
            //        Log.d(TAG, "RhinoManager released.");
            //    }

            // If vibrator is being used, cancel it
            if (vibrator != null) {
                vibrator.cancel();
                vibrator = null;
                Log.d(TAG, "Vibrator stopped.");
            }
        } catch (PorcupineException e) {
            // Catch and log Porcupine-specific exceptions
            Log.e(TAG, "Porcupine exception during service stop: " + e.getMessage());
        } catch (Exception e) {
            // Catch any general exceptions
            Log.e(TAG, "Exception during service stop: " + e.getMessage());
        } finally {
            // Make sure we stop the service entirely
            stopSelf();
            Log.d(TAG, "WakeWordService stopped completely.");
        }
    }





    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

