package com.wei.multimediaplayer.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wei.multimediaplayer.R;
import com.wei.multimediaplayer.controller.MediaManager;
import com.wei.multimediaplayer.utils.FileUtils;
import com.wei.multimediaplayer.utils.TimeUtils;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PlayActivity extends BaseActivity implements SurfaceHolder.Callback, MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private SurfaceHolder mHolder;
    private SurfaceView mSurfaceView;
    private MediaManager mMediaManager;
    private SeekBar skbProgress;
    private ImageView iv_multiscreen, img;
    public TextView tv_title, tv_time, tv_currtime, currentVoiceTxt, voiceLightTurnTxt;
    private LinearLayout linlayout_time;
    private boolean mIsVideoSizeKnown = false;
    private boolean mIsVideoReadyToBePlayed = false;
    private int mVideoWidth;
    private int mVideoHeight;
    private Timer mTimer = new Timer(true);
    private Timer seekBarTimer = new Timer();
    private MyTimerTask mTimerTask = null;
    private SeekBarTimerTask seekBarTimerTask = null;
    private ImageButton optsBtn, openFileBtn, openUrlBtn;
    private final int DELAYTIME = 3000;
    private int currentprogress;
    private RelativeLayout topRl;
    private String playUrl = "";
    private boolean playCompletion = false;
    private GestureDetector mGestureDetector;
    private LinearLayout seekBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "--- onCreate ---");
        // 初始化界面
        initView();
        if (savedInstanceState != null) {
            currentprogress = savedInstanceState.getInt("PRE_POSITION", 0);
            playUrl = savedInstanceState.getString("PRE_URL");
        }

        mGestureDetector = new GestureDetector(this, new MyGestureListener());
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    private void initView() {
        //设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_play);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mMediaManager = MediaManager.getInstance(this);

        iv_multiscreen = (ImageView) this.findViewById(R.id.iv_multiscreen);
        img = (ImageView) findViewById(R.id.img_show);
        tv_title = (TextView) this.findViewById(R.id.tv_title);
        tv_time = (TextView) this.findViewById(R.id.tv_time);
        tv_currtime = (TextView) this.findViewById(R.id.tv_currtime);
        linlayout_time = (LinearLayout) this.findViewById(R.id.linlayout_time);
        optsBtn = (ImageButton) findViewById(R.id.btn_opts);
        openFileBtn = (ImageButton) findViewById(R.id.btn_openFile);
        openUrlBtn = (ImageButton) findViewById(R.id.btn_openUrl);
        optsBtn.setOnClickListener(this);
        openFileBtn.setOnClickListener(this);
        openUrlBtn.setOnClickListener(this);
        topRl = (RelativeLayout) findViewById(R.id.rl_top);
        currentVoiceTxt = (TextView) findViewById(R.id.txt_voice);
        voiceLightTurnTxt = (TextView) findViewById(R.id.txt_voice_light_turn);
        seekBarLayout = (LinearLayout) findViewById(R.id.linearly_seekbar);

        skbProgress = (SeekBar) this.findViewById(R.id.skbProgress);
        skbProgress.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBarTimerTask = new SeekBarTimerTask();
        seekBarTimer.schedule(seekBarTimerTask, 0, 1000);
    }

    /**
     * 加载数据
     */
    private void setDate() {
        if (playUrl.contains("/")) {
            String title = playUrl.substring(playUrl.lastIndexOf("/") + 1);
            tv_title.setText(title);
        }
    }

    /*******************************************************
     * 通过定时器和Handler来更新进度条
     ******************************************************/
    class SeekBarTimerTask extends TimerTask {
        @Override
        public void run() {
            if (mMediaManager.mMediaPlayer == null) {
                return;
            }
            if (mMediaManager.mMediaPlayer.isPlaying()
                    && !skbProgress.isPressed()) {
                Message msg1 = handleProgress.obtainMessage();
                msg1.what = 0;
                handleProgress.sendMessage(msg1);
            }
        }
    }

    Handler handleProgress = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    int position = mMediaManager.getPosition();// 获得现在播放的位置
                    int duration = mMediaManager.getDuration();   // 获得视频长度

                    if (duration > 0) {
                        long progress = skbProgress.getMax() * position / duration;
                        // 每隔一秒更新一下当前播放进度
                        skbProgress.setProgress((int) progress);
                        // 每隔一秒更新一下当前播放时间
                        tv_currtime.setText(TimeUtils.durationMs2String(position));
                        // 设置视频总时间
                        tv_time.setText(TimeUtils.durationMs2String(duration));
                        if (isCurrentShow) {
                            setDate();
                        }
                    }
                    break;

                case 1:
                    optsBtn.setBackgroundResource(R.mipmap.pause);
                    showUI(false);
                    break;

                default:
            }

        }
    };

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        int currentPro;

        /**
         * 拖动条停止拖动的时候调用
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.e(TAG, "--- onStopTrackingTouch ---");
            seek(true);
        }

        /**
         * 拖动条开始拖动的时候调用
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.e("onStartTrackingTouch", "onStartTrackingTouch");
        }

        /**
         * 拖动条进度改变的时候调用
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
//            Log.e("onProgressChanged", "progress : " + progress + ", fromUser : " + fromUser);
            if (fromUser) {
                // 人为滑动进度条
                currentPro = progress * mMediaManager.getDuration() / seekBar.getMax();
                mMediaManager.seekTo(currentPro);
                Log.e(TAG, "人为滑动进度条：" + currentPro);
                openTimerTask();
            }
        }

    };

    private void seek(boolean flag) {
        int duration = mMediaManager.getDuration();
        int currentPro = 0;
        if (flag) {
            if (mMediaManager.mMediaPlayer != null) {
                if (!mMediaManager.mMediaPlayer.isPlaying()) {
                    mMediaManager.mMediaPlayer.start();
                    openTimerTask();
                }
            }
        } else {
            currentPro = skbProgress.getProgress();
        }
        tv_time.setText(TimeUtils.durationMs2String(duration));
        tv_currtime.setText(TimeUtils.durationMs2String(currentPro * duration / 100));
    }

    /**
     * 打开计时器，三秒不操作，就关闭ui
     */
    private void openTimerTask() {
        if (mTimer != null) {
            if (mTimerTask != null) {
                mTimerTask.cancel(); // 将原任务从队列中移除
            }

            mTimerTask = new MyTimerTask(); // 新建一个任务
            mTimer.schedule(mTimerTask, DELAYTIME);
        }

    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            Message msg1 = handleProgress.obtainMessage();
            msg1.what = 1;
            handleProgress.sendMessage(msg1);
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated called");
        if (!TextUtils.isEmpty(playUrl)) {
            playVideo(playUrl);
            setDate();
            if (mMediaManager.isAudio(playUrl)) {
                mSurfaceView.setBackgroundResource(R.mipmap.bg_surfaceview);
//                new DownloadImgTask().execute(mMultiManager.getAlbumArtURI());
//                startProgress("音频缓冲中,请稍候......");
            } else {
                mSurfaceView.setBackgroundResource(0);
            }

            if (mMediaManager.isPic(playUrl)) {
                // 显示图片

            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged called");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed called");
    }

    private void playVideo(String url) {
        releaseMediaPlayer();
        MediaPlayer mediaPlayer = mMediaManager.createMediaPlayer();
//        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//            url = Environment.getExternalStorageDirectory() + "/tencent/QQfile_recv/" + "吴奇隆-转弯.mkv";
//        }
        Log.e(TAG, "url = " + url);
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.setDisplay(mHolder);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnVideoSizeChangedListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnInfoListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 二级缓存
     *
     * @param mp
     * @param percent
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        skbProgress.setSecondaryProgress(percent);
        Log.d(TAG, "onBufferingUpdate percent:" + percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion called, 播放完毕！");
//        releaseMediaPlayer();
//        finish();
        playCompletion = true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared called");
        mIsVideoReadyToBePlayed = true;
        // 播放视频
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback();
            return;
        }

        // 播放音频
        if (mMediaManager.isAudio(playUrl)) {
            startVideoPlayback();
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.v(TAG, "onVideoSizeChanged called");
        if (width == 0 || height == 0) {
            Log.e(TAG, "invalid video width(" + width + ") or height(" + height
                    + ")");
            // return;
        }
        Log.e(TAG, "width : " + width + ", height : " + height);

        WindowManager windowManager = getWindowManager();
        int mSurfaceViewWidth = windowManager.getDefaultDisplay().getWidth();
        int mSurfaceViewHeight = windowManager.getDefaultDisplay().getHeight();

        mIsVideoSizeKnown = true;
        mVideoWidth = width;
        mVideoHeight = height;

        int wid = mMediaManager.mMediaPlayer.getVideoWidth();
        int hig = mMediaManager.mMediaPlayer.getVideoHeight();
        Log.e(TAG, "wid : " + wid + ", hig : " + hig);
        // 根据视频的属性调整其显示的模式
        if (wid > hig) {
            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } else {
            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
        if (width > height) {
            // 竖屏录制的视频，调节其上下的空余（w/width = mSurfaceViewheight/height）
            int w = mSurfaceViewHeight * width / height;
            int margin = (mSurfaceViewWidth - w) / 2;
            Log.d(TAG, "margin:" + margin + ", w : " + w);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(margin, 0, margin, 0);
            mSurfaceView.setLayoutParams(lp);
        } else if (width < height) {
            // 横屏录制的视频，调节其左右的空余
            int h = mSurfaceViewWidth * height / width;
            int margin = (mSurfaceViewHeight - h) / 2;
            Log.d(TAG, "margin:" + margin + ", h : " + h);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, margin, 0, margin);
            mSurfaceView.setLayoutParams(lp);
        }

        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    private void releaseMediaPlayer() {
        if (mMediaManager != null) {
            mMediaManager.stop();
        }
        doCleanUp();
    }

    private void doCleanUp() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        mIsVideoReadyToBePlayed = false;
        mIsVideoSizeKnown = false;
    }

    private void startVideoPlayback() {
        Log.v(TAG, "startVideoPlayback");
//        closeDialog();
        mHolder.setFixedSize(mVideoWidth, mVideoHeight);
        mSurfaceView.setMinimumWidth(100);
        mMediaManager.seekTo(currentprogress);
        mMediaManager.start();
        showUI(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_opts:
                if (null != mMediaManager.mMediaPlayer) {
                    if (mMediaManager.mMediaPlayer.isPlaying()) {
                        mMediaManager.mMediaPlayer.pause();
                        showYouMiSpot();
                        optsBtn.setBackgroundResource(R.mipmap.start);
                        showUI(true);
                    } else {
                        mMediaManager.mMediaPlayer.start();
                        optsBtn.setBackgroundResource(R.mipmap.pause);
                        openTimerTask();
                    }
                }
                break;

            case R.id.btn_openFile:
                openFile();
                break;

            case R.id.btn_openUrl:
                openUrl();
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            Log.e(TAG, "--- mGestureDetector.onTouchEvent(event) ---");
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                /*
                     * 确定键被按下，先设置播放进度，再判定是否正在播放， 假如不是正在播放，就开始播放，
                     * 假如已经在播放，则判定是否为左右拉动操作后按确定， 假如是，则继续播放，假如不是，则暂停。
                     */
                Log.e(TAG, "--- MotionEvent.ACTION_DOWN ---");
                if (null != mMediaManager.mMediaPlayer) {
                    if (mMediaManager.mMediaPlayer.isPlaying()) {
                        if (isCurrentShow) {
                            showUI(false);
                        } else {
                            showUI(true);
                        }
                    } else if (playCompletion) {
                        showUI(true);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                endGesture();
                voiceLightTurnTxt.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        voiceLightTurnTxt.setVisibility(View.GONE);
                    }
                }, 2000);
                break;

            default:
        }
        return true;
    }

    boolean isCurrentShow = false;

    /**
     * 显示其他控件 flag 标示是否按确定键操作，假如按确定键，则把暂停的图标显示出来
     */
    private void showUI(boolean show) {
        Log.e(TAG, "--- showUI ---");
        isCurrentShow = show;
//        iv_multiscreen.setVisibility(show ? View.VISIBLE : View.GONE);
        topRl.setVisibility(show ? View.VISIBLE : View.GONE);
        seekBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        linlayout_time.setVisibility(show ? View.VISIBLE : View.GONE);
        currentVoiceTxt.setVisibility(show ? View.VISIBLE : View.GONE);

        if (show) {
            /**
             * 获取skbProgress焦点，用于拖动事件
             */
            skbProgress.post(new Runnable() {
                @Override
                public void run() {
                    skbProgress.requestFocus();
                }
            });

            // 设置进度条到当前播放位置
            int curPos = mMediaManager.getPosition();
            if (mMediaManager.getDuration() != 0) {
                int progress = curPos * skbProgress.getMax() / mMediaManager.getDuration();
                skbProgress.setProgress(progress);
            }
        }
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "--- onResume ---");
        super.onResume();
        if (null != mMediaManager && null != mMediaManager.mMediaPlayer) {
            mMediaManager.mMediaPlayer.seekTo(currentprogress);
        } else {
            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.e(TAG, "--- onSaveInstanceState ---");
        super.onSaveInstanceState(outState);
        int position = mMediaManager.getPosition();
        outState.putInt("PRE_POSITION", position);
        outState.putString("PRE_URL", playUrl);
        Log.e(TAG, "onSaveInstanceState : position = " + position + ", playUrl = " + playUrl);
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "--- onPause ---");
        super.onPause();
        if (null != mMediaManager.mMediaPlayer) {
            mMediaManager.mMediaPlayer.pause();
            currentprogress = mMediaManager.getPosition();
            showUI(true);
        }
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "--- onDestroy ---");
        super.onDestroy();
        if (seekBarTimerTask != null) {
            seekBarTimerTask.cancel();
            seekBarTimerTask = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        releaseMediaPlayer();
    }

    /**
     * 选取本地文件播放
     */
    void openFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "请选择播放文件"), 100);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "--- onActivityResult ---");
        if (resultCode != Activity.RESULT_OK)
            return;
        switch (requestCode) {
            case 100:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    String path = FileUtils.getPath(this, uri);
                    Log.e(TAG, "path : " + path);
                    if (!TextUtils.isEmpty(path))
                        playUrl = path;
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void openUrl() {
        Log.e(TAG, "--- openUrl ---");
    }

    /**
     * 手势结束
     */
    private void endGesture() {
        mVolume = -1;
        mBrightness = -1f;
    }

    /**
     * 最大声音
     */
    private int mMaxVolume;
    /**
     * 当前声音
     */
    private int mVolume = -1;
    /**
     * 当前亮度
     */
    private float mBrightness = -1f;
    private AudioManager mAudioManager;

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        /**
         * 双击
         *
         * @param e
         * @return
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return super.onDoubleTap(e);
        }

        /**
         * 滑动
         *
         * @param e1
         * @param e2
         * @param distanceX
         * @param distanceY
         * @return
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float oldX = e1.getX(), oldY = e1.getY();
            int y = (int) e2.getRawY();
            Log.e(TAG, "oldX = " + oldX + ", oldY = " + oldY + ", y = " + y);

            Display display = getWindowManager().getDefaultDisplay();
            int windowWidth = display.getWidth();
            int windowHeight = display.getHeight();

            if (oldX > windowWidth * 4 / 5) {
                Log.e(TAG, "右边滑动");
                onVolumeSlide((oldY - y) / windowHeight);
            } else if (oldX < windowWidth / 5) {
                Log.e(TAG, "左边滑动");
                onBrightnessSlide((oldY - y) / windowHeight);
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        if (mVolume == -1) {
            mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolume < 0)
                mVolume = 0;
        }

        int index = (int) (percent * mMaxVolume) + mVolume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;

        Log.e(TAG, "mVolume : " + mVolume + ", percent : " + percent);
        Log.e(TAG, "maxVolume : " + mMaxVolume + ", index : " + index);

        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        Drawable drawable = getResources().getDrawable(R.mipmap.voice_turn);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        voiceLightTurnTxt.setCompoundDrawables(null, drawable, null, null);

        voiceLightTurnTxt.setVisibility(View.VISIBLE);
        currentVoiceTxt.setVisibility(View.VISIBLE);

        String rate = (index * 100) + "000";
        voiceLightTurnTxt.setText(rate.substring(0, 2) + "%");
        currentVoiceTxt.setText(rate.substring(0, 2) + "%");
    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        if (mBrightness < 0) {
            mBrightness = getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f)
                mBrightness = 0.50f;
            if (mBrightness < 0.01f)
                mBrightness = 0.01f;
        }
        WindowManager.LayoutParams lpa = getWindow().getAttributes();
        lpa.screenBrightness = mBrightness + percent;
        if (lpa.screenBrightness > 1.0f)
            lpa.screenBrightness = 1.0f;
        else if (lpa.screenBrightness < 0.01f)
            lpa.screenBrightness = 0.01f;
        getWindow().setAttributes(lpa);

        voiceLightTurnTxt.setVisibility(View.VISIBLE);

        Drawable drawable = getResources().getDrawable(R.mipmap.light_turn);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        voiceLightTurnTxt.setCompoundDrawables(null, drawable, null, null);

        String rate = (lpa.screenBrightness * 100) + "000";
        ;
        Log.e(TAG, "rate : " + rate);
        voiceLightTurnTxt.setText(lpa.screenBrightness < 1 ? rate.substring(0, 2) + "%" : rate.substring(0, 3) + "%");
    }

}
