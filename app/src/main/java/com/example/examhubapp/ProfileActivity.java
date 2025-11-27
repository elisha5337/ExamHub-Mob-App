package com.example.examhubapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private MyDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new MyDatabaseHelper(this);

        TextView firstNameTextView = findViewById(R.id.first_name_text_view);
        TextView lastNameTextView = findViewById(R.id.last_name_text_view);
        TextView emailTextView = findViewById(R.id.email_text_view);
        Button signOutButton = findViewById(R.id.sign_out_button);

        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);

        if (email != null) {
            User user = dbHelper.getUserProfile(email);
            if (user != null) {
                firstNameTextView.setText("First Name: " + user.getFname());
                lastNameTextView.setText("Last Name: " + user.getLname());
                emailTextView.setText("Email: " + user.getEmail());
            }
        }

        signOutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
