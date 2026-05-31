package com.example.unihub;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.example.unihub.databinding.ActivityMenuBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private ActivityMenuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        List<Course> courses;

        //Initialize bottom menus UI look
        binding.btnMenu.setBackgroundResource(R.drawable.nav_active_bg);
        binding.iconMenu.setColorFilter(Color.parseColor("#5DA9FF"));
        binding.btnProgress.setBackgroundResource(R.drawable.nav_selector);
        binding.iconProgress.setColorFilter(Color.parseColor("#FFFFFF"));
        binding.btnCalendar.setBackgroundResource(R.drawable.nav_selector);
        binding.iconCalendar.setColorFilter(Color.parseColor("#FFFFFF"));
        binding.btnAi.setBackgroundResource(R.drawable.nav_selector);
        binding.iconAi.setColorFilter(Color.parseColor("#FFFFFF"));

        //Getting the JSON with the grades from the login
        Intent intent = getIntent();
        String grades_json = intent.getStringExtra("grades_json");
        String user_identifier = intent.getStringExtra("user_identifier");
        assert grades_json != null;
        Log.d("Grades", grades_json);

        try {

            //Creating a list of obj course for each JSON entry
            courses = parseCourses(grades_json);
            Log.d("Courses_Check", courses.toString());

            //Extracting average grade , ects , passed classes of the student from the list
            String[] Data = data(courses);
            String ects = Data[0];
            String passed = Data[1];
            String avg = Data[2];
            Log.d("Data_Check", ects);
            Log.d("Data_Check",passed);
            Log.d("Data_Check",avg);

            //Updating the UI with the information we got
            binding.mathimataNumber.setText(passed);
            binding.EctsNumber.setText(ects);
            binding.moText.setText("M.O. " + avg);
            double avgValue = Double.parseDouble(avg.replace(",","."));
            int progress = (int) (avgValue * 10);

            //Updating the progress bar with the average grade and starting the animation
            ObjectAnimator.ofInt(binding.moProgress, "progress", 0, progress)
                    .setDuration(1200)
                    .start();

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        binding.logoutButton.setOnClickListener(v -> {

            // Build and show the confirmation dialog
            new android.app.AlertDialog.Builder(MenuActivity.this)
                    .setTitle("Αποσύνδεση")
                    .setMessage("Είστε σίγουροι ότι θέλετε να αποσυνδεθήτε;")
                    .setPositiveButton("Ναι", (dialog, which) -> {
                        // Execute the intent only if the user confirms
                        Intent intentMain = new Intent(MenuActivity.this, MainActivity.class);
                        startActivity(intentMain);
                    })
                    .setNegativeButton("Όχι", (dialog, which) -> {
                        // Do nothing, dialog will dismiss automatically
                        dialog.dismiss();
                    })
                    .show();
        });

        //--------------------------------------Navigation Buttons--------------------------------------//


        binding.btnMenu.setOnClickListener(v -> {
            hideAll();
            binding.btnMenu.setBackgroundResource(R.drawable.nav_active_bg);
            binding.iconMenu.setColorFilter(Color.parseColor("#5DA9FF"));


            Intent intentMenu = new Intent(MenuActivity.this, MenuActivity.class);
            intentMenu.putExtra("grades_json", grades_json);
            intentMenu.putExtra("user_identifier",user_identifier);
            startActivity(intentMenu);

        });


        binding.btnCalendar.setOnClickListener(v -> {

            hideAll();
            binding.btnCalendar.setBackgroundResource(R.drawable.nav_active_bg);
            binding.iconCalendar.setColorFilter(Color.parseColor("#5DA9FF"));

            Intent intentCalendar = new Intent(MenuActivity.this, Calendar.class);
            intentCalendar.putExtra("grades_json", grades_json);
            intentCalendar.putExtra("user_identifier",user_identifier);
            startActivity(intentCalendar);

        });


        binding.btnProgress.setOnClickListener(v -> {

            hideAll();
            binding.btnProgress.setBackgroundResource(R.drawable.nav_active_bg);
            binding.iconProgress.setColorFilter(Color.parseColor("#5DA9FF"));

            Intent intentProgress = new Intent(MenuActivity.this, ProgressActivity.class);
            intentProgress.putExtra("grades_json", grades_json);
            intentProgress.putExtra("user_identifier",user_identifier);
            startActivity(intentProgress);

        });


        binding.btnAi.setOnClickListener(v -> {

            hideAll();
            binding.btnAi.setBackgroundResource(R.drawable.nav_active_bg);
            binding.iconAi.setColorFilter(Color.parseColor("#5DA9FF"));
            Intent intentAi = new Intent(this, AiChatActivity.class);

            intentAi.putExtra("grades_json", grades_json);
            intentAi.putExtra("user_identifier",user_identifier);
            startActivity(intentAi);

        });

    }

    //--------------------------------------Methods--------------------------------------//

    //Method for extracting the courses from the JSON making each one
    //a Course object and putting it on a list witch it returns
    private List<Course> parseCourses(String gradesJson) throws JSONException {

        JSONArray array = new JSONArray(gradesJson);
        List<Course> courses = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);

            String name = obj.getString("title");

            double grade = obj.isNull("grade") ? 0.0 : obj.getDouble("grade");

            String courseCode = obj.getString("courseCode");
            int semesterId = obj.getInt("semesterId");
            String courseType = obj.getString("courseType");
            String direction = obj.getString("direction");

            Double ects = obj.isNull("ects") ? null : obj.getDouble("ects");
            Boolean passed = obj.isNull("Passed") ? false : obj.getBoolean("Passed");

            courses.add(new Course(
                    name,
                    grade,
                    courseCode,
                    semesterId,
                    courseType,
                    ects,
                    passed,
                    direction
            ));
        }
        return courses;
    }


    //Method that takes a list of courses and returns avg , ects , passed classes
    @SuppressLint("DefaultLocale")
    private String[] data(List<Course> courses) {

        int totalEctsPassed = 0;
        int passedCoursesCount = 0;

        double gradeSum = 0.0;
        int gradedCoursesCount = 0;

        for (Course c : courses) {

            if (Boolean.TRUE.equals(c.getPassed())) {

                passedCoursesCount++;

                if (c.getEcts() != null) {
                    totalEctsPassed += c.getEcts();
                }

                if (c.getGrade() > 0) {
                    gradeSum += c.getGrade()*10;
                    gradedCoursesCount++;
                }
            }
        }

        double averageGrade = (gradedCoursesCount > 0)
                ? gradeSum / gradedCoursesCount
                : 0.0;

        String ectsStr = String.valueOf(totalEctsPassed);
        String passedStr = String.valueOf(passedCoursesCount);
        String avgStr = String.format("%.2f", averageGrade);

        return new String[]{ectsStr, passedStr, avgStr};
    }

    private void hideAll(){

        binding.btnMenu.setBackgroundResource(android.R.color.transparent);
        binding.btnProgress.setBackgroundResource(android.R.color.transparent);
        binding.btnCalendar.setBackgroundResource(android.R.color.transparent);
        binding.btnAi.setBackgroundResource(android.R.color.transparent);

    }

}