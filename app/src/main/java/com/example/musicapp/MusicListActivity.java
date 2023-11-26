package com.example.musicapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MusicListActivity extends AppCompatActivity {
    private ListView lvMusicList;
    ArrayList<Music> musics = new ArrayList<>();

    private int songIndex = 0;

    class Music {
        private Drawable logo;
        private String name;
        private String author;
        private String duration;

        public Music(Drawable logo, String name, String author, String duration){
            this.logo = logo;
            this.name = name;
            this.author = author;
            this.duration = duration;
        }
    }

    class MusicAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return musics.size();
        }

        @Override
        public Object getItem(int i) {
            return musics.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = LayoutInflater.from(getBaseContext());
            View itemView = inflater.inflate(R.layout.music_item, null);
            Music musicItem = musics.get(i);

            if(songIndex == i){
                GradientDrawable shape = new GradientDrawable();
                shape.setShape(GradientDrawable.RECTANGLE);
                shape.setColor(Color.parseColor("#40FFFFFF"));
                shape.setCornerRadius(40);

                itemView.setBackground(shape);
            }

            ImageView ivLogo = itemView.findViewById(R.id.ivLogo);
            ivLogo.setImageDrawable(musicItem.logo);

            TextView tvName = itemView.findViewById(R.id.tvName);
            tvName.setText(musicItem.name);

            TextView tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvAuthor.setText(musicItem.author);

            TextView tvDuration = itemView.findViewById(R.id.tvDuration);
            tvDuration.setText(musicItem.duration);

            return itemView;
        }
    }

    private void readFile(AssetManager assetManager, String fileName, ArrayList<String> array){
        try {
            InputStream fileIS = assetManager.open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileIS));
            String line = "";
            while ((line = bufferedReader.readLine()) != null){
                array.add(line);
            }
        }
        catch (Exception e){

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        Intent iMusicData = getIntent();
        songIndex = iMusicData.getIntExtra("songIndex", 0);

        ArrayList<String> songs = new ArrayList<>();
        ArrayList<Drawable> songLogos = new ArrayList<>();
        ArrayList<String> songNames = new ArrayList<>();
        ArrayList<String> songAuthors = new ArrayList<>();
        AssetManager assetManager = getAssets();

        readFile(assetManager,"musics/song_names.txt", songNames);
        readFile(assetManager,"musics/song_authors.txt", songAuthors);

        try {
            for(String songName : songNames){
                String songPath = "musics/songs/" + songName + ".mp3";
                songs.add(songPath);

                InputStream songLogoIS = assetManager.open("musics/song_logos/" + songName + " Logo.jpg");
                Drawable songLogo = Drawable.createFromStream(songLogoIS, null);
                songLogos.add(songLogo);
            }
        }
        catch (Exception e){}

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(getResources().getString(R.string.music_list_title) + " (" + songs.size() + ")");

        lvMusicList = findViewById(R.id.lvMusicList);
        int duration;
        MediaPlayer player = null;

        for(int i = 0; i < songs.size(); i++){
            try {
                player = new MediaPlayer();
                AssetFileDescriptor afd = assetManager.openFd(songs.get(i));
                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();

                player.prepare();
                duration = player.getDuration();
                musics.add(new Music(songLogos.get(i), songNames.get(i), songAuthors.get(i), formatDuration(duration)));
            }catch (Exception e){}
        }

        MusicAdapter adapter = new MusicAdapter();
        lvMusicList.setAdapter(adapter);

        lvMusicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                musicItemClick(i);
            }
        });

        Button btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeClick();
            }
        });
    }

    private String formatDuration(int duration){
        String durationStr = "";
        duration /= 1000;
        int minute = duration / 60;
        int second = duration % 60;

        if(minute < 10){
            durationStr += "0";
        }
        durationStr += minute + ":";

        if(second < 10){
            durationStr += "0";
        }
        durationStr += second;

        return durationStr;
    }

    private void musicItemClick(int songIndex){
        Intent iMusic = new Intent();
        iMusic.putExtra("songIndex", songIndex);
        setResult(Activity.RESULT_OK, iMusic);
        finish();
    };

    private void closeClick(){
        finish();
    };
}