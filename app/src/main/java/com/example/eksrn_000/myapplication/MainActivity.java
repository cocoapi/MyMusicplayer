package com.example.eksrn_000.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.*;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends Activity {
    private MainActivity main;
    private MusicList List;
    private MediaPlayer mp;
    private AudioManager manager;
    private String nowPlay;
    private BroadcastReceiver br;

    public MainActivity(){
        main = this;
        mp = new MediaPlayer();
        nowPlay = new String();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        List = new MusicList();
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        ListView songView;
        android.util.Log.i("서비스 테스트", "onCreate()");
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        if(List.setList(getApplicationContext()) == false){
            songView = (ListView)findViewById(R.id.songlist);
            TextView tError = new TextView(getApplicationContext());
            tError.setText("NO MUSIC in device");
            songView.addHeaderView(tError);
        }

        songView = (ListView)findViewById(R.id.songlist);
        Collections.sort(List.getList(), new Comparator<Music>(){
            public int compare(Music a, Music b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        songView.setAdapter(List.getAdapter());
        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(),
                        musicplaying.class);
                intent.putExtra("Music", (Serializable) List.getList().get(i));
                intent.putExtra("int", i);
                Intent ServIntent = new Intent(getApplicationContext(), musicplaying.MusicService.class);
                if(nowPlay.isEmpty()){
                    nowPlay = String.copyValueOf(List.getList().get(i).getSrc().toCharArray());
                    ServIntent.putExtra("Music", (Serializable) List.getList().get(i));
                    if(manager == null){
                        manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    }
                    startService(ServIntent);
                }else{
                    if(!nowPlay.equals(List.getList().get(i).getSrc())){
                        Intent musicIntent = new Intent("MUSIC.Change");
                        musicIntent.putExtra("src", List.getList().get(i).getSrc());
                        sendBroadcast(musicIntent);
                    }
                }
                startActivity(intent);
            }
        });
        final TabHost mainHost = (TabHost) findViewById(R.id.tabhost);
        mainHost.setup();

        TabHost.TabSpec tabSpecMusic = mainHost.newTabSpec("MUSIC").setIndicator("음악별");
        tabSpecMusic.setContent(R.id.tab1);
        TabHost.TabSpec tabSpecArtist = mainHost.newTabSpec("ARTIST").setIndicator("가수별");
        tabSpecArtist.setContent(R.id.tab2);
        TabHost.TabSpec tabSpecAlbum = mainHost.newTabSpec("ALBUM").setIndicator("앨범별");
        tabSpecAlbum.setContent(R.id.tab3);

        mainHost.addTab(tabSpecMusic);
        mainHost.addTab(tabSpecArtist);
        mainHost.addTab(tabSpecAlbum);

        mainHost.setCurrentTab(0);

        IntentFilter intentFilter = new IntentFilter("MUSIC.turn");
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int moveTo = intent.getIntExtra("Index", 0);
                Intent changeIntent = new Intent("MUSIC.Change");
                changeIntent.putExtra("src", List.getList().get(moveTo).getSrc());
                Intent actIntent = new Intent("PLAYING.Change");
                actIntent.putExtra("Music", (Serializable) List.getList().get(moveTo));
                sendBroadcast(actIntent);
                sendBroadcast(changeIntent);
            }
        };
        registerReceiver(br, intentFilter);
    }

    public class MusicList {
        private ArrayList<Music> songList;
        private SongAdapter songadt;
        public MusicList(){
            songList = new ArrayList<Music>();
        }

        public boolean setList(Context cont) {
            ContentResolver musicResolver = cont.getContentResolver();
            if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
                if(musicCursor!= null && musicCursor.moveToFirst()){
                    //get columns
                    int dataColumn = musicCursor.getColumnIndex
                            (MediaStore.Audio.Media.DATA);
                    int titleColumn = musicCursor.getColumnIndex
                            (android.provider.MediaStore.Audio.Media.TITLE);
                    int idColumn = musicCursor.getColumnIndex
                            (android.provider.MediaStore.Audio.Media._ID);
                    int artistColumn = musicCursor.getColumnIndex
                            (android.provider.MediaStore.Audio.Media.ARTIST);
                    int albumColumn = musicCursor.getColumnIndex
                            (MediaStore.Audio.Media.ALBUM);
                    int durationColumn = musicCursor.getColumnIndex
                            (MediaStore.Audio.Media.DURATION);
                    //add songs to list
                    do {
                        long thisId = musicCursor.getLong(idColumn);
                        String thisData = musicCursor.getString(dataColumn);
                        String thisTitle = musicCursor.getString(titleColumn);
                        String thisArtist = musicCursor.getString(artistColumn);
                        String thisAlbum = musicCursor.getString(albumColumn);
                        long thisDuration = musicCursor.getLong(durationColumn);
                        songList.add(new Music(thisData, thisId, thisTitle, thisArtist, thisAlbum, thisDuration));
                    }
                    while (musicCursor.moveToNext());
                }
            }
            songadt = new SongAdapter(cont);
            if(songList.isEmpty()) {
                return false;
            }else {
                return true;
            }
        }

        public ArrayList<Music> getList(){
            return songList;
        }

        public class SongAdapter extends BaseAdapter {
            private LayoutInflater songInf;

            private SongAdapter(Context c){
                songInf= LayoutInflater.from(c);
            }

            @Override
            public int getCount() {
                return songList.size();
            }

            @Override
            public Object getItem(int arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public long getItemId(int arg0) {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                //map to song layout
                LinearLayout songLay = (LinearLayout)songInf.inflate
                        (R.layout.song, parent, false);
                //get title and artist views
                TextView songView = (TextView)songLay.findViewById(R.id.song_title);
                TextView artistView = (TextView)songLay.findViewById(R.id.song_artist);
                //get song using position
                Music currSong = songList.get(position);
                //get title and artist strings
                songView.setText(currSong.getTitle());
                artistView.setText(currSong.getArtist());
                //set position as tag
                songLay.setTag(position);
                return songLay;
            }
        }

        public SongAdapter getAdapter(){
            return songadt;
        }
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
}
