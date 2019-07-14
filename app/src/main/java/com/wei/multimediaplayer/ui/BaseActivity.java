package com.wei.multimediaplayer.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.wei.multimediaplayer.constant.GlobalVariable;

import net.youmi.android.AdManager;
import net.youmi.android.spot.SpotDialogListener;
import net.youmi.android.spot.SpotManager;

public class BaseActivity extends Activity
{
    protected String TAG = "";
    private ProgressDialog dialog = null;
    protected Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        TAG = getClass().getSimpleName();
        mContext = this;
        AdManager.getInstance(mContext).init(GlobalVariable.PUBLISH_ID, GlobalVariable.APP_SECURITY);
//        setupSpotAd(); // 图片很大
    }

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

    protected void closeDialog()
    {
        if (null != dialog) {
            dialog.dismiss();
            dialog = null;
        }
    }

    // 进度条
    protected void startProgress(String msg)
    {
        Log.i(TAG, "startProgress :" + msg );
        if (null == dialog)
        {
            dialog = new ProgressDialog(this);
            dialog.setMessage(msg);
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();
        }
    }

    /**
     * 测试用的提示信息弹出框
     * @param string
     */
    private void showMsg(String string)
    {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }

    /**
     * 设置插屏广告
     */
    private void setupSpotAd() {
        // 预加载插屏广告数据
        SpotManager.getInstance(mContext).loadSpotAds();
        // 设置插屏动画的横竖屏展示方式，如果设置了横屏，则在有广告资源的情况下会是优先使用横屏图
        SpotManager.getInstance(mContext)
                .setSpotOrientation(SpotManager.ORIENTATION_LANDSCAPE);
        // 插屏动画效果，0:ANIM_NONE为无动画，1:ANIM_SIMPLE为简单动画效果，2:ANIM_ADVANCE为高级动画效果
        SpotManager.getInstance(mContext).setAnimationType(SpotManager.ANIM_ADVANCE);
    }

    // 有米插屏广告
    protected void showYouMiSpot()
    {
        // 展示插屏广告，可以不调用预加载方法独立使用
        SpotManager.getInstance(mContext)
                .showSpotAds(mContext, new SpotDialogListener() {
                    @Override
                    public void onShowSuccess() {
                        Log.i(TAG, "插屏展示成功");
                    }

                    @Override
                    public void onShowFailed() {
                        Log.i(TAG, "插屏展示失败");
                    }

                    @Override
                    public void onSpotClosed() {
                        Log.i(TAG, "插屏被关闭");
                    }

                    @Override
                    public void onSpotClick(boolean isWebPath) {
                        Log.i(TAG, "插屏被点击，isWebPath = " + isWebPath);
                    }

                });
    }

    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if((System.currentTimeMillis()-exitTime) > 2000){
                Toast.makeText(getApplicationContext(), "再按一次退出", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
