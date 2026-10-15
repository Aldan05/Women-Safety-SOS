package com.example.assistant;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class AssistantSettings extends AppCompatActivity {

    private Toolbar toolbar;
    private Switch wakeWordSwitch, callEmergencySwitch,sosSwitch;
    private SharedPreferences sharedPreferences;
    private SharedPreferences preferences;

    private static final String PREF_NAME = "WakeWordServicePreferences";
    private static final String KEY_WAKE_WORD_ENABLED = "WakeWordEnabled";
    private static final String KEY_FIRST_TIME_WAKE_WORD = "FirstTimeWakeWord";
    private static final String KEY_CALL_EMERGENCY_ENABLED = "CallEmergencyEnabled";
    private static final int MICROPHONE_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant_settings);


        sosSwitch = findViewById(R.id.switch_sos);

        sosSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("sos_switch_state", true); // or false based on logic
                    editor.apply();



                    startButtonSosService();  // Start the service
                } else {

                    SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("sos_switch_state", false); // or false based on logic
                    editor.apply();



                    stopButtonSosService();   // Stop the service
                }
            }
        });

        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
        }

        wakeWordSwitch = findViewById(R.id.wakeWordSwitch);
        callEmergencySwitch = findViewById(R.id.CallEmergencySwitch);

        // First-time logic for wake word switch
        boolean isFirstTime = preferences.getBoolean(KEY_FIRST_TIME_WAKE_WORD, true);
        boolean isWakeWordEnabled = preferences.getBoolean(KEY_WAKE_WORD_ENABLED, false);

        if (isFirstTime) {
            updateWakeWordSwitchState(true);
            wakeWordSwitch.setChecked(true);
            preferences.edit().putBoolean(KEY_FIRST_TIME_WAKE_WORD, false).apply();
            askMicrophonePermission();
        } else {
            wakeWordSwitch.setChecked(isWakeWordEnabled);
        }

        wakeWordSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit();
            editor.putBoolean("isWakeWordSwitchOn2", isChecked);
            editor.apply();

            if (isChecked) {
                askMicrophonePermission();
            } else {
                stopWakeWordService();
                updateWakeWordSwitchState(false);
                Toast.makeText(this, "Wake Word Service Stopped", Toast.LENGTH_SHORT).show();
            }
        });




        // Load Call Emergency Switch state
        boolean isCallEmergencyEnabled = preferences.getBoolean(KEY_CALL_EMERGENCY_ENABLED, true);
        callEmergencySwitch.setChecked(isCallEmergencyEnabled);

        callEmergencySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateCallEmergencyState(isChecked);
            SharedPreferences.Editor editor = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit();
            editor.putBoolean("isEmergencyCallSwitchOn", isChecked);
            editor.apply();
            Toast.makeText(this, "Emergency Call " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
        });

        // Custom Wake Word Setup
        EditText editCustomWakeWord = findViewById(R.id.editCustomWakeWord);
        Button btnSaveWakeWord = findViewById(R.id.btnSaveWakeWord);

        SharedPreferences myPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String savedWakeWord = myPrefs.getString("customWakeWord", "Assistant");
        editCustomWakeWord.setText(savedWakeWord);

        btnSaveWakeWord.setOnClickListener(v -> {
            String word = editCustomWakeWord.getText().toString().trim();
            if (word.isEmpty()) {
                Toast.makeText(this, "Please enter a wake word", Toast.LENGTH_SHORT).show();
                return;
            }
            myPrefs.edit().putString("customWakeWord", word).apply();
            Toast.makeText(this, "Wake word set to: " + word, Toast.LENGTH_SHORT).show();
        });

        // Custom SOS Message Setup
        EditText editCustomSOSMessage = findViewById(R.id.editCustomSOSMessage);
        Button btnSaveSOSMessage = findViewById(R.id.btnSaveSOSMessage);

        String savedSOSMsg = myPrefs.getString("customSOSMessage", "🚨 EMERGENCY! I need immediate help. My live GPS location is:");
        editCustomSOSMessage.setText(savedSOSMsg);

        btnSaveSOSMessage.setOnClickListener(v -> {
            String msg = editCustomSOSMessage.getText().toString().trim();
            if (msg.isEmpty()) {
                Toast.makeText(this, "Please enter an emergency message", Toast.LENGTH_SHORT).show();
                return;
            }
            myPrefs.edit().putString("customSOSMessage", msg).apply();
            Toast.makeText(this, "Emergency message saved!", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateCallEmergencyState(boolean isEnabled) {
        preferences.edit().putBoolean(KEY_CALL_EMERGENCY_ENABLED, isEnabled).apply();
    }

    private void askMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE_PERMISSION_CODE);
        } else {
            if (wakeWordSwitch.isChecked()) {
                startWakeWordService();
                Toast.makeText(this, "Wake Word Service Started", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MICROPHONE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startWakeWordService();
                updateWakeWordSwitchState(true);
                Toast.makeText(this, "Microphone permission granted. Wake Word Service Started", Toast.LENGTH_SHORT).show();
            } else {
                updateWakeWordSwitchState(false);
                wakeWordSwitch.setChecked(false);
                Toast.makeText(this, "Microphone permission denied. Wake Word Service not started", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void BackFromAssistantSettings(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isEmergencyContactsSaved = preferences.getBoolean("isEmergencyContactsSaved", false);

        if (isEmergencyContactsSaved) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Emergency Contacts Saved", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AssistantSettings.this, EmergencyContacts.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        }
    }

    private void startWakeWordService() {
        Intent serviceIntent = new Intent(this, WakeWordService.class);
        startService(serviceIntent);
    }

    private void stopWakeWordService() {
        Intent serviceIntent = new Intent(this, WakeWordService.class);
        stopService(serviceIntent);
    }

    public void MoveToNextFromSettings(View view) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("AssistantVar", 1);
        editor.apply();

        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isEmergencyContactsSaved = preferences.getBoolean("isEmergencyContactsSaved", false);

        if (isEmergencyContactsSaved) {
            Toast.makeText(this, "Emergency Contacts Saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Emergency Contacts Saved", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AssistantSettings.this, communication.class);
            startActivity(intent);
            finish();
        }
    }
    public void startButtonSosService() {
        Intent intent = new Intent(this, SOSService.class);
        startService(intent);
    }

    public void stopButtonSosService() {
        Intent intent = new Intent(this, SOSService.class);
        stopService(intent);
    }

    private void updateWakeWordSwitchState(boolean isEnabled) {
        wakeWordSwitch.setChecked(isEnabled);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_WAKE_WORD_ENABLED, isEnabled);
        editor.apply();
    }
}
