package com.example.assistant;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationService extends Service {
    private static final String TAG = "LocationService";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Create the LocationRequest
        locationRequest = new LocationRequest.Builder(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .build();

        // Create LocationCallback to handle location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null && locationResult.getLocations().size() > 0) {
                    // Get the most recent location
                    android.location.Location location = locationResult.getLastLocation();
                    Log.d(TAG, "Location: " + location.getLatitude() + ", " + location.getLongitude());
                    // You can update UI or send this data to your app components here
                }
            }
        };

        // Request location updates
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } catch (SecurityException e) {
            Log.e(TAG, "Permission error", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Location service started
        Log.d(TAG, "Location Service Started");
        return START_STICKY; // Ensures the service keeps running
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop location updates when service is destroyed
        fusedLocationClient.removeLocationUpdates(locationCallback);
        Log.d(TAG, "Location Service Destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // We are not binding this service, just starting it.
    }
}
