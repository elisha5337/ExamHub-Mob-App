package com.example.examhubapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private MyDatabaseHelper dbHelper;
    private ImageView profileImageView;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private String userEmail;
    private CircleImageView menuProfileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new MyDatabaseHelper(this);

        profileImageView = findViewById(R.id.profile_image_view);
        TextView firstNameTextView = findViewById(R.id.first_name_text_view);
        TextView progress = findViewById(R.id.progress);
        TextView status = findViewById(R.id.status);
        TextView lastNameTextView = findViewById(R.id.last_name_text_view);
        TextView emailTextView = findViewById(R.id.email_text_view);
        TextView totalPointsTextView = findViewById(R.id.total_points_text_view);
        TextView questionsAnsweredCorrectlyTextView = findViewById(R.id.questions_answered_correctly_text_view);
        TextView questionsAnsweredIncorrectlyTextView = findViewById(R.id.questions_answered_incorrectly_text_view);
        Button signOutButton = findViewById(R.id.sign_out_button);

        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        userEmail = sharedPreferences.getString("email", null);

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            saveImageToInternalStorage(imageUri);
                        }
                    }
                });

        profileImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        if (userEmail != null) {
            loadUserProfile();
        }

        questionsAnsweredCorrectlyTextView.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, QuestionListActivity.class);
            intent.putExtra(QuestionListActivity.EXTRA_IS_CORRECT, true);
            startActivity(intent);
        });

        questionsAnsweredIncorrectlyTextView.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, QuestionListActivity.class);
            intent.putExtra(QuestionListActivity.EXTRA_IS_CORRECT, false);
            startActivity(intent);
        });

        signOutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private void loadUserProfile() {
        User user = dbHelper.getUserProfile(userEmail);
        if (user != null) {
            TextView firstNameTextView = findViewById(R.id.first_name_text_view);
            TextView lastNameTextView = findViewById(R.id.last_name_text_view);
            TextView emailTextView = findViewById(R.id.email_text_view);
            TextView totalPointsTextView = findViewById(R.id.total_points_text_view);
            TextView questionsAnsweredCorrectlyTextView = findViewById(R.id.questions_answered_correctly_text_view);
            TextView questionsAnsweredIncorrectlyTextView = findViewById(R.id.questions_answered_incorrectly_text_view);
            TextView progress = findViewById(R.id.progress);
            TextView status = findViewById(R.id.status);

            firstNameTextView.setText("First Name: " + user.getFname());
            lastNameTextView.setText("Last Name: " + user.getLname());
            emailTextView.setText("Email: " + user.getEmail());
            totalPointsTextView.setText("Total Points: " + user.getTotalScore());
            questionsAnsweredCorrectlyTextView.setText("Questions Answered Correctly: " + user.getAnsweredQuestions());
            questionsAnsweredIncorrectlyTextView.setText("Questions Answered Incorrectly: " + user.getMissedQuestions());

            if (user.getProfileImagePath() != null) {
                profileImageView.setImageURI(Uri.parse(user.getProfileImagePath()));
                if (menuProfileImageView != null) {
                    menuProfileImageView.setImageURI(Uri.parse(user.getProfileImagePath()));
                }
            }

            double ans = user.getAnsweredQuestions();
            double lose = user.getMissedQuestions();
            double total = ans + lose;
            if (total > 0) {
                double percent = ans / total;
                double pr = percent * 100;
                if (pr >= 50) {
                    progress.setText("progress in percent:" + String.format("%.2f", pr) + "%");
                    status.setText("Status: Good");
                } else {
                    progress.setText("progress in percent:" + String.format("%.2f", pr) + "%");
                    progress.setTextColor(getResources().getColor(R.color.red_dark));
                    status.setText("Status: Bad");
                }
            }
        }
    }

    private void saveImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return;

            File directory = getDir("profile_images", Context.MODE_PRIVATE);
            File imageFile = new File(directory, userEmail + ".jpg");

            OutputStream outputStream = new FileOutputStream(imageFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            String imagePath = imageFile.getAbsolutePath();
            dbHelper.updateProfileImagePath(userEmail, imagePath);
            profileImageView.setImageURI(Uri.parse(imagePath));
            if (menuProfileImageView != null) {
                menuProfileImageView.setImageURI(Uri.parse(imagePath));
            }
            Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to update profile picture.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem profileItem = menu.findItem(R.id.action_profile_image);
        if (profileItem != null) {
            View actionView = profileItem.getActionView();
            if (actionView != null) {
                menuProfileImageView = actionView.findViewById(R.id.profile_image);
                loadUserProfile();
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
