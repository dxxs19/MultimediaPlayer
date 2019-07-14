package com.wei.multimediaplayer.ui;

import android.os.Bundle;
import android.view.MotionEvent;

import com.wei.multimediaplayer.R;

public class MainActivity extends BaseActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}
