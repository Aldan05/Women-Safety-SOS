



package com.example.assistant;

import android.content.SharedPreferences;
import android.Manifest;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.graphics.Color;

import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;


import android.view.Gravity;
import android.widget.ImageButton;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;

import android.content.BroadcastReceiver;

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
import androidx.core.view.GravityCompat;


public class communication extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private String currentLocation = "";
    private EditText mobileNumberEditText;
    private boolean isBackPressedOnce = false;

    TextView phoneNumberTextView;

    private DrawerLayout drawerLayout;
    private ImageButton hamburgerIcon;
    private Toolbar toolbar;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int SMS_PERMISSION_REQUEST_CODE = 2;
    private static final int CALL_PERMISSION_REQUEST_CODE = 3;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private static final int MICROPHONE_PERMISSION_REQUEST_CODE = 1002;


    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private TextView textViewPhoneNumber;
    private static final int MICROPHONE_PERMISSION_CODE = 1;


    // BroadcastReceiver to listen for the wake word detected action
    private BroadcastReceiver wakeWordReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.assistant.WAKE_WORD_DETECTED".equals(intent.getAction())) {
                // Trigger the SOS button click when the wake word is detected
                sendSOSButtonClicked(null);  // Passing null as the view argument
            }
        }
    };




    private static final int REQUEST_CODE_OVERLAY_PERMISSION = 2;
    private WindowManager windowManager;
    private View floatingView;
    private TextView timerTextView, nameTextView, FirstLetter;
    private Handler handler;
    private int timeRemaining = 10;  // Starting timer value
    private boolean isFloatingViewVisible = false; // Track the visibility of the floating view
    private Runnable timerRunnable; // To hold the reference to the running timer
    private Vibrator vibrator; // To handle vibration functionality

    TextView onSwitch, offSwitch;
    private static final String PREFS_NAME = "SwitchPrefs";
    private static final String KEY_STATE = "switch_state"; // "ON" or "OFF"

    TextView statusText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);

        // Automatically start SOS Service and Wake Word Service so they work immediately
        startButtonSosService();
        startWakeWordService();


        SharedPreferences prefs1 = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isSosEnabled = prefs1.getBoolean("sos_switch_state", true);  // default is true

        SharedPreferences preferences2 = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isWakeWordEnabled = preferences2.getBoolean("isWakeWordSwitchOn", false); // Default is false



        statusText = findViewById(R.id.statusText);
        onSwitch = findViewById(R.id.onSwitch);
        offSwitch = findViewById(R.id.offSwitch);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String state = prefs.getString(KEY_STATE, "OFF"); // default state is OFF
        applySwitchState(state);

        if (onSwitch != null) {
            onSwitch.setOnClickListener(v -> {
                saveSwitchState("ON");
                applySwitchState("ON");
            });
        }

        if (offSwitch != null) {
            offSwitch.setOnClickListener(v -> {
                saveSwitchState("OFF");
                applySwitchState("OFF");
            });
        }




        // Initialize the TextView
        nameTextView = findViewById(R.id.nameTextView);  // Make sure this is in your XML layout
        FirstLetter = findViewById(R.id.letterTextView);
        // Retrieve the stored name from SharedPreferences
        SharedPreferences sharedPreferences2 = getSharedPreferences("UserData", MODE_PRIVATE);
        String name = sharedPreferences2.getString("name", "Default Name");
        if (name == null || name.trim().isEmpty()) {
            name = "Default Name";
        }
        String firstLetter = String.valueOf(name.trim().charAt(0)).toUpperCase();
        // Display the retrieved name in the TextView
        nameTextView.setText(name);
        FirstLetter.setText(firstLetter);

        // Retrieve the fullPhoneNumber from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String fullPhoneNumber = sharedPreferences.getString("fullPhoneNumber", "No phone number available");

        // Find the TextView and set the text
        TextView phoneNumberTextView = findViewById(R.id.phoneNumberTextView);
        phoneNumberTextView.setText(fullPhoneNumber);







        // In EmergencyContactsActivity or wherever you're saving the flag
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isEmergencyContactsSaved", true);  // Save the flag as true
        editor.apply();


        // Find views
        drawerLayout = findViewById(R.id.drawer_layout);
        hamburgerIcon = findViewById(R.id.hamburger_icon);
        toolbar = findViewById(R.id.toolbar);

        // Set up the Toolbar as the ActionBar
        setSupportActionBar(toolbar);

        // Remove the title from the Toolbar (ActionBar)
        // Remove the title from the Toolbar
        // Remove the title from the Toolbar (ActionBar)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null); // This removes the title
        }

        // Set up the hamburger menu button
        hamburgerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the drawer when the hamburger icon is clicked
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });



        // Optional: Set swipe gestures to open/close the drawer
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);  // Ensure it's not locked


        phoneNumberTextView = findViewById(R.id.phoneNumberTextView);

        // Retrieve the phone number from the Intent
        String phoneNumber = getIntent().getStringExtra("phoneNumber");

        // Set the phone number in the TextView
        if (phoneNumber != null) {
            phoneNumberTextView.setText("Entered Phone Number: " + phoneNumber);
        }


        // Initialize vibrator
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ) {

           // showFloatingLayoutPermissionDialog();

        }

        //else {

         //   if (!Settings.canDrawOverlays(this)) {

          //      appearOnTopPermissionDialog();

                // If permission is already granted, enable the button to show the floating notification
                // findViewById(R.id.button).setOnClickListener(v -> {startSosTriggeredByAssistant(); startSendLocationService(); }); // triggerDelayedFloatingNotification()
      //      }
        //}





        // startWakeWordService();

        // Register the BroadcastReceiver to listen for wake word detection
        IntentFilter filter = new IntentFilter("com.example.assistant.WAKE_WORD_DETECTED");
        ContextCompat.registerReceiver(this, wakeWordReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        // Initialize location request
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(500)
                .build();

        // Initialize location callback to handle location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLocations() != null && !locationResult.getLocations().isEmpty()) {
                    // Get the latest location from the list of locations
                    currentLocation = "Lat: " + locationResult.getLastLocation().getLatitude() + ", Lon: " + locationResult.getLastLocation().getLongitude();
                    // Log the location for debugging
                    Toast.makeText(communication.this, "Location fetched: " + currentLocation, Toast.LENGTH_SHORT).show();
                    // Stop location updates once you have the location
                    fusedLocationClient.removeLocationUpdates(locationCallback);

                    // Send live location after it is fetched
                    sendLiveLocation(mobileNumberEditText.getText().toString().trim());
                }
            }
        };
    }

    private void appearOnTopPermissionDialog() {
        new AlertDialog.Builder(this)
                .setMessage("permission needed to appear on top of other apps")       //ALTER WORDINGS HERE
                .setCancelable(false)
                .setPositiveButton("OK, GOT IT", (dialog, which) -> appearOnTopPermission())
                .show();
    }


    private void appearOnTopPermission(){
        if(!Settings.canDrawOverlays(this)){
            // Request overlay permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION);
        }
    }


    public void openAssistantSettings(View view) {
        Intent intent_Assistant_Settings = new Intent(communication.this, AssistantSettingsInitial.class);
        startActivity(intent_Assistant_Settings);
    }

    public void openProfile(View view) {
        Intent intent_Details = new Intent(communication.this, Details.class);
        startActivity(intent_Details);
    }

    public void openPermission(View view) {
        Intent intent_Permission = new Intent(communication.this, Permission.class);
        startActivity(intent_Permission);
    }

    public void openSettings(View view) {
        Intent intent_Permission = new Intent(communication.this, AssistantSettings.class);
        startActivity(intent_Permission);
    }

    public void openEmergencyContacts(View view) {
        Intent intent_EmContact = new Intent(communication.this, EmergencyContacts.class);
        startActivity(intent_EmContact);
    }

    private void saveSwitchState(String state) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_STATE, state)
                .apply();
    }

    private void applySwitchState(String state) {
        if ("ON".equals(state)) {
            onSwitch.setBackgroundResource(R.drawable.rounded_green_left);
            offSwitch.setBackgroundResource(R.drawable.rounded_gray_right);
            onSwitch.setTextColor(Color.WHITE);
            offSwitch.setTextColor(Color.BLACK);
            startWakeWordService();
            statusText.setText("Voice detection on... Speak 'Assistant call police'");
        } else {
            onSwitch.setBackgroundResource(R.drawable.rounded_gray_left);
            offSwitch.setBackgroundResource(R.drawable.rounded_red_right);
            onSwitch.setTextColor(Color.BLACK);
            offSwitch.setTextColor(Color.WHITE);
            stopWakeWordService();
            statusText.setText("Voice detection off");
        }
    }


    @Override
    public void onBackPressed() {
        // If the drawer (slide-in menu) is open, close it
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // If the drawer is closed and we're on the home page, show "press again to exit" message
            if (isBackPressedOnce) {
                super.onBackPressed();  // Exit the app
                return;
            }

            // Show the toast and set the flag to true
            this.isBackPressedOnce = true;
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();

            // Reset the flag after 2 seconds (2000 milliseconds)
            new android.os.Handler().postDelayed(() -> isBackPressedOnce = false, 2000);
        }
    }


    private void showFloatingLayoutPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Please allow permission for app to function properly. These permissions are only for various app functionality & we are not accessing any of your contacts / data")       //ALTER WORDINGS HERE
                .setCancelable(false)
                .setPositiveButton("OK, GOT IT", (dialog, which) -> openPermission())
                .show();
    }




    private void openPermission(){
        // Start next activity
        Intent intent = new Intent(communication.this, Permission.class);   // THIS LINE
        startActivity(intent);
        finish();
    }



    private void startAllService() {
        Intent serviceIntent = new Intent(this, StartService.class);
        startService(serviceIntent);
    }


    // Trigger the floating notification after a delay (5 seconds for demo purpose)
    private void triggerDelayedFloatingNotification() {
        if (!isFloatingViewVisible) {
            // Only trigger the floating notification if it's not already visible
            new Handler().postDelayed(this::showFloatingNotification, 5000); // Delay of 5 seconds before showing the floating notification
        }
    }

    // Show the floating notification with timer
    private void showFloatingNotification() {
        if (isFloatingViewVisible) {
            return; // Prevent showing the view again if it's already visible
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Inflate the custom floating layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        floatingView = inflater.inflate(R.layout.floating_layout, null);

        // Set up the parameters for the floating view
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY; // Required for Android 8.0+
        params.format = PixelFormat.TRANSLUCENT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL; // Allow interaction
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.x = 0;
        params.y = 200;

        // Add the floating view to the window
        windowManager.addView(floatingView, params);

        // Set the flag indicating that the floating view is visible
        isFloatingViewVisible = true;

        // Set up the dismiss button click listener
        Button dismissButton = floatingView.findViewById(R.id.btnDismiss);
        dismissButton.setOnClickListener(v -> dismissFloatingView()); // Remove the floating view when dismissed

        // Initialize the timer text view
        timerTextView = floatingView.findViewById(R.id.timerText);

        // Reset the timer to 10 seconds
        timeRemaining = 10;

        // Start the timer countdown
        startTimer();
    }


    // Start the countdown timer
    private void startTimer() {
        // Stop any previous timer if it's running
        if (handler != null && timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }

        handler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (timeRemaining > 0) {
                    timerTextView.setText(String.valueOf(timeRemaining));
                    timeRemaining--;
                    handler.postDelayed(this, 1000); // Run this every second
                } else {

                    // ActivateSOS();
                    dismissFloatingView();
                    startSendLocationService();


                    // Timer finished, stop and remove the floating view
                    if (vibrator != null && isFloatingViewVisible) {
                        // Vibrate when the timer reaches zero, only if floating view is still visible
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            vibrator.vibrate(500); // For devices below Android Oreo
                        }
                    }


                    triggerDelayedFakeCall();

                }
            }
        };

        handler.post(timerRunnable); // Start the countdown
    }






    // Trigger the floating notification after a delay (5 seconds for demo purpose)
    private void triggerDelayedFakeCall() {
        if (!isFloatingViewVisible) {
            // Only trigger the floating notification if it's not already visible
            new Handler().postDelayed(this::showFakeCall, 15000); // Delay of 5 seconds before showing the floating notification
        }
    }





    private void startSendLocationService() {
        Intent serviceIntent = new Intent(this, SendLocationService.class);
        startService(serviceIntent);
    }

    private void startSosTriggeredByAssistant() {
        Intent intent = new Intent(communication.this, SosTriggeredByAssistant.class);
        startActivity(intent);
    }

    public void startButtonSosService() {
        Intent intent = new Intent(this, SOSService.class);
        startService(intent);  // Start SOSService in the background
    }


    public void startService() {
        Intent intent = new Intent(this, StartService.class);
        startService(intent);  // Start, Start Service in the background
    }


    // Show the floating notification with timer
    private void showFakeCall() {
        if (isFloatingViewVisible) {
            return; // Prevent showing the view again if it's already visible
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Inflate the custom floating layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        floatingView = inflater.inflate(R.layout.fake_call, null);

        // Set up the parameters for the floating view
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY; // Required for Android 8.0+
        params.format = PixelFormat.TRANSLUCENT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL; // Allow interaction
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.x = 0;
        params.y = 200;

        // Add the floating view to the window
        windowManager.addView(floatingView, params);

        // Set the flag indicating that the floating view is visible
        isFloatingViewVisible = true;

        // Set up the dismiss button click listener
        Button dismissButton = floatingView.findViewById(R.id.btnCutCall2);
        dismissButton.setOnClickListener(v -> dismissFloatingView()); // Remove the floating view when dismissed

        // Set up the dismiss button click listener
        //     Button dismissButton = floatingView.findViewById(R.id.btnCutCall11);
        //    dismissButton.setOnClickListener(v -> dismissFloatingView()); // Remove the floating view when dismissed



        // Initialize the timer text view
        timerTextView = floatingView.findViewById(R.id.timerText);

        // Reset the timer to 26 seconds
        timeRemaining = 26;

        // Start the timer countdown
        startTimerForFakeCall();
    }

    // Timer logic to handle the countdown
    private void startTimerForFakeCall() {
        // Create a CountDownTimer that counts down from 26 seconds
        new CountDownTimer(timeRemaining * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the timer display with the remaining time
                int secondsLeft = (int) (millisUntilFinished / 1000);
                timerTextView.setText(String.format("%d seconds", secondsLeft));
            }

            @Override
            public void onFinish() {
                // Stop the floating view after the timer finishes
                dismissFloatingView();  // Call dismissFloatingView() to remove the view
            }
        }.start();
    }






    // Dismiss the floating view safely
    private void dismissFloatingView() {



        if (isFloatingViewVisible && floatingView != null) {
            // Remove the floating view only if it's still attached
            windowManager.removeView(floatingView);
            isFloatingViewVisible = false;
            timeRemaining = 10; // Reset the timer for next time

            // Stop the timer if it's running
            if (handler != null && timerRunnable != null) {
                handler.removeCallbacks(timerRunnable); // Remove the ongoing countdown task
            }
        }
    }

    // Handle the result from enabling location (if needed)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OVERLAY_PERMISSION && Settings.canDrawOverlays(this)) {
            // Permission granted, proceed with showing the floating notification
            findViewById(R.id.button).setOnClickListener(v ->  startTimerPage(getApplicationContext()));
        }
    }






    private void requestPermissions() {
        // Request location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            checkLocationSettings();
        }

        // Request SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        }

        // Request Call permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_REQUEST_CODE);
        }
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
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
                Toast.makeText(communication.this, "Location format is incorrect.", Toast.LENGTH_SHORT).show();
            }
        } else {

            Toast.makeText(communication.this, "Location not available. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendSOSButtonClicked(View view) {
        startTimerPage(getApplicationContext());
        startSendLocationService();
    }



    public void ActivateSOS(){

        String phoneNumber = mobileNumberEditText.getText().toString().trim();

        if (!phoneNumber.isEmpty()) {
            Toast.makeText(communication.this, "Fetching your location, please wait...", Toast.LENGTH_SHORT).show();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            // Try to fetch the last known location first
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // If last known location is available, use it
                            currentLocation = "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude();
                            sendLiveLocation(phoneNumber);  // Send location immediately
                        } else {
                            // If no last known location, request live updates
                            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                        }
                    });
        } else {
            Toast.makeText(communication.this, "Please enter a mobile number", Toast.LENGTH_SHORT).show();
        }

        requestPermissions();
        // Emergency contact number
        String emergencyNumber = "+919787887823";  // Emergency contact number
        makeCall(emergencyNumber);  // Make the emergency call
        sendHelloMessage(emergencyNumber);


    }

    private void startTimerPage(Context context) {
        Intent intent = new Intent(context, FullScreenSosActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    private void makeCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(android.net.Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        } else {
            Toast.makeText(this, "Call permission is not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkLocationSettings() {
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
                        Toast.makeText(communication.this, "Failed to start intent for resolution.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(communication.this, "Location is not enabled", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    private void startMovementDetectionService() {
        Intent serviceIntent = new Intent(this, MovementDetectionService.class);
        startService(serviceIntent);
    }

    private void startWakeWordService() {
        Intent serviceIntent = new Intent(this, WakeWordService.class);
        startService(serviceIntent);
    }

    private void stopWakeWordService() {
        Intent serviceIntent = new Intent(this, WakeWordService.class);
        stopService(serviceIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Re-load the name and first letter from SharedPreferences
        SharedPreferences sharedPreferences2 = getSharedPreferences("UserData", MODE_PRIVATE);
        String name = sharedPreferences2.getString("name", "Default Name");
        if (name == null || name.trim().isEmpty()) {
            name = "Default Name";
        }
        String firstLetter = String.valueOf(name.trim().charAt(0)).toUpperCase();

        if (nameTextView != null) nameTextView.setText(name);
        if (FirstLetter != null) FirstLetter.setText(firstLetter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(wakeWordReceiver);
        } catch (Exception e) {
            // Ignored if not registered
        }
    }

}

