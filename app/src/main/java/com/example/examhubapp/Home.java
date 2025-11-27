package com.example.examhubapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

public class Home extends AppCompatActivity {
    private HomeViewModel homeViewModel;
    private Spinner examTypeSpinner;
    private static final String EXAM_TYPE_KEY = "EXAM_TYPE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // Initialize UI components
        TextView name = findViewById(R.id.name);
        TextView totalQuestionsTextView = findViewById(R.id.totalQuestions);
        TextView totalPoints = findViewById(R.id.totalPoints);
        Button startQuiz = findViewById(R.id.startQuiz);
        Button create = findViewById(R.id.create);
        RelativeLayout solvedQuizesLayout = findViewById(R.id.solvedQuizesLayout);
        RelativeLayout yourQuizes = findViewById(R.id.yourQuizes);
        ImageView signout = findViewById(R.id.signout);
        TextView signup = findViewById(R.id.signup);
        examTypeSpinner = findViewById(R.id.examTypeSpinner);

        // Initialize the database helper and ViewModel
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(this);
        HomeViewModelFactory factory = new HomeViewModelFactory(dbHelper);
        homeViewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);

        // Observe the questions LiveData
        homeViewModel.getQuestions().observe(this, questions -> {
            if (questions != null) {
                totalQuestionsTextView.setText(String.valueOf(questions.size()));
            }
        });

        // Populate the spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.exam_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        examTypeSpinner.setAdapter(adapter);

        // Retrieve user data from Intent
        Intent intent = getIntent();
        String firstName = intent.getStringExtra("FIRST_NAME");

        // Set the user's name in the TextView
        if (firstName != null) {
            name.setText("Welcome, " + firstName);
        } else {
            name.setText("Welcome, Guest");
        }

        // Set total points (this could be retrieved from a database)
        totalPoints.setText("100"); // Example static data

        // Set up button click listeners
        startQuiz.setOnClickListener(v -> {
            String selectedExamType = examTypeSpinner.getSelectedItem() != null ? examTypeSpinner.getSelectedItem().toString() : null;
            if (selectedExamType != null) {
                Intent quizIntent = new Intent(Home.this, Exam.class);
                quizIntent.putExtra(EXAM_TYPE_KEY, selectedExamType);
                startActivity(quizIntent);
            } else {
                Toast.makeText(Home.this, "Please select an exam type.", Toast.LENGTH_SHORT).show();
            }
        });

        create.setOnClickListener(v -> {
            // Start the create quiz activity
            Intent createIntent = new Intent(Home.this, Exam.class);
            startActivity(createIntent);
        });

        solvedQuizesLayout.setOnClickListener(v -> {
            // Show solved quizzes (this could lead to another activity)
            Toast.makeText(Home.this, "Showing solved quizzes", Toast.LENGTH_SHORT).show();
        });

        yourQuizes.setOnClickListener(v -> {
            // Show user's quizzes (this could lead to another activity)
            Toast.makeText(Home.this, "Showing your quizzes", Toast.LENGTH_SHORT).show();
        });

        signout.setOnClickListener(v -> {
            // Handle sign out logic (e.g., clear user session, return to login screen)
            Toast.makeText(Home.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
            Intent signOutIntent = new Intent(Home.this, LoginActivity.class);
            startActivity(signOutIntent);
            finish(); // Close the Home activity
        });

        signup.setOnClickListener(v -> {
            // Redirect to signup activity if needed
            Intent signupIntent = new Intent(Home.this, Signup.class);
            startActivity(signupIntent);
        });
    }
}
