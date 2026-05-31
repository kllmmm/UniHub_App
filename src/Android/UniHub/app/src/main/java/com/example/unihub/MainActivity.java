package com.example.unihub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.unihub.databinding.ActivityMainBinding;
import com.google.gson.JsonArray;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
//Main Activity
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Retrofit
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

        // Login Button Click Listener
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = binding.etUsername.getText().toString();
                String password = binding.etPassword.getText().toString();

                //Username and Password Validation
                if (username.isEmpty() || password.isEmpty() || !username.startsWith("p3")) {
                    Toast.makeText(MainActivity.this, "Please enter valid credentials", Toast.LENGTH_SHORT).show();
                } else {

                    binding.btnLogin.setEnabled(false);
                    Toast.makeText(MainActivity.this, "Connecting to server...", Toast.LENGTH_LONG).show();

                    // Creating Request
                    LoginRequest request = new LoginRequest(username, password);

                    // API call
                    apiService.getGrades(request).enqueue(new Callback<JsonArray>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonArray> call, @NonNull Response<JsonArray> response) {
                            binding.btnLogin.setEnabled(true);

                            if (response.isSuccessful() && response.body() != null) {

                                Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                                Log.d("API_SUCCESS", response.body().toString());


                                 Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                                 intent.putExtra("grades_json", response.body().toString());
                                 intent.putExtra("user_identifier", username);
                                 startActivity(intent);
                            } else {
                                Toast.makeText(MainActivity.this, "Invalid Credentials or Server Error", Toast.LENGTH_SHORT).show();
                                Log.d("API_ERROR", "Error: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonArray> call, @NonNull Throwable t) {
                            binding.btnLogin.setEnabled(true);
                            Toast.makeText(MainActivity.this, "Connection Failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("API_ERROR", "Error: ", t);
                        }
                    });
                }
            }
        });
    }
}