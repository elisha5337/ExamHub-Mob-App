package com.example.examhubapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class Home extends AppCompatActivity {
    private HomeViewModel homeViewModel;
    private Spinner examTypeSpinner;
    private TextView totalPoints;
    private MyDatabaseHelper dbHelper;
    private ActivityResultLauncher<Intent> addQuestionLauncher;
    private ActivityResultLauncher<Intent> examLauncher;
    private CircleImageView profileImageView;

    private static final String EXAM_TYPE_KEY = "EXAM_TYPE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new MyDatabaseHelper(this);

        addQuestionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Question newQuestion = (Question) result.getData().getSerializableExtra(AddQuestionActivity.EXTRA_NEW_QUESTION);
                        if (newQuestion != null) {
                            dbHelper.insertQuestion(newQuestion);
                            Toast.makeText(this, "Question added successfully!", Toast.LENGTH_SHORT).show();

                            if (examTypeSpinner.getSelectedItem() != null) {
                                String selectedCourse = examTypeSpinner.getSelectedItem().toString();
                                homeViewModel.loadQuestionsByCourse(selectedCourse);
                            }
                        }
                    }
                });

        examLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        loadTotalPoints();
                    }
                });

        TextView name = findViewById(R.id.name);
        TextView totalQuestionsTextView = findViewById(R.id.totalQuestions);
        totalPoints = findViewById(R.id.totalPoints);
        Button startQuiz = findViewById(R.id.startQuiz);
        Button create = findViewById(R.id.create);
        RelativeLayout solvedQuizesLayout = findViewById(R.id.solvedQuizesLayout);
        RelativeLayout yourQuizes = findViewById(R.id.yourQuizes);
        examTypeSpinner = findViewById(R.id.examTypeSpinner);
        Button setReminderButton = findViewById(R.id.set_reminder_button);

        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        boolean isAdmin = sharedPreferences.getBoolean("is_admin", false);

        if (isAdmin) {
            findViewById(R.id.create_quiz_layout).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.create_quiz_layout).setVisibility(View.GONE);
        }

        HomeViewModelFactory factory = new HomeViewModelFactory(dbHelper);
        homeViewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);

        homeViewModel.getQuestions().observe(this, questions -> {
            if (questions != null) {
                totalQuestionsTextView.setText(String.valueOf(questions.size()));
            }
        });

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

        String firstName = sharedPreferences.getString("first_name", "Guest");
        name.setText("Welcome, " + firstName);

        loadTotalPoints();

        startQuiz.setOnClickListener(v -> {
            String selectedExamType = examTypeSpinner.getSelectedItem() != null ? examTypeSpinner.getSelectedItem().toString() : null;
            if (selectedExamType != null) {
                Intent quizIntent = new Intent(Home.this, Exam.class);
                quizIntent.putExtra(EXAM_TYPE_KEY, selectedExamType);
                examLauncher.launch(quizIntent);
            } else {
                Toast.makeText(Home.this, "Please select an exam type.", Toast.LENGTH_SHORT).show();
            }
        });

        create.setOnClickListener(v -> {
            Intent createIntent = new Intent(Home.this, AddQuestionActivity.class);
            addQuestionLauncher.launch(createIntent);
        });

        solvedQuizesLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, SolvedQuestionsActivity.class);
            startActivity(intent);
        });

        yourQuizes.setOnClickListener(v -> {
            Toast.makeText(Home.this, "Showing your quizzes", Toast.LENGTH_SHORT).show();
        });

        setReminderButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    Toast.makeText(this, "Please grant permission to set exact alarms.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            showTimePickerDialog();
        });
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(Home.this,
                (view, hourOfDay, minuteOfHour) -> {
                    Calendar reminderTime = Calendar.getInstance();
                    reminderTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    reminderTime.set(Calendar.MINUTE, minuteOfHour);
                    reminderTime.set(Calendar.SECOND, 0);
                    reminderTime.set(Calendar.MILLISECOND, 0);

                    if (reminderTime.before(Calendar.getInstance())) {
                        reminderTime.add(Calendar.DATE, 1);
                    }

                    scheduleNotification(reminderTime.getTimeInMillis());
                }, hour, minute, false).show();
    }

    private void scheduleNotification(long time) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            Toast.makeText(this, "Reminder set successfully!", Toast.LENGTH_SHORT).show();

            SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
            sharedPreferences.edit().putLong("reminder_time", time).apply();
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
                actionView.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ProfileActivity.class);
                    startActivity(intent);
                });
                if (profileImageView != null) {
                    profileImageView.post(this::loadProfileImage);
                }
            }
        }
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
        } else if (itemId == R.id.action_feedback) {
            Intent intent = new Intent(this, FeedbackActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTotalPoints();
        invalidateOptionsMenu();
    }

    public void loadTotalPoints() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);
        if (email != null) {
            User user = dbHelper.getUserProfile(email);
            if (user != null) {
                totalPoints.setText(String.valueOf(user.getTotalScore()));
            }
        }
    }
}
