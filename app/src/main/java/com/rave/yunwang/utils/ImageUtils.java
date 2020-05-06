package com.rave.yunwang.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

import java.nio.ByteBuffer;

public class ImageUtils {
    public static Bitmap convertDataToBitmap(byte[] data, int imageWidth, int imageHeight) {
        try {
            //Create bitmap with width, height, and 4 bytes color (RGBA)
            Bitmap bmp = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
            final int pixCount = imageWidth * imageHeight;
            int[] intGreyBuffer = new int[pixCount];
            for (int i = 0; i < pixCount - 1; i++) {

                int greyValue = (int) data[i] & 0xff;
                intGreyBuffer[i] = 0xff000000 | (greyValue << 16) | (greyValue << 8) | greyValue;
            }

            bmp.setPixels(intGreyBuffer, 0, imageWidth, 0, 0, imageWidth, imageHeight);
            return bmp;
        } catch (Exception e) {
            Log.e("ImageUtils", "convertDataToBitmap " + e.getMessage());
            return null;
        }
    }

    /**
     * 调用后结果需要释放
     * 自由旋转BMP图像，并返回旋转后新图像
     *
     * @param objBmp   给入的 Bitmap 图片
     * @param nDegrees 旋转的角度 specified number of degrees
     * @return 返回的旋转后的 Bitmap 图片；null为失败
     */
    public static Bitmap rotateBmp(Bitmap objBmp, int nDegrees) {
        Bitmap objReturn = objBmp;
        if (nDegrees != 0 && objBmp != null) {
            Matrix objMatrix = new Matrix();
            objMatrix.setRotate(nDegrees, (float) objBmp.getWidth() / 2, (float) objBmp.getHeight() / 2);
            try {
                objReturn = Bitmap.createBitmap(objBmp, 0, 0, objBmp.getWidth(), objBmp.getHeight(), objMatrix, true);
                // free objBmp
                objBmp.recycle();
                objBmp = null;
            } catch (OutOfMemoryError ex) {
                // Android123建议大家如何出现了内存不足异常，最好return 原始的bitmap对象。
                Log.i("rotateBmp()", "内存不足");
                return objBmp;
            }
        }
        return objReturn;
    }

    public static Bitmap cropBitmap(Bitmap bitmap, Rect cropRect) {
        return Bitmap.createBitmap(bitmap, cropRect.left, cropRect.top, cropRect.width(), cropRect.height());
    }

    public static byte[] bitmapToGrayByteArray(Bitmap bm) {
        int iBytes = bm.getWidth() * bm.getHeight();
        byte[] res = new byte[iBytes];
        Bitmap.Config format = bm.getConfig();
        if (format == Bitmap.Config.ARGB_8888) {
            ByteBuffer buffer = ByteBuffer.allocate(iBytes * 4);
            bm.copyPixelsToBuffer(buffer);
            byte[] arr = buffer.array();
            for (int i = 0; i < iBytes; i++) {
                int A, R, G, B;
                R = (int) (arr[i * 4]) & 0xff;
                G = (int) (arr[i * 4 + 1]) & 0xff;
                B = (int) (arr[i * 4 + 2]) & 0xff;
                //A=arr[i*4+3];
                byte r = (byte) ((R + G + B) / 3);
                res[i] = r;
            }
        } else if (format == Bitmap.Config.RGB_565) {
            ByteBuffer buffer = ByteBuffer.allocate(iBytes * 2);
            // Log.e("DBG", buffer.remaining()+""); -- Returns a correct number based on dimensions
            // Copy to buffer and then into byte array
            bm.copyPixelsToBuffer(buffer);
            byte[] arr = buffer.array();
            for (int i = 0; i < iBytes; i++) {
                float A, R, G, B;
                R = ((arr[i * 2] & 0xF8));
                G = ((arr[i * 2] & 0x7) << 5) + ((arr[i * 2 + 1] & 0xE0) >> 5);
                B = ((arr[i * 2 + 1] & 0x1F) << 3);
                byte r = (byte) ((R + G + B) / 3);
                res[i] = r;
            }
        }
        return res;
    }

}
