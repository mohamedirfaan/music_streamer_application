package com.example.music_streamer.util;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.music_streamer.R;

import java.util.Locale;

public class SongPlayer extends AppCompatActivity {

    private TextView songTitle;
    private TextView artistName;
    private TextView currentTime;
    private TextView totalDuration;

    private boolean isPrepared = false;

    private ImageView playerCover;
    private ImageButton playPauseButton;

    private SeekBar songProgress;

    private MediaPlayer mediaPlayer;
    private SongHolder songHolder;

    Handler handler = new Handler();
    Runnable updateSeekbar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int position = mediaPlayer.getCurrentPosition();
                songProgress.setProgress(position);
                currentTime.setText(formatTime(position));
                handler.postDelayed(this, 1000);
            }
        }
    };

    // Change this to your Django server IP and port.
    private static final String BASE_URL = "http://10.173.217.248:8000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songplayer);
        Intent intent = getIntent();
        String instanceKey = intent.getStringExtra("songHolderInstanceKey");
        songHolder = SongHolder.getInstance(instanceKey);
        setListeners();
        receiveSongDetailsFromIntent();
    }

    private void setListeners() {
        currentTime = findViewById(R.id.currentTime);
        artistName = findViewById(R.id.artistName);
        songTitle = findViewById(R.id.songTitle);
        totalDuration = findViewById(R.id.totalDuration);
        playerCover = findViewById(R.id.playerCover);

        playPauseButton = findViewById(R.id.playPauseButton);
        playPauseButton.setOnClickListener(view -> {
            if (mediaPlayer == null || !isPrepared) {
                return;
            }
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playPauseButton.setImageResource(
                        android.R.drawable.ic_media_play
                );
            } else {
                mediaPlayer.start();
                playPauseButton.setImageResource(
                        android.R.drawable.ic_media_pause
                );
                handler.removeCallbacks(updateSeekbar);
                handler.post(updateSeekbar);
            }
        });

        songProgress = findViewById(R.id.songProgress);
        songProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentTime.setText(formatTime(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateSeekbar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && isPrepared) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
                handler.post(updateSeekbar);
            }
        });
    }

    private void receiveSongDetailsFromIntent() {
        Intent intent = getIntent();
        long songId = intent.getLongExtra("songId", -1L);
        receiveSongDetails(songHolder.getSong(songId));
    }
    private void receiveSongDetails(Song song) {
        String songName = song.getSongName();
        String artWorkUrl = song.getArtWorkUrl();
        long dbSongId = song.getSongId();

        songTitle.setText(songName != null ? songName : "Unknown Song");
        artistName.setText("");

        if (artWorkUrl != null && !artWorkUrl.isEmpty()) {
            artWorkUrl = artWorkUrl.replace("100x100bb.jpg", "500x500bb.png");
            Glide.with(this)
                    .load(artWorkUrl)
                    .into(playerCover);
        }

        if (dbSongId == -1) {
            Toast.makeText(this, "Invalid song ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String streamUrl = BASE_URL + "/streamAudio/" + dbSongId;

        playSong(streamUrl);
    }

    private void playSong(String url) {
        mediaPlayer = null;
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(url));
            mediaPlayer.setOnPreparedListener(mp->{
                isPrepared = true;
                int duration = mp.getDuration();
                totalDuration.setText(formatTime(duration));
                songProgress.setMax(duration);

                mp.start();
                playPauseButton.setImageResource(
                        android.R.drawable.ic_media_pause
                );

                handler.post(updateSeekbar);
            });

            mediaPlayer.setOnCompletionListener(mp->{
                playPauseButton.setImageResource(
                        android.R.drawable.ic_media_play
                );
            });

            mediaPlayer.setOnErrorListener((mp, what, extra)->{
                isPrepared = false;
                Log.e(SongPlayer.class.getName(), "Media player error,"+what+" "+extra);
                Toast.makeText(this, "Error playing song.", Toast.LENGTH_SHORT).show();
                playPauseButton.setImageResource(
                        android.R.drawable.ic_media_play
                );
                return true;
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error playing song", Toast.LENGTH_SHORT).show();
            Log.e(SongPlayer.class.getName(), "Error playing song");
        }
        mediaPlayer.prepareAsync();
    }

    private String formatTime(int milliseconds) {

        int seconds = milliseconds / 1000;

        int minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format(
                Locale.getDefault(),
                "%02d:%02d",
                minutes,
                seconds
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}