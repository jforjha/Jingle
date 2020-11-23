package com.example.jingle;

import android.net.Uri;

public class MusicFile {
    private String path;
    private String title;
    private String Artist;
    private String Album;
    private String Duration;
    private String id;

    public MusicFile(String path, String title, String artist, String album, String duration,String id) {
        this.path = path;
        this.title = title;
        Artist = artist;
        Album = album;
        Duration = duration;
        this.id=id;
    }

    public MusicFile(String path) {
        this.path = path;
    }

    public Uri getPath() {
        return Uri.parse(path);
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return Artist;
    }

    public void setArtist(String artist) {
        Artist = artist;
    }

    public String getAlbum() {
        return Album;
    }

    public void setAlbum(String album) {
        Album = album;
    }

    public String getDuration() {
        return Duration;
    }

    public void setDuration(String duration) {
        Duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
