package com.example.music_streamer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences("auth", MODE_PRIVATE);
        String authToken = preferences.getString("token", null);
        Intent intent;
        if (authToken != null) {
            intent = new Intent(SplashScreen.this, Dashboard.class);
        } else {
            intent = new Intent(SplashScreen.this, Login.class);
        }
        startActivity(intent);
    }
}