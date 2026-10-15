package com.example.assistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;

public class ServiceRestartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent wakeIntent = new Intent(context, WakeWordService.class);
            Intent sosIntent = new Intent(context, SOSService.class);
            try {
                ContextCompat.startForegroundService(context, wakeIntent);
                ContextCompat.startForegroundService(context, sosIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
