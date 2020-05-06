package com.rave.yunwang.widget.camera;

import android.graphics.Bitmap;

/**
 * Created by paipeng on 16/02/16.
 */
public interface DemoCameraBaseInterface {

    /**
     * 处理视频流回调数据
     * @param data
     * @param previewWidth
     * @param previewHeight
     * @param imageType
     */
    void receivePreviewImageData(byte[] data, int previewWidth, int previewHeight, int imageType);
    void receivePreviewImageData(Bitmap bitmap, byte[] data, int imageType);
}
