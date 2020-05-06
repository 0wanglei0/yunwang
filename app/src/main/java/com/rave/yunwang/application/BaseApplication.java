package com.rave.yunwang.application;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.s2icode.dao.S2iClientInitResult;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class BaseApplication extends Application {

    public enum EServerType {
        EServerInnerDev,
        EServerExtTest,
        EServerBossSpecial,
        EServerExtHttp,
        EServerExtFormal
    }

    public boolean isDebug = true;
    public boolean showMsgId = true;

    private static BaseApplication baseApplication;

    public static S2iClientInitResult s2iClientInitResult;
    public static BaseApplication getBaseInstance() {
        return baseApplication;
    }

    public volatile static boolean isInLoginActivity;

    public static boolean isAppForeground = false;
    private int count = 0;

    public abstract void onAppForegroundChange(boolean isAppForeground);

    @Override
    public void onCreate() {
        super.onCreate();

        baseApplication = this;
        if(!isDebug)
        {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run()
                {
                    while (true)
                    {
                        try
                        {
                            Looper.loop();
                        }
                        catch (Throwable e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks);

        try {
            closeAndroidPDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            count++;
            if (count == 1) {
                if (!isAppForeground) {
                    onAppForegroundChange(true);
                    isAppForeground = true;
                }
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            count--;
            if (count == 0) {
                if (isAppForeground) {
                    onAppForegroundChange(false);
                    isAppForeground = false;
                }
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    };

    public static boolean isInLoginActivity() {
        return isInLoginActivity;
    }

    private void closeAndroidPDialog() {
        try {
            if (android.os.Build.VERSION.SDK_INT < 28) {
                return;
            }
            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception e) {
            //do nothing
        }
        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            //do nothing
        }
    }

    public EServerType serverType = EServerType.EServerInnerDev;

}
