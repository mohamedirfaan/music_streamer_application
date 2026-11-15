package com.example.music_streamer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.music_streamer.client.ClientConnector;
import com.example.music_streamer.logger.Logger;

import org.json.JSONObject;

public class Login extends AppCompatActivity {
    private static final String LOGIN_URL = "http://10.173.217.248:8000/login/";
    private final Logger logger = new Logger(this);
    EditText email, password;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.loginButton);
        login.setOnClickListener(view -> {
            new Thread(()->{
                login(email.getText().toString(), password.getText().toString());
            }).start();
        });
    }

    private void showLoginResult(boolean status) {
        runOnUiThread(()->{
            if (status) {
                Toast.makeText(Login.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Login.this, Dashboard.class);
                startActivity(intent);

            } else {
                Toast.makeText(Login.this, "Error logging in", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void login(String email, String password) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("email", email);
            payload.put("password", password);
            String response = ClientConnector.send(LOGIN_URL, "POST", payload.toString());
            logger.log(response);
            JSONObject jsonResponse = new JSONObject(response);
            String status = jsonResponse.has("status") ? jsonResponse.getString("status") : "0";
            if ("1".equals(status)) {
                String authToken = jsonResponse.getString("token");
                long userId = jsonResponse.getLong("userId");
                SharedPreferences preferences = getSharedPreferences("auth", MODE_PRIVATE);
                preferences.edit().putString("token", authToken).apply();
                preferences.edit().putLong("userId", userId).apply();
                showLoginResult(true);
            } else {
                Log.e(Login.class.getName(), "Error logging in: "+jsonResponse);
                showLoginResult(false);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}