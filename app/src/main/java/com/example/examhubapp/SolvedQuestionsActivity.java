package com.example.examhubapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SolvedQuestionsActivity extends AppCompatActivity {

    private MyDatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private SolvedQuestionAdapter adapter;
    private CircleImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solved_questions);

        Toolbar toolbar = findViewById(R.id.toolbar_solved_questions);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Solved Quizzes");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new MyDatabaseHelper(this);
        recyclerView = findViewById(R.id.solved_questions_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadSolvedQuestions();
    }

    private void loadSolvedQuestions() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);
        if (email != null) {
            List<AnsweredQuestion> answeredQuestions = dbHelper.getSolvedQuestions(email);
            adapter = new SolvedQuestionAdapter(answeredQuestions);
            recyclerView.setAdapter(adapter);
        }
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
    protected void onStart() {
        super.onStart();
        invalidateOptionsMenu();
    }
}
