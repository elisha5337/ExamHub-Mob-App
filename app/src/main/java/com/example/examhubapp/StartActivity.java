package com.example.examhubapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000; // 2 seconds
    private static final long SESSION_EXPIRATION_TIME = 30 * 60 * 1000L; // 30 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        new Handler().postDelayed(() -> {
            if (isUserLoggedIn()) {
                navigateToHome();
            } else {
                navigateToLogin();
            }
        }, SPLASH_TIME_OUT);
    }

    private boolean isUserLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);
        long loginTimestamp = sharedPreferences.getLong("login_timestamp", 0);
        long currentTime = System.currentTimeMillis();

        // Check if the session has expired
        if (isLoggedIn && (currentTime - loginTimestamp) < SESSION_EXPIRATION_TIME) {
            return true; // User is logged in and session is valid
        } else {
            // Clear the session if expired
            clearSession();
            return false; // User is not logged in or session has expired
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(StartActivity.this, Home.class);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(StartActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void clearSession() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Clear all session data
        editor.apply();
    }
}
