package com.example.assistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RestartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Start the service after boot
        if (intent != null && Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            ServiceUtils.scheduleServiceRestart(context); // Use context directly
        }
    }
}
