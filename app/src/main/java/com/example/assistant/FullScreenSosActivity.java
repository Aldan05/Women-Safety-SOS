package com.example.assistant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class FullScreenSosActivity extends Activity {

    private TextView timerTextView;
    private int timeRemaining = 10;
    private Handler handler;
    private Runnable timerRunnable;
    private Vibrator vibrator;
    private LocationRequest locationRequest;
    private boolean shouldRunOnResume = false;
    private boolean shouldAskLocationWhenUnlocked = false;
    private boolean shouldStartSosAfterLocation = false;
    private boolean isFloatingViewVisible = false;
    private MediaPlayer alarmPlayer;

    private FusedLocationProviderClient fusedLocationClient;
    private double currentLat = 0.0;
    private double currentLon = 0.0;
    private boolean isLocationFetched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationRequest = new LocationRequest.Builder(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(500)
                .build();

        openPage();
    }

    public void openPage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        );

        setContentView(R.layout.floating_layout);

        timerTextView = findViewById(R.id.timerText);
        Button dismissBtn = findViewById(R.id.btnDismiss);
        TextView tvLocationStatus = findViewById(R.id.tvLocationStatus);
        Button btnViewMap = findViewById(R.id.btnViewMap);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        startAlarm();
        startTimer();
        isFloatingViewVisible = true;

        if (dismissBtn != null) {
            dismissBtn.setOnClickListener(v -> cancelSos());
        }

        if (!isLocationEnabled(this)) {
            Toast.makeText(this, "Please enable GPS/Location for real tracking!", Toast.LENGTH_LONG).show();
            try {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            } catch (Exception ignored) {}
        }

        // Fetch high-accuracy live location instantly
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentLat = location.getLatitude();
                    currentLon = location.getLongitude();
                    isLocationFetched = true;
                    if (tvLocationStatus != null) {
                        tvLocationStatus.setText("📍 Accurate GPS Location:\nLat: " + currentLat + ", Lon: " + currentLon);
                    }
                    if (btnViewMap != null) {
                        btnViewMap.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (tvLocationStatus != null) {
                        tvLocationStatus.setText("📍 Acquiring high-precision GPS signal...");
                    }
                }
            });
        }

        if (btnViewMap != null) {
            btnViewMap.setOnClickListener(v -> {
                if (isLocationFetched) {
                    Uri gmmIntentUri = Uri.parse("https://maps.google.com/?q=" + currentLat + "," + currentLon);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    try {
                        startActivity(mapIntent);
                    } catch (Exception e) {
                        Toast.makeText(this, "Could not open map", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Fetching location, please wait...", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void startTimer() {
        handler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (timeRemaining > 0) {
                    timerTextView.setText(String.valueOf(timeRemaining));
                    vibrateShort();
                    timeRemaining--;
                    handler.postDelayed(this, 1000);
                } else {
                    vibrateLong();
                    shouldRunOnResume = true;
                    shouldAskLocationWhenUnlocked = true;
                    shouldStartSosAfterLocation = true;
                    startSosTriggeredByAssistant();
                    startTwilioCall();
                    startSendLocationService();
                    dismissFloatingView();
                }
            }
        };
        handler.post(timerRunnable);
    }

    private void cancelSos() {
        stopTimer();
        stopAlarm();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("isSosTriggered", false).apply();
        finish();
    }

    private void stopTimer() {
        if (handler != null && timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
    }

    private void vibrateShort() {
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(300);
            }
        }
    }

    private void startTwilioCall() {
        Intent intent = new Intent(FullScreenSosActivity.this, CallActivity.class);
        startActivity(intent);
    }

    private void vibrateLong() {
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(3000, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(3000);
            }
        }
    }

    private void dismissFloatingView() {
        stopTimer();
        stopAlarm();
        timeRemaining = 10;
        isFloatingViewVisible = false;
        finish();
    }

    private void startSosTriggeredByAssistant() {
        Intent intent = new Intent(FullScreenSosActivity.this, SosTriggeredByAssistant.class);
        startActivity(intent);
    }

    private void startSendLocationService() {
        Intent serviceIntent = new Intent(this, SendLocationService.class);
        startService(serviceIntent);
    }

    private void startAlarm() {
        if (alarmPlayer == null) {
            alarmPlayer = MediaPlayer.create(this, R.raw.sos_alarm);
            alarmPlayer.setLooping(true);
        }
        if (alarmPlayer != null) {
            alarmPlayer.start();
        }
    }

    private void stopAlarm() {
        if (alarmPlayer != null) {
            if (alarmPlayer.isPlaying()) {
                alarmPlayer.stop();
            }
            alarmPlayer.release();
            alarmPlayer = null;
        }
    }

    private boolean isLocationEnabled(Context context) {
        android.location.LocationManager lm = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;
        try {
            gpsEnabled = lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {}
        try {
            networkEnabled = lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
        } catch (Exception ignored) {}
        return gpsEnabled || networkEnabled;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        stopAlarm();
    }

    @Override
    public void onBackPressed() {
        // Prevent back button from canceling SOS
    }
}
