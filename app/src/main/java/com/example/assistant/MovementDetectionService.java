

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
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.Handler;
import android.util.Log;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraCharacteristics;
import androidx.core.app.NotificationCompat;

public class MovementDetectionService extends Service implements SensorEventListener {

    private static final float SHAKE_THRESHOLD = 100;  // Adjust this threshold to change sensitivity
    private static final long ON_DURATION = 10000; // 10 seconds of detection
    private static final long OFF_DURATION = 10000; // 10 seconds of off period
    private static final long FLASH_DURATION = 1000; // 1 second of flashlight
    private static final long NO_MOVEMENT_TIMEOUT = 20000; // 20 seconds to detect no movement

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastUpdate = 0;
    private float lastX, lastY, lastZ;

    private Vibrator mVibrator;
    private PowerManager.WakeLock wakeLock;  // Declare WakeLock

    private Handler handler = new Handler();
    private Runnable detectMovementRunnable;
    private Runnable stopMovementRunnable;
    private Runnable flashRunnable;

    private boolean isDetectingMovement = false;
    private int movementDetectedFlag = 0;  // Flag to track if movement is detected within the cycle

    private CameraManager cameraManager;
    private String cameraId;

    private long lastMovementTime = 0; // Tracks the last time movement was detected

    private boolean isFlashlightOn = false;  // Flag to track if flashlight is on

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
                    "MovementDetectionService::WakeLock");
            wakeLock.acquire(10 * 60 * 1000L);  // Keep the CPU awake for 10 minutes
        }

        // Initialize the camera manager for controlling the flashlight
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (cameraManager != null) {
                String[] cameraIds = cameraManager.getCameraIdList();
                if (cameraIds.length > 0) {
                    cameraId = cameraIds[0]; // Get the first camera (usually the rear one)
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // Register the sensor listener to listen for accelerometer events
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }

        // Create a foreground service notification to keep the service alive
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = new NotificationCompat.Builder(this, "MovementDetectionChannel")
                    .setContentTitle("Movement Detection Service")
                    .setContentText("Detecting shakes...")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)  // Use your app's icon here
                    .build();

            startForeground(1, notification);  // Start service in foreground mode
        }
        startWakeWordService();
        // Initialize the toggling behavior
        initMovementDetectionToggling();
    }

    private void initMovementDetectionToggling() {
        detectMovementRunnable = new Runnable() {
            @Override
            public void run() {
                movementDetectedFlag = 0;  // Reset movement detection flag to 0

                // Start detecting movement after 10 seconds of "off" time
                isDetectingMovement = true;
                Log.d("MovementService", "Movement detection started.");
                lastMovementTime = System.currentTimeMillis(); // Track the time movement detection started

                // Check if movement was detected during the 10-second period
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (movementDetectedFlag == 0) {
                            // No movement detected within the "on" cycle, turn on the flashlight
                            Log.d("MovementService", "No movement detected, turning on flashlight.");
                            turnOnFlashlightForOneSecond();
                            stopCurrentService();
                            startMovementDetectionServiceTwo();
                            stopWakeWordService();
                        }
                        // After 10 seconds, stop the movement detection
                        handler.post(stopMovementRunnable);
                    }
                }, ON_DURATION);  // Run for 10 seconds

                handler.postDelayed(stopMovementRunnable, ON_DURATION);  // Run stopMovementRunnable after 10 seconds
            }
        };

        stopMovementRunnable = new Runnable() {
            @Override
            public void run() {
                // Stop detecting movement
                isDetectingMovement = false;
                Log.d("MovementService", "Movement detection stopped.");

                // If no movement was detected during the 10 seconds, check for 20 seconds no movement
                if (movementDetectedFlag == 0) {
                    long timeSinceLastMovement = System.currentTimeMillis() - lastMovementTime;
                    if (timeSinceLastMovement >= NO_MOVEMENT_TIMEOUT) {
                        if (!isFlashlightOn) {  // Ensure flashlight is not on already
                            turnOnFlashlightForOneSecond(); // If 20 seconds pass with no movement, turn on the flashlight
                        }
                    }
                }

                // Start the "off" cycle
                handler.postDelayed(detectMovementRunnable, OFF_DURATION);  // Wait for 10 seconds before restarting
            }
        };

        // Start the first movement detection cycle
        handler.post(detectMovementRunnable);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isDetectingMovement && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if (curTime - lastUpdate > 100) {  // Only process if 100ms have passed since last event
                long diffTime = curTime - lastUpdate;
                lastUpdate = curTime;

                float diffX = event.values[0] - lastX;
                float diffY = event.values[1] - lastY;
                float diffZ = event.values[2] - lastZ;

                // Calculate the movement magnitude
                float speed = (float) Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD && movementDetectedFlag == 0) {
                    Log.d("MovementService", "Shake detected!");
                    movementDetectedFlag = 1;  // Movement detected, set the flag to 1
                    lastMovementTime = curTime; // Update the last movement time
                    triggerVibration();
                    //startWakeWordService();
                }

                lastX = event.values[0];
                lastY = event.values[1];
                lastZ = event.values[2];
            }
        }
    }

    // Trigger a vibration
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

    // Turn on the flashlight for 1 second if no movement was detected
    private void turnOnFlashlightForOneSecond() {
        if (cameraManager != null && cameraId != null) {
            try {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                Boolean hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (hasFlash != null && hasFlash) {
                    // Turn on the flashlight
                    cameraManager.setTorchMode(cameraId, true);
                    isFlashlightOn = true;

                    // Schedule to turn off flashlight after 1 second
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                cameraManager.setTorchMode(cameraId, false);
                                isFlashlightOn = false;
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }, FLASH_DURATION); // Turn off flashlight after 1 second
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used, but must be overridden
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;  // We are not binding to this service
    }

    private void startMovementDetectionServiceTwo() {
        Intent serviceIntent = new Intent(this, MovementDetectionServiceTwo.class);
        androidx.core.content.ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void stopMovementDetectionServiceTwo() {
        Intent serviceIntent = new Intent(this, MovementDetectionServiceTwo.class);
        stopService(serviceIntent);
    }

    private void startWakeWordService() {
        Intent serviceIntent = new Intent(this, WakeWordService.class);
        androidx.core.content.ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void stopWakeWordService() {
        Intent serviceIntent = new Intent(this, WakeWordService.class);
        stopService(serviceIntent);
    }

    private void stopCurrentService() {
        // 1. Stop the current service's functionality (terminate running tasks)
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);  // Stop the accelerometer listener
        }

        // Release any held wake lock to avoid power drain
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        // Turn off flashlight if it is on
        if (cameraManager != null && cameraId != null) {
            try {
                cameraManager.setTorchMode(cameraId, false);  // Turn off the flashlight
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        // Remove any pending tasks (handlers)
        handler.removeCallbacks(detectMovementRunnable);
        handler.removeCallbacks(stopMovementRunnable);
        handler.removeCallbacks(flashRunnable);

        Log.d("MovementService", "Current service functionality stopped and all handlers removed for 2.");
        stopSelf();

        // 2. Start the next service (MovementDetectionServiceTwo or WakeWordService)
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up and stop everything when the service is destroyed
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        // Release the wake lock
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        // Turn off flashlight if it is on
        if (cameraManager != null && cameraId != null) {
            try {
                cameraManager.setTorchMode(cameraId, false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        // Cancel any pending tasks (runnables)
        handler.removeCallbacks(detectMovementRunnable);
        handler.removeCallbacks(stopMovementRunnable);
        handler.removeCallbacks(flashRunnable);
        Log.d("MovementService", "Service destroyed and all handlers removed.");
    }
}



