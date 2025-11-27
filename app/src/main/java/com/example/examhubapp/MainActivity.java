package com.example.examhubapp;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;


public class MainActivity extends AppCompatActivity {


    private MyDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        dbHelper=new MyDatabaseHelper(this);
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put("name","John Doe");
        db.insert("my_table",null,values);
        Cursor cursor=db.query("my_table",null,null,null,null,null,null);
        while(cursor.moveToNext()){
            int id=cursor.getInt(cursor.getColumnIndex("id"));
            String name=cursor.getString(cursor.getColumnIndex("name"));
            System.out.println("ID: "+id+", Name: "+name);
        }
        cursor.close();

        EditText emailEditText = findViewById(R.id.email);
        EditText passwordEditText = findViewById(R.id.password);
        TextView signupTextView = findViewById(R.id.create);
        Button loginButton = findViewById(R.id.login);

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();


        });

        // Signup button click event
        signupTextView.setOnClickListener(v -> {
            Intent signupIntent = new Intent(MainActivity.this, Signup.class);
            startActivity(signupIntent);
        });
    }

    private void navigateToHome(String userId) {
        Intent homeIntent = new Intent(MainActivity.this, Home.class);
        homeIntent.putExtra("User UID", userId);
        startActivity(homeIntent);
        finish();
    }

    private ProgressDialog createProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            Toast.makeText(this, "R.string.email_required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "R.string.password_required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
