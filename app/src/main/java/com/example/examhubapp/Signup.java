package com.example.examhubapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class Signup extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        Button signup = findViewById(R.id.signup);
        TextView login = findViewById(R.id.login);
        EditText fname = findViewById(R.id.fname);
        EditText lname = findViewById(R.id.lname);
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);
        EditText confirmPassword = findViewById(R.id.confirmPassword);

        signup.setOnClickListener(view -> {
            ProgressDialog progressDialog = new ProgressDialog(Signup.this);
            progressDialog.setMessage("loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String pass = password.getText().toString();
                    String confirmpass = confirmPassword.getText().toString();
                    String em = email.getText().toString();

                    if (!pass.equals(confirmpass)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                confirmPassword.setError("password does not match");
                                progressDialog.dismiss();
                            }
                        });
                        return; // Added return here to exit if passwords do not match
                    }

                    auth.createUserWithEmailAndPassword(em, pass).addOnCompleteListener(Signup.this,
                            (OnCompleteListener<AuthResult>) task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = auth.getCurrentUser();
                                    DatabaseReference reference = databaseReference.child("users").child(user.getUid());
                                    reference.child("fname").setValue(fname.getText().toString());
                                    reference.child("lname").setValue(lname.getText().toString());

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();
                                            Intent i = new Intent(Signup.this, Home.class);
                                            i.putExtra("User UID", user.getUid());
                                            startActivity(i);
                                            finish();
                                        }
                                    });
                                }else{
                                    Toast.makeText(Signup.this,"operation failed.", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();

                                }
                            });
                }
            });
            thread.start(); // Don't forget to start the thread
        });

        login.setOnClickListener(v -> {
            Intent i = new Intent(Signup.this, MainActivity.class);
            startActivity(i);
        });
    }
}
