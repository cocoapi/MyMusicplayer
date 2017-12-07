package com.example.eksrn_000.myapplication;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;


public class musicplaying extends Activity {
    private static MediaPlayer mp;
    private int index;
    private boolean isPlaying;
    private Flag playingFlag;
    private BroadcastReceiver br;
    private AudioManager manager;
    Thread t;
    public enum Flag{
        NULL(0),
        REPEAT(1),
        REPEAT_ALL(2),
        RANDOM(4);

        private int value;

        private Flag(int value) {
            this.value = value;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.musicplaying);
        final TextView MusicTitle = (TextView) findViewById(R.id.playing_title);
        final TextView MusicArtist = (TextView) findViewById(R.id.playing_artist);
        final TextView MusicAlbum = (TextView) findViewById(R.id.playing_album);
        Intent intent = getIntent();
        final Music nowPlay = (Music) intent.getSerializableExtra("Music");
        index = (int) intent.getSerializableExtra("int");
        MusicTitle.setText(nowPlay.getTitle());
        MusicTitle.setSelected(true);
        MusicArtist.setText(nowPlay.getArtist());
        MusicArtist.setSelected(true);
        MusicAlbum.setText(nowPlay.getAlbum());
        MusicAlbum.setSelected(true);
        final ImageView MusicThumb = findViewById(R.id.thumbnail);
        File f = new File(nowPlay.getSrc());
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        byte[] rawArt;
        Bitmap art;
        BitmapFactory.Options bfo=new BitmapFactory.Options();

        mmr.setDataSource(getApplicationContext(), Uri.fromFile(f));
        rawArt = mmr.getEmbeddedPicture();
        bfo.outWidth = 300;
        bfo.outHeight = 300;
        bfo.inSampleSize = 2;
        // if rawArt is null then no cover art is embedded in the file or is not
        // recognized as such.
        if (null != rawArt) {
            art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length, bfo);
            MusicThumb.setImageBitmap(art);
        }else{
            MusicThumb.setBackgroundColor(Color.WHITE);
            MusicThumb.setImageResource(R.drawable.vinyl_yellow_512);
        }
        final SeekBar seekMusic = findViewById(R.id.seekBar2);
        final TextView Time = findViewById(R.id.progressTime);
        seekMusic.setMax((int)nowPlay.getDuration());
        seekMusic.setProgress(0);
        isPlaying = true;
        final Thread t = new Thread(){
            @Override
            public void run(){
                while(isPlaying){
                    seekMusic.setProgress(mp.getCurrentPosition());
                }
            }
        };
        seekMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int m = i / 60000;
                int s = (i % 60000) / 1000;
                String strTime = String.format("%02d:%02d", m, s);
                Time.setText(strTime);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mp.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(seekBar.getProgress());
                mp.start();
                isPlaying = true;
            }
        });
        final ImageButton PlayPause = findViewById(R.id.play);
        ImageButton Next = findViewById(R.id.next);
        ImageButton Prev = findViewById(R.id.prev);

        PlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Play music
                AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if(manager.isMusicActive()) {
                    PlayPause.setImageResource(R.drawable.play);
                    mp.pause();
                    isPlaying = false;
                } else {
                    PlayPause.setImageResource(R.drawable.pause);
                    mp.start();
                    isPlaying = true;
                }
            }
        });

        Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Next Music
                isPlaying = false;
                mp.pause();
                Intent nextIntent = new Intent("MUSIC.turn");
                nextIntent.putExtra("Index", ++index);
                sendBroadcast(nextIntent);
                isPlaying = true;
            }
        });

        Prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Prev Music
                mp.pause();
                Intent prevIntent;
                if(seekMusic.getProgress() > 3000){
                    seekMusic.setProgress(0);
                    mp.seekTo(0);
                    mp.start();
                }else{
                    prevIntent = new Intent("MUSIC.turn");
                    prevIntent.putExtra("Index", --index);
                    sendBroadcast(prevIntent);
                }
            }
        });
        IntentFilter intentFilter = new IntentFilter("PLAYING.Change");
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Music nMusic = (Music) intent.getSerializableExtra("Music");
                MusicTitle.setText(nMusic.getTitle());
                MusicAlbum.setText(nMusic.getAlbum());
                MusicArtist.setText(nMusic.getArtist());
                seekMusic.setMax((int) nMusic.getDuration());
                seekMusic.setProgress(0);
                File f = new File(nMusic.getSrc());
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                byte[] rawArt;
                Bitmap art;
                BitmapFactory.Options bfo = new BitmapFactory.Options();
                mmr.setDataSource(getApplicationContext(), Uri.fromFile(f));
                rawArt = mmr.getEmbeddedPicture();
                bfo.outWidth = 300;
                bfo.outHeight = 300;
                bfo.inSampleSize = 2;
                // if rawArt is null then no cover art is embedded in the file or is not
                // recognized as such.
                if (null != rawArt) {
                    art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length, bfo);
                    MusicThumb.setImageBitmap(art);
                }else{
                    MusicThumb.setBackgroundColor(Color.WHITE);
                    MusicThumb.setImageResource(R.drawable.vinyl_yellow_512);
                }
                isPlaying = true;
            }
        };
        registerReceiver(br, intentFilter);
        t.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(br);
    }
    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        super.unregisterReceiver(receiver);
    }

    public static class MusicService extends Service {
        private AudioManager manager;
        private String play;
        BroadcastReceiver br;
        private Flag flag;

        public MusicService(){
            super();
        };

        @Override
        public IBinder onBind(Intent intent){
            return null;
        }

        @Override
        public void onDestroy(){
            mp.stop();
            super.onDestroy();
            flag = flag.NULL;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId){
            Music newPlay = (Music) (intent.getSerializableExtra("Music"));
            if(play == null) {
                play = newPlay.getSrc();
            }else{
                if(play == newPlay.getSrc()){
                    return Service.START_NOT_STICKY;
                }else{
                    play = newPlay.getSrc();
                }
            }
            if(mp == null) {
                mp = new MediaPlayer();
            }else {
                mp.reset();
            }
            try {
                mp.setDataSource(play);
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mp.start();

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("MUSIC.Change");

            br = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if(action.equals("MUSIC.Change")){
                        mp.reset();
                        try {
                            mp.setDataSource(intent.getStringExtra("src"));
                            mp.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mp.start();
                    }
                }
            };

            registerReceiver(br, intentFilter);
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public void unregisterReceiver(BroadcastReceiver receiver) {
            super.unregisterReceiver(receiver);
        }
    }
}
