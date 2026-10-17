package com.example.music_streamer;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class Dashboard extends AppCompatActivity {
    private static final String SONG_LIST_URL = "http://10.200.168.248:8000/listSongs/";
    private ImageButton homeButton, libraryButton;
    private TextView home_text, library_text;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.dashboard);
        homeButton = findViewById(R.id.homeButton);
        libraryButton = findViewById(R.id.libraryButton);
        home_text = findViewById(R.id.home_text);
        library_text = findViewById(R.id.library_text);
        homeButton.setPadding(10, 10, 10, 0);
        home_text.setVisibility(VISIBLE);
        SharedPreferences sharedPreferences = getSharedPreferences("auth", MODE_PRIVATE);
        String authToken = sharedPreferences.getString("token", null);
        new Thread(()-> getAllSongs(authToken)).start();
        libraryButton.setOnClickListener(view->{
            homeButton.setPadding(10, 10, 10, 10);
            home_text.setVisibility(GONE);

            libraryButton.setPadding(10,10,10,0);
            library_text.setVisibility(VISIBLE);
        });

        homeButton.setOnClickListener(view->{
            libraryButton.setPadding(10,10,10,10);
            library_text.setVisibility(GONE);

            homeButton.setPadding(10,10,10,0);
            home_text.setVisibility(VISIBLE);
        });
    }

    private void renderSongs(JSONArray songs) {
        runOnUiThread(() -> {
            LinearLayout songContainer = findViewById(R.id.songContainer);
            songContainer.removeAllViews();

            for (int i = 0; i < songs.length(); i++) {
                try {
                    JSONObject song = songs.getJSONObject(i);

                    String title = song.optString("title", "Unknown Song");
                    String durationValue = song.optString("duration", "");

                    LinearLayout row = new LinearLayout(this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(android.view.Gravity.CENTER_VERTICAL);
                    row.setPadding(dp(0), dp(10), dp(0), dp(10));

                    // Album cover placeholder
                    TextView cover = new TextView(this);
                    cover.setText("♫");
                    cover.setTextSize(30);
                    cover.setTextColor(Color.WHITE);
                    cover.setGravity(Gravity.CENTER);
                    cover.setBackgroundColor(Color.rgb(38, 48, 45));

                    LinearLayout.LayoutParams coverParams =
                            new LinearLayout.LayoutParams(dp(64), dp(64));
                    row.addView(cover, coverParams);

                    // Song title and subtitle
                    LinearLayout details = new LinearLayout(this);
                    details.setOrientation(LinearLayout.VERTICAL);
                    details.setGravity(Gravity.CENTER_VERTICAL);

                    TextView titleView = new TextView(this);
                    titleView.setText(title);
                    titleView.setTextColor(Color.WHITE);
                    titleView.setTextSize(16);
                    titleView.setMaxLines(1);
                    titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);

                    details.addView(titleView);

                    LinearLayout.LayoutParams detailsParams =
                            new LinearLayout.LayoutParams(0,
                                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                    detailsParams.leftMargin = dp(14);
                    row.addView(details, detailsParams);

                    // Duration
                    TextView durationView = new TextView(this);
                    durationView.setText(formatDuration(durationValue));
                    durationView.setTextColor(Color.rgb(165, 170, 180));
                    durationView.setTextSize(14);

                    row.addView(durationView);

                    // Three-dot menu
                    TextView menu = new TextView(this);
                    menu.setText("⋮");
                    menu.setTextSize(26);
                    menu.setTextColor(Color.rgb(165, 170, 180));
                    menu.setGravity(Gravity.CENTER);

                    LinearLayout.LayoutParams menuParams =
                            new LinearLayout.LayoutParams(dp(32), dp(48));
                    menuParams.leftMargin = dp(8);
                    row.addView(menu, menuParams);

                    songContainer.addView(row,
                            new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT));

                } catch (JSONException e) {
                    Log.e("Dashboard", "Failed to render song", e);
                }
            }
        });
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private String formatDuration(String value) {
        try {
            if (value == null || value.isEmpty() || value.equals("null")) {
                return "--:--";
            }

            int seconds = (int) Double.parseDouble(value);
            return String.format(java.util.Locale.getDefault(),
                    "%d:%02d", seconds / 60, seconds % 60);
        } catch (NumberFormatException e) {
            return "--:--";
        }
    }


    private void getAllSongs(String authToken) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(SONG_LIST_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Token " + authToken);

            int responseCode = connection.getResponseCode();
            InputStream is;
            if (responseCode == 200) {
                is = connection.getInputStream();
            } else {
                is = connection.getErrorStream();
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            JSONArray result = new JSONArray();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.put(new JSONObject(line));
            }
            renderSongs(result);
        } catch (Exception exception) {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
