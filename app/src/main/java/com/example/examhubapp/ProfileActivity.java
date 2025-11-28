package com.example.examhubapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ProfileActivity extends AppCompatActivity {

    private MyDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new MyDatabaseHelper(this);

        TextView firstNameTextView = findViewById(R.id.first_name_text_view);
        TextView lastNameTextView = findViewById(R.id.last_name_text_view);
        TextView emailTextView = findViewById(R.id.email_text_view);
        TextView totalPointsTextView = findViewById(R.id.total_points_text_view);
        TextView questionsAnsweredCorrectlyTextView = findViewById(R.id.questions_answered_correctly_text_view);
        TextView questionsAnsweredIncorrectlyTextView = findViewById(R.id.questions_answered_incorrectly_text_view);
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

        int totalScore = sharedPreferences.getInt("total_score", 0);
        int totalCorrect = sharedPreferences.getInt("questions_answered_correctly", 0);
        int totalIncorrect = sharedPreferences.getInt("questions_answered_incorrectly", 0);

        totalPointsTextView.setText("Total Points: " + totalScore);
        questionsAnsweredCorrectlyTextView.setText("Questions Answered Correctly: " + totalCorrect);
        questionsAnsweredIncorrectlyTextView.setText("Questions Answered Incorrectly: " + totalIncorrect);

        signOutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
