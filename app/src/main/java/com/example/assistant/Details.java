package com.example.assistant;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.DatePickerDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.Calendar;

public class Details extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    public int DetailsVar = 0;
    private static final String TAG = "WakeWordService";
    private Button dateButton;
    private DatePickerDialog datePickerDialog;
    private Spinner genderSpinner;
    private EditText nameEditText;
    private EditText addressEditText;
    private SharedPreferences sharedPreferences;
    private Vibrator vibrator;
    private boolean isBackPressedOnce = false;  // Flag for back button press

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);

        // Initialize views
        nameEditText = findViewById(R.id.nameEditText);
        addressEditText = findViewById(R.id.Address);
        dateButton = findViewById(R.id.datePickerButton);
        genderSpinner = findViewById(R.id.spinner_gender);

        // Initialize the date picker
        iniDatePicker();

        // Set the initial placeholder text for the date button
        dateButton.setText("Date of Birth");

        // Set up the Spinner (Gender selection)
        String[] genderOptions = getResources().getStringArray(R.array.gender_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        // Set the first item ("Select Gender") as selected by default
        genderSpinner.setSelection(0);

        // Load saved data
        loadSavedData();

        Intent serviceIntent = new Intent(this, WakeWordService.class);
        stopService(serviceIntent);

        // Listener for item selection in the Spinner
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position > 0) {  // Avoid triggering for "Select Gender"
                    String selectedGender = parentView.getItemAtPosition(position).toString();
                    // Save the selected gender
                    saveData("gender", selectedGender);
                    Toast.makeText(Details.this, getString(R.string.selected_gender, selectedGender), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Optionally handle the case when no item is selected
            }
        });
    }

    public void BackFromDetails(View view) {
        // Your code to handle back press
        onBackPressed();
    }


    // Override onBackPressed to handle the back button press
    @Override
    public void onBackPressed() {
        // Retrieve shared preferences
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isEmergencyContactsSaved = preferences.getBoolean("isEmergencyContactsSaved", false);

        if (isEmergencyContactsSaved) {
            // Emergency contacts are saved, proceed with default back button behavior
            super.onBackPressed();  // Calls the default onBackPressed behavior
        } else {
            // Emergency contacts are not saved, show a message and start next activity
            if (isBackPressedOnce) {
                // If back was pressed twice in quick succession, exit the app
                super.onBackPressed();  // Exit the app
            } else {
                // If back was pressed once, show the "Press again to exit" message
                isBackPressedOnce = true;
                Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();

                // Reset the flag after 2 seconds (2000 milliseconds)
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isBackPressedOnce = false;  // Reset the flag after 2 seconds
                    }
                }, 2000);  // Reset flag after 2 seconds
            }
        }
    }
    // Initialize the DatePicker
    private void iniDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1; // Adjust month as DatePickerDialog uses 0-based month
                String date = makeDateString(day, month, year);
                dateButton.setText(date);  // Update button text to selected date

                // Save the selected date
                saveData("dob", date);
            }
        };

        // Get current date
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        // Style for the DatePickerDialog
        int style = AlertDialog.THEME_HOLO_LIGHT;
        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
    }

    // Method to format the date string
    private String makeDateString(int day, int month, int year) {
        return getMonthFormat(month) + " " + day + " " + year;
    }

    // Get month in text format (e.g., "JAN", "FEB")
    private String getMonthFormat(int month) {
        switch (month) {
            case 1: return "JAN";
            case 2: return "FEB";
            case 3: return "MAR";
            case 4: return "APR";
            case 5: return "MAY";
            case 6: return "JUN";
            case 7: return "JUL";
            case 8: return "AUG";
            case 9: return "SEP";
            case 10: return "OCT";
            case 11: return "NOV";
            case 12: return "DEC";
            default: return "JAN";  // default case
        }
    }

    // Method to open DatePicker when button is clicked
    public void openDatePicker(View view) {
        datePickerDialog.show();
    }

    // Save data to SharedPreferences
    private void saveData(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    // Load saved data from SharedPreferences
    private void loadSavedData() {
        String savedName = sharedPreferences.getString("name", "");
        String savedDOB = sharedPreferences.getString("dob", "Date of Birth");
        String savedGender = sharedPreferences.getString("gender", "Select Gender");
        String savedAddress = sharedPreferences.getString("address", ""); // Load the saved address

        // Set the loaded data to views
        nameEditText.setText(savedName);  // Set saved name
        dateButton.setText(savedDOB);  // Set saved date of birth
        int genderPosition = ((ArrayAdapter<String>) genderSpinner.getAdapter()).getPosition(savedGender);
        genderSpinner.setSelection(genderPosition);  // Set saved gender
        addressEditText.setText(savedAddress);  // Set saved address
    }

    // OnClick method for "UPDATE" button to save all the data (e.g., name, gender, dob)
    public void saveData(View view) {
        String name = nameEditText.getText().toString().trim();
        String dob = dateButton.getText().toString().trim();
        String gender = (String) genderSpinner.getSelectedItem();
        String address = addressEditText.getText().toString().trim(); // Get the address from EditText

        // Check if all fields are filled
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return; // Prevent data update if name is empty
        }

        if (gender.equals("Select Gender")) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            return; // Prevent data update if gender is not selected
        }

        if (dob.equals("Date of Birth") || dob.isEmpty()) {
            Toast.makeText(this, "Please select your date of birth", Toast.LENGTH_SHORT).show();
            return; // Prevent data update if date of birth is not selected
        }

        if (address.isEmpty()) { // Check if the address field is empty
            Toast.makeText(this, "Please enter your address", Toast.LENGTH_SHORT).show();
            return; // Prevent data update if address is empty
        }

        // Save the data
        saveData("name", name);
        saveData("dob", dob);
        saveData("gender", gender);
        saveData("address", address); // Save the address to SharedPreferences

        // to visible the name under account in communication
        SharedPreferences.Editor editor2 = sharedPreferences.edit();
        editor2.putString("name", name); // Save the name value
        editor2.apply();

        // Save DetailsVar to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("DetailsVar", 1);  // Save the value of DetailsVar
        editor.apply();

        // In another Activity (e.g., DetailsActivity or HomeActivity)
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isEmergencyContactsSaved = preferences.getBoolean("isEmergencyContactsSaved", false); // Default to false if not set

        if (isEmergencyContactsSaved) {
            // Handle the case where the emergency contacts have been saved
            Toast.makeText(this, "Data Saved", Toast.LENGTH_SHORT).show();
        } else {
            // Show a message confirming data has been saved
            Toast.makeText(this, "Data Saved", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Details.this, EmergencyContacts.class);
            startActivity(intent);
            finish();
        }
    }
}
