package com.example.examhubapp;

import android.content.Intent;
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
            Intent intent = new Intent(LoginActivity.this, Home.class);
            intent.putExtra("FIRST_NAME", firstName);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "Login failed. Invalid email or password.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToSignup() {
        Intent signupIntent = new Intent(LoginActivity.this, Signup.class);
        startActivity(signupIntent);
    }
}
