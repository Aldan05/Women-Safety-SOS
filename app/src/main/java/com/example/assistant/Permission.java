package com.example.assistant;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Permission extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int SMS_PERMISSION_REQUEST_CODE = 2;
    private static final int CALL_PERMISSION_REQUEST_CODE = 3;
    private static final int MICROPHONE_PERMISSION_REQUEST_CODE = 4;

    private Switch locationSwitch, smsSwitch, callSwitch, microphoneSwitch;
    private Button requestPermissionsButton;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(null);

        locationSwitch = findViewById(R.id.locationSwitch);
        smsSwitch = findViewById(R.id.smsSwitch);
        callSwitch = findViewById(R.id.callSwitch);
        microphoneSwitch = findViewById(R.id.microphoneSwitch);
        requestPermissionsButton = findViewById(R.id.requestPermissionsButton);

        requestPermissionsButton.setOnClickListener(v -> checkAndRequestPermissions());
        updateSwitchStates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSwitchStates();
    }

    private void updateSwitchStates() {
        locationSwitch.setChecked(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        smsSwitch.setChecked(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED);
        callSwitch.setChecked(ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED);
        microphoneSwitch.setChecked(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_REQUEST_CODE);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE_PERMISSION_REQUEST_CODE);
        } else {
            grantCompleted();
        }
    }

    private void grantCompleted() {
        Toast.makeText(this, "All permissions are granted!", Toast.LENGTH_SHORT).show();
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        preferences.edit().putBoolean("isPermissionGranted", true).apply();

        startActivity(new Intent(Permission.this, communication.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(Permission.this, communication.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    public void BackFromPermission(View view) {
        onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case LOCATION_PERMISSION_REQUEST_CODE:
                    locationSwitch.setChecked(true);
                    break;
                case SMS_PERMISSION_REQUEST_CODE:
                    smsSwitch.setChecked(true);
                    break;
                case CALL_PERMISSION_REQUEST_CODE:
                    callSwitch.setChecked(true);
                    break;
                case MICROPHONE_PERMISSION_REQUEST_CODE:
                    microphoneSwitch.setChecked(true);
                    break;
            }
            checkAndRequestPermissions(); // Proceed to next permission
        } else {
            switch (requestCode) {
                case LOCATION_PERMISSION_REQUEST_CODE:
                    locationSwitch.setChecked(false);
                    break;
                case SMS_PERMISSION_REQUEST_CODE:
                    smsSwitch.setChecked(false);
                    break;
                case CALL_PERMISSION_REQUEST_CODE:
                    callSwitch.setChecked(false);
                    break;
                case MICROPHONE_PERMISSION_REQUEST_CODE:
                    microphoneSwitch.setChecked(false);
                    break;
            }
        }
    }
}
