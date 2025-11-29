package com.example.examhubapp;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class HomeViewModelFactory implements ViewModelProvider.Factory {
    private final MyDatabaseHelper dbHelper;

    public HomeViewModelFactory(MyDatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(dbHelper);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
