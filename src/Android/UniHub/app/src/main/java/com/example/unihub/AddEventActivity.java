package com.example.unihub;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import androidx.appcompat.app.AppCompatActivity;

public class AddEventActivity extends AppCompatActivity {

    EditText etDay, etMonth, etYear, etHour, etMinute, etDescription;
    Button btnSave;

    ImageButton btnBack;
    String user_identifier;
    String grades_json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        Intent intent = getIntent();
        grades_json = intent.getStringExtra("grades_json");
        user_identifier = intent.getStringExtra("user_identifier");

        // Initialize UI elements
        etDay = findViewById(R.id.etDay);
        etMonth = findViewById(R.id.etMonth);
        etYear = findViewById(R.id.etYear);
        etHour = findViewById(R.id.etHour);
        etMinute = findViewById(R.id.etMinute);
        etDescription = findViewById(R.id.etDescription);
        btnSave = findViewById(R.id.btnSave);

        btnBack = findViewById(R.id.BackButton);

        //--------------------------------------Navigation Button--------------------------------------//
        btnBack.setOnClickListener(v -> {
            Intent intentBack = new Intent(this, Calendar.class);
            intentBack.putExtra("user_identifier",user_identifier);
            intentBack.putExtra("grades_json",grades_json);
            startActivity(intentBack);
        });

        setupAutoMove(etDay, etMonth, 2);
        setupAutoMove(etMonth, etYear, 2);
        setupAutoMove(etHour, etMinute, 2);

        btnSave.setOnClickListener(v -> saveEvent());
    }

    //--------------------------------------Methods--------------------------------------//

    private void saveToJson(String user_identifier,String date, String time, String desc) {

        try {
            File file = new File(getFilesDir(), "events_new.json");
            JSONArray jsonArray;

            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                StringBuilder builder = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                reader.close();
                fis.close();

                jsonArray = new JSONArray(builder.toString());

            } else {
                jsonArray = new JSONArray();
            }

            JSONObject obj = new JSONObject();
            obj.put("user_identifier",user_identifier);
            obj.put("date", date);
            obj.put("time", time);
            obj.put("description", desc);

            jsonArray.put(obj);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(jsonArray.toString().getBytes());
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupAutoMove(EditText current, EditText next, int maxLength) {
        current.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.length() == maxLength) {
                    next.requestFocus();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }


    private void saveEvent() {

        String day = etDay.getText().toString();
        String month = etMonth.getText().toString();
        String year = etYear.getText().toString();
        String hour = etHour.getText().toString();
        String minute = etMinute.getText().toString();
        String desc = etDescription.getText().toString();

        if (!isValidDate(day, month, year)) {
            Toast.makeText(this, "Invalid date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidTime(hour, minute)) {
            Toast.makeText(this, "Invalid time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (desc.length() == 0) {
            Toast.makeText(this, "Description required", Toast.LENGTH_SHORT).show();
            return;
        }

        String date = day + "/" + month + "/" + year;
        String time = hour + ":" + minute;

        Toast.makeText(this, "Saved: " + date + " " + time, Toast.LENGTH_LONG).show();

        saveToJson(user_identifier,date, time, desc);
        Intent intent = new Intent(this, Calendar.class);
        intent.putExtra("user_identifier",user_identifier);
        intent.putExtra("grades_json",grades_json);
        startActivity(intent);

    }

    private boolean isValidDate(String d, String m, String y) {
        try {
            int day = Integer.parseInt(d);
            int month = Integer.parseInt(m);
            int year = Integer.parseInt(y);

            return (day >= 1 && day <= 31) &&
                    (month >= 1 && month <= 12) &&
                    (year >= 2000 && year <= 2100);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidTime(String h, String m) {
        try {
            int hour = Integer.parseInt(h);
            int min = Integer.parseInt(m);

            return (hour >= 0 && hour <= 23) &&
                    (min >= 0 && min <= 59);
        } catch (Exception e) {
            return false;
        }
    }
}