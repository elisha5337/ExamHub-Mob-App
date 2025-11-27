package com.example.examhubapp;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Signup extends AppCompatActivity {
    private MyDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        dbHelper = new MyDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Button signup = findViewById(R.id.signup);
        TextView login = findViewById(R.id.login);
        EditText fname = findViewById(R.id.fname);
        EditText lname = findViewById(R.id.lname);
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);
        EditText confirmPassword = findViewById(R.id.confirmPassword);

        signup.setOnClickListener(view -> {
            ProgressDialog progressDialog = new ProgressDialog(Signup.this);
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            String firstName = fname.getText().toString().trim();
            String lastName = lname.getText().toString().trim();
            String em = email.getText().toString().trim();
            String pass = password.getText().toString();
            String confirmpass = confirmPassword.getText().toString();

            // Input validation
            if (firstName.isEmpty() || lastName.isEmpty() || em.isEmpty() || pass.isEmpty() || confirmpass.isEmpty()) {
                Toast.makeText(Signup.this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                return;
            }

            if (!pass.equals(confirmpass)) {
                confirmPassword.setError(getString(R.string.password_mismatch));
                progressDialog.dismiss();
                return;
            }

            // Store user data in the database
            ContentValues values = new ContentValues();
            values.put("fname", firstName);
            values.put("lname", lastName);
            values.put("email", em);
            values.put("password", pass); // Consider hashing passwords before storing
            long newRowId = db.insert("registration", null, values);

            if (newRowId != -1) {
                Toast.makeText(Signup.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();
                // Optionally, redirect to login or main activity after successful signup
                Intent intent = new Intent(Signup.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close the signup activity
            } else {
                Toast.makeText(Signup.this, "Error saving data.", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        });

        login.setOnClickListener(v -> {
            Intent i = new Intent(Signup.this, MainActivity.class); // Assuming there's a Login activity
            startActivity(i);
        });
    }
}
