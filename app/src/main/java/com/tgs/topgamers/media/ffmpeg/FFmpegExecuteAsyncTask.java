package com.tgs.topgamers.media.ffmpeg;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

/**
 * Author      : renxiaoming
 * Date        : 2019-08-20
 * Description :
 */
public class FFmpegExecuteAsyncTask extends AsyncTask<Void, String, Integer> {

    private List<String[]> mCmds;
    private OnFFmpegListener mListener;

    FFmpegExecuteAsyncTask(List<String[]> cmds, OnFFmpegListener listener) {
        this.mCmds = cmds;
        this.mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        Log.i("FFmpegExecuteAsyncTask", "onPreExecute");

        if (mListener != null) {
            mListener.onStart();
        }
    }

    @Override
    protected Integer doInBackground(Void... params) {
        for (int i = 0; i < mCmds.size(); i++) {
            Log.i("FFmpegExecuteAsyncTask", "cmd = " + mCmds.get(i).toString());
            int ret = FFmpegHelper.run(mCmds.get(i));
            Log.i("FFmpegExecuteAsyncTask", "ret = " + ret);
            if (ret != 0) {
                mListener.onFail(ret);
                return ret;
            }
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Integer result) {
        Log.i("FFmpegExecuteAsyncTask", "onPostExecute");
        if (result == 0) {
            mListener.onSuccess();
        } else {
            mListener.onFail(result);
        }
    }

}
