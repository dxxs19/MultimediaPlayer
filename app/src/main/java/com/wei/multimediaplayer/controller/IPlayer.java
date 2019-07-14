package com.wei.multimediaplayer.controller;

/**
 * 多媒体播放器接口：主要做四件事，即播放本地及网络的音视频。
 * Created by Administrator on 2016/8/20.
 */
public interface IPlayer
{
    /**
     * 播放本地视频
     */
    void playLocalVideo();

    /**
     * 播放网络视频
     */
    void playUrlVideo();

    /**
     * 播放本地音频
     */
    void playLocalAudio();

    /**
     * 播放网络音频
     */
    void playUrlAudio();

}
