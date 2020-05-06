package com.rave.yunwang.widget.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.params.MeteringRectangle;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.rave.yunwang.R;
import com.rave.yunwang.application.MyApplication;
import com.rave.yunwang.utils.MediaUtils;
import com.rave.yunwang.utils.RxRunner;

import java.io.File;
import java.nio.ByteBuffer;

public abstract class ATextureView extends TextureView {

    private static final String TAG = "ATextureView";

    public static final int ROTATION_90 = 90;

    public static final int ROTATION_270 = 270;

    protected static final int VIDEO_MIN_TIME = 0;

    protected static final int VIDEO_MAX_TIME = Integer.MAX_VALUE;

    protected static final int FAIL = 0x1;

    protected boolean isS2iOpen = false;
    /**
     * Camera
     */
    protected static final int CAMERA_PREPARE = 0x2;

    protected static final int CAMERA_PREVIEW = 0x3;

    /**
     * Picture
     */
    protected static final int PICTURE = 0x4;

    protected static final int PICTURE_SUCCESS = 0x5;

    /**
     * Video
     */
    protected static final int VIDEO = 0x6;

    protected static final int VIDEO_TIME = 0x7;

    protected static final int VIDEO_MIN = 0x8;

    protected static final int VIDEO_SUCCESS = 0x9;

    /**
     * FOCUS
     */
    protected static final int FOUCS = 0x10;

    public void setS2iSwitch(boolean b) {
        this.isS2iOpen = b;
    }

    public enum CameraState {
        FRONT, BACK, CLOSE
    }

    public enum ControllState {
        FAIL,
        CAMERA_PREPARE, CAMERA_PREVIEW,
        PICTURE, PICTURE_SUCCESS,
        VIDEO, VIDEO_SUCCESS
    }

    public interface OnCameraListener {

        void onFail();

        void onCameraPrepare();

        void onCameraPreview();

        void onPicture();

        void onPictureSuccess();

        void onVideo();

        void onVideoSuccess();

        void onVideoTime(String count);

        void onVideoMin();

        void onFoucs(Point point);

    }

    protected CameraState cameraState = CameraState.CLOSE;

    protected ControllState controllState = ControllState.FAIL;

    protected File photoFile;

    protected File videoFile;

    protected ManagerUtils<OnCameraListener> managerUtils = new ManagerUtils<OnCameraListener>() {
        @Override
        protected void handleMsg(Message message) {

            Msg params = (Msg) message.obj;

            switch (message.what) {

                case FAIL:
                    for (OnCameraListener linstener :
                            listeners)
                        linstener.onFail();
                    break;

                case CAMERA_PREPARE:
                    for (OnCameraListener linstener :
                            listeners)
                        linstener.onCameraPrepare();
                    break;

                case CAMERA_PREVIEW:
                    for (OnCameraListener linstener :
                            listeners)
                        linstener.onCameraPreview();
                    break;

                case PICTURE:
                    for (OnCameraListener linstener :
                            listeners)
                        linstener.onPicture();
                    break;

                case PICTURE_SUCCESS:
                    for (OnCameraListener linstener :
                            listeners)
                        linstener.onPictureSuccess();
                    break;

                case VIDEO:
                    for (OnCameraListener linstener :
                            listeners)
                        linstener.onVideo();
                    break;

                case VIDEO_TIME:
                    for (OnCameraListener linstener :
                            listeners)
                        linstener.onVideoTime((String) params.getList().get(0));
                    break;

                case VIDEO_MIN:
                    for (OnCameraListener linstener :
                            listeners)
                        linstener.onVideoMin();
                    break;

                case VIDEO_SUCCESS:
                    for (OnCameraListener linstener :
                            listeners)
                        linstener.onVideoSuccess();
                    break;

                case FOUCS:
                    for (OnCameraListener linstener :
                            listeners)
                        linstener.onFoucs((Point) params.getList().get(0));
                    break;
            }
        }
    };


    private enum MODE {
        NONE, FOCUS, ZOOM;
    }

    private Matrix matrix = new Matrix();

    private MODE mode = MODE.NONE;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private abstract class FocusRunnable implements Runnable {

        protected Point point;

    }

    public ATextureView(Context context) {
        super(context);
        setup();
    }

    public ATextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public ATextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    public abstract void capture();

    public abstract void recordStart();

    public abstract void recordStop();

    public abstract void closeCamera();

    public abstract void startPreview();

    public abstract void stopPreview();

