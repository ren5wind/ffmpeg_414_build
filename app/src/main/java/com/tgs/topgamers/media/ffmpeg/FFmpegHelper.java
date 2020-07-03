package com.tgs.topgamers.media.ffmpeg;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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


    public void cutAudio(String srcAudioPath, float startTimeS, float endTimeS, String desAudioPath) {
        float durationS = endTimeS - startTimeS;
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("ffmpeg");
        cmds.add("-ss");
        cmds.add(String.valueOf(startTimeS));
        cmds.add("-i");
        cmds.add(srcAudioPath);
        cmds.add("-t");
        cmds.add(String.valueOf(durationS));
        cmds.add("-y");
        cmds.add(desAudioPath);
    }

    public void mixAudioByMute(List<MixAudioFileData> audioList, String desVideoPath, long durationMs, OnFFmpegListener listener) {
        if (audioList != null && audioList.size() > 0) {
            //按startTime排序
            Collections.sort(audioList, new Comparator<MixAudioFileData>() {
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

            MixAudioFileData lastDubbing = audioList.get(audioList.size() - 1);
            long offDurationMs = durationMs - lastDubbing.getStartTimeMs() - lastDubbing.getDurationMs();
            long frameCount = 44100 * offDurationMs / 1000;
            int size = audioList.size();

            ArrayList<String> cmds = new ArrayList<>();
            cmds.add("ffmpeg");
            for (int i = 0; i < size; i++) {
                cmds.add("-i");
                cmds.add(audioList.get(i).getFilePath());
            }
            cmds.add("-filter_complex");
            String amix = "";
            for (int i = 0; i < size; i++) {
                long startTime = audioList.get(i).getStartTimeMs();
                amix += "[" + i + ":a]adelay=" + startTime + "|" + startTime + ",volume=2.0[audio" + i + "],";
            }
            for (int i = 0; i < size; i++) {
                amix += "[audio" + i + "]";
            }
            amix += "amix=inputs=" + size + ",apad=pad_len=" + frameCount;
//            amix += "\'";
            cmds.add(amix);
            cmds.add("-y");
            cmds.add(desVideoPath);

            String[] commands = cmds.toArray(new String[cmds.size()]);
            String test = "";
            for (int i = 0; i < commands.length; i++) {
                test += commands[i] + " ";
            }
            Log.i("VideoMixAudio", test);
            mFFmpegExecuteAsyncTask = new FFmpegExecuteAsyncTask(commands, listener);
            mFFmpegExecuteAsyncTask.execute();
        }
    }


    public void videoMixAudioChannels(MixAudioFileData srcVideo, String desVideoPath, List<MixAudioFileData> dubbingAudioList,
                                      float dubbingVolume, MixAudioFileData backgroundAudio, MixAudioFileData templateAudio, OnFFmpegListener listener) {
        String dubbingFilePath = Environment.getExternalStorageDirectory().getPath() + "/ATopGame/temps/dubbingAudio.wav";
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
            amix += "amix=inputs=" + size + ",apad=pad_len=" + frameCount;
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
        if (templateAudio != null) {
            cmds.add("-i");
            cmds.add(templateAudio.getFilePath());
        }
        cmds.add("-filter_complex");
        String complex = "";
        int channelIndex = 0;
        //源文件声音滤镜
        complex += "[" + channelIndex + ":a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,volume=" + srcVideo.getVolume() + "[a0];";
        //配音声音滤镜
        if (isDubbingChannel) {
            channelIndex = 1;
            complex += "[" + channelIndex + ":a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,volume=" + dubbingVolume * 2.0f + "[a1];";
        }
        //背景音乐声音滤镜
        if (backgroundAudio != null) {
            if (channelIndex == 0) {
                channelIndex = 1;
            } else {
                channelIndex = 2;
            }
            complex += "[" + channelIndex + ":a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,volume=" + backgroundAudio.getVolume() + "[a" + channelIndex + "];";
        }

        //模板音乐声音滤镜
        if (templateAudio != null) {
            if (channelIndex == 0) {
                channelIndex = 1;
            } else if (channelIndex == 1) {
                channelIndex = 2;
            } else {
                channelIndex = 3;
            }
            complex += "[" + channelIndex + ":a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,volume=" + templateAudio.getVolume() + "[a" + channelIndex + "];";
        }

        int inputs = 0;
//        complex += "[a0]";
//        inputs++;
//        if (isDubbingChannel) {
//            complex += "[a1]";
//            inputs++;
//        }
//        if (backgroundAudio != null) {
//            if (!isDubbingChannel) {
//                complex += "[a1]";
//            } else {
//                complex += "[a2]";
//            }
//            inputs++;
//        }

        for (int i = 0; i <= channelIndex; i++) {
            complex += "[a" + i + "]";
            inputs++;
        }


//        amerge=inputs=2
        complex += "amix=inputs=" + inputs + ":duration=first:dropout_transition=2";
//        complex += "amerge=inputs=" + inputs;
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

        mFFmpegExecuteAsyncTask = new FFmpegExecuteAsyncTask(cmdList, listener);
        mFFmpegExecuteAsyncTask.execute();
    }


    /**
     * 分离视频并填充静音帧
     *
     * @param inputPath
     * @param outputPath
     * @param listener
     */
    public void extractVideo(String inputPath, String outputPath, OnFFmpegListener listener) {
        List<String[]> cmdList = new ArrayList<>();
        String tempFilePath = Environment.getExternalStorageDirectory().getPath() + "/ATopGame/temps/temp_extractVideo.mp4";

        String[] cmd1 = {
                "ffmpeg",
                "-i",
                inputPath,
                "-vcodec",
                "copy",
                "-an",
                "-y",
                tempFilePath
        };
        cmdList.add(cmd1);

//        ffmpeg -i path -f lavfi -i anullsrc=channel_layout=stereo:sample_rate=44100 -c:v copy -shortest -y path

        String[] cmd2 = {
                "ffmpeg",
                "-i",
                tempFilePath,
                "-f",
                "lavfi",
                "-i",
                "anullsrc=channel_layout=stereo:sample_rate=44100",
                "-vcodec",
                "copy",
                "-shortest",
                "-y",
                outputPath
        };
        cmdList.add(cmd2);
        mFFmpegExecuteAsyncTask = new FFmpegExecuteAsyncTask(cmdList, listener);
        mFFmpegExecuteAsyncTask.execute();
    }


    public void insterAudios(String srcAudioPath, List<MixAudioFileData> insterAudioFileList, String desPath, OnFFmpegListener listener) {
        //ffmpeg -i input.mp3 -acodec libfaac output.aac
        String tempFilePath = Environment.getExternalStorageDirectory().getPath() + "/ATopGame/temps/temp_insterAudio_src.aac";
        List<String[]> cmdList = new ArrayList<>();

        //mp3转aac
        String[] cmd1 = {
                "ffmpeg",
                "-i",
                srcAudioPath,
                "-acodec",
                "aac",
                tempFilePath
        };
        cmdList.add(cmd1);
        //根据insterAudioFileList来分割srcAudio
        List<String> cmd2 = new ArrayList<>();
        List<String> clipAudioPathList = new ArrayList<>();
        float currentClipTimeMs = 0;
        for (int i = 0, size = insterAudioFileList.size(); i < size + 1; i++) {
            cmd2.clear();
            String path = Environment.getExternalStorageDirectory().getPath() + "/ATopGame/temps/temp_insterAudio_clip" + i + ".aac";

            MixAudioFileData data = (i < size) ? insterAudioFileList.get(i) : null;

            float startTime = 0;
            float duration = 0;
            if (data == null) {
                startTime = currentClipTimeMs / 1000f;
                duration = 0;
            } else {
                startTime = currentClipTimeMs / 1000f;
                duration = ((float) data.getStartTimeMs() - currentClipTimeMs) / 1000f;
                currentClipTimeMs = data.getStartTimeMs();
            }
            if ((data != null && data.getStartTimeMs() != 0) || i == size) {
                cmd2.add("ffmpeg");
                cmd2.add("-ss");
                cmd2.add(String.valueOf(startTime));
                cmd2.add("-i");
                cmd2.add(srcAudioPath);
                if (duration > 0) {
                    cmd2.add("-t");
                    cmd2.add(String.valueOf(duration));
                }
                cmd2.add("-y");
                cmd2.add(path);
                clipAudioPathList.add(path);
            }
            if (data != null) {
                clipAudioPathList.add(data.getFilePath());
            }
            if (cmd2.size() != 0) {
                String[] commands = cmd2.toArray(new String[cmd2.size()]);
                cmdList.add(commands);
            }
        }


        //合并分割后的文件
        File tempFile = new File(Environment.getExternalStorageDirectory().toString() + "/ATopGame/temps/concatMedia.txt");
        tempFile.delete();
        FileWriter writer = null;
        BufferedWriter bw = null;
        try {
            writer = new FileWriter(tempFile);
            bw = new BufferedWriter(writer);
            for (int i = 0, size = clipAudioPathList.size(); i < size; i++) {
                if (i == clipAudioPathList.size() - 1) {
                    bw.write("file " + "'" + clipAudioPathList.get(i) + "'");
                } else {
                    bw.write("file " + "'" + clipAudioPathList.get(i) + "'" + "\r\n");
                }
            }
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        ArrayList<String> cmd4 = new ArrayList<>();
        cmd4.add("ffmpeg");
        cmd4.add("-f");
        cmd4.add("concat");
        cmd4.add("-safe");
        cmd4.add("0");
        cmd4.add("-i");
        cmd4.add(tempFile.getPath());
//        cmd3.add("-absf");
//        cmd3.add("aac_adtstoasc");
//        cmd3.add("-movflags");
//        cmd3.add("faststart");
        cmd4.add("-c");
        cmd4.add("copy");
        cmd4.add(desPath);
        String[] cmds3 = cmd4.toArray(new String[cmd2.size()]);
        cmdList.add(cmds3);

        mFFmpegExecuteAsyncTask = new FFmpegExecuteAsyncTask(cmdList, listener);
        mFFmpegExecuteAsyncTask.execute();
    }

    public void concatMedia(String srcAudioPath, List<String> insterAudioPathList, String desPath, OnFFmpegListener listener) {

    }



    /**
     * 分离视频并填充静音帧
     *
     * @param inputPath
     * @param outputPath
     * @param listener
     */
    public void extractVideoByMute(String inputPath, String outputPath, OnFFmpegListener listener) {
        List<String[]> cmdList = new ArrayList<>();
        String tempFilePath = Environment.getExternalStorageDirectory().getPath() + "/ATopGame/temps/temp_extractVideo.mp4";
        createDir(Environment.getExternalStorageDirectory().getPath() + "/ATopGame/temps");

        String[] cmd1 = {
                "ffmpeg",
                "-i",
                inputPath,
                "-vcodec",
                "copy",
                "-an",
                "-y",
                tempFilePath
        };
        cmdList.add(cmd1);

//        ffmpeg -i path -f lavfi -i anullsrc=channel_layout=stereo:sample_rate=44100 -c:v copy -shortest -y path


        String[] cmd2 = {
                "ffmpeg",
                "-i",
                tempFilePath,
                "-f",
                "lavfi",
                "-i",
                "anullsrc=channel_layout=stereo:sample_rate=44100",
                "-vcodec",
                "copy",
                "-shortest",
                "-y",

                outputPath
        };
        cmdList.add(cmd2);
        mFFmpegExecuteAsyncTask = new FFmpegExecuteAsyncTask(cmdList, listener);
        mFFmpegExecuteAsyncTask.execute();
    }

    public native static int run(String[] commands);

    // 创建目录
    public static boolean createDir(String destDirName) {
        File dir = new File(destDirName);
        if (dir.exists()) {// 判断目录是否存在
            return false;
        }
        if (dir.mkdirs()) {// 创建目标目录
            return true;
        } else {
            return false;
        }
    }
}