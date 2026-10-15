
/*
package com.example.assistant;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class StartServiceWorker extends Worker {

    // Constructor
    public StartServiceWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Start the service every time the worker runs
        Intent serviceIntent = new Intent(getApplicationContext(), StartService.class);
        getApplicationContext().startService(serviceIntent);

        // Return success after the service is started
        return Result.success();
    }
}

 */
