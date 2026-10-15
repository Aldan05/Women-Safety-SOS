package com.example.assistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class VerifyOTPActivity extends AppCompatActivity {

    private EditText editTextOTP;
    private String phoneNumber, mVerificationId;
    private FirebaseAuth mAuth;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otpactivity);


        toolbar = findViewById(R.id.toolbar);

        // Set up the Toolbar as the ActionBar
        setSupportActionBar(toolbar);

        // Remove the title from the Toolbar (ActionBar)
        // Remove the title from the Toolbar
        // Remove the title from the Toolbar (ActionBar)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null); // This removes the title
        }



        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Retrieve the phone number and verification ID passed from the previous activity
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        mVerificationId = getIntent().getStringExtra("verificationId");

        // Ensure the verificationId is not null
        if (mVerificationId == null || phoneNumber == null) {
            Toast.makeText(this, "Missing verification details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        editTextOTP = findViewById(R.id.editTextOTP);

        // When the user presses the 'Verify OTP' button
        findViewById(R.id.buttonVerifyOTP).setOnClickListener(v -> verifyOTP());
    }


    public void BackFromVerify(View view) {

        // Start next activity
        Intent intent = new Intent(VerifyOTPActivity.this, PhoneNumberActivity.class);   // THIS LINE
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }







    private void verifyOTP() {
        String otp = editTextOTP.getText().toString().trim();

        if (TextUtils.isEmpty(otp)) {
            Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a PhoneAuthCredential with the entered OTP and verification ID
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);

        // Sign in with the credential
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(VerifyOTPActivity.this, "Verified successfully!", Toast.LENGTH_SHORT).show();
                        // Authentication successful, navigate to the Details page
                        Intent intent = new Intent(VerifyOTPActivity.this, Details.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                    } else {
                        // Authentication failed
                        String errorMessage = task.getException().getMessage();
                        if (errorMessage != null) {
                            if (errorMessage.contains("The sms code has expired")) {
                                Toast.makeText(VerifyOTPActivity.this, "OTP expired. Please try again.", Toast.LENGTH_LONG).show();
                            } else if (errorMessage.contains("The sms code is incorrect")) {
                                Toast.makeText(VerifyOTPActivity.this, "Incorrect OTP. Please try again.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(VerifyOTPActivity.this, "Please enter valid OTP", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(VerifyOTPActivity.this, "Authentication Failed. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
