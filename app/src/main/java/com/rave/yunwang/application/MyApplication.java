package com.rave.yunwang.application;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.rave.yunwang.R;
import com.rave.yunwang.utils.FilePathUtils;
import com.s2icode.main.S2iCodeModule;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreator;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreator;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by admin on 2018/1/26 0026.
 */

public class MyApplication extends BaseApplication {

    static {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
                layout.setPrimaryColorsId(R.color.bgDarkGrey, android.R.color.white);//全局设置主题颜色
                return new ClassicsHeader(context);//.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header
            }
        });
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @Override
            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
                //指定为经典Footer，默认是 BallPulseFooter
                return new ClassicsFooter(context).setDrawableSize(20);
            }
        });
    }

    public static MyApplication getInstance() {
        return (MyApplication) getBaseInstance();
    }

    @Override
    public void onAppForegroundChange(boolean isAppForeground) {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FilePathUtils.init();
        //初始化module工程
        try {
            S2iCodeModule.init(this);
            initAccessToken();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    copyFileFromAssets();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initAccessToken() {
        OCR.getInstance(this).initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken accessToken) {
                String token = accessToken.getAccessToken();
                Log.e("test ocr baidu", token);
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
            }
        }, getApplicationContext());
    }

    private void copyFileFromAssets() {
        String fileName = "group.traineddata";
        File dir = this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (dir != null && (!dir.exists() || !dir.isDirectory())) {
            dir.mkdir();
        }

        File tessDataDir = new File(dir + "/tessdata");
        if (!tessDataDir.exists() || !tessDataDir.isDirectory()) {
            tessDataDir.mkdir();
        }

        File file= new File(tessDataDir, fileName);

        InputStream inputStream = null;
        OutputStream outputStream =null;
        // 检查 SQLite 数据库文件是否存在
        if (!file.exists()) {
            try {
                file.createNewFile();

                inputStream = this.getAssets().open(fileName);
                outputStream = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int len;

                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
                file.delete();
            }
        }
    }

}
