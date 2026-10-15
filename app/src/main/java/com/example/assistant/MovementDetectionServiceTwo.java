package com.example.assistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class MovementDetectionServiceTwo extends Service implements SensorEventListener {

    private static final float SHAKE_THRESHOLD = 100;  // Adjust this threshold to change sensitivity
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastUpdate = 0;
    private float lastX, lastY, lastZ;

    private Vibrator mVibrator;
    private PowerManager.WakeLock wakeLock;

    private Handler handler = new Handler();  // Assuming you have some handler to manage tasks

    @Override
    public void onCreate() {
        super.onCreate();

        // Create a notification channel (required for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Movement Detection";
            String description = "Notification channel for movement detection service";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("MovementDetectionChannel", name, importance);
            channel.setDescription(description);

            // Get the NotificationManager system service
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Initialize the sensor manager and accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // Initialize the vibrator
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Get the PowerManager system service to create a WakeLock
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "MovementDetectionServiceTwo::WakeLock");
            wakeLock.acquire(10 * 60 * 1000L);  // Keep the CPU awake for 10 minutes
        }

        // Register the sensor listener to listen for accelerometer events
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }

        // Create a foreground service notification to keep the service alive
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = new NotificationCompat.Builder(this, "MovementDetectionChannel")
                    .setContentTitle("Movement Detection Service Two")
                    .setContentText("Detecting shakes...")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)  // Use your app's icon here
                    .build();

            startForeground(1, notification);  // Start service in foreground mode
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if (curTime - lastUpdate > 100) {  // Only process if 100ms have passed since last event
                long diffTime = curTime - lastUpdate;
                lastUpdate = curTime;

                float diffX = event.values[0] - lastX;
                float diffY = event.values[1] - lastY;
                float diffZ = event.values[2] - lastZ;

                // Calculate the movement magnitude
                float speed = (float) Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    Log.d("MovementServiceTwo", "Shake detected!");  // This uses Log for debug message
                    triggerVibration();
                    stopCurrentServiceTwo();
                    startMovementDetectionService();
                  //  startWakeWordService();
                    // Optionally perform additional actions here
                }

                lastX = event.values[0];
                lastY = event.values[1];
                lastZ = event.values[2];
            }
        }
    }

    // Trigger a vibration when movement is detected
    private void triggerVibration() {
        if (mVibrator != null && mVibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // For API level 26 and above, use VibrationEffect
                VibrationEffect vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE);
                mVibrator.vibrate(vibrationEffect);
            } else {
                // For below API level 26, use the old vibrate method
                mVibrator.vibrate(500);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used in this case
    }

    private void startMovementDetectionService() {
        Intent serviceIntent = new Intent(this, MovementDetectionService.class);
        androidx.core.content.ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void startWakeWordService() {
        Intent serviceIntent = new Intent(this, WakeWordService.class);
        androidx.core.content.ContextCompat.startForegroundService(this, serviceIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        handler.removeCallbacksAndMessages(null);
    }

    // Method to stop the current service
    private void stopCurrentServiceTwo() {
        // 1. Stop the current service's functionality (terminate running tasks)
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);  // Stop the accelerometer listener
        }

        // Release any held wake lock to avoid power drain
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }



        // Remove any pending tasks (handlers)
        handler.removeCallbacksAndMessages(null);

        Log.d("MovementService", "Current service functionality stopped and all handlers removed.");
        stopSelf();  // Stop the service

        // 2. Start the next service (MovementDetectionServiceTwo or WakeWordService)
        // Uncomment the line below to start the next service
        // startService(new Intent(this, NextService.class));
    }
}
