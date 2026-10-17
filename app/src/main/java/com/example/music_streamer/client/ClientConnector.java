package com.example.music_streamer.client;


import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ClientConnector {
    public static String send(String url, String method, String payload) {
        HttpURLConnection connection = null;
        try {
            URL loginUrl = new URL(url);
            connection = (HttpURLConnection) loginUrl.openConnection();

            connection.setRequestMethod(method);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            if (payload != null) {
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(payload.getBytes(StandardCharsets.UTF_8));
                }
            }

            StringBuilder builder = getStringBuilder(connection);
            return builder.toString();

        } catch (MalformedURLException | ProtocolException exception) {
            System.out.println(exception.getMessage());
        } catch (IOException e) {
            Log.e("ClientConnector", Arrays.toString(e.getStackTrace()));
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new JSONObject().toString();
    }

    @NonNull
    private static StringBuilder getStringBuilder(HttpURLConnection connection) throws IOException {
        BufferedReader bufferedReader = null;
        InputStream inputStream = null;
        try {
            StringBuilder builder = new StringBuilder();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            return builder;
        } catch (IOException exception) {
            if (inputStream != null) {
                inputStream.close();
            }
            if (bufferedReader != null) {
            bufferedReader.close();
            }
        }
        return new StringBuilder();
    }
}
