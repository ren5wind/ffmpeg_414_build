package com.tgs.topgamers.media.ffmpeg;

/**
 * Author      : renxiaoming
 * Date        : 2019/4/19
 * Description :
 */
public interface OnFFmpegListener {
    void onStart();

    void onSuccess();

    void onFail(int ret);
}
