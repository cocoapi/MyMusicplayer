package com.example.eksrn_000.myapplication;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by eksrn_000 on 2017-12-06.
 */

public class Music implements Serializable {
    private long Id, duration;
    private String[] song = new String[4];
    public Music(String src, long songID, String songTitle, String songArtist, String songAlbum, long songDuration) {
        Id=songID;
        song[0] = src;
        song[1] = songTitle;
        song[2] = songArtist;
        song[3] = songAlbum;
        duration = songDuration;
    }

    public long getId(){
        return Id;
    }

    public long getDuration(){
        return duration;
    }

    public String getSrc(){
        return song[0];
    }

    public String getTitle(){
        return song[1];
    }

    public String getArtist(){
        return song[2];
    }

    public String getAlbum(){
        return song[3];
    }
}
