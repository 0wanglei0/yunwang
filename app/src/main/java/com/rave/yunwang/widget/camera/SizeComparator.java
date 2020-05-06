package com.rave.yunwang.widget.camera;


import android.util.Size;
import java.util.Comparator;

/**
 * 预览尺寸与给定的宽高尺寸比较器。首先比较宽高的比例，在宽高比相同的情况下，根据宽和高的最小差进行比较。
 */
public  class SizeComparator implements Comparator<Size> {
    private final int width;
    private final int height;
    //private final float ratio;
    public SizeComparator(int width, int height) {
        if (width < height) {
            this.width = height;
            this.height = width;
        } else {
            this.width = width;
            this.height = height;
        }
       // this.ratio = (float) this.height / this.width;
    }
    @Override
    public int compare(Size size1, Size size2) {
        int width1 = size1.getWidth();
        int height1 = size1.getHeight();
        int width2 = size2.getWidth();
        int height2 = size2.getHeight();
        long ratio1 = width1*height1;//Math.abs((float) height1 / width1 - ratio);
        long ratio2 =width2*height2; //Math.abs((float) height2 / width2 - ratio);
        int result = Long.compare(ratio1, ratio2);
        if (result != 0) {
            return result;
        } else {
            int minGap1 = Math.abs(width - width1) + Math.abs(height - height1);
            int minGap2 = Math.abs(width - width2) + Math.abs(height - height2);
            return minGap1 - minGap2;
        }
    }
}
