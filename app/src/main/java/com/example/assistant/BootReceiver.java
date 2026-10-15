package com.example.assistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.core.content.ContextCompat;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d(TAG, "Device Rebooted! Restarting Assistant Services...");

            Intent wakeIntent = new Intent(context, WakeWordService.class);
            Intent sosIntent = new Intent(context, SOSService.class);
            try {
                ContextCompat.startForegroundService(context, wakeIntent);
                ContextCompat.startForegroundService(context, sosIntent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to start services on boot", e);
            }
        }
    }
}
