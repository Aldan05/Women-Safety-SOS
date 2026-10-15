package com.example.assistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.app.PendingIntent;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class SOSReceiver extends BroadcastReceiver {
    private static int pressCount = 0;
    private static long firstPressTime = 0;
    private static final long TIME_WINDOW = 3000; // 3 seconds window for 3 presses
    private static final int REQUIRED_PRESSES = 3;

    private static final String CHANNEL_ID = "sos_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action != null) {
            if (action.equals(Intent.ACTION_SCREEN_OFF) || action.equals(Intent.ACTION_SCREEN_ON)) {
                long currentTime = System.currentTimeMillis();

                if (firstPressTime == 0 || (currentTime - firstPressTime) > TIME_WINDOW) {
                    firstPressTime = currentTime;
                    pressCount = 1;
                    Log.d("SOSReceiver", "1st power button press recorded");
                } else {
                    pressCount++;
                    Log.d("SOSReceiver", "Power button press count: " + pressCount);

                    if (pressCount >= REQUIRED_PRESSES) {
                        Log.d("SOSReceiver", "SOS triggered by 3 power button presses!");
                        sendSOSAlert(context);
                        showFloatingNotification(context);
                        startTimerPage(context);

                        pressCount = 0;
                        firstPressTime = 0;
                    }
                }
            }
        }
    }

    private void sendSOSAlert(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            long[] pattern = {0, 500, 500, 500, 500}; 
            vibrator.vibrate(pattern, -1);
        }
    }

    private void showFloatingNotification(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SOS Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Heads-up notifications for SOS alerts");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("SOS Triggered!")
                .setContentText("Emergency SOS activated via power button (3 clicks)!")
                .setSmallIcon(R.drawable.tickicon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLights(Color.RED, 3000, 3000);

        builder.addAction(R.drawable.tickicon, "Stop", getStopPendingIntent(context));

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(1, builder.build());
        }
    }

    private PendingIntent getStopPendingIntent(Context context) {
        Intent stopIntent = new Intent(context, StopSOSReceiver.class);
        stopIntent.putExtra("action", "stop_sos");
        return PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void startTimerPage(Context context) {
        Intent intent = new Intent(context, FullScreenSosActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        String channelId = "sos_fullscreen_channel";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Emergency SOS Alarms",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setBypassDnd(true);
            channel.enableVibration(true);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.tickicon)
                .setContentTitle("🚨 EMERGENCY SOS TRIGGERED")
                .setContentText("Tap to open countdown & alert contacts")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(999, builder.build());
        }

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("SOSReceiver", "Failed to start FullScreenSosActivity directly", e);
        }
    }
}
