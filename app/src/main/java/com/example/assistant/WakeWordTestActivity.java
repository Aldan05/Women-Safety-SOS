package com.example.assistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class WakeWordTestActivity extends AppCompatActivity {

    private TextView textView,textView2;
    private Toolbar toolbar;

    // Create a BroadcastReceiver to listen for wake word detection
    private BroadcastReceiver wakeWordReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check if the intent action matches our custom action
            if ("com.example.assistant.WAKE_WORD_DETECTED".equals(intent.getAction())) {
                // Update the TextView color to blue
                textView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark)); // Set text color to blue
            }

            if("com.example.assistant.COMMAND_DETECTED".equals(intent.getAction())){
                textView2.setTextColor(getResources().getColor(android.R.color.holo_blue_dark)); // Set text color to blue
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wake_word_test);  // Use a layout for your activity

        // Initialize the Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null); // Remove title if needed
        }

        textView = findViewById(R.id.textView);
        textView.setText("Computer, ");
        textView.setTextColor(getResources().getColor(android.R.color.black));  // Initially black color


        textView2 = findViewById(R.id.textView2);
        textView2.setText("Call police");
        textView2.setTextColor(getResources().getColor(android.R.color.black));  // Initially black color


     //   startWakeWordServiceTest();
    }

  /*  private void startWakeWordServiceTest() {
        Intent serviceIntent = new Intent(this, WakeWordServiceTest.class);
        startService(serviceIntent);
    }

   */


    public void BackFromTestWakeWordService(View view) {

        onBackPressed();


    }



    @Override
    public void onBackPressed() {
        // Retrieve shared preferences
        super.onBackPressed();

        Intent intent = new Intent(WakeWordTestActivity.this, AssistantSettingsInitial.class);   // THIS LINE
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();

    }


    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.assistant.WAKE_WORD_DETECTED");
        filter.addAction("com.example.assistant.COMMAND_DETECTED");
        androidx.core.content.ContextCompat.registerReceiver(this, wakeWordReceiver, filter,
                androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(wakeWordReceiver);
        } catch (IllegalArgumentException e) {
            // receiver was not registered
        }
    }
}