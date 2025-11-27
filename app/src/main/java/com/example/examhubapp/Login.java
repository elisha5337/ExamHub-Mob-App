package com.example.examhubapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText emailEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.loginButton);
        TextView createAccountTextView = findViewById(R.id.create_account);

        loginButton.setOnClickListener(v -> loginUser());
        createAccountTextView.setOnClickListener(v -> navigateToSignup());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                Intent intent = new Intent(Login.this, Home.class);
                intent.putExtra("User UID", user.getUid());
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(Login.this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToSignup() {
        Intent signupIntent = new Intent(Login.this, Signup.class);
        startActivity(signupIntent);
    }
}
