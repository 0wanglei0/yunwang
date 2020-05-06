package com.rave.yunwang.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.githang.statusbar.StatusBarCompat;
import com.rave.yunwang.R;
import com.s2icode.dao.S2iClientInitResult;
import com.s2icode.dao.S2iCodeResult;
import com.s2icode.dao.S2iCodeResultBase;
import com.s2icode.main.S2iCodeModule;
import com.s2icode.main.S2iCodeResultInterface;


/**
 * 摄像头采集画面类
 *
 * @author Gaoyue 已修正 2013/08/05
 */
@SuppressLint({"NewApi", "NewApi"})
public class S2IDemoCameraDemoActivity extends Activity implements S2iCodeResultInterface {

    private S2iClientInitResult s2iClientInitResult;
    protected CameraBase m_objCamera = null; // 摄像头模块
    private DrawerLayout drawerLayout;
//    protected FocusView m_ctlFocusView; // 对焦视图
    private boolean skipDecode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check Screen size
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(point);
        //超高屏手机不隐藏状态栏和导航栏
        //非超高屏手机如果是刘海屏需要把视频流延伸到刘海范围
        if (1.0 * point.y / point.x <= 1920.0 / 1080) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏显示
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        } else {
            StatusBarCompat.setStatusBarColor(this, Color.TRANSPARENT);
        }

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        drawerLayout = new DrawerLayout(this);
        DrawerLayout view = (DrawerLayout) layoutInflater.inflate(
                R.layout.activity_demo_s2i_camera,
                new DrawerLayout(this), false);
        drawerLayout.addView(view);
        setContentView(drawerLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        skipDecode = false;
        S2iCodeModule.setS2iCodeResultInterface(this);
        m_objCamera = new CameraBase(this);
//        m_objCamera.setDemoCameraBaseInterface(this);
        m_objCamera.setFrame(drawerLayout);
        drawerLayout.addView(m_objCamera);

        int permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permissionCamera == PackageManager.PERMISSION_GRANTED) {
            m_objCamera.setVisibility(View.VISIBLE);
            m_objCamera.setPreviewCallStart();
        }

        Bundle bundle = getIntent().getBundleExtra("bundle");
        s2iClientInitResult = (S2iClientInitResult) bundle.getSerializable("S2iClientInitResult");
        if (s2iClientInitResult != null) {
//            m_ctlFocusView = new FocusView(this);
//            m_ctlFocusView.setFocusValue(s2iClientInitResult.getS2iParam().getFocusFrameWidth(),
//                    s2iClientInitResult.getS2iParam().getFocusFrameHeight());
//            drawerLayout.addView(m_ctlFocusView);
        }

        m_objCamera.startPreviewWithFocusAndFlash();
        m_objCamera.setPreviewCallStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (m_objCamera != null) {
            if (drawerLayout != null) {
                drawerLayout.removeView(m_objCamera);
            }
            m_objCamera.setPreviewCallStop();
            m_objCamera.stopPreview();
        }
        m_objCamera = null;
    }

    /**
     * 视图销毁
     */
    @Override
    protected void onDestroy() {
        if (m_objCamera != null) {
            if (drawerLayout != null) {
                drawerLayout.removeView(m_objCamera);
            }
            m_objCamera.setPreviewCallStop();
            m_objCamera.stopPreview();
        }
        m_objCamera = null;

        super.onDestroy();
    }

    /**
     *     来自CameraBaseInterface的方法
     */
//    @Override
//    public void receivePreviewImageData(byte[] data, int previewWidth, int previewHeight, float zoom) {
//        if (!skipDecode) {
//            int decodeCode = S2iCodeModule.startS2iDecode(data, previewWidth, previewHeight, 1, true);
//            if (decodeCode == 1) {
//                skipDecode = true;
//            }
//        }
//    }

    @Override
    public void onS2iCodeResult(S2iCodeResult s2iCodeResult) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("S2iCodeResult",s2iCodeResult);
        intent.putExtra("bundle",bundle);
        intent.setClass(this,ShowResultActivity.class);
        startActivity(intent);
    }

    @Override
    public void onS2iCodeError(S2iCodeResultBase errorResult) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("S2iCodeResult",errorResult);
        intent.putExtra("bundle",bundle);
        intent.setClass(this,ShowErrorResultActivity.class);
        startActivity(intent);
    }
}