    public abstract void release();

    public abstract void zoomArea(boolean isZoom);

    public abstract void setZoom(float nZoom);

    public abstract void setDemoCameraBaseInterface(DemoCameraBaseInterface demoCameraBaseInterface);

    public abstract void openFlash();

    public abstract void closeFlash();

    public abstract void getVinPicture(Bitmap bitmap);

    public ControllState getControllState() {
        return controllState;
    }

    public File getPhotoFile() {
        return photoFile;
    }

    public void setPhotoFile(File file) {
        this.photoFile = file;
    }

    public File getVideoFile() {
        return videoFile;
    }

    public void setVideoFile(File videoFile) {
        this.videoFile = videoFile;
    }

    public ManagerUtils<OnCameraListener> getManagerUtils() {
        return managerUtils;
    }

    protected void setTextureSize(int previewWidth, int previewHeight, int width, int height) {

        float scale = Math.max(1.0f * width / previewWidth
                , 1.0f * height / previewHeight);

        float previewScaledWidth = previewWidth * scale;
        float previewScaledHeight = previewHeight * scale;

        float scaleX = previewScaledWidth / width;
        float scaleY = previewScaledHeight / height;

        matrix.setScale(scaleX, scaleY);
        setTransform(matrix);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        Log.i(TAG, "onMeasure " + width + "-" + height);

        if (height <=1280) {
            setMeasuredDimension((int) (width * (height / 1300.0)), height);
        } else if (height ==2220 && width == 1080) {
            setMeasuredDimension((int) (width*1.1), height);
        } else if (height ==2160 && width == 1080) {
            setMeasuredDimension((int) (width*1.1), height);
        } else if (height ==2160 && width == 1296) {
            setMeasuredDimension((int) width, height);
        } else if (height ==2611 && width == 1439) {
        } else {
            //setMeasuredDimension((int)(width*(height/1440.0)), height);
            setMeasuredDimension((int)(height/4.0*3.0), height);
        }
    }

    protected boolean isRange(int standard, int compare) {
        if (compare >= standard) return true;
        else if (standard - compare < 50) return true;
        return false;
    }

