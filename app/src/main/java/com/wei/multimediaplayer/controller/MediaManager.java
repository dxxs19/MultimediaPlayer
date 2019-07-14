package com.wei.multimediaplayer.controller;

import android.content.Context;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Administrator on 2016/8/20.
 */
public class MediaManager implements IPlayer
{
    private final String TAG = getClass().getSimpleName();
    private Context mContext;
    private static MediaManager sMediaManager;
    public MediaPlayer mMediaPlayer = new MediaPlayer();

    public MediaManager(Context context) {
        this.mContext = context;
    }

    public static MediaManager getInstance(Context context)
    {
        if (sMediaManager == null)
        {
            sMediaManager = new MediaManager(context);
        }
        return sMediaManager;
    }

    @Override
    public void playLocalVideo() {

    }

    @Override
    public void playUrlVideo() {

    }

    @Override
    public void playLocalAudio() {

    }

    @Override
    public void playUrlAudio() {

    }

    public MediaPlayer createMediaPlayer() {
        if (mMediaPlayer == null)
        {
            mMediaPlayer = new MediaPlayer();
        }
        return mMediaPlayer;
    }

    public void start()
    {
        Log.i(TAG, "start:");
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
        Log.i(TAG, "--- start-- ");
    }

    /*
	 * 获取当前播放的进度
	 */
    public int getPosition() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition() / 1000;
        }
        return 0;
    }

    /*
     * 获取当前视频播放长度
     */
    public int getDuration() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying())
        {
            return mMediaPlayer.getDuration() / 1000;
        }
        return 0;
    }

    public void stop()
    {
        if (mMediaPlayer != null)
        {
            if (mMediaPlayer.isPlaying())
            {
                mMediaPlayer.reset();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void seekTo(int currentPro)
    {
        if (mMediaPlayer != null)
        {
            mMediaPlayer.seekTo(currentPro * 1000);
        }
    }

    public boolean isAudio(String playUrl)
    {
        if (!TextUtils.isEmpty(playUrl))
        {
            if (playUrl.contains(".mp3") || playUrl.contains(".amr") || playUrl.contains(".ogg"))
            {
                return true;
            }
        }
        return false;
    }

    public boolean isPic(String playUrl)
    {
        if (!TextUtils.isEmpty(playUrl) && playUrl.contains("."))
        {
            String suffix = playUrl.substring(playUrl.lastIndexOf("."));
            Log.e(TAG, "后缀为：" + suffix);
            if (suffix.equalsIgnoreCase(".jpg") || suffix.equalsIgnoreCase(".jpeg") || suffix.equals(".png") || suffix.equals(".gif"))
            {
                return true;
            }
        }
        return false;
    }
}
