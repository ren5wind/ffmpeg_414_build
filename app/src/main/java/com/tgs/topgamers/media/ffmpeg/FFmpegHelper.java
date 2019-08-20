package com.tgs.topgamers.media.ffmpeg;

import android.os.Environment;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Author      : renxiaoming
 * Date        : 2019-08-05
 */
public class FFmpegHelper {

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("ffmpeginvoke");
    }

    private static FFmpegHelper instance;
    private FFmpegExecuteAsyncTask mFFmpegExecuteAsyncTask;

    private FFmpegHelper() {
    }

    /**
     * 单例模式
     */
    public static FFmpegHelper getInstance() {
        if (instance == null) {
            synchronized (FFmpegHelper.class) {
                if (instance == null) {
                    instance = new FFmpegHelper();
                }
            }
        }
        return instance;
    }

    private final String TAG = "FFmpegHelper";

    public void videoMixAudioChannels(MixAudioFileData srcVideo, String desVideoPath, List<MixAudioFileData> dubbingAudioList,
                                      float dubbingVolume, MixAudioFileData backgroundAudio, OnFFmpegListener listener) {
        String dubbingFilePath = Environment.getExternalStorageDirectory().getPath() + "/ATopGame/dubbingAudio.wav";
        boolean isDubbingChannel = false;
        List<String[]> cmdList = new ArrayList<>();

        //合并配音文件
        if (dubbingAudioList != null && dubbingAudioList.size() > 0) {//有配音文件
//            ffmpeg -i "/Users/rxm/Desktop/test/1.wav" -i "/Users/rxm/Desktop/test/2.wav"
//            -filter_complex
//            "[0:a]adelay=2000|2000[audio0],[1:a]adelay=5000|5000[audio1],[audio0][audio1]amix,apad=pad_len=308700"
//            -y "/Users/rxm/Desktop/test/insert12_apad.wav"
            isDubbingChannel = true;
            //按startTime排序
            Collections.sort(dubbingAudioList, new Comparator<MixAudioFileData>() {
                @Override
                public int compare(MixAudioFileData o1, MixAudioFileData o2) {
                    if (o1.getStartTimeMs() < o2.getStartTimeMs()) {
                        return -1;
                    } else if (o1.getStartTimeMs() == o2.getStartTimeMs()) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            });

            //读取视频源的时长,计算音轨需要补充的帧数
            long durationMs = srcVideo.getDurationMs();
            MixAudioFileData lastDubbing = dubbingAudioList.get(dubbingAudioList.size() - 1);
            long offDurationMs = durationMs - lastDubbing.getStartTimeMs() - lastDubbing.getDurationMs();
            long frameCount = 44100 * offDurationMs / 1000;
            int size = dubbingAudioList.size();

            ArrayList<String> cmds = new ArrayList<>();
            cmds.add("ffmpeg");
            for (int i = 0; i < size; i++) {
                cmds.add("-i");
                cmds.add(dubbingAudioList.get(i).getFilePath());
            }
            cmds.add("-filter_complex");
            String amix = "";
            for (int i = 0; i < size; i++) {
                long startTime = dubbingAudioList.get(i).getStartTimeMs();
                amix += "[" + i + ":a]adelay=" + startTime + "|" + startTime + ",volume=2.0[audio" + i + "],";
            }
            for (int i = 0; i < size; i++) {
//                [audio0][audio1]
                amix += "[audio" + i + "]";
            }
            amix += "amix=inputs="+size+",apad=pad_len=" + frameCount;
//            amix += "\'";
            cmds.add(amix);
            cmds.add("-y");
            cmds.add(dubbingFilePath);

            String[] commands = cmds.toArray(new String[cmds.size()]);
            String test = "";
            for (int i = 0; i < commands.length; i++) {
                test += commands[i] + " ";
            }
            Log.i("VideoMixAudio", test);

//            int ret = run(commands);
//            Log.i("VideoMixAudio", "ret = " + ret);
            cmdList.add(commands);

        }

        ArrayList<String> cmds = new ArrayList<>();

        cmds.add("ffmpeg");
        cmds.add("-i");
        cmds.add(srcVideo.getFilePath());
        if (isDubbingChannel) {
            cmds.add("-i");
            cmds.add(dubbingFilePath);
        }
        if (backgroundAudio != null) {
            cmds.add("-i");
            cmds.add(backgroundAudio.getFilePath());
        }
        cmds.add("-filter_complex");
        String complex = "";
        //源文件声音滤镜
        complex += "[0:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,volume=" + srcVideo.getVolume() + "[a0];";
        //配音声音滤镜
        if (isDubbingChannel) {
            complex += "[1:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,volume=" + dubbingVolume * 2.0f + "[a1];";
        }
        //背景音乐声音滤镜
        if (backgroundAudio != null) {
            int channel;
            if (!isDubbingChannel) {
                channel = 1;
            } else {
                channel = 2;
            }
            complex += "[" + channel + ":a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,volume=" + backgroundAudio.getVolume() + "[a" + channel + "];";
        }

        int inputs = 0;
        complex += "[a0]";
        inputs++;
        if (isDubbingChannel) {
            complex += "[a1]";
            inputs++;
        }
        if (backgroundAudio != null) {
            if (!isDubbingChannel) {
                complex += "[a1]";
            } else {
                complex += "[a2]";
            }
            inputs++;
        }
//        amerge=inputs=2
//        complex += "amix=inputs=" + inputs + ":duration=first:dropout_transition=2";
        complex += "amerge=inputs=" + inputs;
        cmds.add(complex);
        cmds.add("-acodec");
        cmds.add("aac");
        cmds.add("-b:a");
        cmds.add("128k");
        cmds.add("-ar");
        cmds.add("44100");
        cmds.add("-vcodec");
        cmds.add("copy");
        cmds.add("-y");
        cmds.add(desVideoPath);

        String[] commands = cmds.toArray(new String[cmds.size()]);
        String test = "";
        for (int i = 0; i < commands.length; i++) {
            test += commands[i] + " ";
        }
        cmdList.add(commands);

        Log.i("VideoMixAudio", test);

//        int ret = run(commands);
//        Log.i("VideoMixAudio", "ret = " + ret);

//        ffmpeg -i "/Users/rxm/Desktop/test/test.mp4" -i "/Users/rxm/Desktop/test/insert12_apad.wav" -i "/Users/rxm/Desktop/test/12234.mp3"
//        -filter_complex
//        "[0:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,volume=1.0[a0];
//        [1:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,volume=2.0[a1];
//        [2:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,volume=0.5[a2];
//        [a0][a1][a2]amix=inputs=3:duration=first:dropout_transition=2"
//        -acodec aac -ab 64k -ar 44100 -vcodec copy
//        -y "/Users/rxm/Desktop/test/insert_amix.mp4"

        mFFmpegExecuteAsyncTask = new FFmpegExecuteAsyncTask(cmdList,listener);
        mFFmpegExecuteAsyncTask.execute();
    }

    public native static int run(String[] commands);
}