package com.example.musicapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private ArrayList<String> songs = new ArrayList<>();
    private ArrayList<String> songNames = new ArrayList<>();
    private ArrayList<String> songAuthors = new ArrayList<>();
    private ArrayList<Drawable> songLogoCircles = new ArrayList<>();

    private ArrayList<Drawable> songLogos = new ArrayList<>();
    private ArrayList<Drawable> backgrounds = new ArrayList<>();

    private ArrayList<Typeface> fonts = new ArrayList<>();
    private int songIndex = 0;
    private MediaPlayer player = null;
    private TextView tvSongName;
    private TextView tvAuthorName;
    private TextView tvTimeCurrent;
    private TextView tvDuration;
    private final Random rand = new Random();
    private ConstraintLayout layoutMusic;
    private ImageView ivLogo;
    private Animation anim;
    private Drawable backgroundImage;
    private Drawable playIcon;
    private Drawable pauseIcon;
    private Drawable shuffleIcon;
    private Drawable shuffleActiveIcon;
    private Drawable syncIcon;
    private Drawable syncActiveIcon;
    private Drawable heartIcon;
    private Drawable heartActiveIcon;
    private boolean isShuffle = false;
    private boolean isSync = false;
    private List<Boolean> isHearts = new ArrayList<Boolean>();
    private Button btnShuffle;
    private Button btnPrev;
    private Button btnPlayPause;
    private Button btnNext;
    private Button btnSync;
    private Button btnHeart;
    private Button btnPlayList;

    private int duration;
    private String durationText;
    private int minute;
    private int second;

    private SeekBar sbTimeLine;
    private boolean wasAtMax = false;
    private int loopCount = 0;

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

    private void readFontFile(AssetManager assetManager, String fileName, ArrayList<Typeface> array){
        try {
            InputStream fileIS = assetManager.open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileIS));
            String line = "";
            while ((line = bufferedReader.readLine()) != null){
                Typeface font = Typeface.createFromAsset(assetManager, "fonts/" + line);
                array.add(font);
            }
        }
        catch (Exception e){

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AssetManager assetManager = getAssets();

        readFontFile(assetManager, "fonts/fontPath.txt", fonts);
        readFile(assetManager,"musics/song_names.txt", songNames);
        readFile(assetManager,"musics/song_authors.txt", songAuthors);

        try {
            for(String songName : songNames){
                String songPath = "musics/songs/" + songName + ".mp3";
                songs.add(songPath);

                InputStream songLogoIS = assetManager.open("musics/song_logos/" + songName + " Logo.jpg");
                Drawable songLogo = Drawable.createFromStream(songLogoIS, null);
                songLogos.add(songLogo);

                InputStream songLogoCircleIS = assetManager.open("musics/song_logo_circles/" + songName + " Logo Circle.png");
                Drawable songLogoCircle = Drawable.createFromStream(songLogoCircleIS, null);
                songLogoCircles.add(songLogoCircle);

                InputStream backgroundIS = assetManager.open("musics/backgrounds/" + songName + " BG.jpg");
                Drawable background = Drawable.createFromStream(backgroundIS, null);
                backgrounds.add(background);
            }
        }
        catch (Exception e){}

        for(int i = 0; i < songs.size(); i++){
            isHearts.add(false);
        }

        btnShuffle = findViewById(R.id.btnShuffle);
        btnPrev = findViewById(R.id.btnPrev);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnSync = findViewById(R.id.btnSync);
        btnHeart = findViewById(R.id.btnHeart);
        btnPlayList = findViewById(R.id.btnPlayList);

        try {
            player = new MediaPlayer();
            AssetFileDescriptor afd = assetManager.openFd(songs.get(songIndex));
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            player.prepare();
        }catch (Exception e){}

        backgroundImage = backgrounds.get(songIndex);
        layoutMusic = findViewById(R.id.layoutMusic);
        layoutMusic.setBackground(backgroundImage);

        ivLogo = findViewById(R.id.ivLogo);
        ivLogo.setImageDrawable(songLogoCircles.get(songIndex));

        tvSongName = findViewById(R.id.tvSongName);
        tvSongName.setText(songNames.get(songIndex));
        tvSongName.setTypeface(fonts.get(songIndex));

        tvAuthorName = findViewById(R.id.tvAuthorName);
        tvAuthorName.setText(songAuthors.get(songIndex));
        tvAuthorName.setTypeface(fonts.get(songIndex));

        tvTimeCurrent = findViewById(R.id.tvTimeCurrent);

        duration = player.getDuration()/1000;
        minute = duration/60;
        second = duration%60;

        durationText = "" + minute + ':' + formatNumberString(second);
        tvDuration = findViewById(R.id.tvDuration);
        tvDuration.setText(durationText);

        sbTimeLine = findViewById(R.id.sbTimeLine);
        sbTimeLine.setMax(duration * 1000);

        anim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        playIcon = getResources().getDrawable(R.drawable.play_icon);
        pauseIcon = getResources().getDrawable(R.drawable.pause_icon);
        shuffleIcon = getResources().getDrawable(R.drawable.shuffle_icon);
        shuffleActiveIcon = getResources().getDrawable(R.drawable.shuffle_active_icon);
        syncIcon = getResources().getDrawable(R.drawable.sync_icon);
        syncActiveIcon = getResources().getDrawable(R.drawable.sync_active_icon);
        heartIcon = getResources().getDrawable(R.drawable.heart_icon);
        heartActiveIcon = getResources().getDrawable(R.drawable.heart_active_icon);

        playMusic();

        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shuffleClick();
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevClick();
            }
        });

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPauseClick();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextClick();
            }
        });

        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncClick();
            }
        });

        btnHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heartClick();
            }
        });

        btnPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playListClick();
            }
        });

        // Thiết lập lắng nghe sự kiện cho SeekBar
        sbTimeLine.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // Nếu sự kiện là do người dùng kéo SeekBar, thay đổi vị trí của MediaPlayer
                    player.seekTo(progress);
                }

                if (progress == seekBar.getMax() && !wasAtMax) {
                    wasAtMax = true; // Đánh dấu rằng đã đạt đến max
                    if (isShuffle) {
                        songIndex = rand.nextInt(songs.size());
                    } else if (!isSync) {
                        if (songIndex == songs.size() - 1) {
                            songIndex = 0;
                        } else {
                            songIndex++;
                        }
                    }

                    changeSong();
                } else {
                    // Nếu progress không còn ở max nữa, reset lại biến wasAtMax
                    wasAtMax = false;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Không cần thực hiện gì trong trường hợp này
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Không cần thực hiện gì trong trường hợp này
            }
        });

        // Cập nhật vị trí của SeekBar bằng vị trí hiện tại của MediaPlayer trong một vòng lặp hoặc sự kiện cập nhật
        // (ví dụ: sau mỗi khoảng thời gian)
        Handler handler = new Handler();
        Runnable updateSeekBar = new Runnable() {
            @Override
            public void run() {
                int currentPosition = player.getCurrentPosition();
                sbTimeLine.setProgress(currentPosition);
                handler.postDelayed(this, 1000); // Cập nhật SeekBar mỗi giây (hoặc khoảng thời gian khác)

                minute = (currentPosition/1000)/60;
                second = (currentPosition/1000)%60;
                tvTimeCurrent.setText("" + minute + ':' + formatNumberString(second));
            }
        };
        handler.postDelayed(updateSeekBar, 0); // Bắt đầu cập nhật SeekBar
    }

    @Override
    protected void onPause() {
        super.onPause();

        pauseMusic();
    }

    @Override
    protected void onStop() {
        super.onStop();

        pauseMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isShuffle){
            btnShuffle.setCompoundDrawablesWithIntrinsicBounds(shuffleActiveIcon, null, null, null);
        }else if(isSync){
            btnSync.setCompoundDrawablesWithIntrinsicBounds(syncActiveIcon, null, null, null);
        }

        playMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        player.stop();
        player.release();
    }

    private String formatNumberString(int number){
        String res = "";
        if(number < 10){
            res += "0";
        }
        res += number;
        return res;
    }

    private void changeSong(){
        updateHeart();
        player.stop();
        player.reset();
        player.release();
        player = null;
        ivLogo.clearAnimation();
        try {
            player = new MediaPlayer();
            AssetFileDescriptor afd = getAssets().openFd(songs.get(songIndex));
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            player.prepare();
            sbTimeLine.setMax(player.getDuration());

            duration = player.getDuration()/1000;
            minute = duration/60;
            second = duration%60;

            durationText = "" + minute + ':' + formatNumberString(second);
            tvDuration.setText(durationText);
        }catch (Exception e){}
        tvSongName.setText(songNames.get(songIndex));
        tvSongName.setTypeface(fonts.get(songIndex));

        tvAuthorName.setText(songAuthors.get(songIndex));
        tvAuthorName.setTypeface(fonts.get(songIndex));

        backgroundImage = backgrounds.get(songIndex);
        layoutMusic.setBackground(backgroundImage);
        ivLogo.setImageDrawable(songLogoCircles.get(songIndex));

        playMusic();
    }

    private void updateHeart(){
        if(isHearts.get(songIndex)){
            btnHeart.setCompoundDrawablesWithIntrinsicBounds(heartActiveIcon, null, null, null);
        }else {
            btnHeart.setCompoundDrawablesWithIntrinsicBounds(heartIcon, null, null, null);
        }
    }

    private void shuffleClick(){
        isShuffle = !isShuffle;
        if(isShuffle){
            btnShuffle.setCompoundDrawablesWithIntrinsicBounds(shuffleActiveIcon, null, null, null);
        }else {
            btnShuffle.setCompoundDrawablesWithIntrinsicBounds(shuffleIcon, null, null, null);
        }

        isSync = false;
        btnSync.setCompoundDrawablesWithIntrinsicBounds(syncIcon, null, null, null);
    }

    private void prevClick(){
        if(songIndex == 0){
            songIndex = songs.size() - 1;
        }else {
            songIndex --;
        }

        changeSong();
    }

    private void playMusic(){
        player.start();
        btnPlayPause.setCompoundDrawablesWithIntrinsicBounds(pauseIcon,
                null, null, null);
        ivLogo.startAnimation(anim);
    }

    private void pauseMusic(){
        player.pause();
        btnPlayPause.setCompoundDrawablesWithIntrinsicBounds(playIcon,
                null, null, null);
        ivLogo.clearAnimation();
    }

    private void playPauseClick(){
        if(player.isPlaying()){
            pauseMusic();
        }else {
            playMusic();
        }
    }

    private void nextClick(){
        if(songIndex == songs.size() - 1){
            songIndex = 0;
        }else {
            songIndex ++;
        }

        changeSong();
    }

    private void syncClick(){
        isSync = !isSync;
        if(isSync){
            btnSync.setCompoundDrawablesWithIntrinsicBounds(syncActiveIcon, null, null, null);
        }else {
            btnSync.setCompoundDrawablesWithIntrinsicBounds(syncIcon, null, null, null);
        }
        isShuffle = false;
        btnShuffle.setCompoundDrawablesWithIntrinsicBounds(shuffleIcon, null, null, null);
    }

    private void heartClick(){
        isHearts.set(songIndex, !isHearts.get(songIndex));
        updateHeart();
    }

    private void playListClick(){
        Intent iMusicList = new Intent(this, MusicListActivity.class);
        iMusicList.putExtra("songIndex", songIndex);
        startActivityForResult(iMusicList, 1234);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1234 && resultCode == Activity.RESULT_OK){
            songIndex = data.getIntExtra("songIndex", 0);
            changeSong();
        }
    }
}