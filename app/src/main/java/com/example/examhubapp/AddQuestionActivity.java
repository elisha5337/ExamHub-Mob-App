// File: app/src/main/java/com/example/yourquizapp/AddQuestionActivity.java
package com.example.examhubapp; // IMPORTANT: Adjust this to your actual package name

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddQuestionActivity extends AppCompatActivity {

    // Declare UI elements
    private EditText questionEditText;
    private EditText option1EditText;
    private EditText option2EditText;
    private EditText option3EditText;
    private EditText option4EditText;
    private EditText correctAnswerEditText;
    private Button saveQuestionButton;

    // A constant to identify the data being returned
    public static final String EXTRA_NEW_QUESTION = "com.example.yourquizapp.EXTRA_NEW_QUESTION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question); // Link to your XML layout

        // Initialize UI elements by finding them by their IDs
        questionEditText = findViewById(R.id.question_edit_text);
        option1EditText = findViewById(R.id.option1_edit_text);
        option2EditText = findViewById(R.id.option2_edit_text);
        option3EditText = findViewById(R.id.option3_edit_text);
        option4EditText = findViewById(R.id.option4_edit_text);
        correctAnswerEditText = findViewById(R.id.correct_answer_edit_text);
        saveQuestionButton = findViewById(R.id.save_question_button);

        // Set an OnClickListener for the Save Question button
        saveQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveQuestion(); // Call the method to handle saving
            }
        });
    }

    private void saveQuestion() {
        // 1. Retrieve text from all EditText fields
        String questionText = questionEditText.getText().toString().trim();
        String option1 = option1EditText.getText().toString().trim();
        String option2 = option2EditText.getText().toString().trim();
        String option3 = option3EditText.getText().toString().trim();
        String option4 = option4EditText.getText().toString().trim();
        String correctAnswer = correctAnswerEditText.getText().toString().trim();

        // 2. Perform basic validation
        if (questionText.isEmpty() || option1.isEmpty() || option2.isEmpty() ||
                option3.isEmpty() || option4.isEmpty() || correctAnswer.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return; // Stop execution if any field is empty
        }

        // Optional: More robust validation for correct answer (e.g., check if it matches one of the options)
        if (!correctAnswer.equals(option1) && !correctAnswer.equals(option2) &&
                !correctAnswer.equals(option3) && !correctAnswer.equals(option4)) {
            Toast.makeText(this, "Correct answer must match one of the options", Toast.LENGTH_LONG).show();
            return;
        }


        Question newQuestion = new Question(questionText, option1, option2, option3, option4, correctAnswer);

        // 4. Prepare an Intent to send the new Question back to the calling Activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_NEW_QUESTION, newQuestion);

        // 5. Set the result and finish this Activity
        setResult(RESULT_OK, resultIntent); // Indicate that the operation was successful
        finish(); // Close AddQuestionActivity
    }
}
