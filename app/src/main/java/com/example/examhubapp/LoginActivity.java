package com.example.examhubapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView createAccountTextView;
    private MyDatabaseHelper dbHelper;

    // Corrected to 30 minutes (30 * 60 * 1000L)
    private static final long SESSION_EXPIRATION_TIME = 30 * 60 * 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new MyDatabaseHelper(this);

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        createAccountTextView = findViewById(R.id.create_account);

        loginButton.setOnClickListener(v -> loginUser());
        createAccountTextView.setOnClickListener(v -> navigateToSignup());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.checkUser(email, password)) {
            String firstName = dbHelper.getUserFirstName(email);
            boolean isAdmin = dbHelper.isUserAdmin(email);

            // Save session
            SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("is_logged_in", true);
            editor.putString("first_name", firstName);
            editor.putString("email", email);
            editor.putBoolean("is_admin", isAdmin);
            editor.putLong("login_timestamp", System.currentTimeMillis()); // Save the login timestamp
            editor.apply();

            navigateToHome();
        } else {
            Toast.makeText(LoginActivity.this, "Login failed. Invalid email or password.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, Home.class);
        startActivity(intent);
        finish();
    }

    private void navigateToSignup() {
        Intent signupIntent = new Intent(LoginActivity.this, Signup.class);
        startActivity(signupIntent);
    }
}
