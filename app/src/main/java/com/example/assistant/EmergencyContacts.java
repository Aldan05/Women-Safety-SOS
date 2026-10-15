package com.example.assistant;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContacts extends AppCompatActivity {

    private static final int PICK_DYNAMIC_CONTACT = 1001;
    private static final int REQUEST_READ_CONTACTS = 100;

    private Toolbar toolbar;
    private LinearLayout contactsContainer;
    private Button btnAddContact, saveButton;
    private Spinner languageSpinner;
    private SharedPreferences sharedPreferences;

    private EditText targetMobileEditText = null;
    private EditText targetNameEditText = null;

    private final String[] languages = {
            "Select Language", "Tamil", "English", "Hindi", "Malayalam", "Telungu", "Kannada",
            "Bengali", "Marathi", "Gujarati", "Urdu"
    };

    private final List<View> contactRowViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
        }

        contactsContainer = findViewById(R.id.contactsContainer);
        btnAddContact = findViewById(R.id.btnAddContact);
        saveButton = findViewById(R.id.saveButton);
        languageSpinner = findViewById(R.id.languageSpinner);

        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);

        btnAddContact.setOnClickListener(v -> addContactRow("", "", "Select Relation"));
        saveButton.setOnClickListener(v -> saveData());

        setupSpinners();
        loadSavedData();

        // If no contacts loaded, add at least one default row
        if (contactRowViews.isEmpty()) {
            addContactRow("", "", "Select Relation");
        }
    }

    private void setupSpinners() {
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languages);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(languageAdapter);
    }

    private void addContactRow(String name, String mobile, String relation) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View rowView = inflater.inflate(R.layout.item_emergency_contact, contactsContainer, false);

        TextView label = rowView.findViewById(R.id.contactLabel);
        EditText editName = rowView.findViewById(R.id.editContactName);
        EditText editMobile = rowView.findViewById(R.id.editContactMobile);
        Spinner spinnerRel = rowView.findViewById(R.id.spinnerRelation);
        Button btnSelect = rowView.findViewById(R.id.btnSelectContact);
        Button btnDelete = rowView.findViewById(R.id.btnDeleteContact);

        ArrayAdapter<CharSequence> relationAdapter = ArrayAdapter.createFromResource(this,
                R.array.relation_array, android.R.layout.simple_spinner_item);
        relationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRel.setAdapter(relationAdapter);

        editName.setText(name);
        editMobile.setText(mobile);
        if (!relation.equals("Select Relation")) {
            int pos = relationAdapter.getPosition(relation);
            if (pos >= 0) spinnerRel.setSelection(pos);
        }

        label.setText("Contact " + (contactRowViews.size() + 1));

        btnSelect.setOnClickListener(v -> {
            targetNameEditText = editName;
            targetMobileEditText = editMobile;
            openContactsPicker();
        });

        btnDelete.setOnClickListener(v -> {
            contactsContainer.removeView(rowView);
            contactRowViews.remove(rowView);
            updateContactLabels();
        });

        contactsContainer.addView(rowView);
        contactRowViews.add(rowView);
        updateContactLabels();
    }

    private void updateContactLabels() {
        for (int i = 0; i < contactRowViews.size(); i++) {
            View view = contactRowViews.get(i);
            TextView label = view.findViewById(R.id.contactLabel);
            if (label != null) {
                label.setText("Contact " + (i + 1));
            }
        }
    }

    private void openContactsPicker() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, PICK_DYNAMIC_CONTACT);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_DYNAMIC_CONTACT && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(contactUri, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                int contactIdIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);

                if (nameIndex != -1 && contactIdIndex != -1) {
                    String contactName = cursor.getString(nameIndex);
                    String contactId = cursor.getString(contactIdIndex);

                    Cursor phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{contactId},
                            null
                    );

                    if (phoneCursor != null && phoneCursor.moveToFirst()) {
                        int phoneNumberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        if (phoneNumberIndex != -1) {
                            String phoneNumber = phoneCursor.getString(phoneNumberIndex);

                            if (targetNameEditText != null) targetNameEditText.setText(contactName);
                            if (targetMobileEditText != null) targetMobileEditText.setText(phoneNumber);
                        }
                        phoneCursor.close();
                    }
                }
                cursor.close();
            }
        }
    }

    private void saveData() {
        if (contactRowViews.isEmpty()) {
            Toast.makeText(this, "Please add at least one emergency contact", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedLanguage = languageSpinner.getSelectedItem().toString();
        if (selectedLanguage.equals("Select Language")) {
            Toast.makeText(this, "Please select an alert language", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("selectedLanguage", selectedLanguage);

        int savedCount = 0;
        for (int i = 0; i < contactRowViews.size(); i++) {
            View view = contactRowViews.get(i);
            EditText editName = view.findViewById(R.id.editContactName);
            EditText editMobile = view.findViewById(R.id.editContactMobile);
            Spinner spinnerRel = view.findViewById(R.id.spinnerRelation);

            String name = editName.getText().toString().trim();
            String mobile = editMobile.getText().toString().trim();
            String relation = spinnerRel.getSelectedItem().toString();

            if (!name.isEmpty() && !mobile.isEmpty()) {
                savedCount++;
                editor.putString("contact" + savedCount + "Name", name);
                editor.putString("contact" + savedCount + "Mobile", mobile);
                editor.putString("contact" + savedCount + "Relation", relation);
            }
        }

        if (savedCount == 0) {
            Toast.makeText(this, "Please fill in valid name and mobile for at least one contact", Toast.LENGTH_SHORT).show();
            return;
        }

        editor.putInt("totalContacts", savedCount);
        editor.putInt("EmerContVar", 1);
        editor.apply();

        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().putBoolean("isEmergencyContactsSaved", true).apply();

        appearOnTopPermissionDialog();
    }

    private void loadSavedData() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int totalContacts = sharedPreferences.getInt("totalContacts", 0);

        if (totalContacts > 0) {
            for (int i = 1; i <= totalContacts; i++) {
                String name = sharedPreferences.getString("contact" + i + "Name", "");
                String mobile = sharedPreferences.getString("contact" + i + "Mobile", "");
                String relation = sharedPreferences.getString("contact" + i + "Relation", "Select Relation");
                if (!name.isEmpty() || !mobile.isEmpty()) {
                    addContactRow(name, mobile, relation);
                }
            }
        } else {
            // Legacy fallback
            String c1Name = sharedPreferences.getString("contact1Name", "");
            String c1Mobile = sharedPreferences.getString("contact1Mobile", "");
            String c1Relation = sharedPreferences.getString("contact1Relation", "Select Relation");
            if (!c1Name.isEmpty() || !c1Mobile.isEmpty()) {
                addContactRow(c1Name, c1Mobile, c1Relation);
            }

            String c2Name = sharedPreferences.getString("contact2Name", "");
            String c2Mobile = sharedPreferences.getString("contact2Mobile", "");
            String c2Relation = sharedPreferences.getString("contact2Relation", "Select Relation");
            if (!c2Name.isEmpty() || !c2Mobile.isEmpty()) {
                addContactRow(c2Name, c2Mobile, c2Relation);
            }

            String c3Name = sharedPreferences.getString("contact3Name", "");
            String c3Mobile = sharedPreferences.getString("contact3Mobile", "");
            String c3Relation = sharedPreferences.getString("contact3Relation", "Select Relation");
            if (!c3Name.isEmpty() || !c3Mobile.isEmpty()) {
                addContactRow(c3Name, c3Mobile, c3Relation);
            }
        }

        String savedLanguage = sharedPreferences.getString("selectedLanguage", "Select Language");
        if (savedLanguage != null && languageSpinner.getAdapter() != null) {
            int spinnerPosition = ((ArrayAdapter<String>) languageSpinner.getAdapter()).getPosition(savedLanguage);
            languageSpinner.setSelection(spinnerPosition);
        }
    }

    private void appearOnTopPermissionDialog() {
        String selectedLanguage = languageSpinner.getSelectedItem().toString();

        new AlertDialog.Builder(this)
                .setMessage("Do your emergency contacts know " + selectedLanguage + " language?")
                .setCancelable(false)
                .setPositiveButton("Yes, they know", (dialog, which) -> OpenNextPage())
                .setNegativeButton("No, they don't", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void OpenNextPage() {
        Toast.makeText(this, "Emergency Contacts Saved", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(EmergencyContacts.this, communication.class);
        startActivity(intent);
        finish();
    }

    public void BackFromEmergencyContacts(View view) {
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
            Intent intent = new Intent(EmergencyContacts.this, Details.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        }
    }
}
