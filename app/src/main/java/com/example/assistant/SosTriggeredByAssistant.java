package com.example.assistant;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.Priority;

public class SosTriggeredByAssistant extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient;
    private String currentLocation = "";



    private SharedPreferences sharedPreferences;

    // Declare variables to store mobile numbers
    private String contact1Mobile, contact2Mobile, contact3Mobile;


    private boolean isSosTriggered = false; // Flag to track if SOS is already triggered
    private static final long SOS_RESET_DELAY = 15000; // 15 seconds in milliseconds





    private static final String POLICE_NUMBER_KEY = "PoliceNumberVar";

    private String policeNumber;  // This will hold the value from SharedPreferences


    private String temp = "1 12";
    private String temp1 = "+919787887823";
    private String temp2 = "+9585152285";
    private String temp3 = "+919";


    private static final String EMERGENCY_PHONE_NUMBER = "+919787887823"; // Constant phone number
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int SMS_PERMISSION_REQUEST_CODE = 2;
    private static final int CALL_PERMISSION_REQUEST_CODE = 3;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private String phoneNumber;

    private MediaPlayer mediaPlayer;


    private static final String TAG = "SosTriggeredByAssistant";

    private static final int REQUEST_CODE_OVERLAY_PERMISSION = 2;
    private WindowManager windowManager;
    private View floatingView;
    private TextView timerTextView;
    private Handler handler;
    private int timeRemaining = 10;  // Starting timer value
    private boolean isFloatingViewVisible = false; // Track the visibility of the floating view
    private Runnable timerRunnable; // To hold the reference to the running timer
    private Vibrator vibrator; // To handle vibration functionality


    private int resumeCount = 0;  // Counter to track how many times onResume() is called

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);

        // Initialize location request and client first
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(500)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLocations() != null && !locationResult.getLocations().isEmpty()) {
                    currentLocation = "Lat: " + locationResult.getLastLocation().getLatitude() + ", Lon: " + locationResult.getLastLocation().getLongitude();
                    Toast.makeText(SosTriggeredByAssistant.this, "Location fetched: " + currentLocation, Toast.LENGTH_SHORT).show();
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                    sendLiveLocation(EMERGENCY_PHONE_NUMBER);
                }
            }
        };

        startSendLocationService();
        checkLocationSettings();

        // Get SharedPreferences instance
        SharedPreferences sharedPreferences3 = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isSosTriggered = sharedPreferences3.getBoolean("isSosTriggered", false);

        if (isSosTriggered) {
            Log.d("SharedPrefs", "User is logged in.");
        } else {
            Log.d("SharedPrefs", "User is not logged in.");
        }

        SharedPreferences sharedPreferences2 = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor2 = sharedPreferences2.edit();
        editor2.putBoolean("isSosTriggered", true);
        editor2.apply();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences.Editor editor2 = sharedPreferences2.edit();
                editor2.putBoolean("isSosTriggered", false);
                editor2.apply();
            }
        }, 15000);

        handler = new Handler();
        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        loadEmergencyContacts();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.iphone_ringtone);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing media player", e);
        }
    }




    private void loadEmergencyContacts() {
        // Get the saved mobile numbers from SharedPreferences
        contact1Mobile = sharedPreferences.getString("contact1Mobile", "No Number");
        contact2Mobile = sharedPreferences.getString("contact2Mobile", "No Number");
        contact3Mobile = sharedPreferences.getString("contact3Mobile", "No Number");


    }



    private void sendLiveLocation(String phoneNumber) {
        // Check if the location is available
        if (currentLocation != null && !currentLocation.isEmpty()) {
            // Extract latitude and longitude from currentLocation string
            String[] locationParts = currentLocation.split(", ");
            if (locationParts.length == 2) {
                String latitude = locationParts[0].replace("Lat: ", "").trim();
                String longitude = locationParts[1].replace("Lon: ", "").trim();
                // Create the message in the format you requested
                String message = "My current location: http://maps.google.com/?q=" + latitude + "%2C" + longitude;
                // Send the location via SMS
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Toast.makeText(getApplicationContext(), "Location message sent.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SosTriggeredByAssistant.this, "Location format is incorrect.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(SosTriggeredByAssistant.this, "Location not available. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }


    private void checkLocationSettings() {

        //  onBackPressed();
        Log.e(TAG, "location key is showing !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, locationSettingsResponse -> {
            // Location settings are fine, start fetching the location
            // This will happen only when the button is clicked
        });
        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                int statusCode = resolvableApiException.getStatusCode();
                if (statusCode == CommonStatusCodes.RESOLUTION_REQUIRED) {
                    // Location settings are not satisfied, show the user the dialog to enable location
                    PendingIntent resolutionIntent = resolvableApiException.getResolution();
                    Log.e(TAG, "location key is showing 111 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    try {
                        // This ensures the resolution intent is fired to ask the user to enable location
                        startIntentSenderForResult(resolutionIntent.getIntentSender(),
                                REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null);
                        // onBackPressed();
                        Log.e(TAG, "location key is showing 222 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();
                        Toast.makeText(SosTriggeredByAssistant.this, "Failed to start intent for resolution.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SosTriggeredByAssistant.this, "Location is not enabled", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkLocationSettings();  // This will trigger the dialog to enable location
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                    onBackPressed(); // Optional: go back if denied
                }
                break;

            case SMS_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
                }
                break;

            case CALL_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Call permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Call permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    private void startSendLocationService() {
        Intent serviceIntent = new Intent(this, SendLocationService.class);
        startService(serviceIntent);

    }



    @Override
    protected void onResume() {
        super.onResume();
        if (resumeCount <2) {
            // If less than 2, call checkLocationSettings()
            checkLocationSettings();

            resumeCount++;
        }
        else{
             onBackPressed();
        }

    }
}