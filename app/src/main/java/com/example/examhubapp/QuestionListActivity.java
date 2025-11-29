package com.example.examhubapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QuestionListActivity extends AppCompatActivity {

    public static final String EXTRA_IS_CORRECT = "com.example.examhubapp.EXTRA_IS_CORRECT";

    private MyDatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private SolvedQuestionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_list);

        Toolbar toolbar = findViewById(R.id.toolbar_question_list);
        setSupportActionBar(toolbar);

        boolean isCorrect = getIntent().getBooleanExtra(EXTRA_IS_CORRECT, false);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isCorrect ? "Correctly Answered" : "Incorrectly Answered");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new MyDatabaseHelper(this);
        recyclerView = findViewById(R.id.question_list_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadQuestions(isCorrect);
    }

    private void loadQuestions(boolean isCorrect) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);
        if (email != null) {
            List<AnsweredQuestion> answeredQuestions = dbHelper.getAnsweredQuestions(email, isCorrect);
            adapter = new SolvedQuestionAdapter(answeredQuestions);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
