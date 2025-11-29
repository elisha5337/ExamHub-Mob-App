package com.example.examhubapp;

public class User {
    private String fname;
    private String lname;
    private String email;
    private int totalScore;
    private int answeredQuestions;
    private int missedQuestions;
    private String profileImagePath;

    public User(String fname, String lname, String email) {
        this.fname = fname;
        this.lname = lname;
        this.email = email;
    }

    public User(String fname, String lname, String email, int totalScore, int answeredQuestions, int missedQuestions, String profileImagePath) {
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.totalScore = totalScore;
        this.answeredQuestions = answeredQuestions;
        this.missedQuestions = missedQuestions;
        this.profileImagePath = profileImagePath;
    }

    public String getFname() {
        return fname;
    }

    public String getLname() {
        return lname;
    }

    public String getEmail() {
        return email;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getAnsweredQuestions() {
        return answeredQuestions;
    }

    public int getMissedQuestions() {
        return missedQuestions;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }
}
