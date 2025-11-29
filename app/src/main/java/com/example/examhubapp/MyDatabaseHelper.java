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
    private static final int DATABASE_VERSION = 13; // Incremented for schema change

    private static final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    public interface DatabaseCallback<T> {
        void onComplete(T result);
    }

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createRegistrationTableSQL = "CREATE TABLE registration (id INTEGER PRIMARY KEY, fname TEXT, lname TEXT, email TEXT, password TEXT, confirmPassword TEXT, isAdmin INTEGER DEFAULT 0, total_score INTEGER DEFAULT 0, answered_questions INTEGER DEFAULT 0, missed_questions INTEGER DEFAULT 0, profile_image_path TEXT)";
        db.execSQL(createRegistrationTableSQL);
        addDefaultAdmin(db);

        String createQuestionsTableSQL = "CREATE TABLE questions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "question TEXT, " +
                "option1 TEXT, " +
                "option2 TEXT, " +
                "option3 TEXT, " +
                "option4 TEXT, " +
                "correctAnswer TEXT, " +
                "description TEXT, " +
                "courseType TEXT)";
        db.execSQL(createQuestionsTableSQL);

        String createFeedbackTableSQL = "CREATE TABLE feedback (id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT, feedback TEXT, timestamp INTEGER)";
        db.execSQL(createFeedbackTableSQL);

        String createUserAnsweredQuestionsTableSQL = "CREATE TABLE user_answered_questions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_email TEXT, " +
                "question_id INTEGER, " +
                "selected_answer TEXT, " +
                "is_correct INTEGER, " +
                "UNIQUE(user_email, question_id))" ;
        db.execSQL(createUserAnsweredQuestionsTableSQL);
    }

    private void addDefaultAdmin(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("fname", "Admin");
        values.put("lname", "User");
        values.put("email", "admin@elsa.com");
        values.put("password", "53372545");
        values.put("isAdmin", 1);
        db.insert("registration", null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS registration");
        db.execSQL("DROP TABLE IF EXISTS questions");
        db.execSQL("DROP TABLE IF EXISTS feedback");
        db.execSQL("DROP TABLE IF EXISTS user_answered_questions");
        onCreate(db);
    }

    public long insertUser(String fname, String lname, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fname", fname);
        values.put("lname", lname);
        values.put("email", email);
        values.put("password", password);
        values.put("isAdmin", 0);
        return db.insert("registration", null, values);
    }

    public void updateProfileImagePath(String email, String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("profile_image_path", path);
        String selection = "email = ?";
        String[] selectionArgs = {email};
        db.update("registration", values, selection, selectionArgs);
    }

    public void saveUserAnswer(String userEmail, int questionId, String selectedAnswer, boolean isCorrect) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_email", userEmail);
        values.put("question_id", questionId);
        values.put("selected_answer", selectedAnswer);
        values.put("is_correct", isCorrect ? 1 : 0);
        db.insertWithOnConflict("user_answered_questions", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<AnsweredQuestion> getAnsweredQuestions(String userEmail, boolean isCorrect) {
        List<AnsweredQuestion> answeredQuestions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT q.*, ua.selected_answer, ua.is_correct FROM questions q JOIN user_answered_questions ua ON q.id = ua.question_id WHERE ua.user_email = ? AND ua.is_correct = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{userEmail, isCorrect ? "1" : "0"})) {
            if (cursor.moveToFirst()) {
                do {
                    Question question = new Question(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("question")),
                            cursor.getString(cursor.getColumnIndexOrThrow("option1")),
                            cursor.getString(cursor.getColumnIndexOrThrow("option2")),
                            cursor.getString(cursor.getColumnIndexOrThrow("option3")),
                            cursor.getString(cursor.getColumnIndexOrThrow("option4")),
                            cursor.getString(cursor.getColumnIndexOrThrow("correctAnswer")),
                            cursor.getString(cursor.getColumnIndexOrThrow("description")),
                            cursor.getString(cursor.getColumnIndexOrThrow("courseType"))
                    );
                    String selectedAnswer = cursor.getString(cursor.getColumnIndexOrThrow("selected_answer"));
                    answeredQuestions.add(new AnsweredQuestion(question, selectedAnswer, isCorrect));
                } while (cursor.moveToNext());
            }
        }
        return answeredQuestions;
    }

    public List<AnsweredQuestion> getSolvedQuestions(String userEmail) {
        List<AnsweredQuestion> answeredQuestions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT q.*, ua.selected_answer, ua.is_correct FROM questions q JOIN user_answered_questions ua ON q.id = ua.question_id WHERE ua.user_email = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{userEmail})) {
            if (cursor.moveToFirst()) {
                do {
                    Question question = new Question(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("question")),
                            cursor.getString(cursor.getColumnIndexOrThrow("option1")),
                            cursor.getString(cursor.getColumnIndexOrThrow("option2")),
                            cursor.getString(cursor.getColumnIndexOrThrow("option3")),
                            cursor.getString(cursor.getColumnIndexOrThrow("option4")),
                            cursor.getString(cursor.getColumnIndexOrThrow("correctAnswer")),
                            cursor.getString(cursor.getColumnIndexOrThrow("description")),
                            cursor.getString(cursor.getColumnIndexOrThrow("courseType"))
                    );
                    String selectedAnswer = cursor.getString(cursor.getColumnIndexOrThrow("selected_answer"));
                    boolean isCorrect = cursor.getInt(cursor.getColumnIndexOrThrow("is_correct")) == 1;
                    answeredQuestions.add(new AnsweredQuestion(question, selectedAnswer, isCorrect));
                } while (cursor.moveToNext());
            }
        }
        return answeredQuestions;
    }

    public long insertFeedback(String email, String feedback) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("feedback", feedback);
        values.put("timestamp", System.currentTimeMillis());
        return db.insert("feedback", null, values);
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

    public boolean isUserAdmin(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {"isAdmin"};
        String selection = "email = ?";
        String[] selectionArgs = {email};
        try (Cursor cursor = db.query("registration", columns, selection, selectionArgs, null, null, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow("isAdmin")) == 1;
            }
        }
        return false;
    }

    public User getUserProfile(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {"fname", "lname", "email", "total_score", "answered_questions", "missed_questions", "profile_image_path"};
        String selection = "email = ?";
        String[] selectionArgs = {email};
        try (Cursor cursor = db.query("registration", columns, selection, selectionArgs, null, null, null)) {
            if (cursor.moveToFirst()) {
                return new User(
                        cursor.getString(cursor.getColumnIndexOrThrow("fname")),
                        cursor.getString(cursor.getColumnIndexOrThrow("lname")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("total_score")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("answered_questions")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("missed_questions")),
                        cursor.getString(cursor.getColumnIndexOrThrow("profile_image_path"))
                );
            }
        }
        return null;
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

    public void updateUserStats(String email, int score, int answered, int missed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("total_score", score);
        values.put("answered_questions", answered);
        values.put("missed_questions", missed);
        String selection = "email = ?";
        String[] selectionArgs = {email};
        db.update("registration", values, selection, selectionArgs);
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
        values.put("description", question.getDescription());
        values.put("courseType", question.getCourseType());
        db.insert("questions", null, values);
    }

    public List<Question> getAllQuestions() {
        List<Question> questions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM questions", null)) {
            if (cursor.moveToFirst()) {
                do {
                    Question question = new Question(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("question")),
                            cursor.getString(cursor.getColumnIndexOrThrow("option1")),
                            cursor.getString(cursor.getColumnIndexOrThrow("option2")),
                            cursor.getString(cursor.getColumnIndexOrThrow("option3")),
                            cursor.getString(cursor.getColumnIndexOrThrow("option4")),
                            cursor.getString(cursor.getColumnIndexOrThrow("correctAnswer")),
                            cursor.getString(cursor.getColumnIndexOrThrow("description")),
                            cursor.getString(cursor.getColumnIndexOrThrow("courseType"))
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

    public List<Question> getQuestionsByCourse(String courseType) {
        List<Question> questions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = "courseType = ?";
        String[] selectionArgs = {courseType};
        try (Cursor cursor = db.query("questions", null, selection, selectionArgs, null, null, null)) {
            if (cursor.moveToFirst()) {
                do {
                    Question question = new Question(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("question")),
                            cursor.getString(cursor.getColumnIndexOrThrow("option1")),
                            cursor.getString(cursor.getColumnIndexOrThrow("option2")),
                            cursor.getString(cursor.getColumnIndexOrThrow("option3")),
                            cursor.getString(cursor.getColumnIndexOrThrow("option4")),
                            cursor.getString(cursor.getColumnIndexOrThrow("correctAnswer")),
                            cursor.getString(cursor.getColumnIndexOrThrow("description")),
                            cursor.getString(cursor.getColumnIndexOrThrow("courseType"))
                    );
                    questions.add(question);
                } while (cursor.moveToNext());
            }
        }
        return questions;
    }

    public void getQuestionsByCourseAsync(String courseType, DatabaseCallback<List<Question>> callback) {
        Handler mainThreadHandler = new Handler(Looper.getMainLooper());
        databaseExecutor.execute(() -> {
            List<Question> questions = getQuestionsByCourse(courseType);
            mainThreadHandler.post(() -> callback.onComplete(questions));
        });
    }
}
