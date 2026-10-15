
/*
    public static boolean isButtonServiceRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            // Check if the WakeWordService is running
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (SOSService.class.getName().equals(service.service.getClassName())) {
                    return true;  // WakeWordService is running
                }
            }
        }
        return false;  // WakeWordService is not running
    }


 */








package com.example.assistant;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class ServiceUtils {

    public static void scheduleServiceRestart(Context context) {
        // Create an intent to start the service
        Intent intent = new Intent(context, WakeWordService.class);

        // Create a PendingIntent for the service
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, flags);

        // Set the alarm to restart the service after a delay (e.g., 10 seconds)
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            try {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, pendingIntent);
            } catch (SecurityException se) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, pendingIntent);
            }
        }
    }
}




