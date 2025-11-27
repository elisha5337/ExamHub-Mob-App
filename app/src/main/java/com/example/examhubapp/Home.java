package com.example.examhubapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

public class Home extends AppCompatActivity {
    private HomeViewModel homeViewModel;
    private Spinner examTypeSpinner;
    private TextView totalPoints;
    private static final String EXAM_TYPE_KEY = "EXAM_TYPE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        // Initialize UI components
        TextView name = findViewById(R.id.name);
        TextView totalQuestionsTextView = findViewById(R.id.totalQuestions);
        totalPoints = findViewById(R.id.totalPoints);
        Button startQuiz = findViewById(R.id.startQuiz);
        Button create = findViewById(R.id.create);
        RelativeLayout solvedQuizesLayout = findViewById(R.id.solvedQuizesLayout);
        RelativeLayout yourQuizes = findViewById(R.id.yourQuizes);
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

        examTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCourse = parent.getItemAtPosition(position).toString();
                homeViewModel.loadQuestionsByCourse(selectedCourse);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Retrieve user data from Intent
        Intent intent = getIntent();
        String firstName = intent.getStringExtra("FIRST_NAME");

        // Set the user's name in the TextView
        if (firstName != null) {
            name.setText("Welcome, " + firstName);
        } else {
            name.setText("Welcome, Guest");
        }

        // Load total points
        loadTotalPoints();

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
            Intent createIntent = new Intent(Home.this, AddQuestionActivity.class);
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

        signup.setOnClickListener(v -> {
            // Redirect to signup activity if needed
            Intent signupIntent = new Intent(Home.this, Signup.class);
            startActivity(signupIntent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_contact_me) {
            Intent intent = new Intent(this, ContactMeActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTotalPoints();
    }

    private void loadTotalPoints() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        int totalScore = sharedPreferences.getInt("total_score", 0);
        totalPoints.setText(String.valueOf(totalScore));
    }
}
