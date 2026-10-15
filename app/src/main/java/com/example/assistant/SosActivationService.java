

package com.example.assistant;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class SosActivationService extends Service {

    private WindowManager windowManager;
    private View floatingView;
    private Handler handler;
    private int timeRemaining = 10; // Starting timer value
    private boolean isFloatingViewVisible = false;
    private Runnable timerRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize WindowManager, handler, etc.
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // For Android O and above, create a Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SOS Activation Channel";
            String description = "Channel for SOS activation notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CHANNEL_ID", name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Show floating notification
        showFloatingNotification();

        // Start the service as a foreground service
        Notification notification = new NotificationCompat.Builder(this, "CHANNEL_ID")
                .setContentTitle("SOS Activation")
                .setContentText("Your SOS notification is active")
                .setSmallIcon(R.drawable.tickicon)
                .build();

        startForeground(1, notification); // Notification ID 1

        return START_STICKY;
    }


    private void showFloatingNotification() {
        if (isFloatingViewVisible) return; // Prevent showing the view again if it's already visible

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        floatingView = inflater.inflate(R.layout.floating_layout, null);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY; // Required for Android 8.0+
        params.format = PixelFormat.TRANSLUCENT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;

        windowManager.addView(floatingView, params);
        isFloatingViewVisible = true;

        // Add timer and dismiss button logic
        Button dismissButton = floatingView.findViewById(R.id.btnDismiss);
        dismissButton.setOnClickListener(v -> dismissFloatingView());

        startTimer();
    }

    private void startTimer() {
        handler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (timeRemaining > 0) {
                    // Update the timer every second
                    timeRemaining--;
                    handler.postDelayed(this, 1000);
                } else {
                    // Handle timer completion
                    dismissFloatingView();
                }
            }
        };
        handler.post(timerRunnable); // Start the countdown
    }

    private void dismissFloatingView() {
        if (isFloatingViewVisible && floatingView != null) {
            windowManager.removeView(floatingView);
            isFloatingViewVisible = false;
            timeRemaining = 10; // Reset timer for next time
            if (handler != null && timerRunnable != null) {
                handler.removeCallbacks(timerRunnable); // Stop the countdown task
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not used in this case
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up and dismiss the floating view when the service is destroyed
        dismissFloatingView();
    }
}

