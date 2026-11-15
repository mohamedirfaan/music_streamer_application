package com.example.music_streamer.util;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;

public class SongHolder {
    private final Map<Long, Song> songs = new HashMap<>();
    private static final Map<String, SongHolder> instance = new HashMap<>();
    long counter = 0;
    long currSong;
    private SongHolder() {}
    boolean isShuffleEnabled;

    public static SongHolder getInstance(String key) {
        if (instance.get(key) == null) {
            instance.put(key, new SongHolder());
        }
        return instance.get(key);
    }

    public void addSong(Song song) {
        songs.put(counter++, song);
    }

    public Song getSong(long songId) {
        this.currSong = songId;
        return songs.get(songId);
    }

    private Song getRandomSong() {
        Long randomKey = songs.keySet().stream().skip(new Random().nextInt(songs.size())).findFirst().orElse(null);
        return songs.get(randomKey);
    }
    public Song getNextSong() {
        if (isShuffleEnabled) {
            return getRandomSong();
        }
        if (currSong == songs.size()) {
            currSong = 0;
        }
        return songs.get(currSong++);
    }

    public Song getPreviousSong() {
        if (isShuffleEnabled) {
            return getRandomSong();
        }
        if (currSong <= 0) {
            currSong = songs.size() - 1;
        }
        return songs.get(--currSong);
    }

    public long getCurrentId() {
        return counter;
    }

}
