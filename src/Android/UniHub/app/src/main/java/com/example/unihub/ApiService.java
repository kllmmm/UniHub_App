package com.example.unihub;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/get_grades")
    Call<JsonArray> getGrades(@Body LoginRequest request);

    @GET("/get_courses")
    Call<JsonArray> getCourses(@Body GetCoursesRequest request);

    @POST("/ai_model")
    Call<JsonObject> aiModel(@Body AiModelRequest request);

    @POST("predict_grade")
    Call<JsonObject> predictGrade(@Body PredictionRequest request);
}