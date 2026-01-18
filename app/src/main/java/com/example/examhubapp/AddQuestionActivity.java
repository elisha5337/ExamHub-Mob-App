package com.example.examhubapp;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AddQuestionActivity extends AppCompatActivity {

    private EditText questionEditText;
    private EditText option1EditText, option2EditText, option3EditText, option4EditText;
    private EditText correctAnswerEditText, descriptionEditText;
    private Spinner courseTypeSpinner;
    private Button saveQuestionButton, backButton, importJsonButton, downloadTemplateButton;
    private MyDatabaseHelper dbHelper;

    public static final String EXTRA_NEW_QUESTION = "com.example.examhubapp.EXTRA_NEW_QUESTION";
    private static final int PICK_JSON_FILE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        boolean isAdmin = sharedPreferences.getBoolean("is_admin", false);

        if (!isAdmin) {
            Toast.makeText(this, "You are not authorized to access this page.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_add_question);
        dbHelper = new MyDatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar_add_question);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Question Manager");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        questionEditText = findViewById(R.id.add_question_edit_text);
        option1EditText = findViewById(R.id.option1_edit_text);
        option2EditText = findViewById(R.id.option2_edit_text);
        option3EditText = findViewById(R.id.option3_edit_text);
        option4EditText = findViewById(R.id.option4_edit_text);
        correctAnswerEditText = findViewById(R.id.correct_answer_edit_text);
        descriptionEditText = findViewById(R.id.description_edit_text);
        courseTypeSpinner = findViewById(R.id.course_type_spinner);
        saveQuestionButton = findViewById(R.id.save_question_button);
        backButton = findViewById(R.id.back_button);
        importJsonButton = findViewById(R.id.import_json_button);
        downloadTemplateButton = findViewById(R.id.download_template_button);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.exam_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseTypeSpinner.setAdapter(adapter);

        backButton.setOnClickListener(v -> finish());
        saveQuestionButton.setOnClickListener(v -> saveQuestion());
        importJsonButton.setOnClickListener(v -> openFilePicker(PICK_JSON_FILE, "application/json"));
        downloadTemplateButton.setOnClickListener(v -> saveSampleJsonToDownloads());
    }

    private void openFilePicker(int requestCode, String type) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(type);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            if (requestCode == PICK_JSON_FILE) {
                try {
                    String jsonContent = readTextFromUri(uri);
                    importQuestionsFromJson(jsonContent);
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to read JSON", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String readTextFromUri(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }

    private void importQuestionsFromJson(String jsonContent) {
        try {
            JSONArray jsonArray = new JSONArray(jsonContent);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Question q = new Question(
                        0, obj.getString("question"), obj.getString("option1"),
                        obj.getString("option2"), obj.getString("option3"),
                        obj.getString("option4"), obj.getString("correctAnswer"),
                        obj.getString("description"), obj.getString("courseType")
                );
                dbHelper.insertQuestionAsync(q, result -> {});
            }
            Toast.makeText(this, "Bulk import complete!", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Toast.makeText(this, "JSON Error", Toast.LENGTH_SHORT).show();
        }
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

        if (questionText.isEmpty() || option1.isEmpty() || option2.isEmpty() || correctAnswer.isEmpty()) {
            Toast.makeText(this, "Please fill required fields (Question, Options, Correct Answer)", Toast.LENGTH_SHORT).show();
            return;
        }

        Question newQuestion = new Question(0, questionText, option1, option2, option3, option4, correctAnswer, description, courseType);

        dbHelper.insertQuestionAsync(newQuestion, result -> {
            Toast.makeText(this, "Question saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void saveSampleJsonToDownloads() {
        String jsonString = getSampleJsonContent();
        String fileName = "exam_import_template.json";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/json");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                    os.write(jsonString.getBytes(StandardCharsets.UTF_8));
                    Toast.makeText(this, "Template saved to Downloads.", Toast.LENGTH_SHORT).show();
                } catch (IOException e) { Log.e("AddQuestion", "Save failed", e); }
            }
        }
    }

    private String getSampleJsonContent() {
        return "[{\"question\":\"Example?\",\"option1\":\"A\",\"option2\":\"B\",\"option3\":\"C\",\"option4\":\"D\",\"correctAnswer\":\"A\",\"description\":\"Exp.\",\"courseType\":\"General Knowledge\"}]";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
