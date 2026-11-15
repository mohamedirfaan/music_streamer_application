package com.example.music_streamer.util;

public class Song {
    long songId;
    String artWorkUrl, songName;

    Song nextSong;
    public Song(long id, String url, String name) {
        songId = id;
        artWorkUrl = url;
        songName = name;
    }

    public long getSongId() {
        return songId;
    }

    public String getSongName() {
        return songName;
    }

    public String getArtWorkUrl() {
        return artWorkUrl;
    }

    public void setNextSong(Song song) {
        nextSong = song;
    }
}
