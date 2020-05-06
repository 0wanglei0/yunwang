package com.rave.yunwang.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.rave.yunwang.application.BaseApplication;

public class WindowUtils {

    private static float window_scale;

    private static int window_width;

    private static int window_height;

    public static int getStatusBarHeight(Activity activity) {
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getStateBarHeight(Activity activity) {
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }

    public static int getTitleBarHeight(Activity activity) {
        int contentTop = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        return contentTop - frame.top;
    }

    public static int dip2px(float dpValue) {
        final float scale = BaseApplication.getBaseInstance().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static float getTextWidth(String text, float textSize) {
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(textSize);
        float[] widths = new float[text.length()];
        textPaint.getTextWidths(text, widths);
        float all = 0;
        for (float w : widths) {
            all += w;
        }
        return all;
    }

    public static void setFitsSystemWindows(Activity activity, boolean value) {
        ViewGroup contentFrameLayout = (ViewGroup) activity.findViewById(android.R.id.content);
        View parentView = contentFrameLayout.getChildAt(0);
        if (parentView != null && Build.VERSION.SDK_INT >= 14) {
            parentView.setFitsSystemWindows(value);
        }
    }

    public static void addStatusViewWithColor(Activity activity, int color) {
        ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
        View statusBarView = new View(activity);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight(activity));
        statusBarView.setBackgroundColor(color);
        contentView.addView(statusBarView, lp);
    }

    public static int[] getWindowParams(Context context) {
        WindowManager wm = (WindowManager) context.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        int[] params = {width, height};
        return params;
    }

    public static float getWindowScale(Context context) {
        if (window_scale == 0)
            window_scale = 1.0f * WindowUtils.getWindowParams(context)[0] / WindowUtils.getWindowParams(context)[1];
        return window_scale;
    }

    public static int getWindowWidth(Context context) {
        if (window_width == 0)
            window_width = WindowUtils.getWindowParams(context)[0];
        return window_width;
    }

    public static int getWindowHeight(Context context) {
        if (window_height == 0)
            window_height = WindowUtils.getWindowParams(context)[1];
        return window_height;
    }

    public static int getColorPrimaryDarkHeight(Context context) {
        Resources resources = context.getResources();
        return resources
                .getDimensionPixelSize(resources
                        .getIdentifier("status_bar_height", "dimen", "android"));
    }

    public static int getColorPrimaryHeight(Context context) {
        Resources resources = context.getResources();
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, resources.getDisplayMetrics());
        }
        return 0;
    }

    public static int getNavigationHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static int getDisplayHeight(Context context) {
        return getWindowHeight(context) - getColorPrimaryDarkHeight(context) - getNavigationHeight(context);
    }

    public static Rect getTitleAndContentViewRect(View view) {
        Rect frame = new Rect();
        view.getWindowVisibleDisplayFrame(frame);
        return frame;
    }

    public static int getContentAndUpViewHeight(View view) {
        return getTitleAndContentViewRect(view).bottom;
    }

    public static int getTitleAndContentViewHeight(View view) {
        Rect frame = getTitleAndContentViewRect(view);
        return frame.bottom - frame.top;
    }

    public static int getScreenHeight(Context context) {
        WindowManager windowManager =
                (WindowManager) context.getApplicationContext().getSystemService(Context.
                        WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();
        Point outPoint = new Point();
        if (Build.VERSION.SDK_INT >= 19) {
            display.getRealSize(outPoint);
        } else {
            display.getSize(outPoint);
        }
        return outPoint.y;
    }

    public static int getScreenHeight1(Context context) {
        WindowManager windowManager =
                (WindowManager) context.getApplicationContext().getSystemService(Context.
                        WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();
        Point outPoint = new Point();
        if (Build.VERSION.SDK_INT >= 19) {
            display.getRealSize(outPoint);
        }
        return outPoint.y;
    }

    public static int getScreenHeight2(Context context) {
        WindowManager windowManager =
                (WindowManager) context.getApplicationContext().getSystemService(Context.
                        WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();
        Point outPoint = new Point();
        if (Build.VERSION.SDK_INT >= 19) {
            display.getSize(outPoint);
        }
        return outPoint.y;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }
}
