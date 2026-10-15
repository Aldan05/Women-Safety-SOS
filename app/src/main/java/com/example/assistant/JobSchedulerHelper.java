/*
package com.example.assistant;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

public class JobSchedulerHelper {

    public static void scheduleJob(Context context) {
        // Create the JobInfo for WakeWordJobService
        JobInfo.Builder builder = new JobInfo.Builder(0, new ComponentName(context, WakeWordJobService.class))
                .setPersisted(true)  // Ensures the job survives device reboots
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // You can change this to NETWORK_TYPE_UNMETERED, NETWORK_TYPE_WIFI, etc.
                .setRequiresCharging(false) // Set to true if the job should only run when the device is charging
                .setPeriodic(15 * 60 * 1000); // Sets a periodic job every 15 minutes (set 0 for one-time)

        // If the device is running on API 26 or above, you need to set an explicit deadline for the job
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setMinimumLatency(30 * 1000); // Set minimum delay before the job runs (e.g., 30 seconds)
            builder.setOverrideDeadline(60 * 1000); // Maximum delay (e.g., 1 minute)
        }

        // Schedule the job using the JobScheduler
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler != null) {
            scheduler.schedule(builder.build());
        }
    }
}


 */

