package com.example.assistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WakeWordReceiver extends BroadcastReceiver {

    private static final String TAG = "WakeWordReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("com.example.assistant.WAKE_WORD_DETECTED".equals(intent.getAction())) {
            Log.d(TAG, "Wake word detected. Opening the app...");

            // Open the CommunicationActivity where the SOS button resides
            Intent launchIntent = new Intent(context, communication.class);  // Adjust the class as needed
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Ensures the activity opens even if app is in background
            context.startActivity(launchIntent);
        }
    }
}
