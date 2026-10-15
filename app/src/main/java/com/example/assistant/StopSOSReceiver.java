package com.example.assistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StopSOSReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Handle the Stop SOS action
        String action = intent.getStringExtra("action");
        if ("stop_sos".equals(action)) {
            Log.d("StopSOSReceiver", "SOS stopped!");

            // Perform any additional actions here, like stopping vibration or canceling the notification
            // For example: cancel vibration, stop services, or other actions

            // Optionally, stop any services or activities
        }
    }
}
