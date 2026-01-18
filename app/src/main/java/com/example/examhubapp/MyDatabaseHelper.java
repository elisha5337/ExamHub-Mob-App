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
    private static final int DATABASE_VERSION = 14; 

    private static final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

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
                "courseType TEXT, " +
                "year INTEGER DEFAULT 0)";
        db.execSQL(createQuestionsTableSQL);

        String createExamsTableSQL = "CREATE TABLE exams (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "subject TEXT, " +
                "pdf_path TEXT, " +
                "UNIQUE(subject))";
        db.execSQL(createExamsTableSQL);

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
        if (oldVersion < 14) {
            db.execSQL("DROP TABLE IF EXISTS exams");
            db.execSQL("CREATE TABLE exams (id INTEGER PRIMARY KEY AUTOINCREMENT, subject TEXT, pdf_path TEXT, UNIQUE(subject))");
        }
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

    public void updateProfileImagePathAsync(String email, String imagePath) {
        databaseExecutor.execute(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("profile_image_path", imagePath);
            db.update("registration", values, "email = ?", new String[]{email});
        });
    }

    public void insertFeedbackAsync(String email, String feedback, DatabaseCallback<Long> callback) {
        databaseExecutor.execute(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("email", email);
            values.put("feedback", feedback);
            values.put("timestamp", System.currentTimeMillis());
            long id = db.insert("feedback", null, values);
            mainThreadHandler.post(() -> callback.onComplete(id));
        });
    }

    public void getQuestionsCountAsync(String courseType, DatabaseCallback<Integer> callback) {
        databaseExecutor.execute(() -> {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT COUNT(*) FROM questions WHERE courseType = ?";
            int count = 0;
            try (Cursor cursor = db.rawQuery(query, new String[]{courseType})) {
                if (cursor.moveToFirst()) count = cursor.getInt(0);
            }
            final int finalCount = count;
            mainThreadHandler.post(() -> callback.onComplete(finalCount));
        });
    }

    public void getQuestionsByCourseAsync(String courseType, DatabaseCallback<List<Question>> callback) {
        databaseExecutor.execute(() -> {
            List<Question> list = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT * FROM questions WHERE courseType = ?";
            try (Cursor cursor = db.rawQuery(query, new String[]{courseType})) {
                if (cursor.moveToFirst()) {
                    do {
                        Question q = new Question(
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
                        list.add(q);
                    } while (cursor.moveToNext());
                }
            }
            mainThreadHandler.post(() -> callback.onComplete(list));
        });
    }

    public void getAllQuestionsAsync(DatabaseCallback<List<Question>> callback) {
        databaseExecutor.execute(() -> {
            List<Question> list = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            try (Cursor cursor = db.rawQuery("SELECT * FROM questions", null)) {
                if (cursor.moveToFirst()) {
                    do {
                        Question q = new Question(
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
                        list.add(q);
                    } while (cursor.moveToNext());
                }
            }
            mainThreadHandler.post(() -> callback.onComplete(list));
        });
    }

    public void insertQuestionAsync(Question question, DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
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
            mainThreadHandler.post(() -> callback.onComplete(null));
        });
    }

    public void checkUserAsync(String email, String password, DatabaseCallback<Boolean> callback) {
        databaseExecutor.execute(() -> {
            SQLiteDatabase db = this.getReadableDatabase();
            boolean exists = false;
            try (Cursor cursor = db.query("registration", new String[]{"id"}, "email = ? AND password = ?", new String[]{email, password}, null, null, null)) {
                exists = cursor.getCount() > 0;
            }
            final boolean finalResult = exists;
            mainThreadHandler.post(() -> callback.onComplete(finalResult));
        });
    }

    public void isUserAdminAsync(String email, DatabaseCallback<Boolean> callback) {
        databaseExecutor.execute(() -> {
            SQLiteDatabase db = this.getReadableDatabase();
            boolean isAdmin = false;
            try (Cursor cursor = db.query("registration", new String[]{"isAdmin"}, "email = ?", new String[]{email}, null, null, null)) {
                if (cursor.moveToFirst()) isAdmin = cursor.getInt(0) == 1;
            }
            final boolean finalResult = isAdmin;
            mainThreadHandler.post(() -> callback.onComplete(finalResult));
        });
    }

    public void getUserFirstNameAsync(String email, DatabaseCallback<String> callback) {
        databaseExecutor.execute(() -> {
            SQLiteDatabase db = this.getReadableDatabase();
            String name = null;
            try (Cursor cursor = db.query("registration", new String[]{"fname"}, "email = ?", new String[]{email}, null, null, null)) {
                if (cursor.moveToFirst()) name = cursor.getString(0);
            }
            final String finalResult = name;
            mainThreadHandler.post(() -> callback.onComplete(finalResult));
        });
    }

    public void getUserProfileAsync(String email, DatabaseCallback<User> callback) {
        databaseExecutor.execute(() -> {
            User user = getUserProfile(email);
            mainThreadHandler.post(() -> callback.onComplete(user));
        });
    }

    public User getUserProfile(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query("registration", null, "email = ?", new String[]{email}, null, null, null)) {
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

    public void updateUserStatsAsync(String email, int score, int answered, int missed) {
        databaseExecutor.execute(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("total_score", score);
            values.put("answered_questions", answered);
            values.put("missed_questions", missed);
            db.update("registration", values, "email = ?", new String[]{email});
        });
    }

    public void saveUserAnswerAsync(String email, int qId, String answer, boolean isCorrect) {
        databaseExecutor.execute(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("user_email", email);
            values.put("question_id", qId);
            values.put("selected_answer", answer);
            values.put("is_correct", isCorrect ? 1 : 0);
            db.insertWithOnConflict("user_answered_questions", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        });
    }

    public void insertUserAsync(String fname, String lname, String email, String password, DatabaseCallback<Long> callback) {
        databaseExecutor.execute(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("fname", fname);
            values.put("lname", lname);
            values.put("email", email);
            values.put("password", password);
            long id = db.insert("registration", null, values);
            mainThreadHandler.post(() -> callback.onComplete(id));
        });
    }
    public void getAnsweredQuestionsAsync(String userEmail, boolean isCorrect, DatabaseCallback<List<AnsweredQuestion>> callback) {
        databaseExecutor.execute(() -> {
            List<AnsweredQuestion> list = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();

            // Query joining questions and user_answered_questions to get full question details
            String query = "SELECT q.*, ua.selected_answer, ua.is_correct " +
                    "FROM questions q " +
                    "JOIN user_answered_questions ua ON q.id = ua.question_id " +
                    "WHERE ua.user_email = ? AND ua.is_correct = ?";

            String[] selectionArgs = {userEmail, isCorrect ? "1" : "0"};

            try (Cursor cursor = db.rawQuery(query, selectionArgs)) {
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
                        boolean correct = cursor.getInt(cursor.getColumnIndexOrThrow("is_correct")) == 1;

                        list.add(new AnsweredQuestion(question, selectedAnswer, correct));
                    } while (cursor.moveToNext());
                }
            }
            mainThreadHandler.post(() -> callback.onComplete(list));
        });
    }
}
