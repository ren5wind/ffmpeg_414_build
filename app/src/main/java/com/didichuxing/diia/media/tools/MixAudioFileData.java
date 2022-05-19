package com.didichuxing.diia.media.tools;

import android.media.MediaMetadataRetriever;
import android.text.TextUtils;

import java.io.File;

/**
 * Author      : renxiaoming
 * Date        : 2019-08-13
 * Description :
 */
public class MixAudioFileData {
    private String filePath;
    private float volume;
    private long startTimeMs;
    private long durationMs;

    public static final int NO_ERROR = 0;
    public static final int ERROR_PATH_NULL = 1;

    public String getFilePath() {
        return filePath;
    }


    public int setFilePath(String path) {
        if (TextUtils.isEmpty(path)) {
            return ERROR_PATH_NULL;
        }
        filePath = path;
        File file = new File(path);
        if (!file.exists()) {
            return ERROR_PATH_NULL;
        }
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(file.getAbsolutePath());


        String durationString = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        if (!TextUtils.isEmpty(durationString)) {
            durationMs = Long.valueOf(durationString);
        }
        metadataRetriever.release();
        return NO_ERROR;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public void setStartTimeMs(long startTimeMs) {
        this.startTimeMs = startTimeMs;
    }

    public long getDurationMs() {
        return durationMs;
    }
}
