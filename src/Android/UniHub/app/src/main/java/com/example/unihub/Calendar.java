package com.example.unihub;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.util.Log;

public class Calendar extends AppCompatActivity {

    private RecyclerView recyclerView;

    private List<Event> eventList;

    private ImageView btnAddEvent;
    private LinearLayout btnMenu, btnProgress, btnCalendar, btnAi;
    private ImageView icon_menu, icon_progress, icon_calendar, icon_ai;

    private EventAdapter adapter;

    private String user_identifier;
    private String grades_json;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        //Getting the JSON with the grades from the login
        Intent intent = getIntent();
        grades_json = intent.getStringExtra("grades_json");
        user_identifier = intent.getStringExtra("user_identifier");
        Log.d("Check_User",user_identifier);

        //--------------------------------------Initialize UI elements--------------------------------------//

        recyclerView = findViewById(R.id.recyclerEvents);
        btnAddEvent = findViewById(R.id.btnAddEvent);
        btnMenu = findViewById(R.id.btn_menu);
        btnProgress = findViewById(R.id.btn_progress);
        btnCalendar = findViewById(R.id.btn_calendar);
        btnAi = findViewById(R.id.btn_ai);
        icon_menu = findViewById(R.id.icon_menu);
        icon_progress = findViewById(R.id.icon_progress);
        icon_calendar = findViewById(R.id.icon_calendar);
        icon_ai = findViewById(R.id.icon_ai);

        btnMenu.setBackgroundResource(R.drawable.nav_selector);
        icon_menu.setColorFilter(Color.parseColor("#FFFFFF"));
        btnProgress.setBackgroundResource(R.drawable.nav_selector);
        icon_progress.setColorFilter(Color.parseColor("#FFFFFF"));
        btnCalendar.setBackgroundResource(R.drawable.nav_active_bg);
        icon_calendar.setColorFilter(Color.parseColor("#5DA9FF"));
        btnAi.setBackgroundResource(R.drawable.nav_selector);
        icon_ai.setColorFilter(Color.parseColor("#FFFFFF"));


        eventList = new ArrayList<>();

        //--------------------------------------Initialize EventAdapter and RecyclerView--------------------------------------//
        adapter = new EventAdapter(eventList, (event, position) -> {

            eventList.remove(position);
            adapter.notifyItemRemoved(position);

            deleteEventFromJson(event);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        eventList.addAll(loadFromJson());
        Log.d("List_Check",eventList.toString());
        adapter.notifyDataSetChanged();

        //Add a new Event
        btnAddEvent.setOnClickListener(v -> {
            Intent intentAdd = new Intent(this, AddEventActivity.class);
            intentAdd.putExtra("user_identifier",user_identifier);
            intentAdd.putExtra("grades_json",grades_json);
            startActivity(intentAdd);
        });

        //--------------------------------------Navigation Buttons--------------------------------------//

        btnMenu.setOnClickListener(v -> {
            hideAll();
            btnMenu.setBackgroundResource(R.drawable.nav_active_bg);
            icon_menu.setColorFilter(Color.parseColor("#5DA9FF"));

            Intent intentMenu = new Intent(this, MenuActivity.class);
            intentMenu.putExtra("grades_json", grades_json);
            intentMenu.putExtra("user_identifier",user_identifier);
            startActivity(intentMenu);
        });

        btnProgress.setOnClickListener(v -> {
            hideAll();
            btnProgress.setBackgroundResource(R.drawable.nav_active_bg);
            icon_progress.setColorFilter(Color.parseColor("#5DA9FF"));

            Intent intentProgress = new Intent(this, ProgressActivity.class);
            intentProgress.putExtra("grades_json", grades_json);
            intentProgress.putExtra("user_identifier",user_identifier);
            startActivity(intentProgress);
        });

        btnCalendar.setOnClickListener(v -> {
            hideAll();
            btnCalendar.setBackgroundResource(R.drawable.nav_active_bg);
            icon_calendar.setColorFilter(Color.parseColor("#5DA9FF"));

            Intent intentCalendar = new Intent(this, Calendar.class);
            intentCalendar.putExtra("grades_json", grades_json);
            intentCalendar.putExtra("user_identifier",user_identifier);
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
    }

    private void hideAll() {
        btnMenu.setBackgroundResource(android.R.color.transparent);

        btnProgress.setBackgroundResource(android.R.color.transparent);

        btnCalendar.setBackgroundResource(android.R.color.transparent);

        btnAi.setBackgroundResource(android.R.color.transparent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        eventList.clear();
        eventList.addAll(loadFromJson());
        adapter.notifyDataSetChanged();
    }


    //--------------------------------------Methods--------------------------------------//

    //Method for loading the events from the JSON file (events_new.json) and returning them as a list of Events
    private List<Event> loadFromJson() {

        List<Event> list = new ArrayList<>();

        try {
            File file = new File(getFilesDir(), "events_new.json");

            if (!file.exists()) return list;

            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            reader.close();
            fis.close();

            JSONArray jsonArray = new JSONArray(builder.toString());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String jsonUserIdentifier = obj.getString("user_identifier");

                if(jsonUserIdentifier.equals(this.user_identifier)) {

                    String date = obj.getString("date");
                    String time = obj.getString("time");
                    String desc = obj.getString("description");

                    list.add(new Event(date, time, desc, jsonUserIdentifier));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        sortEventsByDateTime(list);
        return list;
    }

    //Method for deleting an event from the JSON file (events_new.json)
    private void deleteEventFromJson(Event eventToDelete) {

        try {

            File file = new File(getFilesDir(), "events_new.json");

            if (!file.exists()) return;

            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            reader.close();
            fis.close();

            JSONArray jsonArray = new JSONArray(builder.toString());

            JSONArray newArray = new JSONArray();

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject obj = jsonArray.getJSONObject(i);

                String date = obj.getString("date");
                String time = obj.getString("time");
                String desc = obj.getString("description");
                String userId = obj.getString("user_identifier");

                boolean isSameEvent =
                        date.equals(eventToDelete.getDate()) &&
                                time.equals(eventToDelete.getTime()) &&
                                desc.equals(eventToDelete.getDescription()) &&
                                userId.equals(eventToDelete.getUser_identifier());

                if (!isSameEvent) {
                    newArray.put(obj);
                }
            }

            FileOutputStream fos = new FileOutputStream(file, false);
            fos.write(newArray.toString().getBytes());
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sortEventsByDateTime(List<Event> list) {

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        Collections.sort(list, (e1, e2) -> {

            try {

                Date d1 = format.parse(e1.getDate() + " " + e1.getTime());
                Date d2 = format.parse(e2.getDate() + " " + e2.getTime());

                return d1.compareTo(d2);

            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        });
    }
}