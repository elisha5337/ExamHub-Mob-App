package com.example.examhubapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class MainActivity extends AppCompatActivity {

    private static final long SESSION_EXPIRATION_TIME = 30 * 60 * 1000L; // 30 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // This MUST be called before super.onCreate()
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // No content view is needed for a dispatcher activity

        if (isUserLoggedIn()) {
            navigateToHome();
        } else {
            navigateToLogin();
        }
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
        Intent intent = new Intent(MainActivity.this, Home.class);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
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
