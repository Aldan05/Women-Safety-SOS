package com.example.assistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraManager;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            // Screen turned off (device is going to sleep)
            vibrate(context);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // Screen turned on (device woke up)
            toggleFlashlight(context, true);
        }
    }

    private void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            // Vibrate for 1 second (1000 ms)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(1000);
            }
        }
    }

    private void toggleFlashlight(Context context, boolean turnOn) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (cameraManager == null) return;
        try {
            String[] cameraIds = cameraManager.getCameraIdList();
            if (cameraIds.length > 0) {
                String cameraId = cameraIds[0]; // Get the camera ID of the rear camera
                cameraManager.setTorchMode(cameraId, turnOn); // Turn on or off the flashlight
                if (turnOn) {
                    // Keep the flashlight on for 1 second, then turn it off
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> toggleFlashlight(context, false), 1000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