    protected void savePicture(final int width, final int height) {

        RxRunner.getInstance().doTask(new RxRunner.Runner(null, RxRunner.ThreadState.thread, new RxRunner.RunnerHandler(RxRunner.ThreadState.main) {
            @Override
            public void onError(Object data) {

            }

            @Override
            public void onResult(Object data) {
                if (controllState != ControllState.PICTURE) return;

                boolean isSuccess = (boolean) data;

                if (isSuccess) {
                    controllState = ControllState.PICTURE_SUCCESS;
                    managerUtils.sendMsg(PICTURE_SUCCESS);
                } else {
                    cameraState = CameraState.CLOSE;
                    controllState = ControllState.FAIL;
                    managerUtils.sendMsg(FAIL);
                }
            }
        }) {
            @Override
            public Object run(Object input) {
                try {

                    Bitmap bitmap = getBitmap();

                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                            bitmap.getHeight(), matrix, true);

                    int distanceW = Math.abs(bitmap.getWidth() - width);

                    int distanceH = Math.abs(bitmap.getHeight() - height);

                    if (distanceW != 0 || distanceH != 0)
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth() - distanceW,
                                bitmap.getHeight() - distanceH, null, false);

                    photoFile = MediaUtils.saveTakePicture(bitmap);

                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    Log.d(TAG, MyApplication.getInstance().getString(R.string.picture_oom));
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, e.toString());
                    return false;
                }

                return true;
            }
        });

    }

    protected void savePicture(byte[] data) {

        RxRunner.getInstance().doTask(new RxRunner.Runner(data, RxRunner.ThreadState.thread, new RxRunner.RunnerHandler(RxRunner.ThreadState.main) {
            @Override
            public void onError(Object data) {

            }

            @Override
            public void onResult(Object data) {
                if (controllState != ControllState.PICTURE) return;

                boolean isSuccess = (boolean) data;

                if (isSuccess) {
                    controllState = ControllState.PICTURE_SUCCESS;
                    managerUtils.sendMsg(PICTURE_SUCCESS);
                } else {
                    cameraState = CameraState.CLOSE;
                    controllState = ControllState.FAIL;
                    managerUtils.sendMsg(FAIL);
                }
            }
        }) {
            @Override
            public Object run(Object input) {
                try {

                    if (input == null) {
                        Log.d(TAG, MyApplication.getInstance().getString(R.string.picture_empty));
                        return false;
                    }

                    byte[] data = (byte[]) input;

                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                    if (cameraState.equals(CameraState.BACK)) {
                        bitmap = MediaUtils.wtitePictureDegree(bitmap, ROTATION_90, false);
                    } else if (cameraState.equals(CameraState.FRONT)) {
                        bitmap = MediaUtils.wtitePictureDegree(bitmap, ROTATION_270, true);
                    }

                    photoFile = MediaUtils.saveTakePicture(bitmap);

                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    Log.d(TAG, MyApplication.getInstance().getString(R.string.picture_oom));
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, e.toString());
                    return false;
                }

                return true;
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected void savePicture(ImageReader reader) {

        RxRunner.getInstance().doTask(new RxRunner.Runner(reader, RxRunner.ThreadState.thread, new RxRunner.RunnerHandler(RxRunner.ThreadState.main) {
            @Override
            public void onError(Object data) {

            }

            @Override
            public void onResult(Object data) {

                if (controllState != ControllState.PICTURE) return;

                boolean isSuccess = (boolean) data;
                if (isSuccess) {
                    controllState = ControllState.PICTURE_SUCCESS;
                    managerUtils.sendMsg(PICTURE_SUCCESS);
                } else {
                    cameraState = CameraState.CLOSE;
                    controllState = ControllState.FAIL;
                    managerUtils.sendMsg(FAIL);
                }
            }
        }) {

            @Override
            public Object run(Object input) {
                try {

                    if (input == null) {
                        Log.d(TAG, MyApplication.getInstance().getString(R.string.picture_empty));
                        return false;
                    }

                    ImageReader reader = (ImageReader) input;
                    Image image = reader.acquireLatestImage();

                    ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
                    byte[] data = new byte[byteBuffer.remaining()];
                    byteBuffer.get(data);
                    image.close();

                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    if (cameraState.equals(CameraState.BACK)) {
                        bitmap = MediaUtils.wtitePictureDegree(bitmap, ROTATION_90, false);
                    } else if (cameraState.equals(CameraState.FRONT)) {
                        bitmap = MediaUtils.wtitePictureDegree(bitmap, ROTATION_270, true);
                    }


                    photoFile = MediaUtils.saveTakePicture(bitmap);

                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    Log.d(TAG, MyApplication.getInstance().getString(R.string.picture_oom));
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, e.toString());
                    return false;
                }

                return true;
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected MeteringRectangle convertToCameraMR(Point touchPoint, int width, int height, int size, int weight, Rect rect) {
//        Rect rect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
//        LogUtils.d(TAG, "active Rect:" + rect.toString());

        int newX = touchPoint.y;
        int newY = width - touchPoint.x;

        newX = (int) ((1.0f * newX / height) * rect.right);
        newY = (int) ((1.0f * newY / width) * rect.bottom);

//        int newX = touchPoint.x / width * rect.bottom;
//        int newY = touchPoint.y / height * rect.right;
//
//        int focusX = newY;
//        int focusY = rect.bottom - newX;


        Rect focusRect = new Rect(clamp(newX - size, rect.left, rect.right)
                , clamp(newY - size, rect.top, rect.top)
                , clamp(newX + size, rect.left, rect.right)
                , clamp(newY + size, rect.top, rect.bottom));

        return new MeteringRectangle(focusRect, weight);
    }


    private void setup() {

        setOnTouchListener(new OnTouchListener() {

            float oldDist;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (controllState != ControllState.CAMERA_PREVIEW && controllState != ControllState.PICTURE_SUCCESS && controllState != ControllState.VIDEO)
                    return true;

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
//                        focusRunnable.point = new Point((int) event.getX(), (int) event.getY());
//                        mHandler.postDelayed(focusRunnable, 500);
//                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:
//                        mHandler.removeCallbacks(focusRunnable);
//                        mode = MODE.ZOOM;
//                        oldDist = getFingerSpacing(event);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (mode.equals(MODE.ZOOM)) {

                            float newDist = getFingerSpacing(event);

                            if (newDist > oldDist) {
                                zoomArea(true);
                            } else if (newDist < oldDist) {
                                zoomArea(false);
                            }

                            oldDist = newDist;
                        }

                        break;

                    case MotionEvent.ACTION_UP:
                        mode = MODE.NONE;
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        mode = MODE.NONE;
                        break;
                }

                return true;
            }
        });

    }

    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

}
