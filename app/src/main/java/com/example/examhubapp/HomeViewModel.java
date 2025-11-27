package com.example.examhubapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<List<Question>> questions = new MutableLiveData<>();
    private final MyDatabaseHelper dbHelper;

    public HomeViewModel(MyDatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        loadQuestions();
    }

    public LiveData<List<Question>> getQuestions() {
        return questions;
    }

    private void loadQuestions() {
        dbHelper.getAllQuestionsAsync(questions::postValue);
    }

    public void loadQuestionsByCourse(String courseType) {
        dbHelper.getQuestionsByCourseAsync(courseType, questions::postValue);
    }
}
