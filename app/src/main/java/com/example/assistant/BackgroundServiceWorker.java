package com.example.assistant;

import androidx.work.Worker;
import androidx.work.WorkerParameters;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BackgroundServiceWorker extends Worker {

    private static final String TAG = "BackgroundServiceWorker";

    public BackgroundServiceWorker(Context context, WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public Result doWork() {
        // Log the execution of the background task
        Log.d(TAG, "Executing background service after delay (Starting Wake word service)");

        // Start your background service here
        Intent intent = new Intent(getApplicationContext(), WakeWordService.class);
        getApplicationContext().startService(intent);

        // Return success after starting the service
        return Result.success();
    }
}
