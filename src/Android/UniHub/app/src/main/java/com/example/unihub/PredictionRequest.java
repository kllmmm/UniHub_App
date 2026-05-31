package com.example.unihub;

public class PredictionRequest {

    private int target_course;
    private String grades;

    public PredictionRequest(int target_course, String grades) {
        this.target_course = target_course;
        this.grades = grades;
    }
}
