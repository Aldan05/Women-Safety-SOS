/* package com.example.assistant;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "MyAccessibilityService";

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Accessibility Service Started!");

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;
        setServiceInfo(info);

        restartBackgroundService();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Detect if the service is still running
        Log.d(TAG, "Accessibility Event Triggered: " + event.getEventType());
        restartBackgroundService();
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "Accessibility Service Interrupted!");
    }

    private void restartBackgroundService() {
        Log.d(TAG, "Checking if the background service is running...");
     //   Intent serviceIntent = new Intent(this, WakeWordService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    //        startForegroundService(serviceIntent);
        } else {
     //       startService(serviceIntent);
        }
        Log.d(TAG, "Background Service Restarted!");
    }
}

 */
