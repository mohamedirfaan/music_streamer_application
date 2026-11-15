package com.example.music_streamer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class AddSong extends AppCompatActivity {
    private static final String SERVER_URL = "http://10.173.217.248:8000/";
    Button chooseFileButton, uploadButton, closeButton;
    LinearLayout uploadLayout, imageViewLayout;
    TextView songNameView;
    EditText songNameInput;
    String songUrl;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.add_song);
        chooseFileButton = findViewById(R.id.chooseFileButton);
        songNameInput = findViewById(R.id.songNameInput);
        uploadButton = findViewById(R.id.uploadButton);

        ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            songUrl = data.getDataString();
                            uploadLayout = findViewById(R.id.uploadLayout);
                            imageViewLayout = findViewById(R.id.imageViewLayout);
                            uploadLayout.setVisibility(View.GONE);
                            imageViewLayout.setVisibility(View.VISIBLE);
                            songNameView = findViewById(R.id.songNameView);
                            songNameView.setText(songUrl);
                            closeButton = findViewById(R.id.closeButton);
                            closeButton.setOnClickListener(view -> {
                                uploadLayout.setVisibility(View.VISIBLE);
                                imageViewLayout.setVisibility(View.GONE);
                                songUrl = null;
                            });
                        }
                    }
                }
        );
        chooseFileButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("mp3/*");
            resultLauncher.launch(intent);
        });

        uploadButton.setOnClickListener(view -> {
            String songName = songNameInput.getText().toString();
            if (songName.isEmpty()) {
                Toast.makeText(AddSong.this, "Song name can't be empty", Toast.LENGTH_SHORT).show();
            } else if(songUrl == null) {
                Toast.makeText(AddSong.this, "Select a song first", Toast.LENGTH_SHORT).show();
            } else {
                uploadSong(songName, songUrl);
            }
        });
    }

    private void uploadSong(String songName, String songUrl) {
        SharedPreferences sharedPreferences = getSharedPreferences("auth", MODE_PRIVATE);
        String authToken = sharedPreferences.getString("token", null);
        long userId = sharedPreferences.getLong("userId", -1L);
        OkHttpClient client = new OkHttpClient();
        Uri uri = Uri.parse(songUrl);
        RequestBody fileBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse("audio/mpeg");
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                try (InputStream is = getContentResolver().openInputStream(uri)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        sink.write(buffer, 0, bytesRead);
                    }
                }
            }

            @Override
            public long contentLength() throws IOException {
                return getContentResolver().openAssetFileDescriptor(uri, "r").getLength();
            }
        };
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title", songName)
                .addFormDataPart("songFile", songName, fileBody)
                .addFormDataPart("owner", String.valueOf(userId))
                .build();
        Request request = new Request.Builder()
                .url(SERVER_URL+"uploadSong/")
                .addHeader("Authorization", "Token "+authToken)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException exception) {
                Log.e(AddSong.class.getName(), "Upload failed", exception);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null
                        ? response.body().string()
                        : "";
                Log.d(AddSong.class.getName(), "Status: "+response.code());
                Log.d(AddSong.class.getName(), "Response: "+responseBody);
                response.close();
            }
        });
    }
}
