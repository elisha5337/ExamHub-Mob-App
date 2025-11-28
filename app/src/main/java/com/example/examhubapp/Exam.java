package com.example.examhubapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class Exam extends AppCompatActivity {

    private MyDatabaseHelper dbHelper;
    private List<Question> questions;
    private QuestionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

        Toolbar toolbar = findViewById(R.id.toolbar_exam);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Exam");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new MyDatabaseHelper(this);
        questions = new ArrayList<>();

        ListView listView = findViewById(R.id.list_item);
        adapter = new QuestionAdapter(this, questions);
        listView.setAdapter(adapter);

        Button submitButton = findViewById(R.id.submit);
        submitButton.setOnClickListener(v -> handleSubmit());

        loadQuestions();
    }

    private void loadQuestions() {
        String courseType = getIntent().getStringExtra("EXAM_TYPE");
        if (courseType != null) {
            dbHelper.getQuestionsByCourseAsync(courseType, new MyDatabaseHelper.DatabaseCallback<List<Question>>() {
                @Override
                public void onComplete(List<Question> result) {
                    questions.clear();
                    questions.addAll(result);
                    adapter.notifyDataSetChanged();
                }
            });
        } else {
            dbHelper.getAllQuestionsAsync(new MyDatabaseHelper.DatabaseCallback<List<Question>>() {
                @Override
                public void onComplete(List<Question> result) {
                    questions.clear();
                    questions.addAll(result);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void handleSubmit() {
        int score = 0;
        int correctAnswers = 0;
        int incorrectAnswers = 0;
        StringBuilder resultsBuilder = new StringBuilder();

        for (Question question : questions) {
            if (question.getSelectAnswer() != null && question.isAnswerCorrect()) {
                correctAnswers++;
                score++;
            } else {
                incorrectAnswers++;
            }
            resultsBuilder.append("Question: ").append(question.getQuestion()).append("\n");
            resultsBuilder.append("Your Answer: ").append(question.getSelectAnswer()).append("\n");
            resultsBuilder.append("Correct Answer: ").append(question.getCorrectAnswer()).append("\n");
            resultsBuilder.append("Description: ").append(question.getDescription()).append("\n\n");
        }

        // Save the score and stats
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        int totalScore = sharedPreferences.getInt("total_score", 0);
        int totalCorrect = sharedPreferences.getInt("questions_answered_correctly", 0);
        int totalIncorrect = sharedPreferences.getInt("questions_answered_incorrectly", 0);

        totalScore += score;
        totalCorrect += correctAnswers;
        totalIncorrect += incorrectAnswers;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("total_score", totalScore);
        editor.putInt("questions_answered_correctly", totalCorrect);
        editor.putInt("questions_answered_incorrectly", totalIncorrect);
        editor.apply();

        new AlertDialog.Builder(this)
                .setTitle("Exam Results")
                .setMessage("You scored " + score + " out of " + questions.size() + "\n\n" + resultsBuilder.toString())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
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
