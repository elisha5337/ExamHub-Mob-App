package com.example.examhubapp;

import java.io.Serializable;

public class Question implements Serializable {
    private int id;
    private String question;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private String correctAnswer;
    private String description;
    private String courseType; 
    private String selectAnswer;
    private int year; // New field for exam year

    public Question(int id, String question, String option1, String option2, String option3, String option4, String correctAnswer, String description, String courseType) {
        this.id = id;
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctAnswer = correctAnswer;
        this.description = description;
        this.courseType = courseType;
        this.selectAnswer = null;
        this.year = 0; // Default year
    }

    // Getters
    public int getId() { return id; }
    public String getQuestion() { return question; }
    public String getOption1() { return option1; }
    public String getOption2() { return option2; }
    public String getOption3() { return option3; }
    public String getOption4() { return option4; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getDescription() { return description; }
    public String getCourseType() { return courseType; }
    public String getSelectAnswer() { return selectAnswer; }
    public int getYear() { return year; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setQuestion(String question) { this.question = question; }
    public void setOption1(String option1) { this.option1 = option1; }
    public void setOption2(String option2) { this.option2 = option2; }
    public void setOption3(String option3) { this.option3 = option3; }
    public void setOption4(String option4) { this.option4 = option4; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public void setDescription(String description) { this.description = description; }
    public void setCourseType(String courseType) { this.courseType = courseType; }
    public void setSelectAnswer(String selectAnswer) { this.selectAnswer = selectAnswer; }
    public void setYear(int year) { this.year = year; }

    public boolean isAnswerCorrect() {
        return correctAnswer != null && correctAnswer.equals(selectAnswer);
    }

    public String displayQuestion() {
        return question + "\n1. " + option1 + "\n2. " + option2 + "\n3. " + option3 + "\n4. " + option4;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", question='" + question + '\'' +
                ", year=" + year +
                ", courseType='" + courseType + '\'' +
                '}';
    }
}
