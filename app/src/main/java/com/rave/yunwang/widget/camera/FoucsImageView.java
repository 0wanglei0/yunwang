package com.rave.yunwang.widget.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.rave.yunwang.R;


@SuppressLint("AppCompatCustomView")
public class FoucsImageView extends ImageView {

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public FoucsImageView(Context context) {
        super(context);
        init();
    }

    public FoucsImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FoucsImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setImageResource(R.drawable.focus_point);
        setVisibility(GONE);
    }

    public void startFocus(Point point) {
        setVisibility(GONE);
        mHandler.removeCallbacksAndMessages(null);
        if (point == null)
            return;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
        params.topMargin = point.y - getHeight() / 2;
        params.leftMargin = point.x - getWidth() / 2;
        setLayoutParams(params);
        setVisibility(View.VISIBLE);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setVisibility(View.GONE);
            }
        }, 1000);
    }

}
