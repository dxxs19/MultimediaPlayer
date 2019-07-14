package com.wei.multimediaplayer.utils;

/**
 * Created by Administrator on 2016/8/21.
 */
public class TimeUtils
{
    /**
     * 格式:HH:mm:ss
     *
     * @param durationMs
     * @return
     */
    public static String durationMs2String(int durationMs) {
        String out = "";
        int hours = durationMs/(60*60);
        int minutes = (durationMs % (60 * 60) )/60;
        int seconds = durationMs % 60;

        if (hours == 0) {
            out = (minutes < 10 ? "0" + minutes : minutes) + ":"
                    + (seconds < 10 ? "0" + seconds : seconds);
        } else {
            out = (hours < 10 ? "0" + hours : hours) + ":"
                    + (minutes < 10 ? "0" + minutes : minutes) + ":"
                    + (seconds < 10 ? "0" + seconds : seconds);
        }

        return out;
    }
}
