package com.example.examhubapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mydatabase.db";
    private static final int DATABASE_VERSION = 3;

    private static final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    public interface DatabaseCallback<T> {
        void onComplete(T result);
    }

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableSQL = "CREATE TABLE registration (id INTEGER PRIMARY KEY, fname TEXT,lname TEXT,email TEXT,password TEXT,confirmPassword TEXT)";
        db.execSQL(createTableSQL);
        String createTableSQL2 = "CREATE TABLE questions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "question TEXT, " +
                "option1 TEXT, " +
                "option2 TEXT, " +
                "option3 TEXT, " +
                "option4 TEXT, " +
                "correctAnswer TEXT)";
        db.execSQL(createTableSQL2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS registration");
        db.execSQL("DROP TABLE IF EXISTS questions");
        onCreate(db);
    }

    public long insertUser(String fname, String lname, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fname", fname);
        values.put("lname", lname);
        values.put("email", email);
        values.put("password", password);
        return db.insert("registration", null, values);
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {"id"};
        String selection = "email = ? AND password = ?";
        String[] selectionArgs = {email, password};
        try (Cursor cursor = db.query("registration", columns, selection, selectionArgs, null, null, null)) {
            return cursor.getCount() > 0;
        }
    }

    public String getUserFirstName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {"fname"};
        String selection = "email = ?";
        String[] selectionArgs = {email};
        try (Cursor cursor = db.query("registration", columns, selection, selectionArgs, null, null, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow("fname"));
            }
        }
        return null;
    }

    public void insertQuestion(Question question) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("question", question.getQuestion());
        values.put("option1", question.getOption1());
        values.put("option2", question.getOption2());
        values.put("option3", question.getOption3());
        values.put("option4", question.getOption4());
        values.put("correctAnswer", question.getCorrectAnswer());
        db.insert("questions", null, values);
    }

    public List<Question> getAllQuestions() {
        List<Question> questions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM questions", null)) {
            if (cursor.moveToFirst()) {
                do {
                    Question question = new Question(
                            cursor.getString(cursor.getColumnIndexOrThrow("question")),
                            cursor.getString(cursor.getColumnIndexOrThrow("option1")),
                            cursor.getString(cursor.getColumnIndexOrThrow("option2")),
                            cursor.getString(cursor.getColumnIndexOrThrow("option3")),
                            cursor.getString(cursor.getColumnIndexOrThrow("option4")),
                            cursor.getString(cursor.getColumnIndexOrThrow("correctAnswer"))
                    );
                    questions.add(question);
                } while (cursor.moveToNext());
            }
        }
        return questions;
    }

    public void getAllQuestionsAsync(DatabaseCallback<List<Question>> callback) {
        Handler mainThreadHandler = new Handler(Looper.getMainLooper());
        databaseExecutor.execute(() -> {
            List<Question> questions = getAllQuestions();
            mainThreadHandler.post(() -> callback.onComplete(questions));
        });
    }
}
