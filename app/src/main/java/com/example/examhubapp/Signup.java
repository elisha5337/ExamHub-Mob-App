package com.example.examhubapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Signup extends AppCompatActivity {
    private MyDatabaseHelper dbHelper;
    private int[] images={
             R.drawable.jesus,
            R.drawable.marek,
            R.drawable.building,
            R.drawable.samsung
    };
    private int index=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        dbHelper = new MyDatabaseHelper(this);

        Button signup = findViewById(R.id.signup);
        TextView login = findViewById(R.id.login);
        EditText fname = findViewById(R.id.fname);
        ImageView image=findViewById(R.id.image);
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
                Toast.makeText(Signup.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                return;
            }

            if (!pass.equals(confirmpass)) {
                confirmPassword.setError("Passwords do not match");
                progressDialog.dismiss();
                return;
            }

            long newRowId = dbHelper.insertUser(firstName, lastName, em, pass);

            if (newRowId != -1) {
                Toast.makeText(Signup.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Signup.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(Signup.this, "Error saving data.", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        });

        login.setOnClickListener(v -> {
            Intent i = new Intent(Signup.this, LoginActivity.class);
            startActivity(i);
        });
        image.setOnClickListener(v->{
            index++;
            if(index>=images.length){
                index=0;
            }
            image.setImageResource(images[index]);
        });

    }
}
