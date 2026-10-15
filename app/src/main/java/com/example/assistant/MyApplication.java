// MyApplication.java (or modify if already exists)
package com.example.assistant;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class MyApplication extends Application {

    private static Activity currentActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

            @Override
            public void onActivityStarted(Activity activity) {}

            @Override
            public void onActivityResumed(Activity activity) {
                currentActivity = activity;  // Track the current activity
            }

            @Override
            public void onActivityPaused(Activity activity) {
                if (currentActivity == activity) {
                    currentActivity = null;
                }
            }

            @Override
            public void onActivityStopped(Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

            @Override
            public void onActivityDestroyed(Activity activity) {
                if (currentActivity == activity) {
                    currentActivity = null;
                }
            }
        });
    }

    public static Activity getCurrentActivity() {
        return currentActivity;
    }
}
