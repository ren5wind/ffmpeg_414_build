package com.tgs.topgamers.media.ffmpeg;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.tgs.topgamers.media.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        run();
    }

    private void test() {
        MixAudioFileData src = new MixAudioFileData();
        MixAudioFileData backgroundAudio = new MixAudioFileData();
        String desPath = Environment.getExternalStorageDirectory().getPath() + "/final1.mp4";
        List<MixAudioFileData> dubbingList = new ArrayList<>();
        src.setFilePath(Environment.getExternalStorageDirectory().getPath() + "/test1.mp4");
        src.setVolume(1.0f);
        MixAudioFileData dubbing1 = new MixAudioFileData();
        dubbing1.setFilePath(Environment.getExternalStorageDirectory().getPath() + "/123.wav");
        dubbing1.setVolume(1.0f);
        dubbing1.setStartTimeMs(2000);
        dubbingList.add(dubbing1);
        MixAudioFileData dubbing2 = new MixAudioFileData();
        dubbing2.setFilePath(Environment.getExternalStorageDirectory().getPath() + "/456.wav");
        dubbing2.setVolume(1.0f);
        dubbing2.setStartTimeMs(8000);
        dubbingList.add(dubbing2);

        MixAudioFileData dubbing3 = new MixAudioFileData();
        dubbing3.setFilePath(Environment.getExternalStorageDirectory().getPath() + "/789.wav");
        dubbing3.setVolume(1.0f);
        dubbing3.setStartTimeMs(15000);
        dubbingList.add(dubbing3);

        backgroundAudio.setFilePath(Environment.getExternalStorageDirectory().getPath() + "/16249.mp3");
        backgroundAudio.setVolume(1.0f);


        FFmpegHelper.getInstance().videoMixAudioChannels(src, desPath, dubbingList, 1.0f, backgroundAudio, new OnFFmpegListener() {
            @Override
            public void onStart() {
                Log.i("MainActivity","onStart");
            }

            @Override
            public void onSuccess() {
                Log.i("MainActivity","onSuccess");
            }

            @Override
            public void onFail(int ret) {

            }
        });
    }

    public void run() {
        String base = Environment.getExternalStorageDirectory().getPath();
        FFmpegHelper.getInstance().extractVideo(base + "/test1.mp4", base + "/FFmpegTest.mp4", new OnFFmpegListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onFail(int ret) {

            }
        });
    }
}
