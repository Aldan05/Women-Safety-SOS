package com.example.assistant;




import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.appcompat.app.AppCompatActivity;


import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import java.util.concurrent.TimeUnit;

import android.provider.Settings;
import android.util.Log;




import android.Manifest;
import android.content.IntentSender;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
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


import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



public class MainActivity extends AppCompatActivity {


    private static final String PREFS_NAME = "UserData";
    private static final String MOBILE_VERIFIED_KEY = "mobile_verified";  // This is where you store mobile verification status
    private static final String DETAILS_VAR_KEY = "DetailsVar";
    private static final String ASSISTANT_VAR_KEY = "AssistantVar";
    private static final String EMER_CONT_VAR_KEY = "EmerContVar";

    private static final String TAG = "assistant::"; // or any relevant tag

    private FirebaseAuth mAuth;

    private FusedLocationProviderClient fusedLocationClient;
    private String currentLocation = "";
    private EditText mobileNumberEditText;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int SMS_PERMISSION_REQUEST_CODE = 2;
    private static final int CALL_PERMISSION_REQUEST_CODE = 3;
    private static final int REQUEST_CHECK_SETTINGS = 100;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    String emergencyNumber = "+919787887823";  // Emergency contact number

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check overlay permission
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }

        // Firebase setup
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, FirstSigningPage.class));
            finish();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int detailsVar = sharedPreferences.getInt(DETAILS_VAR_KEY, 0);
        int emerContVar = sharedPreferences.getInt(EMER_CONT_VAR_KEY, 0);

        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isEmergencyContactsSaved = preferences.getBoolean("isEmergencyContactsSaved", false);

        if (detailsVar == 0) {
            startActivity(new Intent(this, Details.class));
            finish();
            return;
        }

        if (emerContVar == 0 || !isEmergencyContactsSaved) {
            startActivity(new Intent(this, EmergencyContacts.class));
            finish();
            return;
        }

        SharedPreferences preferences2 = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isPermissionGranted = preferences2.getBoolean("isPermissionGranted", false);

        if (!isPermissionGranted) {
            startActivity(new Intent(this, Permission.class));
            finish();
            return;
        }

        startActivity(new Intent(this, communication.class));
        finish();
    }

    private void initLocation() {
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        }
        if (locationRequest == null) {
            locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                    .setMinUpdateIntervalMillis(1000)
                    .build();
        }
        if (locationCallback == null) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                        currentLocation = "Lat: " + locationResult.getLastLocation().getLatitude() +
                                ", Lon: " + locationResult.getLastLocation().getLongitude();
                        Toast.makeText(MainActivity.this, "Location fetched: " + currentLocation, Toast.LENGTH_SHORT).show();
                        fusedLocationClient.removeLocationUpdates(locationCallback);

                        sendLiveLocation(emergencyNumber);
                    }
                }
            };
        }
    }



    @Override
    protected void onDestroy() {

        super.onDestroy();
        Log.d(TAG, "App ForeStopped!");

    }




    private void scheduleBackgroundService() {
        Log.d(TAG, "Time Started After App ForeStopped!");
        // Define the work to run your background service after a 5-second delay
        OneTimeWorkRequest backgroundWorkRequest = new OneTimeWorkRequest.Builder(BackgroundServiceWorker.class)
                .setInitialDelay(5, TimeUnit.SECONDS)  // Delay for 5 seconds
                .build();

        // Enqueue the work request
        WorkManager.getInstance(this).enqueue(backgroundWorkRequest);

        Log.d(TAG, "Time Started After App ForeStopped! reach at last line");

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, start your service or perform actions
                    //  startYourService();
                } else {
                    // Permission denied, notify the user
                    Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;

            case SMS_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, perform actions
                } else {
                    // Permission denied, notify the user
                    Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;

            case CALL_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, perform actions
                } else {
                    // Permission denied, notify the user
                    Toast.makeText(this, "Call Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void getLocation() {
        initLocation();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // Request location updates
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }


    private void sendHelloMessage(String phoneNumber) {
        String helloMessage = "Hi, I'm here";
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, helloMessage, null, null);
        Toast.makeText(getApplicationContext(), "Hello message sent.", Toast.LENGTH_SHORT).show();
    }

    private void sendLiveLocation(String phoneNumber)
    {
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
                Toast.makeText(MainActivity.this, "Location format is incorrect.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Location not available. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendSOSButtonClicked(View view) {
        initLocation();
        if (mobileNumberEditText == null) {
            mobileNumberEditText = findViewById(R.id.mobileNumber);
        }
        String phoneNumber = (mobileNumberEditText != null) ? mobileNumberEditText.getText().toString().trim() : "";

        if (!phoneNumber.isEmpty()) {
            Toast.makeText(MainActivity.this, "Fetching your location, please wait...", Toast.LENGTH_SHORT).show();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            Toast.makeText(MainActivity.this, "Please enter a mobile number", Toast.LENGTH_SHORT).show();
        }

        sendLiveLocation(emergencyNumber);
        makeCall(emergencyNumber);
        sendHelloMessage(emergencyNumber);
    }

    private void makeCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(android.net.Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        } else {
            Toast.makeText(this, "Call permission is not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void startAllService() {
        Intent serviceIntent = new Intent(this, StartService.class);
        startService(serviceIntent);
    }

    private void checkLocationSettings() {
        initLocation();
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
                    try {
                        // This ensures the resolution intent is fired to ask the user to enable location
                        startIntentSenderForResult(resolutionIntent.getIntentSender(),
                                REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null);
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();
                        Toast.makeText(MainActivity.this, "Failed to start intent for resolution.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Location is not enabled", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openEmergencyContact(){
        Intent intent = new Intent(MainActivity.this, EmergencyContacts.class);
        startActivity(intent);

    }



    private void openDetailsPage(){
        Intent intent2 = new Intent(MainActivity.this, Details.class);
        startActivity(intent2);

    }




    private void startMovementDetectionService() {
        Intent serviceIntent = new Intent(this, MovementDetectionService.class);
        startService(serviceIntent);
    }

    private void stopMovementDetectionService() {
        Intent serviceIntent = new Intent(this, MovementDetectionService.class);
        stopService(serviceIntent);
    }


    private void startMovementDetectionServiceTwo() {
        Intent serviceIntent = new Intent(this, MovementDetectionServiceTwo.class);
        startService(serviceIntent);
    }


    private void stopMovementDetectionServiceTwo() {
        Intent serviceIntent = new Intent(this, MovementDetectionServiceTwo.class);
        stopService(serviceIntent);
    }

    private void startWakeWordService() {
        Intent serviceIntent = new Intent(this, WakeWordService.class);
        startService(serviceIntent);
        Toast.makeText(this, "Working service call", Toast.LENGTH_SHORT).show();
    }

    private void startButtonService() {
        Intent serviceIntent = new Intent(this, SOSService.class);
        startService(serviceIntent);

    }


    private void stopWakeWordService() {
        Intent serviceIntent = new Intent(this, WakeWordService.class);
        stopService(serviceIntent);
    }


    // Trigger vibration
    private void vibrateDevice(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500); // Vibration duration in milliseconds
            }
        }
    }
}
