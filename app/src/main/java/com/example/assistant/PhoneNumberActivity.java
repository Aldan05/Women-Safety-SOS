package com.example.assistant;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.PhoneAuthProvider;

public class PhoneNumberActivity extends AppCompatActivity {

    public String fullPhoneNumber;
    private Spinner spinnerCountry;
    private EditText editTextPhoneNumber;
    private Toolbar toolbar;
    private String[] countriesArray;
    private String[] countryCodesArray;
    private String[] policeNumbersArray;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);

        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //   Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        //   startActivity(intent);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
        }

        // Initialize UI elements
        spinnerCountry = findViewById(R.id.spinner_country);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);

        // Retrieve country names and country codes from string arrays
        countriesArray = getResources().getStringArray(R.array.countries_array);
        countryCodesArray = getResources().getStringArray(R.array.country_codes_array);
        policeNumbersArray = getResources().getStringArray(R.array.police_numbers_array);

        // Create an ArrayAdapter for the Spinner with the countries
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, countriesArray) {
            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view2 = super.getDropDownView(position, convertView, parent);
                if (view2 != null) {
                    hideKeyboard(getCurrentFocus());
                }
                String country = getItem(position);
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setText(country);
                return view;
            }

            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                String country = getItem(position);
                if (position == spinnerCountry.getSelectedItemPosition()) {
                    country = country.replaceAll("[a-zA-Z(), ]", "");
                    country = " " + country;
                }
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setText(country);
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(adapter);

        // Load saved values from SharedPreferences
        loadSavedValues();

        // Handle item selection in the Spinner
        spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                saveValues();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        // Handle Enter key press in the EditText
        editTextPhoneNumber.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                saveValues(); // Save when Enter is pressed
                hideKeyboard(editTextPhoneNumber);
                Toast.makeText(PhoneNumberActivity.this, "Phone number saved", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedValues(); // Reload saved values when the activity resumes
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void BackFromPhoneNumber(View view) {
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isEmergencyContactsSaved = preferences.getBoolean("isEmergencyContactsSaved", false);

        if (isEmergencyContactsSaved) {
            onBackPressed();
        } else {
            Toast.makeText(this, "Emergency Contacts Saved", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(PhoneNumberActivity.this, FirstSigningPage.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        }
    }

    public void ToVerifyPage(View view) {
        String selectedCountryCode = countryCodesArray[spinnerCountry.getSelectedItemPosition()];

        saveValues(); // Save when button is clicked

        if (selectedCountryCode.isEmpty()) {
            Toast.makeText(this, "Please select a country", Toast.LENGTH_SHORT).show();
            return;
        }

        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        phoneNumber = phoneNumber.replaceAll("[^0-9]", "");

        if (phoneNumber.isEmpty() || phoneNumber.length() < 10) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        fullPhoneNumber = "+" + selectedCountryCode + phoneNumber;

        // Save the full phone number in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor2 = sharedPreferences.edit();
        editor2.putString("fullPhoneNumber", fullPhoneNumber);
        editor2.apply();  // or editor.commit(); if you want synchronous saving

        Toast.makeText(this, "Full Phone Number: " + fullPhoneNumber, Toast.LENGTH_LONG).show();

        // Save the police number for the selected country
        int position = spinnerCountry.getSelectedItemPosition();
        String Police_Number = policeNumbersArray[position];

        Toast.makeText(this, "Police number: " + Police_Number, Toast.LENGTH_LONG).show();

        // Save police number to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("PoliceNumberVar", Police_Number);
        editor.apply();  // Save the police number

        // Log the saved police number for debugging


        Toast.makeText(this, "Sending verification code...", Toast.LENGTH_SHORT).show();

        // Proceed with phone verification
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                fullPhoneNumber, 60, java.util.concurrent.TimeUnit.SECONDS,
                this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(com.google.firebase.auth.PhoneAuthCredential phoneAuthCredential) {
                        // Handle verification completion
                    }

                    @Override
                    public void onVerificationFailed(com.google.firebase.FirebaseException e) {
                        Toast.makeText(PhoneNumberActivity.this, "Verification failed. Please check your internet connection.", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(String verificationId, com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken token) {
                        Toast.makeText(PhoneNumberActivity.this, "OTP sent successfully!", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(PhoneNumberActivity.this, VerifyOTPActivity.class);
                        i.putExtra("phoneNumber", fullPhoneNumber);
                        i.putExtra("verificationId", verificationId);
                        startActivity(i);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String returnedPhoneNumber = data.getStringExtra("phoneNumber");
            if (returnedPhoneNumber != null) {
                editTextPhoneNumber.setText(returnedPhoneNumber);
            }
        }
    }

    // User-defined function to check if the internet is available
    public static void checkInternetAndShowToast(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            // If there is no active network or it is not connected, show a toast message
            if (activeNetwork == null || !activeNetwork.isConnected()) {
                Toast.makeText(context, "Please turn on mobile data", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void saveValues() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("SelectedCountry", spinnerCountry.getSelectedItemPosition());
        editor.putString("PhoneNumber", editTextPhoneNumber.getText().toString().trim());
        editor.apply();
    }

    private void loadSavedValues() {
        spinnerCountry.setSelection(sharedPreferences.getInt("SelectedCountry", 98));  // 98 is to set indian number "+91" initially
        editTextPhoneNumber.setText(sharedPreferences.getString("PhoneNumber", ""));
    }
}
