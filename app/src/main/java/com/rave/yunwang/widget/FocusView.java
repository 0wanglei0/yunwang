package com.rave.yunwang.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * 对焦框类
 *
 */
public class FocusView extends View {
    private float focusBoxWidth;
    private float focusBoxHeight;

    public FocusView(Context context) {
        this(context, null);
    }

    public FocusView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas objCanvas) {
        drawMacroLayer(objCanvas);
    }

    public void drawMacroLayer(Canvas objCanvas) {
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();

        drawLayer(objCanvas, (w - focusBoxWidth) / 2f
                , (h - focusBoxHeight) / 2f
                , (w + focusBoxWidth) / 2f
                , (h + focusBoxHeight) / 2f);
    }

    protected void drawLayer(Canvas objCanvas, float left, float top, float right, float bottom) {
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        Paint layerPaint = new Paint();
        layerPaint.setColor(Color.parseColor("#99000000"));
        objCanvas.drawRect(0, 0, w, top, layerPaint);//上半部分蒙层
        objCanvas.drawRect(0, bottom, w, h, layerPaint);//下半部分蒙层
        objCanvas.drawRect(0, top, left, bottom, layerPaint);//左半部分蒙层
        objCanvas.drawRect(right, top, w, bottom, layerPaint);//右半部分蒙层
    }

    public void setFocusValue(float focusBoxWidth, float focusBoxHeight) {
        this.focusBoxWidth = focusBoxWidth;
        this.focusBoxHeight = focusBoxHeight;

        invalidate();
    }
}
