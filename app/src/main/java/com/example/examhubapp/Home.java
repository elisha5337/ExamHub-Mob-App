package com.example.examhubapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class Home extends AppCompatActivity {
    private Spinner examTypeSpinner;
    private TextView totalPoints, availableQuestions, solvedQuestions;
    private MyDatabaseHelper dbHelper;
    private ActivityResultLauncher<Intent> addQuestionLauncher;
    private ActivityResultLauncher<Intent> examLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private CircleImageView profileImageView;
    private Button startQuizButton, setReminderButton;
    private LinearLayout practiceLayout, adminLayout, studentSelectionLayout, statisticsLayout;

    private static final String EXAM_TYPE_KEY = "EXAM_TYPE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new MyDatabaseHelper(this);

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) showTimePickerDialog();
            else Toast.makeText(this, "Notification permission required for reminders.", Toast.LENGTH_SHORT).show();
        });

        addQuestionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> { if (result.getResultCode() == Activity.RESULT_OK) refreshCurrentExamData(); });

        examLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> { if (result.getResultCode() == Activity.RESULT_OK) loadUserStats(); });

        initViews();
        checkUserRole();
        setupSpinner();
        loadUserInfo();
    }

    private void initViews() {
        totalPoints = findViewById(R.id.totalPoints);
        availableQuestions = findViewById(R.id.availableQuestions);
        solvedQuestions = findViewById(R.id.solvedQuestions);
        startQuizButton = findViewById(R.id.startQuiz);
        practiceLayout = findViewById(R.id.practice_layout);
        adminLayout = findViewById(R.id.create_quiz_layout);
        studentSelectionLayout = findViewById(R.id.student_selection_layout);
        statisticsLayout = findViewById(R.id.statistics_layout);
        examTypeSpinner = findViewById(R.id.examTypeSpinner);
        setReminderButton = findViewById(R.id.set_reminder_button);

        startQuizButton.setOnClickListener(v -> startQuiz());
        findViewById(R.id.create).setOnClickListener(v -> startActivity(new Intent(this, AddQuestionActivity.class)));
        findViewById(R.id.solvedQuizesLayout).setOnClickListener(v -> startActivity(new Intent(this, SolvedQuestionsActivity.class)));
        setReminderButton.setOnClickListener(v -> handleReminder());
    }

    private void checkUserRole() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        boolean isAdmin = prefs.getBoolean("is_admin", false);

        if (isAdmin) {
            adminLayout.setVisibility(View.VISIBLE);
            studentSelectionLayout.setVisibility(View.GONE);
            statisticsLayout.setVisibility(View.GONE);
            practiceLayout.setVisibility(View.GONE);
            setReminderButton.setVisibility(View.GONE);
        } else {
            adminLayout.setVisibility(View.GONE);
            studentSelectionLayout.setVisibility(View.VISIBLE);
            statisticsLayout.setVisibility(View.VISIBLE);
            setReminderButton.setVisibility(View.VISIBLE);
            loadUserStats();
        }
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.exam_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        examTypeSpinner.setAdapter(adapter);

        examTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshCurrentExamData();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void refreshCurrentExamData() {
        String subject = examTypeSpinner.getSelectedItem().toString();
        dbHelper.getQuestionsCountAsync(subject, count -> {
            if (availableQuestions != null) availableQuestions.setText(String.valueOf(count));
            if (practiceLayout != null) {
                practiceLayout.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void startQuiz() {
        String subject = examTypeSpinner.getSelectedItem().toString();
        Intent intent = new Intent(Home.this, Exam.class);
        intent.putExtra(EXAM_TYPE_KEY, subject);
        examLauncher.launch(intent);
    }

    private void handleReminder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (am != null && !am.canScheduleExactAlarms()) {
                startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getPackageName())));
            } else { showTimePickerDialog(); }
        } else { showTimePickerDialog(); }
    }

    private void showTimePickerDialog() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (v, h, m) -> {
            Calendar target = Calendar.getInstance();
            target.set(Calendar.HOUR_OF_DAY, h);
            target.set(Calendar.MINUTE, m);
            if (target.before(Calendar.getInstance())) target.add(Calendar.DATE, 1);
            scheduleNotification(target.getTimeInMillis());
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
    }

    private void scheduleNotification(long time) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, NotificationReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        if (am != null) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi);
            Toast.makeText(this, "Reminder set!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String email = prefs.getString("email", "");
        dbHelper.getUserFirstNameAsync(email, fname -> {
            TextView nameTextView = findViewById(R.id.name);
            if (nameTextView != null) nameTextView.setText("Welcome, " + fname);
        });
    }

    private void loadUserStats() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);
        if (email != null) {
            dbHelper.getUserProfileAsync(email, user -> {
                if (user != null) {
                    if (totalPoints != null) totalPoints.setText(String.valueOf(user.getTotalScore()));
                    if (solvedQuestions != null) solvedQuestions.setText(String.valueOf(user.getAnsweredQuestions()));
                }
            });
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
                actionView.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
                loadProfileImage();
            }
        }
        return true;
    }

    private void loadProfileImage() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);
        if (email != null && profileImageView != null) {
            dbHelper.getUserProfileAsync(email, user -> {
                if (user != null && user.getProfileImagePath() != null) {
                    profileImageView.setImageURI(Uri.parse(user.getProfileImagePath()));
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_profile) startActivity(new Intent(this, ProfileActivity.class));
        return super.onOptionsItemSelected(item);
    }
}
