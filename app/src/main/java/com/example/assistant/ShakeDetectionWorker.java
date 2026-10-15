
package com.example.assistant;




import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraAccessException;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ShakeDetectionWorker extends Worker implements SensorEventListener {

    private static final float SHAKE_THRESHOLD = 100;  // Sensitivity for shake detection
    private static final long SENSOR_UPDATE_INTERVAL = 100;  // 100ms interval for updates

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastUpdate = 0;
    private float lastX, lastY, lastZ;
    private Vibrator mVibrator;
    private CameraManager cameraManager;
    private String cameraId;

    public ShakeDetectionWorker(Context context, WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public Result doWork() {
        // Initialize the sensor and vibrator services
        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        mVibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        cameraManager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);

        // Get the camera id for the flashlight
        try {
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                if (characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) != null
                        && characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                    cameraId = id;
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // Start listening for shake events (if accelerometer is available)
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }

        // Let WorkManager know the task is done and the result
        return Result.success();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if (curTime - lastUpdate > SENSOR_UPDATE_INTERVAL) {
                long diffTime = curTime - lastUpdate;
                lastUpdate = curTime;

                // Calculate the movement magnitude
                float diffX = event.values[0] - lastX;
                float diffY = event.values[1] - lastY;
                float diffZ = event.values[2] - lastZ;
                float speed = (float) Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ) / diffTime * 10000;

                // Trigger vibration and flashlight if a shake is detected
                if (speed > SHAKE_THRESHOLD) {
                    triggerVibrationAndFlash();
                }

                lastX = event.values[0];
                lastY = event.values[1];
                lastZ = event.values[2];
            }
        }
    }

    private void triggerVibrationAndFlash() {
        if (mVibrator != null && mVibrator.hasVibrator()) {
            // Trigger vibration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE);
                mVibrator.vibrate(vibrationEffect);
            } else {
                mVibrator.vibrate(500);
            }
        }

        if (cameraId != null && cameraManager != null) {
            try {
                // Turn on the flashlight
                cameraManager.setTorchMode(cameraId, true);

                // Turn off the flashlight after 500ms
                new Thread(() -> {
                    try {
                        Thread.sleep(500);  // Keep the flashlight on for 500ms
                        cameraManager.setTorchMode(cameraId, false);  // Turn off flashlight
                    } catch (InterruptedException | CameraAccessException e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used in this case
    }

    @Override
    public void onStopped() {
        super.onStopped();
        // Unregister the sensor listener when the work is stopped
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
}

