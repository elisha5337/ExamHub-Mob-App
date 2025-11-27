package com.example.examhubapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddQuestionActivity extends AppCompatActivity {

    private EditText questionEditText;
    private EditText option1EditText;
    private EditText option2EditText;
    private EditText option3EditText;
    private EditText option4EditText;
    private EditText correctAnswerEditText;
    private EditText descriptionEditText;
    private Spinner courseTypeSpinner;
    private Button saveQuestionButton, backButton;

    public static final String EXTRA_NEW_QUESTION = "com.example.examhubapp.EXTRA_NEW_QUESTION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);

        questionEditText = findViewById(R.id.question_edit_text);
        option1EditText = findViewById(R.id.option1_edit_text);
        option2EditText = findViewById(R.id.option2_edit_text);
        option3EditText = findViewById(R.id.option3_edit_text);
        option4EditText = findViewById(R.id.option4_edit_text);
        correctAnswerEditText = findViewById(R.id.correct_answer_edit_text);
        descriptionEditText = findViewById(R.id.description_edit_text);
        courseTypeSpinner = findViewById(R.id.course_type_spinner);
        saveQuestionButton = findViewById(R.id.save_question_button);
        backButton = findViewById(R.id.back_button);

        // Populate the spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.exam_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseTypeSpinner.setAdapter(adapter);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddQuestionActivity.this, Home.class);
            startActivity(intent);
        });
        saveQuestionButton.setOnClickListener(v -> saveQuestion());
    }

    private void saveQuestion() {
        String questionText = questionEditText.getText().toString().trim();
        String option1 = option1EditText.getText().toString().trim();
        String option2 = option2EditText.getText().toString().trim();
        String option3 = option3EditText.getText().toString().trim();
        String option4 = option4EditText.getText().toString().trim();
        String correctAnswer = correctAnswerEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String courseType = courseTypeSpinner.getSelectedItem().toString();

        if (questionText.isEmpty() || option1.isEmpty() || option2.isEmpty() ||
                option3.isEmpty() || option4.isEmpty() || correctAnswer.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!correctAnswer.equals(option1) && !correctAnswer.equals(option2) &&
                !correctAnswer.equals(option3) && !correctAnswer.equals(option4)) {
            Toast.makeText(this, "Correct answer must match one of the options", Toast.LENGTH_LONG).show();
            return;
        }

        Question newQuestion = new Question(questionText, option1, option2, option3, option4, correctAnswer, description, courseType);

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_NEW_QUESTION, newQuestion);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
