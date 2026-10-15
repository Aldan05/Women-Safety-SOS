/*
package com.example.assistant;

import ai.picovoice.rhino.*;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import java.util.Map;

public class RhinoActivity extends AppCompatActivity {

    private static final String TAG = "RhinoActivity";
    private RhinoManager rhinoManager;
    private Vibrator vibrator;  // Declare the vibrator
    private static final String ACCESS_KEY = "yfKUCFtTCDBj8L4qs9Z2K4TE0GDR0DOfoTHY+S4ANZBYuGqPKdzecw==";  // Replace with your actual Picovoice access key
    private static final String CONTEXT_FILE_PATH = "call-police_en_android_v3_0_0.rhn"; // Path to the custom context file in assets folder
    private static final int REQUEST_CODE_RECORD_AUDIO = 100; // For runtime permissions

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rhino);

        // Initialize Vibrator
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Request necessary permissions if not already granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_RECORD_AUDIO);
        } else {
            // Initialize RhinoManager
            initializeRhinoManager();
        }
    }

    private void initializeRhinoManager() {
        // Define the callback for inference
        RhinoManagerCallback inferenceCallback = new RhinoManagerCallback() {
            @Override
            public void invoke(RhinoInference inference) {
                if (inference.getIsUnderstood()) {
                    String intent = inference.getIntent();
                    Map<String, String> slots = inference.getSlots();
                    // Take action based on inferred intent and slot values
                    handleInferenceResult(intent, slots);
                    Toast.makeText(getApplicationContext(), "Comment detected", Toast.LENGTH_SHORT).show();

                    // Vibrate the phone when the context is detected
                    if (vibrator != null) {
                        // Vibrate for 500 milliseconds (0.5 seconds)
                        vibrator.vibrate(500);
                    } else {
                        Log.e(TAG, "Vibrator not available.");
                    }
                } else {
                    // Handle unsupported commands
                    Log.e(TAG, "Command not understood.");
                }
            }
        };

        // Create an instance of RhinoManager with your access key and context file
        try {
            rhinoManager = new RhinoManager.Builder()
                    .setAccessKey(ACCESS_KEY)
                    .setContextPath(CONTEXT_FILE_PATH)  // Context file path in the assets folder
                    .setSensitivity(0.9f)
                    .build(getApplicationContext(), inferenceCallback);

            Log.d(TAG, "RhinoManager initialized successfully.");

            // Start the audio capture and speech-to-intent processing
            rhinoManager.process();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing RhinoManager: " + e.getMessage());
            e.printStackTrace();  // Log the full stack trace
            Toast.makeText(this, "Failed to initialize speech processing.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleInferenceResult(String intent, Map<String, String> slots) {
        // Process intent and slots based on your custom context
        Log.d(TAG, "Intent: " + intent);
        Log.d(TAG, "Slots: " + slots);

        // Example: Handle an action based on the inferred intent
        if ("play_music".equals(intent)) {
            String song = slots.get("song");
            Log.d(TAG, "Playing song: " + song);
            // Implement your action, e.g., play the song using an audio player
        } else {
            Log.d(TAG, "Unsupported intent.");
        }
    }

    private String getContextFilePath() {
        // Path to the context file in assets folder
        return CONTEXT_FILE_PATH;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clean up and free resources used by RhinoManager
        if (rhinoManager != null) {
            try {
                rhinoManager.delete(); // Deletes RhinoManager and frees resources
            } catch (Exception e) {
                Log.e(TAG, "Error freeing resources: " + e.getMessage());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with initialization
                initializeRhinoManager();
            } else {
                // Handle the case when permission is denied
                Toast.makeText(this, "Permission denied for audio recording", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

 */
