package com.example.assistant;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class callEmergencyContacts extends AppCompatActivity {
    private static final int REQUEST_CALL_PERMISSION = 1;
    private List<String> phoneNumbers;
    private int currentCallIndex = 0;
    private TelephonyManager telephonyManager;
    private boolean callOngoing = false; // To track if a call was active
    private PhoneCallListener phoneCallListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneNumbers = new ArrayList<>();

        // Add constant numbers directly
        phoneNumbers.add("9787887823");
        phoneNumbers.add("9585152285");
        phoneNumbers.add("9787887823");

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneCallListener = new PhoneCallListener();
        telephonyManager.listen(phoneCallListener, PhoneStateListener.LISTEN_CALL_STATE);

        requestCallPermission();
    }

    private void requestCallPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        } else {
            initiateCall();
        }
    }

    private void initiateCall() {
        if (currentCallIndex < phoneNumbers.size()) {
            String phoneNumber = phoneNumbers.get(currentCallIndex);
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                callOngoing = true; // Mark that a call has started
                startActivity(callIntent);
            }
        }
    }

    private class PhoneCallListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            super.onCallStateChanged(state, phoneNumber);

            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    if (callOngoing) {
                        callOngoing = false; // Reset flag
                        if (currentCallIndex < phoneNumbers.size() - 1) {
                            currentCallIndex++;  // Move to the next number
                            initiateCall();  // Start the next call
                        } else {
                            // All calls completed, reset for next use
                            currentCallIndex = 0;
                        }
                    }
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    callOngoing = true; // Call is in progress
                    break;

                case TelephonyManager.CALL_STATE_RINGING:
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        telephonyManager.listen(phoneCallListener, PhoneStateListener.LISTEN_NONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initiateCall();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
