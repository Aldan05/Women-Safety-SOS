

package com.example.assistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class StartService extends Service {

    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private Vibrator vibrator;
    private boolean isScreenStateReceiverRegistered = false;
    private ScreenStateReceiver screenStateReceiver;
    private static final String TAG = "MyService";

    @Override
    public IBinder onBind(Intent intent) {
        // This is for bound services, we don't need it for this example
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ScreenVibrationService", "ONSTART COMMAND IS CALLED");
        // Initialize the PowerManager
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);


        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);


        boolean isScreenOn = powerManager.isInteractive();  // Check if the screen is on (interactive)



     //   boolean isServiceRunning = ServiceUtils.isButtonServiceRunning(StartService.this);


     //   if (!isServiceRunning) {
            Log.d("ScreenVibrationService", "SCREEN ON AND SERVICE NOT RUNNING SO SERVICE IS STARTED");
            startWakeWordService();
            startButtonSOSService();

     //   }


        if (isScreenOn) {  //button service is off            !isFlagSet &&
            Log.d("ScreenVibrationService", "SCREEN ON SO SERVICE UTILL CALLED");
            ServiceUtils.scheduleServiceRestart(getApplicationContext());

        }
        else{

            startListeningForScreenState();


        }





        return START_STICKY;
    }


    private void toggleFlashlight(boolean enable) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (cameraManager != null) {
            try {
                String[] cameraIds = cameraManager.getCameraIdList();
                if (cameraIds.length > 0) {
                    cameraManager.setTorchMode(cameraIds[0], enable);
                }
            } catch (CameraAccessException e) {
                Log.e(TAG, "Error accessing camera: " + e.getMessage());
            }
        }
    }


    private void startListeningForScreenState() {
        if (!isScreenStateReceiverRegistered) { // Check if receiver is already registered
            screenStateReceiver = new ScreenStateReceiver();
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(screenStateReceiver, filter); // Register the receiver
            isScreenStateReceiverRegistered = true; // Set flag to true
        }
    }

    // Function to stop listening for screen state
    private void stopListeningForScreenState() {
        if (isScreenStateReceiverRegistered && screenStateReceiver != null) {
            try {
                unregisterReceiver(screenStateReceiver); // Unregister the receiver
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Receiver not registered: " + e.getMessage());
            }
            isScreenStateReceiverRegistered = false; // Reset flag to false
        }
    }



    // ScreenStateReceiver will handle screen on/off events
    private class ScreenStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("ScreenVibrationService", "SCREEN STATE RECEIVER GET ON");
            if (intent == null) return;
            String action = intent.getAction();

            if (Intent.ACTION_SCREEN_ON.equals(action)) {

                Log.d("ScreenVibrationService", "SCREEN TURN TO ON SO I STARTED SERVICE UTILLS CYCLE");
                ServiceUtils.scheduleServiceRestart(getApplicationContext());


                // Screen is ON
                Log.e("ScreenStateReceiver", "Screen is ON");
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {

/*
                        USE THIS CODE, IF ANY BUGS OCCUR

                boolean isServiceRunning = ServiceUtils.isButtonServiceRunning(StartService.this);

                if (!isServiceRunning) {
                    Log.d("ScreenVibrationService", "SCREEN ON AND SERVICE NOT RUNNING SO SERVICE IS STARTED");
                    startWakeWordService();
                    startButtonSOSService();

                }

 */

                // stop cycle

                // Screen is OFF
                Log.e("ScreenStateReceiver", "Screen is OFF");

                // Vibrate the device when the screen turns off
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null) {
                    vibrator.vibrate(500); // Vibrate for 500 milliseconds
                }
            }
        }
    }




    // BroadcastReceiver that listens for screen on and screen off actions

    private void stopMovementDetectionService() {
        Intent serviceIntent = new Intent(this, MovementDetectionService.class);
        stopService(serviceIntent);
    }


    private void stopMovementDetectionServiceTwo() {
        Intent serviceIntent = new Intent(this, MovementDetectionServiceTwo.class);
        stopService(serviceIntent);
    }

    private void startMovementDetectionService() {
        Intent serviceIntent = new Intent(this, MovementDetectionService.class);
        androidx.core.content.ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void startWakeWordService() {
        Intent serviceIntent = new Intent(this, WakeWordService.class);
        androidx.core.content.ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void startButtonSOSService() {
        Intent serviceIntent = new Intent(this, SOSService.class);
        androidx.core.content.ContextCompat.startForegroundService(this, serviceIntent);
    }







    @Override
    public void onDestroy() {
        super.onDestroy();
        stopListeningForScreenState();
        Log.d(TAG, "Service destroyed");
    }
}

