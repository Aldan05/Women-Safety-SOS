package com.example.assistant;

import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.*;

public class CallActivity extends AppCompatActivity {

    private Button callButton;
    private Handler retryHandler = new Handler();
    private int callAttemptCount = 0;
    private static final int MAX_CALL_ATTEMPTS = 2;

    // Twilio Credentials (Replace with your actual Twilio credentials)
    private static final String ACCOUNT_SID = "YOUR_TWILIO_ACCOUNT_SID";
    private static final String AUTH_TOKEN = "YOUR_TWILIO_AUTH_TOKEN";
    private static final String TWILIO_NUMBER = "+1234567890";
    private static final String TO_PHONE_NUMBER = "+1234567890";
    private static final String TWILIO_FUNCTION_URL = "https://your-domain.twil.io/word";

    private String selectedLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        // Get selected language from SharedPreferences
        selectedLanguage = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                .getString("selectedLanguage", "english")
                .toLowerCase();  // default to English if not found


        makeCall();

    }

    private void makeCall() {
        if (callAttemptCount >= MAX_CALL_ATTEMPTS) {
            runOnUiThread(() -> Toast.makeText(CallActivity.this, "Max call attempts reached.", Toast.LENGTH_LONG).show());
            return;
        }

        callAttemptCount++;

        try {
            String encodedLanguage = URLEncoder.encode(selectedLanguage, "UTF-8");

            OkHttpClient client = new OkHttpClient();

            String authStr = ACCOUNT_SID + ":" + AUTH_TOKEN;
            String authHeader = "Basic " + Base64.encodeToString(authStr.getBytes(), Base64.NO_WRAP);

            String url = "https://api.twilio.com/2010-04-01/Accounts/" + ACCOUNT_SID + "/Calls.json";

            RequestBody body = new FormBody.Builder()
                    .add("To", TO_PHONE_NUMBER)
                    .add("From", TWILIO_NUMBER)
                    .add("Url", TWILIO_FUNCTION_URL + "?audio=" + encodedLanguage)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Authorization", authHeader)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(CallActivity.this, "Call Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(CallActivity.this, "Call Attempt " + callAttemptCount + " Sent!", Toast.LENGTH_SHORT).show());
                        if (callAttemptCount < MAX_CALL_ATTEMPTS) {
                            retryHandler.postDelayed(CallActivity.this::makeCall, 120000); // 2 min
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(CallActivity.this, "Call Error: " + response.message(), Toast.LENGTH_LONG).show());
                    }
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
