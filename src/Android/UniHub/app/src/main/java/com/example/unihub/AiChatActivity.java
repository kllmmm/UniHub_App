package com.example.unihub;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AiChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private LinearLayout btnMenu, btnProgress, btnCalendar, btnAi;

    private ImageView icon_menu, icon_progress, icon_calendar, icon_ai;

    private String user_identifier;

    private String grades_json;
    private MessageAdapter adapter;
    private ApiService apiService;
    private String aiMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        //Getting the JSON with the grades from the login
        Intent intent = getIntent();
        grades_json = intent.getStringExtra("grades_json");
        user_identifier = intent.getStringExtra("user_identifier");
        Log.d("Check_User",user_identifier);

        recyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);


        btnMenu = findViewById(R.id.btn_menu);
        btnProgress = findViewById(R.id.btn_progress);
        btnCalendar = findViewById(R.id.btn_calendar);
        btnAi = findViewById(R.id.btn_ai);
        icon_menu = findViewById(R.id.icon_menu);
        icon_progress = findViewById(R.id.icon_progress);
        icon_calendar = findViewById(R.id.icon_calendar);
        icon_ai = findViewById(R.id.icon_ai);

        //Initialize bottom menus UI look
        btnMenu.setBackgroundResource(R.drawable.nav_selector);
        icon_menu.setColorFilter(Color.parseColor("#FFFFFF"));
        btnProgress.setBackgroundResource(R.drawable.nav_selector);
        icon_progress.setColorFilter(Color.parseColor("#FFFFFF"));
        btnCalendar.setBackgroundResource(R.drawable.nav_selector);
        icon_calendar.setColorFilter(Color.parseColor("#FFFFFF"));
        btnAi.setBackgroundResource(R.drawable.nav_active_bg);
        icon_ai.setColorFilter(Color.parseColor("#5DA9FF"));

        adapter = new MessageAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

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


        sendButton.setOnClickListener(v -> {

            String input = messageInput.getText().toString().trim();

            if (!input.isEmpty()) {

                messageInput.setText("");


                AiModelRequest request = new AiModelRequest(input);

                //Api Call
                apiService.aiModel(request).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {


                        if (response.isSuccessful() && response.body() != null) {

                            Log.d("API_SUCCESS", response.body().toString());

                            aiMessage = preapreString(response.body().toString());

                            new Handler().postDelayed(() -> {

                                adapter.setMessage(aiMessage);
                                recyclerView.scrollToPosition(0);

                            }, 1500);

                        } else {
                            Toast.makeText(AiChatActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            Log.d("API_ERROR", "Error: " + response.code() + response.body());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {

                        Toast.makeText(AiChatActivity.this, "Connection Failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("API_ERROR", "Error: ", t);
                    }
                });

            }

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

    //--------------------------------------Methods--------------------------------------//
    @NonNull
    public static String preapreString(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }


        String prefix = "\"response\":\"";
        int startIndex = input.indexOf(prefix);

        if (startIndex == -1) {
            return input;
        }
        startIndex += prefix.length();

        int endIndex = input.lastIndexOf("\"}");

        if (endIndex == -1 || endIndex <= startIndex) {
            endIndex = input.lastIndexOf("\"");
        }

        String extractedText = input.substring(startIndex, endIndex);

        return extractedText
                .replace("\\n", " ")
                .replace("\n", " ")
                .replace("\\\"", "\"")
                .replace("\\t*", " ")
                .replace("\\t+", " ")
                .replace("\\t", " ")
                .replace("*", " ");
    }

    private void hideAll() {
        btnMenu.setBackgroundResource(android.R.color.transparent);

        btnProgress.setBackgroundResource(android.R.color.transparent);

        btnCalendar.setBackgroundResource(android.R.color.transparent);

        btnAi.setBackgroundResource(android.R.color.transparent);
    }
}