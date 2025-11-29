package com.example.examhubapp;

public class AnsweredQuestion {
    private Question question;
    private String selectedAnswer;
    private boolean isCorrect;

    public AnsweredQuestion(Question question, String selectedAnswer, boolean isCorrect) {
        this.question = question;
        this.selectedAnswer = selectedAnswer;
        this.isCorrect = isCorrect;
    }

    public Question getQuestion() {
        return question;
    }

    public String getSelectedAnswer() {
        return selectedAnswer;
    }

    public boolean isCorrect() {
        return isCorrect;
    }
}
