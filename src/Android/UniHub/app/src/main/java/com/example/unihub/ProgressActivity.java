package com.example.unihub;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.JsonObject;


import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProgressActivity extends AppCompatActivity {
    SemesterAdapter adapter;
    LinearLayout btnMenu, btnProgress, btnCalendar, btnAi;
    ImageView icon_menu, icon_progress, icon_calendar, icon_ai;

    private ApiService apiService;


    LinkedHashMap<String, List<Course>> mapSemesters = new LinkedHashMap<>();
    LinkedHashMap<String, List<Course>> mapRequired = new LinkedHashMap<>();
    LinkedHashMap<String, List<Course>> mapDirections = new LinkedHashMap<>();
    LinkedHashMap<String, List<Course>> mapPredictions = new LinkedHashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        List<Course> courses;

        btnMenu = findViewById(R.id.btn_menu);
        btnProgress = findViewById(R.id.btn_progress);
        btnCalendar = findViewById(R.id.btn_calendar);
        btnAi = findViewById(R.id.btn_ai);

        icon_menu = findViewById(R.id.icon_menu);
        icon_progress = findViewById(R.id.icon_progress);
        icon_calendar = findViewById(R.id.icon_calendar);
        icon_ai = findViewById(R.id.icon_ai);

        //Getting the JSON with the grades from the login
        Intent intent = getIntent();
        String grades_json = intent.getStringExtra("grades_json");
        assert grades_json != null;
        String user_identifier = intent.getStringExtra("user_identifier");
        Log.d("Grades", grades_json);

        //Initialize bottom menus UI look
        btnMenu.setBackgroundResource(R.drawable.nav_selector);
        icon_menu.setColorFilter(Color.parseColor("#FFFFFF"));
        btnProgress.setBackgroundResource(R.drawable.nav_active_bg);
        icon_progress.setColorFilter(Color.parseColor("#5DA9FF"));
        btnCalendar.setBackgroundResource(R.drawable.nav_selector);
        icon_calendar.setColorFilter(Color.parseColor("#FFFFFF"));
        btnAi.setBackgroundResource(R.drawable.nav_selector);
        icon_ai.setColorFilter(Color.parseColor("#FFFFFF"));

        try {
            //Creating a list of obj course for each JSON entry
            courses = parseCourses(grades_json);
            Log.d("Courses_Check", courses.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }



        // Εξάμηνα
        List<Course> sem1 = new ArrayList<>();
        List<Course> sem2 = new ArrayList<>();
        List<Course> sem3 = new ArrayList<>();
        List<Course> sem4 = new ArrayList<>();
        List<Course> sem5 = new ArrayList<>();
        List<Course> sem6 = new ArrayList<>();
        List<Course> sem7 = new ArrayList<>();
        List<Course> sem8 = new ArrayList<>();

        // Υποχρεωτικά
        List<Course> req = new ArrayList<>();


        // Κατευθύνσεις
        List<Course> dir_ED = new ArrayList<>();
        List<Course> dir_EE = new ArrayList<>();
        List<Course> dir_EM = new ArrayList<>();
        List<Course> dir_TP = new ArrayList<>();
        List<Course> dir_SD = new ArrayList<>();
        List<Course> dir_SL = new ArrayList<>();
        List<Course> dir_DDD = new ArrayList<>();
        List<Course> dir_K = new ArrayList<>();

        List<Course> notPassed = new ArrayList<>();
        List<Course> notDeclared = new ArrayList<>();
        Set<String> declaredCourseCodes = new HashSet<>();

        for(Course i : courses){

            declaredCourseCodes.add(i.getCourseCode());

            if(!i.getPassed()) notPassed.add(i);

            if(i.getGrade()>=0.00) {

                //Semesters
                if (i.getSemesterId() == 1) {
                    sem1.add(i);
                } else if (i.getSemesterId() == 2) {
                    sem2.add(i);
                } else if (i.getSemesterId() == 3) {
                    sem3.add(i);
                } else if (i.getSemesterId() == 4) {
                    sem4.add(i);
                } else if (i.getSemesterId() == 5) {
                    sem5.add(i);
                } else if (i.getSemesterId() == 6) {
                    sem6.add(i);
                } else if (i.getSemesterId() == 7) {
                    sem7.add(i);
                } else if (i.getSemesterId() == 8) {
                    sem8.add(i);
                }

                //Required
                if(i.getCourseType().equals("Υ")){
                    req.add(i);
                }

                String[] directions = i.getDirection().split(",");

                for(String e : directions){
                    if(Objects.equals(e, "1")){
                        dir_ED.add(i);
                    }
                    if(Objects.equals(e, "2")){
                        dir_EE.add(i);
                    }
                    if(Objects.equals(e, "3")){
                        dir_EM.add(i);
                    }
                    if(Objects.equals(e, "4")){
                        dir_TP.add(i);
                    }
                    if(Objects.equals(e, "5")){
                        dir_SD.add(i);
                    }
                    if(Objects.equals(e, "6")){
                        dir_SL.add(i);
                    }
                    if(Objects.equals(e, "7")){
                        dir_DDD.add(i);
                    }
                    if(Objects.equals(e, "8")){
                        dir_K.add(i);
                    }
                }

            }
        }

        try {
            List<Course> allAvailableCourses = parseCourses(get_all_courses());
            for (Course c : allAvailableCourses) {
                if (!declaredCourseCodes.contains(c.getCourseCode())) {
                    c.setDeclared(false);
                    notDeclared.add(c);
                }
            }

            //Sort notDeclared by course code
            java.util.Collections.sort(notDeclared, (c1, c2) -> {
                boolean c1StartsWith3 = c1.getCourseCode().startsWith("3");
                boolean c2StartsWith3 = c2.getCourseCode().startsWith("3");

                if (c1StartsWith3 && !c2StartsWith3) {
                    return -1; // Put c1 first
                } else if (!c1StartsWith3 && c2StartsWith3) {
                    return 1;  // Put c2 first
                } else {
                    // If both start with 3 (or neither do), sort them numerically by their code
                    return c1.getCourseCode().compareTo(c2.getCourseCode());
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }



        mapSemesters.put("Εξάμηνο 1", sem1);
        mapSemesters.put("Εξάμηνο 2", sem2);
        mapSemesters.put("Εξάμηνο 3", sem3);
        mapSemesters.put("Εξάμηνο 4", sem4);
        mapSemesters.put("Εξάμηνο 5", sem5);
        mapSemesters.put("Εξάμηνο 6", sem6);
        mapSemesters.put("Εξάμηνο 7", sem7);
        mapSemesters.put("Εξάμηνο 8", sem8);

        mapRequired.put("Υποχρεωτικά", req);

        mapPredictions.put("Οφειλόμενα",notPassed);
        mapPredictions.put("Μη Δηλωμένα",notDeclared);


        mapDirections.put("Επιστήμη Δεδομένων", dir_ED);
        mapDirections.put("Επιχειρησιακή Έρευνα", dir_EE);
        mapDirections.put("Εφαρμοσμένα Μαθηματικά", dir_EM);
        mapDirections.put("Θεωρητική Πληροφορική", dir_TP);
        mapDirections.put("Συστήματα-Δίκτυα", dir_SD);
        mapDirections.put("Συστήματα Λογισμικού", dir_SL);
        mapDirections.put("Διαχείρηση Δεδομένων και Γνώσεων", dir_DDD);
        mapDirections.put("Κυβερνοασφάλεια", dir_K);


        Spinner spinner = findViewById(R.id.spinnerFilter);

        RecyclerView recycler = findViewById(R.id.recyclerSemesters);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SemesterAdapter(mapSemesters, course -> {

            if (!course.getPassed()) showCourseActionSheet(course, grades_json);


        });

        recycler.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {
                    adapter.updateData(mapSemesters); // Εξάμηνα
                } else if (position == 1) {
                    adapter.updateData(mapRequired); // Υποχρεωτικά
                } else if (position == 2) {
                    adapter.updateData(mapDirections); // Κατευθύνσεις
                } else{
                    adapter.updateData(mapPredictions); // Προβλέψεις
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        //--------------------------------------Navigation Buttons--------------------------------------//

        btnMenu.setOnClickListener(v -> {
            hideAll();
            btnMenu.setBackgroundResource(R.drawable.nav_active_bg);
            icon_menu.setColorFilter(Color.parseColor("#5DA9FF"));

            Intent intentMenu = new Intent(this, MenuActivity.class);
            intentMenu.putExtra("grades_json", grades_json);
            intentMenu.putExtra("user_identifier", user_identifier);
            startActivity(intentMenu);
        });

        btnProgress.setOnClickListener(v -> {
            hideAll();
            btnProgress.setBackgroundResource(R.drawable.nav_active_bg);
            icon_progress.setColorFilter(Color.parseColor("#5DA9FF"));

            Intent intentProgress = new Intent(this, ProgressActivity.class);
            intentProgress.putExtra("grades_json", grades_json);
            intentProgress.putExtra("user_identifier", user_identifier);
            startActivity(intentProgress);
        });

        btnCalendar.setOnClickListener(v -> {
            hideAll();
            btnCalendar.setBackgroundResource(R.drawable.nav_active_bg);
            icon_calendar.setColorFilter(Color.parseColor("#5DA9FF"));

            Intent intentCalendar = new Intent(this, Calendar.class);
            intentCalendar.putExtra("grades_json", grades_json);
            intentCalendar.putExtra("user_identifier", user_identifier);
            startActivity(intentCalendar);
        });

        btnAi.setOnClickListener(v -> {
            hideAll();
            btnAi.setBackgroundResource(R.drawable.nav_active_bg);
            icon_ai.setColorFilter(Color.parseColor("#5DA9FF"));

            Intent intentAi = new Intent(this, AiChatActivity.class);
            intentAi.putExtra("grades_json", grades_json);
            intentAi.putExtra("user_identifier",user_identifier);
            startActivity(intentAi);
        });

        //-----------------------------------------------------------//
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        final String Url = "http://10.0.2.2:5002/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Url)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }
    //-----------------------------------------------------------//

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

    private void hideAll() {

        btnMenu.setBackgroundResource(android.R.color.transparent);

        btnProgress.setBackgroundResource(android.R.color.transparent);

        btnCalendar.setBackgroundResource(android.R.color.transparent);

        btnAi.setBackgroundResource(android.R.color.transparent);
    }





    private void showCourseActionSheet(Course course, String grades_json) {
        //Create the dialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        //Inflate your custom layout
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_course, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        //Find the views inside the layout
        TextView txtTitle = bottomSheetView.findViewById(R.id.bsCourseTitle);
        Button btnAiAction = bottomSheetView.findViewById(R.id.bsBtnPredictGrade);

        //Set the text to the clicked course name
        txtTitle.setText(course.getName());

        //Handle what happens when the button inside the bottom sheet is clicked
        btnAiAction.setOnClickListener(v -> {
            bottomSheetDialog.dismiss(); //Close the bottom sheet first

            PredictionRequest request = new PredictionRequest(Integer.parseInt(course.getCourseCode()), grades_json);
            //Api Call for Prediction
            apiService.predictGrade(request).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {


                    if (response.isSuccessful() && response.body() != null) {

                        Log.d("API_SUCCESS", response.body().toString());
                        showPredictionResult(course.getName(), response.body());




                    } else {
                        Toast.makeText(ProgressActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                        Log.d("API_ERROR", "Error: " + response.code() + response.body());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {

                    Toast.makeText(ProgressActivity.this, "Connection Failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("API_ERROR", "Error: ", t);
                }
            });

        });

        //Show the bottom sheet on the screen
        bottomSheetDialog.show();
    }



    private void showPredictionResult(String targetCourseName, JsonObject apiResponse) {
        try {
            //Get the nested JSON string from the "response" key
            String nestedJsonString = apiResponse.get("response").getAsString();

            //Parse that string into a JSONObject
            JSONObject resultJson = new JSONObject(nestedJsonString);

            //Extract the exact data you want
            double predictedGrade = resultJson.getDouble("predicted_grade");
            JSONArray contributingCourses = resultJson.getJSONArray("contributing_courses");

            //Set up the Bottom Sheet
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_prediction_result, null);
            bottomSheetDialog.setContentView(bottomSheetView);

            //Find Views
            TextView txtCourseName = bottomSheetView.findViewById(R.id.bsTargetCourse);
            TextView txtPredictedGrade = bottomSheetView.findViewById(R.id.bsPredictedGrade);
            LinearLayout container = bottomSheetView.findViewById(R.id.bsContributingCoursesContainer);
            Button btnClose = bottomSheetView.findViewById(R.id.bsBtnClose);

            //Bind the main data
            txtCourseName.setText(targetCourseName);
            txtPredictedGrade.setText(String.format(java.util.Locale.US, "%.1f", predictedGrade));

            //Loop through the contributing courses and add them to the screen dynamically
            container.removeAllViews();
            System.out.println(contributingCourses.length());
            if (contributingCourses.length() == 0) {
                TextView tv = new TextView(this);
                tv.setText("Δεν υπάρχουν μαθήματα να βασιστεί ο βαθμός.");
                tv.setTextSize(14f);
                tv.setTextColor(Color.parseColor("#000000"));
                tv.setPadding(0, 8, 0, 8);
                container.addView(tv);

            }
            else {
                for (int i = 0; i < contributingCourses.length(); i++) {
                    JSONObject course = contributingCourses.getJSONObject(i);
                    String courseTitle = course.getString("title");
                    double courseGrade = course.getDouble("grade");

                    //Create a small text view for each course
                    TextView tv = new TextView(this);
                    tv.setText("• " + courseTitle + " (" + String.format(java.util.Locale.US, "%.1f", courseGrade)  + ")");
                    tv.setTextSize(14f);
                    tv.setTextColor(Color.parseColor("#000000"));
                    tv.setPadding(0, 8, 0, 8);

                     //Add it to the layout
                    container.addView(tv);
                }
            }

            //Handle button click and show
            btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
            bottomSheetDialog.show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error formatting prediction data", Toast.LENGTH_SHORT).show();
        }
    }

    //All Courses
    public String get_all_courses() {
        return "[\n" +
                "    {\n" +
                "        \"id\": 1,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΓΕΝΙΚΗ ΟΙΚΟΝΟΜΙΚΗ ΙΣΤΟΡΙΑ\",\n" +
                "        \"courseCode\": \"1131\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 2,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΟΙΚΟΝΟΜΙΚΗ ΙΣΤΟΡΙΑ ΤΗΣ ΕΛΛΑΔΟΣ\",\n" +
                "        \"courseCode\": \"1225\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 3,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΜΑΚΡΟΟΙΚΟΝΟΜΙΚΗ ΘΕΩΡΙΑ Ι\",\n" +
                "        \"courseCode\": \"1311\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 4,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΜΙΚΡΟΟΙΚΟΝΟΜΙΚΗ ΘΕΩΡΙΑ Ι\",\n" +
                "        \"courseCode\": \"1313\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 5,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΜΑΡΞΙΣΤΙΚΗ ΟΙΚΟΝΟΜΙΚΗ Ι\",\n" +
                "        \"courseCode\": \"1321\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 6,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΙΕΘΝΗΣ ΟΙΚΟΝΟΜΙΚΗ\",\n" +
                "        \"courseCode\": \"1373\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 7,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΟΙΚΟΝΟΜΙΚΗ ΓΕΩΓΡΑΦΙΑ\",\n" +
                "        \"courseCode\": \"1385\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 8,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΜΙΚΡΟΟΙΚΟΝΟΜΙΚΗ ΘΕΩΡΙΑ ΙΙ\",\n" +
                "        \"courseCode\": \"1402\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 9,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΜΑΚΡΟΟΙΚΟΝΟΜΙΚΗ ΘΕΩΡΙΑ ΙΙ\",\n" +
                "        \"courseCode\": \"1412\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 10,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΟΙΚΟΝΟΜΕΤΡΙΑ Ι\",\n" +
                "        \"courseCode\": \"1508\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 11,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΙΣΤΟΡΙΑ ΟΙΚΟΝΟΜΙΚΗΣ ΣΚΕΨΗΣ\",\n" +
                "        \"courseCode\": \"1531\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 12,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΟΙΚΟΝΟΜΙΚΗ ΤΗΣ ΕΡΓΑΣΙΑΣ\",\n" +
                "        \"courseCode\": \"1562\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 13,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΘΕΩΡΙΑ ΒΙΟΜΗΧΑΝΙΚΗΣ ΟΡΓΑΝΩΣΗΣ\",\n" +
                "        \"courseCode\": \"1603\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 14,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΟΙΚΟΝΟΜΕΤΡΙΑ  ΙΙ\",\n" +
                "        \"courseCode\": \"1609\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 15,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΘΕΩΡΙΑ ΟΙΚΟΝΟΜΙΚΗΣ ΠΟΛΙΤΙΚΗΣ\",\n" +
                "        \"courseCode\": \"1612\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 16,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΝΟΜΙΣΜΑΤΙΚΗ ΘΕΩΡΙΑ & ΠΟΛΙΤΙΚΗ\",\n" +
                "        \"courseCode\": \"1642\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 17,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΗΜΟΣΙΑ ΟΙΚΟΝΟΜΙΚΗ ΙΙ\",\n" +
                "        \"courseCode\": \"1651\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 18,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΘΕΩΡΙΑ ΠΑΙΓΝΙΩΝ & ΑΒΕΒΑΙΟΤΗΤΑΣ\",\n" +
                "        \"courseCode\": \"1705\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 19,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΝΑΛΥΣΗ ΧΡΗΜΑΤΑΓΟΡΩΝ ΚΑΙ ΚΕΦΑΛΑΙΑΓΟΡΩΝ\",\n" +
                "        \"courseCode\": \"1742\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 20,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΙΕΘΝΕΙΣ ΝΟΜΙΣΜΑΤΙΚΕΣ ΣΧΕΣΕΙΣ\",\n" +
                "        \"courseCode\": \"1745\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 21,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΟΙΚΟΝΟΜΙΚΗ ΤΟΥ ΠΕΡΙΒΑΛΛΟΝΤΟΣ ΚΑΙ ΤΩΝ ΦΥΣΙΚΩΝ ΠΟΡΩΝ\",\n" +
                "        \"courseCode\": \"1764\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 22,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΘΕΜΑΤΑ ΔΥΝΑΜΙΚΗΣ ΟΙΚΟΝΟΜΙΚΗΣ\",\n" +
                "        \"courseCode\": \"1808\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 23,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΦΑΡΜΟΣΜΕΝΗ ΒΙΟΜΗΧΑΝΙΚΗ ΟΡΓΑΝΩΣΗ\",\n" +
                "        \"courseCode\": \"1852\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 24,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΘΕΩΡΙΑ ΟΙΚΟΝΟΜΙΚΗΣ ΜΕΓΕΘΥΝΣΗΣ\",\n" +
                "        \"courseCode\": \"1861\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 25,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΠΕΡΙΦΕΡΕΙΑΚΗ ΚΑΙ ΑΣΤΙΚΗ ΟΙΚΟΝΟΜΙΚΗ\",\n" +
                "        \"courseCode\": \"1881\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 26,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΣΤΙΚΟ ΔΙΚΑΙΟ (ΑΣΤΙΚΟ ΔΙΚΑΙΟ Ι)\",\n" +
                "        \"courseCode\": \"2117\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 27,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΜΑΝΑΤΖΜΕΝΤ\",\n" +
                "        \"courseCode\": \"2317\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 28,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΠΡΟΧΩΡΗΜΕΝΗ ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗ ΛΟΓΙΣΤΙΚΗ (ΛΟΓΙΣΤΙΚΗ ΙΙ)\",\n" +
                "        \"courseCode\": \"2410\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 29,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΙΟΙΚΗΣΗ ΕΦΟΔΙΑΣΤΙΚΗΣ ΑΛΥΣΙΔΑΣ (logistics)\",\n" +
                "        \"courseCode\": \"2608\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 30,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΠΙΧΕΙΡΗΣΙΑΚΗ ΣΤΡΑΤΗΓΙΚΗ\",\n" +
                "        \"courseCode\": \"2610\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 31,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΛΟΓΙΣΤΙΚΗ ΚΟΣΤΟΥΣ\",\n" +
                "        \"courseCode\": \"2612\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 32,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΙΑΧΕΙΡΙΣΗ ΕΠΕΝΔΥΣΕΩΝ\",\n" +
                "        \"courseCode\": \"2622\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 33,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΛΕΓΚΤΙΚΗ\",\n" +
                "        \"courseCode\": \"2719\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 34,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΙΟΙΚΗΤΙΚΗ ΛΟΓΙΣΤΙΚΗ\",\n" +
                "        \"courseCode\": \"2731\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 35,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΣΤΡΑΤΗΓΙΚΟΣ ΣΧΕΔΙΑΣΜΟΣ ΜΑΡΚΕΤΙΝΓΚ\",\n" +
                "        \"courseCode\": \"2735\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 36,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΘΕΜΑΤΑ ΕΠΙΧΕΙΡΗΣΙΑΚΗΣ ΠΟΛΙΤΙΚΗΣ ΚΑΙ ΣΤΡΑΤΗΓΙΚΗΣ\",\n" +
                "        \"courseCode\": \"2812\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 37,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΣΤΡΑΤΗΓΙΚΟ ΗΛΕΚΤΡΟΝΙΚΟ ΜΑΡΚΕΤΙΝΓΚ\",\n" +
                "        \"courseCode\": \"2836\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 38,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΠΡΑΚΤΙΚΗ ΑΣΚΗΣΗ ΣΤΗ ΔΙΔΑΣΚΑΛΙΑ (Π.Α.Δ.) Ι\",\n" +
                "        \"courseCode\": \"3070\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 39,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΙΣΑΓΩΓΗ ΣΤΗΝ ΠΑΙΔΑΓΩΓΙΚΗ ΕΠΙΣΤΗΜΗ\",\n" +
                "        \"courseCode\": \"3074\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 40,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΟΡΓΑΝΩΣΗ ΚΑΙ ΔΙΟΙΚΗΣΗ ΤΗΣ ΕΚΠΑΙΔΕΥΣΗΣ ΚΑΙ ΤΩΝ ΕΚΠΑΙΔΕΥΤΙΚΩΝ ΜΟΝΑΔΩΝ\",\n" +
                "        \"courseCode\": \"3075\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 41,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΙΣΑΓΩΓΗ ΣΤΗ ΔΙΔΑΚΤΙΚΗ ΜΕΘΟΔΟΛΟΓΙΑ-ΑΝΑΛΥΤΙΚΑ ΠΡΟΓΡΑΜΜΑΤΑ\",\n" +
                "        \"courseCode\": \"3076\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 42,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΚΠΑΙΔΕΥΤΙΚΗ ΑΞΙΟΛΟΓΗΣΗ\",\n" +
                "        \"courseCode\": \"3078\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 43,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΠΡΑΚΤΙΚΗ ΑΣΚΗΣΗ ΣΤΗ ΔΙΔΑΣΚΑΛΙΑ (Π.Α.Δ.) ΙΙ\",\n" +
                "        \"courseCode\": \"3080\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 44,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΓΕΝΙΚΗ ΚΑΙ  ΕΞΕΛΙΚΤΙΚΗ ΨΥΧΟΛΟΓΙΑ\",\n" +
                "        \"courseCode\": \"3084\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 45,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΠΟΙΟΤΗΤΑ ΣΤΗΝ ΕΚΠΑΙΔΕΥΣΗ ΚΑΙ ΤΗ ΔΙΔΑΣΚΑΛΙΑ\",\n" +
                "        \"courseCode\": \"3085\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 46,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΙΣΑΓΩΓΗ ΣΤΟΥΣ Η/Υ - ΠΑΙΔΑΓΩΓΙΚΕΣ ΕΦΑΡΜΟΓΕΣ ΣΤΗΝ ΕΚΠΑΙΔΕΥΣΗ\",\n" +
                "        \"courseCode\": \"3086\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 47,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΙΔΙΚΗ ΔΙΔΑΚΤΙΚΗ ΜΕΘΟΔΟΛΟΓΙΑ-ΔΙΔΑΚΤΙΚΗ ΜΑΘΗΜΑΤΩΝ ΠΛΗΡΟΦΟΡΙΚΗΣ\",\n" +
                "        \"courseCode\": \"3088\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 48,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΨΗΦΙΑΚΟ ΕΚΠΑΙΔΕΥΤΙΚΟ ΥΛΙΚΟ\",\n" +
                "        \"courseCode\": \"3090\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 49,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΙΑΚΡΙΤΑ ΜΑΘΗΜΑΤΙΚΑ\",\n" +
                "        \"courseCode\": \"3117\",\n" +
                "        \"semesterId\": \"1\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 50,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΜΑΘΗΜΑΤΙΚΑ Ι\",\n" +
                "        \"courseCode\": \"3119\",\n" +
                "        \"semesterId\": \"1\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 51,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΙΣΑΓΩΓΗ ΣΤΟΝ ΠΡΟΓΡΑΜΜΑΤΙΣΜΟ ΥΠΟΛΟΓΙΣΤΩΝ\",\n" +
                "        \"courseCode\": \"3125\",\n" +
                "        \"semesterId\": \"1\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 52,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΙΣΑΓΩΓΗ ΣΤΗΝ ΕΠΙΣΤΗΜΗ ΤΩΝ ΥΠΟΛΟΓΙΣΤΩΝ\",\n" +
                "        \"courseCode\": \"3135\",\n" +
                "        \"semesterId\": \"1\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 53,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΙΣΑΓΩΓΗ ΣΤΗΝ ΟΙΚΟΝΟΜΙΚΗ ΕΠΙΣΤΗΜΗ\",\n" +
                "        \"courseCode\": \"3151\",\n" +
                "        \"semesterId\": \"1\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 54,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΣΤΑΤΙΣΤΙΚΗ ΣΤΗΝ ΠΛΗΡΟΦΟΡΙΚΗ\",\n" +
                "        \"courseCode\": \"3155\",\n" +
                "        \"semesterId\": \"5\",\n" +
                "        \"courseType\": \"ΥπΚ\",\n" +
                "        \"direction\": \"1,3\",\n" +
                "        \"ects\": 7.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 55,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΜΑΘΗΜΑΤΙΚΑ ΙΙ\",\n" +
                "        \"courseCode\": \"3214\",\n" +
                "        \"semesterId\": \"2\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 56,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΠΡΟΓΡΑΜΜΑΤΙΣΜΟΣ ΥΠΟΛΟΓΙΣΤΩΝ ΜΕ JAVA\",\n" +
                "        \"courseCode\": \"3222\",\n" +
                "        \"semesterId\": \"2\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 57,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΥΠΟΛΟΓΙΣΤΙΚΑ ΜΑΘΗΜΑΤΙΚΑ\",\n" +
                "        \"courseCode\": \"3230\",\n" +
                "        \"semesterId\": \"3\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 8.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 58,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΙΣΑΓΩΓΗ ΣΤΗ ΔΙΟΙΚΗΣΗ ΕΠΙΧΕΙΡΗΣΕΩΝ\",\n" +
                "        \"courseCode\": \"3254\",\n" +
                "        \"semesterId\": \"2\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 59,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΣΧΕΔΙΑΣΗ ΨΗΦΙΑΚΩΝ ΣΥΣΤΗΜΑΤΩΝ\",\n" +
                "        \"courseCode\": \"3262\",\n" +
                "        \"semesterId\": \"2\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 60,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΠΙΘΑΝΟΤΗΤΕΣ\",\n" +
                "        \"courseCode\": \"3311\",\n" +
                "        \"semesterId\": \"2\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 61,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΠΡΟΓΡΑΜΜΑΤΙΣΜΟΣ ΥΠΟΛΟΓΙΣΤΩΝ ΜΕ C++\",\n" +
                "        \"courseCode\": \"3321\",\n" +
                "        \"semesterId\": \"3\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 8.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 62,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΟΜΕΣ ΔΕΔΟΜΕΝΩΝ\",\n" +
                "        \"courseCode\": \"3335\",\n" +
                "        \"semesterId\": \"3\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 7.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 63,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΟΡΓΑΝΩΣΗ ΣΥΣΤΗΜΑΤΩΝ ΥΠΟΛΟΓΙΣΤΩΝ\",\n" +
                "        \"courseCode\": \"3365\",\n" +
                "        \"semesterId\": \"3\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 7.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 64,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΛΓΟΡΙΘΜΟΙ\",\n" +
                "        \"courseCode\": \"3432\",\n" +
                "        \"semesterId\": \"4\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 7.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 65,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΘΕΩΡΙΑ ΥΠΟΛΟΓΙΣΜΟΥ\",\n" +
                "        \"courseCode\": \"3434\",\n" +
                "        \"semesterId\": \"4\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 7.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 66,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΒΑΣΕΙΣ ΔΕΔΟΜΕΝΩΝ\",\n" +
                "        \"courseCode\": \"3436\",\n" +
                "        \"semesterId\": \"4\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 8.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 67,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΛΕΙΤΟΥΡΓΙΚΑ ΣΥΣΤΗΜΑΤΑ\",\n" +
                "        \"courseCode\": \"3464\",\n" +
                "        \"semesterId\": \"4\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 8.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 68,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΘΕΩΡΙΑ ΚΑΙ ΥΠΟΔΕΙΓΜΑΤΑ ΒΕΛΤΙΣΤΟΠΟΙΗΣΗΣ\",\n" +
                "        \"courseCode\": \"3511\",\n" +
                "        \"semesterId\": \"6\",\n" +
                "        \"courseType\": \"ΥπΚ\",\n" +
                "        \"direction\": \"2,3\",\n" +
                "        \"ects\": 7.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 69,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΡΙΘΜΗΤΙΚΗ ΓΡΑΜΜΙΚΗ ΑΛΓΕΒΡΑ\",\n" +
                "        \"courseCode\": \"3513\",\n" +
                "        \"semesterId\": \"6\",\n" +
                "        \"courseType\": \"ΥπΚ\",\n" +
                "        \"direction\": \"1,3\",\n" +
                "        \"ects\": 7.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 70,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΛΟΓΙΚΗ\",\n" +
                "        \"courseCode\": \"3515\",\n" +
                "        \"semesterId\": \"5\",\n" +
                "        \"courseType\": \"ΥπΚ\",\n" +
                "        \"direction\": \"4,7\",\n" +
                "        \"ects\": 7.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 71,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΥΠΟΛΟΓΙΣΙΜΟΤΗΤΑ ΚΑΙ ΠΟΛΥΠΛΟΚΟΤΗΤΑ\",\n" +
                "        \"courseCode\": \"3517\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"4\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 72,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΤΕΧΝΗΤΗ ΝΟΗΜΟΣΥΝΗ\",\n" +
                "        \"courseCode\": \"3531\",\n" +
                "        \"semesterId\": \"5\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 8.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 73,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΝΑΛΥΣΗ ΚΑΙ ΣΧΕΔΙΑΣΗ ΠΛΗΡΟΦΟΡΙΑΚΩΝ ΣΥΣΤΗΜΑΤΩΝ\",\n" +
                "        \"courseCode\": \"3541\",\n" +
                "        \"semesterId\": \"6\",\n" +
                "        \"courseType\": \"ΥπΚ\",\n" +
                "        \"direction\": \"6,7\",\n" +
                "        \"ects\": 8.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 74,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΣΥΣΤΗΜΑΤΑ ΔΙΑΧΕΙΡΙΣΗΣ ΚΑΙ ΑΝΑΛΥΣΗΣ ΔΕΔΟΜΕΝΩΝ\",\n" +
                "        \"courseCode\": \"3543\",\n" +
                "        \"semesterId\": \"6\",\n" +
                "        \"courseType\": \"ΥπΚ\",\n" +
                "        \"direction\": \"1,6,7\",\n" +
                "        \"ects\": 7.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 75,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΡΧΙΤΕΚΤΟΝΙΚΗ ΥΠΟΛΟΓΙΣΤΩΝ\",\n" +
                "        \"courseCode\": \"3561\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"5\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 76,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΙΚΤΥΑ ΕΠΙΚΟΙΝΩΝΙΩΝ\",\n" +
                "        \"courseCode\": \"3571\",\n" +
                "        \"semesterId\": \"5\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 8.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 77,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΣΗΜΑΤΑ, ΣΥΣΤΗΜΑΤΑ ΚΑΙ ΨΗΦΙΑΚΗ ΕΠΕΞΕΡΓΑΣΙΑ ΣΗΜΑΤΩΝ\",\n" +
                "        \"courseCode\": \"3573\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"1,3,5\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 78,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΤΕΧΝΟΛΟΓΙΚΗ ΚΑΙΝΟΤΟΜΙΑ ΚΑΙ ΕΠΙΧΕΙΡΗΜΑΤΙΚΟΤΗΤΑ\",\n" +
                "        \"courseCode\": \"3584\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 79,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΙΔΙΚΑ ΘΕΜΑΤΑ ΔΙΑΚΡΙΤΩΝ ΜΑΘΗΜΑΤΙΚΩΝ\",\n" +
                "        \"courseCode\": \"3612\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"3,4\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 81,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΦΑΡΜΟΣΜΕΝΕΣ ΠΙΘΑΝΟΤΗΤΕΣ ΚΑΙ ΠΙΘΑΝΟΤΙΚΟΙ ΑΛΓΟΡΙΘΜΟΙ\",\n" +
                "        \"courseCode\": \"3614\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"2,3,4\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 82,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΙΔΙΚΑ ΘΕΜΑΤΑ ΑΛΓΟΡΙΘΜΩΝ\",\n" +
                "        \"courseCode\": \"3632\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"2,4\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 83,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΜΕΤΑΓΛΩΤΤΙΣΤΕΣ\",\n" +
                "        \"courseCode\": \"3634\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"5,6\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 84,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΠΑΛΗΘΕΥΣΗ, ΕΠΙΚΥΡΩΣΗ ΚΑΙ ΣΥΝΤΗΡΗΣΗ ΛΟΓΙΣΜΙΚΟΥ\",\n" +
                "        \"courseCode\": \"3642\",\n" +
                "        \"semesterId\": \"6\",\n" +
                "        \"courseType\": \"ΥπΚ\",\n" +
                "        \"direction\": \"6,8\",\n" +
                "        \"ects\": 7.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 85,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΣΥΣΤΗΜΑΤΑ ΑΝΑΚΤΗΣΗΣ ΠΛΗΡΟΦΟΡΙΩΝ\",\n" +
                "        \"courseCode\": \"3644\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"1,7\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 86,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΤΕΧΝΟΛΟΓΙΑ ΛΟΓΙΣΜΙΚΟΥ\",\n" +
                "        \"courseCode\": \"3648\",\n" +
                "        \"semesterId\": \"5\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 8.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 87,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΚΥΒΕΡΝΟΑΣΦΑΛΕΙΑ\",\n" +
                "        \"courseCode\": \"3662\",\n" +
                "        \"semesterId\": \"6\",\n" +
                "        \"courseType\": \"ΥπΚ\",\n" +
                "        \"direction\": \"8\",\n" +
                "        \"ects\": 7.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 88,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΚΑΤΑΝΕΜΗΜΕΝΑ ΣΥΣΤΗΜΑΤΑ\",\n" +
                "        \"courseCode\": \"3664\",\n" +
                "        \"semesterId\": \"6\",\n" +
                "        \"courseType\": \"Υ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 8.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 89,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΙΚΤΥΑ ΥΠΟΛΟΓΙΣΤΩΝ\",\n" +
                "        \"courseCode\": \"3672\",\n" +
                "        \"semesterId\": \"6\",\n" +
                "        \"courseType\": \"ΥπΚ\",\n" +
                "        \"direction\": \"5,6,8\",\n" +
                "        \"ects\": 7.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 90,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΘΕΩΡΙΑ ΠΑΙΓΝΙΩΝ ΚΑΙ ΑΠΟΦΑΣΕΩΝ\",\n" +
                "        \"courseCode\": \"3713\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"2,4\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 91,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΝΝΟΙΟΛΟΓΙΚΗ ΜΟΝΤΕΛΟΠΟΙΗΣΗ ΚΑΙ ΟΡΓΑΝΩΣΗ ΓΝΩΣΕΩΝ\",\n" +
                "        \"courseCode\": \"3741\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"6,7\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 92,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΞΟΡΥΞΗ ΓΝΩΣΗΣ ΚΑΙ ΕΠΙΣΤΗΜΗ ΔΕΔΟΜΕΝΩΝ\",\n" +
                "        \"courseCode\": \"3743\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"1,7\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 93,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΜΗΧΑΝΙΚΗ ΜΑΘΗΣΗ\",\n" +
                "        \"courseCode\": \"3745\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΥπΚ\",\n" +
                "        \"direction\": \"1,7,8\",\n" +
                "        \"ects\": 7.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 94,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΤΕΧΝΟΛΟΓΙΕΣ ΚΑΙ ΠΡΟΓΡΑΜΜΑΤΙΣΜΟΣ ΕΦΑΡΜΟΓΩΝ ΣΤΟΝ ΙΣΤΟ\",\n" +
                "        \"courseCode\": \"3747\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"6\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 95,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΣΦΑΛΕΙΑ ΔΙΚΤΥΩΝ\",\n" +
                "        \"courseCode\": \"3761\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"5,8\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 96,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΣΥΡΜΑΤΑ ΔΙΚΤΥΑ ΚΑΙ ΚΙΝΗΤΕΣ ΕΠΙΚΟΙΝΩΝΙΕΣ\",\n" +
                "        \"courseCode\": \"3771\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"5\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 97,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΓΡΑΦΙΚΑ ΥΠΟΛΟΓΙΣΤΩΝ\",\n" +
                "        \"courseCode\": \"3781\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"6\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 98,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΛΛΗΛΕΠΙΔΡΑΣΗ ΑΝΘΡΩΠΟΥ-ΥΠΟΛΟΓΙΣΤΗ\",\n" +
                "        \"courseCode\": \"3783\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"6,7\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 99,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΣΤΟΙΧΕΙΑ ΔΙΚΑΙΟΥ ΤΗΣ ΠΛΗΡΟΦΟΡΙΑΣ\",\n" +
                "        \"courseCode\": \"3791\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"8\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 100,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΠΤΥΧΙΑΚΗ ΕΡΓΑΣΙΑ\",\n" +
                "        \"courseCode\": \"3802\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 103,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΠΡΑΚΤΙΚΗ ΑΣΚΗΣΗ\",\n" +
                "        \"courseCode\": \"3804\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 104,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΟΙΚΟΝΟΜΙΚΑ ΔΙΚΤΥΩΝ\",\n" +
                "        \"courseCode\": \"3818\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"2,5\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 105,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΠΑΡΑΛΛΗΛΟΣ ΠΡΟΓΡΑΜΜΑΤΙΣΜΟΣ\",\n" +
                "        \"courseCode\": \"3822\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"5,6\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 106,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΝΑΠΤΥΞΗ ΕΦΑΡΜΟΓΩΝ ΠΛΗΡΟΦΟΡΙΑΚΩΝ ΣΥΣΤΗΜΑΤΩΝ\",\n" +
                "        \"courseCode\": \"3842\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"7\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 107,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΡΕΥΝΗΤΙΚΗ ΕΡΓΑΣΙΑ\",\n" +
                "        \"courseCode\": \"3850\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 109,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΝΑΛΥΣΗ ΕΠΙΔΟΣΗΣ ΠΟΛΥΠΛΟΚΩΝ ΔΙΚΤΥΩΜΕΝΩΝ ΣΥΣΤΗΜΑΤΩΝ\",\n" +
                "        \"courseCode\": \"3862\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"2,5\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 110,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΤΕΧΝΟΛΟΓΙΑ ΠΟΛΥΜΕΣΩΝ\",\n" +
                "        \"courseCode\": \"3882\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"5\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 111,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΓΓΛΙΚΗ ΓΛΩΣΣΑ V - ΕΠΙΧΕΙΡΗΣΙΑΚΗ ΑΛΛΗΛΟΓΡΑΦΙΑ & ΕΠΙΚΟΙΝΩΝΙΑ\",\n" +
                "        \"courseCode\": \"3939\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 112,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΓΓΛΙΚΗ ΓΛΩΣΣΑ VI - ΕΠΙΧΕΙΡΗΣΙΑΚΗ ΟΡΟΛΟΓΙΑ\",\n" +
                "        \"courseCode\": \"3940\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 113,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΓΑΛΛΙΚΗ ΓΛΩΣΣΑ V - ΕΠΙΧΕΙΡΗΣΙΑΚΗ ΑΛΛΗΛΟΓΡΑΦΙΑ & ΕΠΙΚΟΙΝΩΝΙΑ\",\n" +
                "        \"courseCode\": \"3949\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 114,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΓΓΛΙΚΗ ΓΛΩΣΣΑ Ι - ΒΑΣΙΚΑ ΕΠΙΧΕΙΡΗΣΙΑΚΑ ΑΓΓΛΙΚΑ : Μέρος Α\",\n" +
                "        \"courseCode\": \"3951\",\n" +
                "        \"semesterId\": \"1\",\n" +
                "        \"courseType\": \"ΞΓ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 115,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΓΓΛΙΚΗ ΓΛΩΣΣΑ ΙΙ - ΒΑΣΙΚΑ ΕΠΙΧΕΙΡΗΣΙΑΚΑ ΑΓΓΛΙΚΑ : Μέρος Β\",\n" +
                "        \"courseCode\": \"3952\",\n" +
                "        \"semesterId\": \"2\",\n" +
                "        \"courseType\": \"ΞΓ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 116,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΓΓΛΙΚΗ ΓΛΩΣΣΑ ΙΙΙ - ΕΝΔΙΑΜΕΣΑ ΕΠΙΧΕΙΡΗΣΙΑΚΑ ΑΓΓΛΙΚΑ : Μέρος Α\",\n" +
                "        \"courseCode\": \"3953\",\n" +
                "        \"semesterId\": \"3\",\n" +
                "        \"courseType\": \"ΞΓ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 117,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΓΓΛΙΚΗ ΓΛΩΣΣΑ IV - ΕΝΔΙΑΜΕΣΑ ΕΠΙΧΕΙΡΗΣΙΑΚΑ ΑΓΓΛΙΚΑ : Μέρος Β\",\n" +
                "        \"courseCode\": \"3954\",\n" +
                "        \"semesterId\": \"4\",\n" +
                "        \"courseType\": \"ΞΓ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 118,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΓΕΡΜΑΝΙΚΗ ΓΛΩΣΣΑ  V - ΕΠΙΧΕΙΡΗΣΙΑΚΗ ΑΛΛΗΛΟΓΡΑΦΙΑ & ΕΠΙΚΟΙΝΩΝΙΑ\",\n" +
                "        \"courseCode\": \"3959\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 119,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΓΕΡΜΑΝΙΚΗ ΓΛΩΣΣΑ VΙ - ΕΠΙΧΕΙΡΗΣΙΑΚΗ ΟΡΟΛΟΓΙΑ\",\n" +
                "        \"courseCode\": \"3960\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 120,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΓΑΛΛΙΚΗ ΓΛΩΣΣΑ Ι - ΒΑΣΙΚΑ ΕΠΙΧΕΙΡΗΣΙΑΚΑ ΓΑΛΛΙΚΑ : Μέρος Α\",\n" +
                "        \"courseCode\": \"3961\",\n" +
                "        \"semesterId\": \"1\",\n" +
                "        \"courseType\": \"ΞΓ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 121,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΓΑΛΛΙΚΗ ΓΛΩΣΣΑ ΙΙ - ΒΑΣΙΚΑ ΕΠΙΧΕΙΡΗΣΙΑΚΑ ΓΑΛΛΙΚΑ : Μέρος Β\",\n" +
                "        \"courseCode\": \"3962\",\n" +
                "        \"semesterId\": \"2\",\n" +
                "        \"courseType\": \"ΞΓ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 122,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΓΑΛΛΙΚΗ ΓΛΩΣΣΑ ΙΙΙ - ΕΝΔΙΑΜΕΣΑ ΕΠΙΧΕΙΡΗΣΙΑΚΑ ΓΑΛΛΙΚΑ : Μέρος Α\",\n" +
                "        \"courseCode\": \"3963\",\n" +
                "        \"semesterId\": \"3\",\n" +
                "        \"courseType\": \"ΞΓ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 123,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΓΑΛΛΙΚΗ ΓΛΩΣΣΑ IV - ΕΝΔΙΑΜΕΣΑ ΕΠΙΧΕΙΡΗΣΙΑΚΑ ΓΑΛΛΙΚΑ : Μέρος Β\",\n" +
                "        \"courseCode\": \"3964\",\n" +
                "        \"semesterId\": \"4\",\n" +
                "        \"courseType\": \"ΞΓ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 124,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΓΕΡΜΑΝΙΚΗ ΓΛΩΣΣΑ Ι - ΒΑΣΙΚΑ ΕΠΙΧΕΙΡΗΣΙΑΚΑ ΓΕΡΜΑΝΙΚΑ : Μέρος Α\",\n" +
                "        \"courseCode\": \"3971\",\n" +
                "        \"semesterId\": \"1\",\n" +
                "        \"courseType\": \"ΞΓ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 125,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΓΕΡΜΑΝΙΚΗ ΓΛΩΣΣΑ ΙΙ - ΒΑΣΙΚΑ ΕΠΙΧΕΙΡΗΣΙΑΚΑ ΓΕΡΜΑΝΙΚΑ : Μέρος Β\",\n" +
                "        \"courseCode\": \"3972\",\n" +
                "        \"semesterId\": \"2\",\n" +
                "        \"courseType\": \"ΞΓ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 126,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΓΕΡΜΑΝΙΚΗ ΓΛΩΣΣΑ ΙΙΙ - ΕΝΔΙΑΜΕΣΑ ΕΠΙΧΕΙΡΗΣΙΑΚΑ ΓΕΡΜΑΝΙΚΑ : Μέρος Α\",\n" +
                "        \"courseCode\": \"3973\",\n" +
                "        \"semesterId\": \"3\",\n" +
                "        \"courseType\": \"ΞΓ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 127,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΓΕΡΜΑΝΙΚΗ ΓΛΩΣΣΑ IV - ΕΝΔΙΑΜΕΣΑ ΕΠΙΧΕΙΡΗΣΙΑΚΑ ΓΕΡΜΑΝΙΚΑ : Μέρος Β\",\n" +
                "        \"courseCode\": \"3974\",\n" +
                "        \"semesterId\": \"4\",\n" +
                "        \"courseType\": \"ΞΓ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 128,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΓΑΛΛΙΚΗ ΓΛΩΣΣΑ VI - ΕΠΙΧΕΙΡΗΣΙΑΚΗ ΟΡΟΛΟΓΙΑ\",\n" +
                "        \"courseCode\": \"3980\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 129,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΙΣΑΓΩΓΗ ΣΤΗΝ ΠΟΛΙΤΙΚΗ  & ΤΙΣ ΔΙΕΘΝΕΙΣ ΣΧΕΣΕΙΣ\",\n" +
                "        \"courseCode\": \"4110\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 130,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΥΡΩΠΑΪΚΟ ΔΙΚΑΙΟ\",\n" +
                "        \"courseCode\": \"4116\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 131,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΙΕΘΝΕΣ ΟΙΚΟΝΟΜΙΚΟ ΔΙΚΑΙΟ\",\n" +
                "        \"courseCode\": \"4126\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 132,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΞΩΤΕΡΙΚΕΣ ΣΧΕΣΕΙΣ ΤΗΣ Ε.Ε.\",\n" +
                "        \"courseCode\": \"4128\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 133,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΙΑΧΕΙΡΙΣΗ ΧΑΡΤΟΦΥΛΑΚΙΟΥ\",\n" +
                "        \"courseCode\": \"4137\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 134,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΙΕΘΝΕΙΣ ΟΡΓΑΝΙΣΜΟΙ\",\n" +
                "        \"courseCode\": \"4142\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 135,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΛΗΨΗ ΕΠΙΧΕΙΡΗΜΑΤΙΚΩΝ ΑΠΟΦΑΣΕΩΝ\",\n" +
                "        \"courseCode\": \"5133\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 136,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΟΡΓΑΝΩΣΙΑΚΗ ΣΥΜΠΕΡΙΦΟΡΑ \",\n" +
                "        \"courseCode\": \"5412\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 137,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΙΟΙΚΗΣΗ ΑΝΘΡΩΠΙΝΟΥ ΔΥΝΑΜΙΚΟΥ\",\n" +
                "        \"courseCode\": \"5414\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 138,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΙΟΙΚΗΣΗ ΑΛΛΑΓΩΝ ΣΤΗΝ ΨΗΦΙΑΚΗ ΕΠΟΧΗ\",\n" +
                "        \"courseCode\": \"5415\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 139,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΗΓΕΣΙΑ ΚΑΙ ΑΝΑΠΤΥΞΗ ΠΡΟΣΩΠΙΚΩΝ ΔΕΞΙΟΤΗΤΩΝ\",\n" +
                "        \"courseCode\": \"5428\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 140,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΙΣΑΓΩΓΗ ΣΤΟ ΜΑΡΚΕΤΙΝΓΚ\",\n" +
                "        \"courseCode\": \"5622\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 141,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΣΥΜΠΕΡΙΦΟΡΑ ΚΑΤΑΝΑΛΩΤΗ\",\n" +
                "        \"courseCode\": \"5623\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 142,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΠΟΛΙΤΙΚΗ ΠΡΟΪΟΝΤΟΣ\",\n" +
                "        \"courseCode\": \"5624\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 143,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΙΟΙΚΗΣΗ ΠΩΛΗΣΕΩΝ\",\n" +
                "        \"courseCode\": \"5625\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 144,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΨΗΦΙΑΚΟ ΜΑΡΚΕΤΙΝΓΚ\",\n" +
                "        \"courseCode\": \"5626\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 145,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΒΙΟΜΗΧΑΝΙΚΟ (B2B) ΜΑΡΚΕΤΙΝΓΚ\",\n" +
                "        \"courseCode\": \"5627\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 146,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΡΕΥΝΑ ΜΑΡΚΕΤΙΝΓΚ\",\n" +
                "        \"courseCode\": \"5634\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 147,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΙΑΦΗΜΙΣΗ ΚΑΙ ΕΠΙΚΟΙΝΩΝΙΑ\",\n" +
                "        \"courseCode\": \"5636\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 148,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΜΑΡΚΕΤΙΝΓΚ ΥΠΗΡΕΣΙΩΝ\",\n" +
                "        \"courseCode\": \"5637\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 149,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΙΕΘΝΕΣ ΜΑΡΚΕΤΙΝΓΚ\",\n" +
                "        \"courseCode\": \"5638\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 150,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΜΑΡΚΕΤΙΝΓΚ ΛΙΑΝΙΚΟΥ ΚΑΙ ΧΟΝΔΡΙΚΟΥ ΕΜΠΟΡΙΟΥ\",\n" +
                "        \"courseCode\": \"5657\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 151,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΤΟΥΡΙΣΤΙΚΟ ΜΑΡΚΕΤΙΝΓΚ\",\n" +
                "        \"courseCode\": \"5658\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 152,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΣΧΕΔΙΑΣΜΟΣ ΚΑΙ ΑΝΑΛΥΣΗ ΠΡΟΩΘΗΤΙΚΩΝ ΕΝΕΡΓΕΙΩΝ\",\n" +
                "        \"courseCode\": \"5667\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 153,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΣΧΕΔΙΑΣΜΟΣ ΔΗΜΙΟΥΡΓΙΚΟΥ ΚΑΙ ΔΙΑΦΗΜΙΣΤΙΚΩΝ ΜΗΝΥΜΑΤΩΝ\",\n" +
                "        \"courseCode\": \"5677\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 154,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΜΑΡΚΕΤΙΝΓΚ ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΧΕΙΡΗΣΕΩΝ ΚΑΙ ΜΗ ΚΕΡΔΟΣΚΟΠΙΚΩΝ ΟΡΓΑΝΙΣΜΩΝ\",\n" +
                "        \"courseCode\": \"5678\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 155,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΠΟΣΟΤΙΚΑ ΜΟΝΤΕΛΑ ΜΑΡΚΕΤΙΝΓΚ\",\n" +
                "        \"courseCode\": \"5688\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 156,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΝΑΛΥΤΙΚΗ ΔΙΑΔΙΚΤΥΟΥ\",\n" +
                "        \"courseCode\": \"5689\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 157,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΝΑΛΥΤΙΚΗ ΜΑΡΚΕΤΙΝΓΚ\",\n" +
                "        \"courseCode\": \"5691\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 158,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΙΑΠΟΛΙΤΙΣΜΙΚΗ ΕΠΙΚΟΙΝΩΝΙΑ ΚΑΙ ΔΙΟΙΚΗΣΗ ΕΠΙΧΕΙΡΗΣΕΩΝ\",\n" +
                "        \"courseCode\": \"5718\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 159,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΨΥΧΟΛΟΓΙΑ\",\n" +
                "        \"courseCode\": \"5721\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 160,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΙΣΑΓΩΓΗ ΣΤΗΝ ΕΠΙΚΟΙΝΩΝΙΑ\",\n" +
                "        \"courseCode\": \"5722\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 161,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΗΛΕΚΤΡΟΝΙΚΗ ΕΠΙΚΟΙΝΩΝΙΑ\",\n" +
                "        \"courseCode\": \"5724\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 162,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΗΜΟΣΙΕΣ ΣΧΕΣΕΙΣ ΚΑΙ ΔΙΑΧΕΙΡΙΣΗ ΚΡΙΣΕΩΝ\",\n" +
                "        \"courseCode\": \"5725\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 163,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΙΑΠΡΑΓΜΑΤΕΥΣΕΙΣ \",\n" +
                "        \"courseCode\": \"5728\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 164,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΤΑΙΡΙΚΗ ΕΠΙΚΟΙΝΩΝΙΑΚΗ ΣΤΡΑΤΗΓΙΚΗ\",\n" +
                "        \"courseCode\": \"5738\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 165,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΤΑΙΡΙΚΗ ΗΘΙΚΗ ΚΑΙ ΥΠΕΥΘΥΝΟΤΗΤΑ\",\n" +
                "        \"courseCode\": \"5781\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 166,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΣΥΜΒΟΥΛΕΥΤΙΚΗ ΤΩΝ ΕΠΙΧΕΙΡΗΣΕΩΝ\",\n" +
                "        \"courseCode\": \"5783\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 167,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΙΟΙΚΗΣΗ ΔΙΕΘΝΩΝ ΕΠΙΧΕΙΡΗΣΕΩΝ\",\n" +
                "        \"courseCode\": \"5785\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 168,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΝΑΛΥΣΗ ΔΕΔΟΜΕΝΩΝ\",\n" +
                "        \"courseCode\": \"6005\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"1\",\n" +
                "        \"ects\": 8.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 169,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΓΡΑΜΜΙΚΑ ΜΟΝΤΕΛΑ\",\n" +
                "        \"courseCode\": \"6023\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"1\",\n" +
                "        \"ects\": 8.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 171,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΕΙΓΜΑΤΟΛΗΨΙΑ\",\n" +
                "        \"courseCode\": \"6033\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 172,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΓΡΑΜΜΙΚΗ ΑΛΓΕΒΡΑ ΙΙ\",\n" +
                "        \"courseCode\": \"6082\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"3\",\n" +
                "        \"ects\": 7.5\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 173,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΣΤΑΤΙΣΤΙΚΗ ΚΑΤΑ BAYES\",\n" +
                "        \"courseCode\": \"6106\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 7.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 174,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΜΗ ΠΑΡΑΜΕΤΡΙΚΗ ΣΤΑΤΙΣΤΙΚΗ\",\n" +
                "        \"courseCode\": \"6113\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 175,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΡΙΘΜΗΤΙΚΕΣ ΜΕΘΟΔΟΙ ΣΤΗ ΣΤΑΤΙΣΤΙΚΗ\",\n" +
                "        \"courseCode\": \"6115\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 7.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 176,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΘΕΩΡΙΑ ΠΙΘΑΝΟΤΗΤΩΝ\",\n" +
                "        \"courseCode\": \"6116\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 8.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 177,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΠΡΟΣΟΜΟΙΩΣΗ\",\n" +
                "        \"courseCode\": \"6125\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"3\",\n" +
                "        \"ects\": 7.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 178,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΜΕΘΟΔΟΙ ΣΤΑΤΙΣΤΙΚΗΣ ΚΑΙ ΜΗΧΑΝΙΚΗΣ ΜΑΘΗΣΗΣ\",\n" +
                "        \"courseCode\": \"6127\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"1\",\n" +
                "        \"ects\": 8.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 179,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΙΣΑΓΩΓΗ ΣΤΗ ΜΑΘΗΜΑΤΙΚΗ ΑΝΑΛΥΣΗ\",\n" +
                "        \"courseCode\": \"6133\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 7.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 180,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΝΑΛΟΓΙΣΤΙΚΑ Ι\",\n" +
                "        \"courseCode\": \"6135\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 7.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 181,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΠΙΘΑΝΟΤΗΤΕΣ ΙΙ\",\n" +
                "        \"courseCode\": \"6142\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"3\",\n" +
                "        \"ects\": 7.5\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 182,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"Ε.Θ.Σ.Π.: ΕΙΣ.ΣΤΗ ΘΕΩΡ. ΜΕΤΡΟΥ ΜΕ ΑΝΑΦ.ΣΤΙΣ ΠΙΘΑΝ. ΚΑΙ ΤΗ ΣΤΑΤΙΣΤΙΚΗ\",\n" +
                "        \"courseCode\": \"6256\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 183,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΡΧΕΣ ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ ΛΟΓΙΣΤΙΚΗΣ\",\n" +
                "        \"courseCode\": \"7102\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 184,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗ ΑΝΑΛΥΣΗ ΚΑΙ ΑΠΟΤΙΜΗΣΗ ΕΠΙΧΕΙΡΗΣΕΩΝ\",\n" +
                "        \"courseCode\": \"7108\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 185,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΛΟΓΙΣΤΙΚΑ ΠΛΗΡΟΦΟΡΙΑΚΑ ΣΥΣΤΗΜΑΤΑ ΜΕΣΩ ΔΙΑΔΙΚΤΥΟΥ\",\n" +
                "        \"courseCode\": \"7116\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 186,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΑ ΤΗΣ ΝΑΥΤΙΛΙΑΣ\",\n" +
                "        \"courseCode\": \"7127\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 187,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΛΗΨΗ ΕΠΙΧΕΙΡΗΜΑΤΙΚΩΝ ΑΠΟΦΑΣΕΩΝ\",\n" +
                "        \"courseCode\": \"8111\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 188,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΟΡΓΑΝΩΣΙΑΚΗ ΣΥΜΠΕΡΙΦΟΡΑ ΚΑΙ ΗΓΕΣΙΑ\",\n" +
                "        \"courseCode\": \"8115\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 189,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΜΑΘΗΜΑΤΙΚΗ ΒΕΛΤΙΣΤΟΠΟΙΗΣΗ\",\n" +
                "        \"courseCode\": \"8116\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"2\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 190,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΙΟΙΚΗΣΗ ΕΠΙΧΕΙΡΗΣΕΩΝ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑ\",\n" +
                "        \"courseCode\": \"8125\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 191,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΝΑΛΥΣΗ ΚΑΙ ΜΟΝΤΕΛΟΠΟΙΗΣΗ ΔΙΑΔΙΚΑΣΙΩΝ ΚΑΙ ΣΥΣΤΗΜΑΤΩΝ\",\n" +
                "        \"courseCode\": \"8126\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 192,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΟΡΓΑΝΩΣΙΑΚΗ ΨΥΧΟΛΟΓΙΑ\",\n" +
                "        \"courseCode\": \"8127\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 193,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΠΡΟΓΡΑΜΜΑΤΙΣΜΟΣ ΣΥΣΤΗΜΑΤΩΝ ΔΙΑΝΟΜΗΣ ΚΑΙ ΜΕΤΑΦΟΡΩΝ\",\n" +
                "        \"courseCode\": \"8133\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 194,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΔΙΟΙΚΗΣΗ ΠΑΡΑΓΩΓΗΣ ΚΑΙ ΥΠΗΡΕΣΙΩΝ\",\n" +
                "        \"courseCode\": \"8134\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 195,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΑΝΑΠΤΥΞΗ ΠΡΟΣΩΠΙΚΩΝ ΚΑΙ ΗΓΕΤΙΚΩΝ ΙΚΑΝΟΤΗΤΩΝ\",\n" +
                "        \"courseCode\": \"8135\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 196,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΠΙΧΕΙΡΗΜΑΤΙΚΗ ΕΥΦΥΪΑ ΚΑΙ ΜΗΧΑΝΙΚΗ ΔΕΔΟΜΕΝΩΝ\",\n" +
                "        \"courseCode\": \"8137\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 197,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΤΕΧΝΟΛΟΓΙΑ ΛΟΓΙΣΜΙΚΟΥ ΣΤΗΝ ΠΡΑΞΗ\",\n" +
                "        \"courseCode\": \"8138\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 198,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΣΥΝΔΥΑΣΤΙΚΗ ΒΕΛΤΙΣΤΟΠΟΙΗΣΗ\",\n" +
                "        \"courseCode\": \"8143\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕπΚ\",\n" +
                "        \"direction\": \"2,4\",\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 199,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΤΕΧΝΟΛΟΓΙΕΣ ΚΑΙ ΕΦΑΡΜΟΓΕΣ ΗΛΕΚΤΡΟΝΙΚΟΥ ΕΠΙΧΕΙΡΕΙΝ\",\n" +
                "        \"courseCode\": \"8146\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 200,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΠΙΧΕΙΡΗΜΑΤΙΚΟΤΗΤΑ\",\n" +
                "        \"courseCode\": \"8154\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 201,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΣΥΣΤΗΜΑΤΑ ΔΙΑΧΕΙΡΙΣΗΣ ΕΠΙΧΕΙΡΗΣΙΑΚΩΝ ΠΟΡΩΝ\",\n" +
                "        \"courseCode\": \"8159\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 202,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΣΥΣΤΗΜΑΤΑ ΔΙΑΧΕΙΡΙΣΗΣ ΜΕΓΑΛΩΝ ΔΕΔΟΜΕΝΩΝ\",\n" +
                "        \"courseCode\": \"8170\",\n" +
                "        \"semesterId\": \"8\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 203,\n" +
                "        \"grade\": null,\n" +
                "        \"Passed\": null,\n" +
                "        \"title\": \"ΕΦΑΡΜΟΣΜΕΝΗ ΜΗΧΑΝΙΚΗ ΜΑΘΗΣΗ \",\n" +
                "        \"courseCode\": \"8185\",\n" +
                "        \"semesterId\": \"7\",\n" +
                "        \"courseType\": \"ΕΕ\",\n" +
                "        \"direction\": null,\n" +
                "        \"ects\": 6.0\n" +
                "    }\n" +
                "]";

    }

}