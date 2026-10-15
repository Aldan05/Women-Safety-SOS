
/*
package com.example.assistant;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;

public class MyJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        // Perform your background task, e.g., vibration
        return false; // No need to reschedule
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false; // No need to reschedule
    }
}

// In your main service or activity, schedule the job:
JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
JobInfo jobInfo = new JobInfo.Builder(123, new ComponentName(this, MyJobService.class))
        .setRequiresCharging(false)
        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        .setPersisted(true)  // Ensures job runs even after app is closed
        .setBackoffCriteria(5000, JobInfo.BACKOFF_POLICY_LINEAR) // Retry after 5 seconds
        .build();
jobScheduler.schedule(jobInfo);


 */