package com.example.examhubapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class Exam extends AppCompatActivity {

    private static final int ADD_QUESTION_REQUEST = 1;

    private MyDatabaseHelper dbHelper;
    private List<Question> questions;
    private QuestionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

        dbHelper = new MyDatabaseHelper(this);
        questions = new ArrayList<>();

        ListView listView = findViewById(R.id.list_item);
        adapter = new QuestionAdapter(this, questions);
        listView.setAdapter(adapter);

        Button addQuestionButton = findViewById(R.id.add_question_button);
        addQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Exam.this, AddQuestionActivity.class);
                startActivityForResult(intent, ADD_QUESTION_REQUEST);
            }
        });

        Button submitButton = findViewById(R.id.submit);
        submitButton.setOnClickListener(v -> handleSubmit());

        loadQuestions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_QUESTION_REQUEST && resultCode == RESULT_OK && data != null) {
            Question newQuestion = (Question) data.getSerializableExtra(AddQuestionActivity.EXTRA_NEW_QUESTION);
            if (newQuestion != null) {
                dbHelper.insertQuestion(newQuestion);
                loadQuestions(); // Refresh the list
                Toast.makeText(this, "Question added successfully!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadQuestions() {
        dbHelper.getAllQuestionsAsync(new MyDatabaseHelper.DatabaseCallback<List<Question>>() {
            @Override
            public void onComplete(List<Question> result) {
                questions.clear();
                questions.addAll(result);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void handleSubmit() {
        int score = 0;
        for (Question question : questions) {
            if (question.isAnswerCorrect()) {
                score++;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Exam Results")
                .setMessage("You scored " + score + " out of " + questions.size())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
