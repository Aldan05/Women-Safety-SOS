package com.example.assistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class SOSService extends Service {

    // CODE TO TROGGER SOS WHEN POWER BUTTON IS PRESSED

    private SOSReceiver receiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SOS service", "sos service started");
        // Initialize and register the SOSReceiver
        receiver = new SOSReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver, filter);

        // Start the service as a foreground service
        startForegroundService();




    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;  // Keep the service running
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);  // Unregister the receiver when the service is stopped
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startForegroundService() {
        String channelId = "SOS_CHANNEL";

        // Create NotificationChannel for devices running Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "SOS Service", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Create a persistent notification
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("SOS App")
                .setContentText("Listening for SOS trigger...")
                .setSmallIcon(R.drawable.tickicon) // Replace with your actual icon
                .setOngoing(true) // Keeps the notification persistent
                .build();

        // Start the service in the foreground with the notification
        startForeground(1, notification);
    }

    private void startSosTriggeredByAssistant() {
        Intent intent = new Intent(SOSService.this, SosTriggeredByAssistant.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  // Add this line to set the new task flag
        startActivity(intent);
    }

}
