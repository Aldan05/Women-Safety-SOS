package com.example.assistant;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;

public class LocationSettingsActivity extends Activity {

    private static final int REQUEST_CHECK_SETTINGS = 1001; // Request code for settings resolution
    private static final int REQUEST_LOCATION_PERMISSION = 1002; // Request code for location permission

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_settings);

        // Check and prompt for location settings
        checkAndPromptLocationSettings();
    }

    private void checkAndPromptLocationSettings() {
        // Check if location permission is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If not granted, request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            // If permission granted, proceed with location settings check
            promptLocationSettings();
        }
    }

    private void promptLocationSettings() {
        // Build the LocationRequest to check if location services are enabled
        LocationRequest locationRequest = new LocationRequest.Builder(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 10000).build();

        // Create a LocationSettingsRequest to check the status of location services
        LocationSettingsRequest.Builder locationSettingsRequestBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        // Create a SettingsClient to check location settings
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);

        // Check if location settings are satisfied
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            // Location settings are adequate, no need to prompt user
            Toast.makeText(LocationSettingsActivity.this, "Location settings are already enabled.", Toast.LENGTH_SHORT).show();
        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, resolve the issue
                ResolvableApiException resolvable = (ResolvableApiException) e;
                try {
                    // Start the resolution for enabling location services
                    resolvable.startResolutionForResult(LocationSettingsActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // If an error occurs while resolving, display a toast
                    Toast.makeText(LocationSettingsActivity.this, "Error resolving location settings.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                // Location services were successfully enabled
                Toast.makeText(this, "Location services enabled successfully.", Toast.LENGTH_SHORT).show();
            } else {
                // Location settings were not enabled
                Toast.makeText(this, "Failed to enable location services.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with location settings check
                promptLocationSettings();
            } else {
                // Permission denied, show a message or handle as needed
                Toast.makeText(this, "Location permission is required to check and enable location settings.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
