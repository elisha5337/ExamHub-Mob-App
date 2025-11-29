package com.example.examhubapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class Exam extends AppCompatActivity {

    private MyDatabaseHelper dbHelper;
    private List<Question> questions;
    private QuestionAdapter adapter;
    private CountDownTimer countDownTimer;
    private TextView timerText;
    private boolean isTimerRunning = false;
    private CircleImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

        Toolbar toolbar = findViewById(R.id.toolbar_exam);
        setSupportActionBar(toolbar);

        timerText = findViewById(R.id.timer_text);

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
                    startTimer(questions.size() * 10 * 1000); // 1 minute per question
                }
            });
        } else {
            dbHelper.getAllQuestionsAsync(new MyDatabaseHelper.DatabaseCallback<List<Question>>() {
                @Override
                public void onComplete(List<Question> result) {
                    questions.clear();
                    questions.addAll(result);
                    adapter.notifyDataSetChanged();
                    startTimer(questions.size() * 60 * 1000); // 1 minute per question
                }
            });
        }
    }

    private void startTimer(long timeInMillis) {
        if (isTimerRunning) {
            countDownTimer.cancel();
        }

        isTimerRunning = true;
        countDownTimer = new CountDownTimer(timeInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                timerText.setText("00:00");
                isTimerRunning = false;
                Toast.makeText(Exam.this, "Time's up! Submitting answers.", Toast.LENGTH_SHORT).show();
                handleSubmit();
            }
        }.start();
    }

    private void handleSubmit() {
        if (isTimerRunning) {
            countDownTimer.cancel();
            isTimerRunning = false;
        }

        int score = 0;
        int correctAnswers = 0;
        int incorrectAnswers = 0;
        StringBuilder resultsBuilder = new StringBuilder();

        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);

        for (Question question : questions) {
            boolean isCorrect = question.getSelectAnswer() != null && question.isAnswerCorrect();
            if (isCorrect) {
                correctAnswers++;
                score++;
            } else {
                incorrectAnswers++;
            }
            resultsBuilder.append("Question: ").append(question.getQuestion()).append("\n");
            resultsBuilder.append("Your Answer: ").append(question.getSelectAnswer()).append("\n");
            resultsBuilder.append("Correct Answer: ").append(question.getCorrectAnswer()).append("\n");
            resultsBuilder.append("Description: ").append(question.getDescription()).append("\n\n");

            if (email != null) {
                dbHelper.saveUserAnswer(email, question.getId(), question.getSelectAnswer(), isCorrect);
            }
        }

        if (email != null) {
            User user = dbHelper.getUserProfile(email);
            if (user != null) {
                int totalScore = user.getTotalScore() + score;
                int totalCorrect = user.getAnsweredQuestions() + correctAnswers;
                int totalIncorrect = user.getMissedQuestions() + incorrectAnswers;
                dbHelper.updateUserStats(email, totalScore, totalCorrect, totalIncorrect);

                setResult(Activity.RESULT_OK);
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Exam Results")
                .setMessage("You scored " + score + " out of " + questions.size() + "\n\n" + resultsBuilder.toString())
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void loadProfileImage() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);
        if (email != null && profileImageView != null) {
            User user = dbHelper.getUserProfile(email);
            if (user != null && user.getProfileImagePath() != null) {
                profileImageView.setImageURI(Uri.parse(user.getProfileImagePath()));
            } else {
                profileImageView.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem profileItem = menu.findItem(R.id.action_profile_image);
        if (profileItem != null) {
            View actionView = profileItem.getActionView();
            if (actionView != null) {
                profileImageView = actionView.findViewById(R.id.profile_image);
                loadProfileImage();
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (itemId == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if (itemId == R.id.action_contact_me) {
            startActivity(new Intent(this, ContactMeActivity.class));
            return true;
        } else if (itemId == R.id.action_feedback) {
            startActivity(new Intent(this, FeedbackActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
