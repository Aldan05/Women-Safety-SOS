package com.example.assistant;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Switch;
import androidx.appcompat.widget.Toolbar;

public class AssistantSettingsInitial extends AppCompatActivity {
    private Toolbar toolbar;
    private Switch wakeWordSwitch;
    private SharedPreferences preferences;
    private static final String PREF_NAME = "WakeWordServicePreferences";
    private static final String KEY_WAKE_WORD_ENABLED = "WakeWordEnabled";
    private static final int MICROPHONE_PERMISSION_CODE = 1;
    private SharedPreferences sharedPreferences;
    private boolean shouldStartTestWakeWordAfterPermission = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant_settings_initial);

        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);

        // Initialize the Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null); // Remove title if needed
        }


    }



    public void MoveToTestWakeWord(View view) {

        startTestWakeWord();

    }

    public void BackFromAssistantSettingsInitial(View view) {

        onBackPressed();

    }



    private void startTestWakeWord() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            shouldStartTestWakeWordAfterPermission = true; // 👈 set flag
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE_PERMISSION_CODE);
        } else {
            Intent intent = new Intent(AssistantSettingsInitial.this, WakeWordTestActivity.class);
            startActivity(intent);
            finish();
        }
    }



    @Override
    public void onBackPressed() {
        // Retrieve shared preferences
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isEmergencyContactsSaved = preferences.getBoolean("isEmergencyContactsSaved", false);

        // Check if emergency contacts are saved
        if (isEmergencyContactsSaved) {
            // Emergency contacts are saved, proceed with default back button behavior
            super.onBackPressed(); // Calls the default onBackPressed behavior
        } else {
            // Emergency contacts are not saved, show a message and start next activity
            Toast.makeText(this, "Emergency Contacts Saved", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AssistantSettingsInitial.this,EmergencyContacts.class); // Start Details activity
            startActivity(intent);

            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

            finish(); // Optionally, finish this activity so it's not in the back stack
        }
    }


}
