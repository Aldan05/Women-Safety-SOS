package com.example.assistant;

import android.app.Activity;
import android.app.KeyguardManager;
import android.os.Bundle;

public class KeyguardActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use KeyguardManager to dismiss the keyguard
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

        if (keyguardManager != null && keyguardManager.isKeyguardLocked()) {
            keyguardManager.requestDismissKeyguard(this, null);
        }

        // Finish the activity immediately
        finish();
    }
}
