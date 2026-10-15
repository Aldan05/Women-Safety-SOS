package com.example.assistant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.view.View; // Import the View class

public class FirstSigningPage extends AppCompatActivity {

    private static final int REQUEST_CODE_OVERLAY_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_signing_page);  // Correct layout name

        if (!Settings.canDrawOverlays(this)) {

            appearOnTopPermissionDialog();

            // If permission is already granted, enable the button to show the floating notification
            // findViewById(R.id.button).setOnClickListener(v -> {startSosTriggeredByAssistant(); startSendLocationService(); }); // triggerDelayedFloatingNotification()
        }


    }



    private void appearOnTopPermissionDialog() {
        new AlertDialog.Builder(this)
                .setMessage("permission needed to appear on top of other apps")       //ALTER WORDINGS HERE
                .setCancelable(false)
                .setPositiveButton("OK, GOT IT", (dialog, which) -> appearOnTopPermission())
                .show();
    }


    private void appearOnTopPermission(){
        if(!Settings.canDrawOverlays(this)){
            // Request overlay permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION);
        }
    }

    // Method to handle the button click and navigate to PhoneNumberActivity
    public void OpenPhoneNumberPage(View view) {
        // Create the Intent to go to the PhoneNumberActivity
        Intent intent = new Intent(FirstSigningPage.this, PhoneNumberActivity.class);
        startActivity(intent);

        // Optional: Finish this activity so that the user can't go back to it
        finish();
    }
}
