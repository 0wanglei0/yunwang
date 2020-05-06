package com.rave.yunwang.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import androidx.drawerlayout.widget.DrawerLayout;

import com.rave.yunwang.R;
import com.rave.yunwang.application.BaseApplication;
import com.rave.yunwang.application.MyApplication;
import com.rave.yunwang.utils.MediaUtils;
import com.rave.yunwang.utils.RxRunner;
import com.rave.yunwang.widget.camera.ATextureView;
import com.rave.yunwang.widget.camera.DemoCameraBaseInterface;
import com.rave.yunwang.widget.camera.ManagerUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

/**
 * 定义摄像配置基类，该类包括设摄像头设置，摄像头并将摄像头内容绘制到surfaceView上
 * <p/>
 * ?本类待修改:目前本类与CameraView为互相调用关系，在逻辑上会本类不够独立
 * ?日后可在本类视频设备产生事件时，向CameraView发消息，后续逻辑处理由前台类完成
 *
 * @author Gaoyue 已修正 2013/07/26
 */

@SuppressLint("NewApi")
public class CameraBase extends SurfaceView implements SurfaceHolder.Callback, Runnable, Camera.AutoFocusCallback,
        Camera.PreviewCallback, Camera.PictureCallback, Camera.ErrorCallback {
    public static final String TAG = CameraBase.class.getSimpleName();
    private Timer mTimer;
    protected File photoFile;

    protected File videoFile;
    private long mStartTime;
    private int mNextTime;
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
    public static final int ROTATION_90 = 90;

    public static final int ROTATION_270 = 270;

    protected static final int VIDEO_MIN_TIME = 0;

    protected static final int VIDEO_MAX_TIME = Integer.MAX_VALUE;
    public enum CameraState {
        FRONT, BACK, CLOSE
    }

    public enum ControllState {
        FAIL,
        CAMERA_PREPARE, CAMERA_PREVIEW,
        PICTURE, PICTURE_SUCCESS,
        VIDEO, VIDEO_SUCCESS
    }
    private MediaRecorder mMediaRecorder;
    protected CameraState cameraState = CameraState.CLOSE;

    protected ControllState controllState = ControllState.FAIL;

    private boolean isPreviewStart;
    private DemoCameraBaseInterface demoCameraBaseInterface;
    public static Timer m_objAlwaysTime = null; // 持续对焦时间片

    public void setDemoCameraBaseInterface(DemoCameraBaseInterface demoCameraBaseInterface) {
        this.demoCameraBaseInterface = demoCameraBaseInterface;
    }

    public void getVinPicture(Bitmap bitmap) {
        try {
            controllState = ControllState.PICTURE;
            managerUtils.sendMsg(PICTURE);

            photoFile = MediaUtils.saveTakePicture(bitmap);
            if (photoFile != null) {
                controllState = ControllState.PICTURE_SUCCESS;
                managerUtils.sendMsg(PICTURE_SUCCESS);
            } else {
                cameraState = CameraState.CLOSE;
                controllState = ControllState.FAIL;
                managerUtils.sendMsg(FAIL);
            }
        } catch (Exception e) {
            e.printStackTrace();
            cameraState = CameraState.CLOSE;
            controllState = ControllState.FAIL;
            managerUtils.sendMsg(FAIL);
        }
    }

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

    public void capture() {
        try {
            controllState = ControllState.PICTURE;
            managerUtils.sendMsg(PICTURE);
        } catch (Exception e) {
            e.printStackTrace();
            cameraState = CameraState.CLOSE;
            controllState = ControllState.FAIL;
            managerUtils.sendMsg(FAIL);
        }
    }

    public void savePicture(final Bitmap bitmap) {
        RxRunner.getInstance().doTask(new RxRunner.Runner(bitmap, RxRunner.ThreadState.thread, new RxRunner.RunnerHandler(RxRunner.ThreadState.main) {
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

    protected ManagerUtils<ATextureView.OnCameraListener> managerUtils = new ManagerUtils<ATextureView.OnCameraListener>() {
        @Override
        protected void handleMsg(Message message) {

            Msg params = (Msg) message.obj;

            switch (message.what) {

                case FAIL:
                    for (ATextureView.OnCameraListener linstener :
                            listeners)
                        linstener.onFail();
                    break;

                case CAMERA_PREPARE:
                    for (ATextureView.OnCameraListener linstener :
                            listeners)
                        linstener.onCameraPrepare();
                    break;

                case CAMERA_PREVIEW:
                    for (ATextureView.OnCameraListener linstener :
                            listeners)
                        linstener.onCameraPreview();
                    break;

                case PICTURE:
                    for (ATextureView.OnCameraListener linstener :
                            listeners)
                        linstener.onPicture();
                    break;

                case PICTURE_SUCCESS:
                    for (ATextureView.OnCameraListener linstener :
                            listeners)
                        linstener.onPictureSuccess();
                    break;

                case VIDEO:
                    for (ATextureView.OnCameraListener linstener :
                            listeners)
                        linstener.onVideo();
                    break;

                case VIDEO_TIME:
                    for (ATextureView.OnCameraListener linstener :
                            listeners)
                        linstener.onVideoTime((String) params.getList().get(0));
                    break;

                case VIDEO_MIN:
                    for (ATextureView.OnCameraListener linstener :
                            listeners)
                        linstener.onVideoMin();
                    break;

                case VIDEO_SUCCESS:
                    for (ATextureView.OnCameraListener linstener :
                            listeners)
                        linstener.onVideoSuccess();
                    break;

                case FOUCS:
                    for (ATextureView.OnCameraListener linstener :
                            listeners)
                        linstener.onFoucs((Point) params.getList().get(0));
                    break;
            }
        }
    };

    public ManagerUtils<ATextureView.OnCameraListener> getManagerUtils() {
        return managerUtils;
    }

    private void createVideo() {
        try {
            mMediaRecorder = new MediaRecorder();
            m_objCamera.unlock();
            mMediaRecorder.setCamera(m_objCamera);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setVideoEncodingBitRate(10000000);
            mMediaRecorder.setVideoEncodingBitRate(900*1024);
            mMediaRecorder.setVideoFrameRate(30);
            if (m_objCameraResolution != null) {
                mMediaRecorder.setVideoSize(m_objCameraResolution.x, m_objCameraResolution.y);
            } else {
                mMediaRecorder.setVideoSize(1920, 1080);
            }

            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setOrientationHint(ROTATION_90);
            mMediaRecorder.setPreviewDisplay(m_objHolder.getSurface());

            //输出路径
            videoFile = MediaUtils.saveTakeVideo();
            mMediaRecorder.setOutputFile(videoFile.getAbsolutePath());
            mMediaRecorder.prepare();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public void takePicture() {
        try {
            if (null != m_objCamera) {
//                openFlashWhileCapture();
                m_objCamera.takePicture(null, null, pictureCallback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 拍照时闪光灯开启
     */
    public void openFlashWhileCapture() {
        try {
            m_objCameraParam.setFlashMode(Parameters.FLASH_MODE_TORCH);
            m_objCamera.setParameters(m_objCameraParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (data != null) {
                //解析生成相机返回的图片
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                savePicture(bitmap);
                demoCameraBaseInterface.receivePreviewImageData(bitmap, data, 8);
            }
        }
    };

    private void destroyVideo() {

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
            } catch (Exception e) {
                mMediaRecorder.reset();
            }
            mMediaRecorder.release();
            mMediaRecorder = null;
        }

    }

    // 相机控制线程状态值
    public enum CameraThreadState {
        STATE_STAN_BY, // 闲置状态
    }

    // 相机动作状态
    public enum CameraAction {
        ACTION_NONE, ACTION_FOCUS // 对焦
    }

    public Camera m_objCamera = null; // 摄像机设备
    private SurfaceHolder m_objHolder = null; // 表面持有
    private boolean m_bIsRun = false; // 线程控制标志，FALSE时，线程自动退出

    private CameraThreadState m_eCameraState = CameraThreadState.STATE_STAN_BY; // 线程监控状态，靠这个状态来进行后续操作
    private CameraAction m_eCameraAction = CameraAction.ACTION_NONE; // 相机当期那动作

    private static Parameters m_objCameraParam; // 摄像头参数
    private Thread m_objCameraThread = null; // 线程句柄

    private static final Pattern COMMA_PATTERN = Pattern.compile(",");
    private Point m_objCameraResolution;
    private boolean cameraConfigured = false;
    private DrawerLayout frame = null;

    public CameraBase(Context context){
        super(context);
        initAll();
    }

    /**
     * 初始化各主要对象及状态
     */
    private void initAll() {
        this.setVisibility(INVISIBLE);
        // 取得该view的控制权限,getHolder函数属于SurfaceView对象
        m_objHolder = getHolder();
        // m_objHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 添加设备控制回调监听器，这样在view被创建的时候就会进行相应的回调
        m_objHolder.addCallback(this);
        m_eCameraState = CameraThreadState.STATE_STAN_BY;
    }

    /**
     * 找到最好的视频流尺寸
     *
     * @param previewSizeValueString 获取的预览尺寸字符串
     * @param screenResolution       屏幕宽高
     * @return 视频流尺寸
     */
    private static Point findBestPreviewSizeValue(CharSequence previewSizeValueString, Point screenResolution) {
        int nBestX = 0;
        int nBestY = 0;
        int nDiff = Integer.MAX_VALUE;
        for (String strPreviewSize : COMMA_PATTERN.split(previewSizeValueString)) {

            strPreviewSize = strPreviewSize.trim();
            int nDimPosition = strPreviewSize.indexOf('x');
            if (nDimPosition < 0) {
                continue;
            }

            int nNewX;
            int nNewY;
            try {
                nNewX = Integer.parseInt(strPreviewSize.substring(0, nDimPosition));
                nNewY = Integer.parseInt(strPreviewSize.substring(nDimPosition + 1));
            } catch (NumberFormatException nfe) {
                continue;
            }
            int nNewDiff = Math.abs(nNewX - screenResolution.y) + Math.abs(nNewY - screenResolution.x);
            if (nNewDiff == 0) {
                nBestX = nNewX;
                nBestY = nNewY;
                break;
            } else if (nNewDiff < nDiff) {
                nBestX = nNewX;
                nBestY = nNewY;
                nDiff = nNewDiff;
            }
        }

        Camera.Size result = null;
        if (BaseApplication.s2iClientInitResult.getS2iParam().getPreviewImageWidth() != 0 && BaseApplication.s2iClientInitResult.getS2iParam().getPreviewImageHeight() != 0) {//后台预览尺寸判断，全都不为0时使用后台的，否则使用代码计算最佳预览尺寸
            nBestX = BaseApplication.s2iClientInitResult.getS2iParam().getPreviewImageWidth();
            nBestY = BaseApplication.s2iClientInitResult.getS2iParam().getPreviewImageHeight();
        } else {//后台预览尺寸无效时，本地计算最佳预览尺寸
            for (Camera.Size size : m_objCameraParam.getSupportedPreviewSizes()) {
                if (result == null) {
                    float ratio = 0.5625f;
                    if (size.height * 1f / size.width == ratio) {
                        result = size;
                    }
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea &&  size.height * 1f / size.width == 0.75) {
                        result = size;
                    }
                }
            }
            if (result != null) {
                nBestX = result.width;
                nBestY = result.height;
            }
        }

        if (nBestX > 0 && nBestY > 0) {
            return new Point(nBestX, nBestY);
        }
        return null;
    }

    void initFromCameraParameters() {
        // 对Point对象赋值屏幕宽高
        Point m_objScreenResolution = new Point(getMeasuredWidth(), getMeasuredHeight());
        m_objCameraResolution = getCameraResolution(m_objScreenResolution);
    }

    /**
     * 获得相机分辨率
     *
     * @param screenResolution
     * @return
     */
    private Point getCameraResolution(Point screenResolution) {
        String strPreviewSizeValueString = m_objCameraParam.get("preview-size-values");
        if (strPreviewSizeValueString == null) {
            strPreviewSizeValueString = m_objCameraParam.get("preview-size-value");
        }
        Point objCameraResolution = null;
        if (strPreviewSizeValueString != null) {
            objCameraResolution = findBestPreviewSizeValue(strPreviewSizeValueString, screenResolution);
        }
        if (objCameraResolution == null) {
            objCameraResolution = new Point((screenResolution.x >> 3) << 3, (screenResolution.y >> 3) << 3);
        }
        return objCameraResolution;
    }

    /**
     * 设置焦距
     * @param nZoom 1 到 100 百分率
     */
    public void setZoom(float nZoom) {
        try {
            if (m_objCameraParam != null) {
                m_objCameraParam.setZoom(calculateZoomByDpi(BaseApplication.s2iClientInitResult.getS2iParam().getZoom()));
                m_objCamera.setParameters(m_objCameraParam);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int calculateZoomByDpi(float zoomRatio) {
        if (zoomRatio == 0 || zoomRatio == 1) {
            return 100;
        }

        int maxZoom = m_objCameraParam.getMaxZoom();
        if (maxZoom == 0) {
            return 100;
        }
        int baseZoom = (int) (zoomRatio * 100);

        // camera2
        int zoom = Math.round (1f * baseZoom / maxZoom);

        // camera1
        if (m_objCameraParam.getZoomRatios() != null) {
            int index = closestNumber(m_objCameraParam.getZoomRatios().toArray(new Integer[0]), baseZoom) + 1;
            zoom = Math.round(index * 100 / m_objCameraParam.getMaxZoom());
        }

        if (zoom <= 0 || zoom > 100) {
            zoom = 100;
        }

        return zoom;
    }

    /**
     * @param A an integer array sorted in ascending order
     * @param target an integer
     * @return an integer
     */
    private static int closestNumber(Integer[] A, int target) {
        if (A == null || A.length == 0) {
            return -1;
        }
        int index = firstIndex(A, target);
        if (index == 0) {
            return 0;
        }
        if (index == A.length) {
            return A.length - 1;
        }
        if (Math.abs(A[index] - target) < Math.abs(A[index - 1] - target)) {
            return index;
        }
        return index - 1;
    }

    //定义一个方法，典型的二分查找模板
    private static int firstIndex(Integer[] A, int target) {
        int start = 0;
        int end = A.length - 1;
        //二分查找
        while (start + 1 < end) {
            int mid = start + (end - start) / 2;
            if (A[mid] == target) {
                return mid;
            }
            if (A[mid] < target) {
                start = mid;
            }
            if (A[mid] > target) {
                end = mid;
            }
        }

        if (A[end] >= target) {
            return end;
        }
        //此为正常情况的A[start] = target和异常情况target < A[0]此时start = 0
        if (A[start] >= target) {
            return start;
        }
        //此为异常情况的target大于A[A.length - 1]
        return A.length;
    }

    /**
     * 设置对焦区域 设置对焦点在屏幕正中心
     */
    private void setFocusArea() {
        if (m_objCameraParam != null) {
            int maxNumFocusAreas = m_objCameraParam.getMaxNumFocusAreas();
            if (maxNumFocusAreas > 0) {
                ArrayList<Camera.Area> al = new ArrayList<Camera.Area>();
                int focus_size = 100;
                Camera.Area a = new Camera.Area(new Rect(-focus_size / 2, -focus_size / 2, focus_size / 2, focus_size / 2), 1000);
                al.add(a);

                try {
                    m_objCameraParam.setFocusAreas(al);
                    m_objCamera.setParameters(m_objCameraParam);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 设置报告区域 在中间区域 视频流长宽的各1/10中间区域，测试权重最高 1000
     */
    private void setExposureArea() {
        try {
            if (m_objCameraParam != null) {
                ArrayList<Camera.Area> al = new ArrayList<Camera.Area>();
                Camera.Area a = new Camera.Area(new Rect(-100, -100, 100, 100), 1000);
                al.add(a);
                m_objCameraParam.setMeteringAreas(al);
                m_objCamera.setParameters(m_objCameraParam);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 如果支持视频流防抖功能，启动防抖功能
     */
    private void startStabilizationIfSupported() {
        if (m_objCameraParam != null) {
            if (m_objCameraParam.isVideoStabilizationSupported() && !m_objCameraParam.getVideoStabilization()) {
                try {
                    m_objCameraParam.setVideoStabilization(true);
                    m_objCamera.setParameters(m_objCameraParam);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 设置对焦模式
     */
    public void initFocusMode() {
        if (m_objCameraParam != null) {
            List<String> focusModeList = m_objCameraParam.getSupportedFocusModes();
            if (focusModeList != null && focusModeList.size() > 0) {
                createAlwaysFocusTimer(2000);
                m_objCameraParam.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
        }
    }

    /**
     * 创建对焦时间片，即X秒后调用AUTOFOCUS
     */
    public void createAlwaysFocusTimer(long lPeriod) {
        if (m_objAlwaysTime == null) {
            m_objAlwaysTime = new Timer();
            CameraViewTimerTask m_objTimerTask = new CameraViewTimerTask();
            m_objAlwaysTime.schedule(m_objTimerTask, 100, lPeriod);
        }
    }

    /**
     * 时间片对焦类 该类负责当一定时间到来时，调用相机AUTOFOCUS进行对焦操作 主要解决某些不支持跟踪对焦模式的相机，在进入该类开始时
     * 进行一次autofocus对焦模式，让当前画面看起来比较清晰
     */
    public class CameraViewTimerTask extends TimerTask {
        private static final int N_MSG_AUTOFOCUS = 0;
        //@SuppressLint("HandlerLeak")
        @SuppressLint("HandlerLeak")
        Handler m_objHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == N_MSG_AUTOFOCUS) {
                    setCameraAction(CameraAction.ACTION_FOCUS);
                }
                super.handleMessage(msg);
            }
        };

        // 时间来临时向句柄发送消息，指导相机进行autofocus操作
        @Override
        public void run() {
            // Log.i("CameraViewTimerTask","持续对焦时间片!");
            Message message = new Message();
            message.what = N_MSG_AUTOFOCUS;
            m_objHandler.sendMessage(message);
        }

        @Override
        public long scheduledExecutionTime() {
            return super.scheduledExecutionTime();
        }
    }

    /**
     * 设置当前相机动作，并激活后台线程按照当前动作执行指令
     *
     * @param eAction 给入照相指令
     */
    public void setCameraAction(CameraAction eAction) {
        m_eCameraAction = eAction;
        if (m_objCameraThread != null) {
            //noinspection SynchronizeOnNonFinalField
            synchronized (m_objCameraThread) {
                m_objCameraThread.notify();
            }
        }
    }

    /**
     * Camera.AutoFocusCallback
     * 相应相机的自动对焦回调函数,该函数会返回相机对焦结果
     * @param success 对焦是否成功
     * @param camera 返回照相机对象
     */
    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (success) {
            try {
                m_objCamera.cancelAutoFocus();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 切换成初始状态，初始化相机控制参数
     */
    private void cameraStandBy() {
        if (m_objCamera == null) {
            return;
        }
        try {
            m_objCamera.setParameters(m_objCameraParam);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 激活后台线程处理
        m_eCameraState = CameraThreadState.STATE_STAN_BY;// 切换状态
        synchronized (m_objCameraThread) {
            m_objCameraThread.notify();
        }
    }

    /**
     * 打开相机
     */
    public int openCamera(int cameraId) {
        stopPreview();
        try {
            m_objCamera = Camera.open(cameraId);
            
            // 取得摄像头属性
            if (m_objCameraParam == null && m_objCamera != null) {
                m_objCameraParam = m_objCamera.getParameters();
//                openFlash();
                setZoom(1f);
            }

            // 将摄像头的预览画面与表面绑定
            m_objCamera.setPreviewDisplay(m_objHolder);
            m_objCamera.setErrorCallback(this);

            m_eCameraAction = CameraAction.ACTION_NONE;
            // 设置启动预览标识
            m_bIsRun = true;
            // 启动对焦线程
            m_objCameraThread = (new Thread(this));
            m_objCameraThread.start();

            return 0;
        } catch (Exception e) {
            stopPreview();
            return -1;
        }
    }

    /**
     * 初始化相机参数
     */
    private void initCamera() {
        if (m_objCamera == null)
            return;
        m_objCamera.setDisplayOrientation(90);
        initFromCameraParameters();
        // 设置对焦区域
        setFocusArea();
        setExposureArea();
        startStabilizationIfSupported();
        // 设置新的摄像头参数
        try {
            m_objCameraParam.setPreviewSize(m_objCameraResolution.x, m_objCameraResolution.y);
//            m_objCameraParam.setPictureSize(m_objCameraResolution.x, m_objCameraResolution.y);
            setPicSize();
            m_objCamera.setParameters(m_objCameraParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPicSize() {
        if (m_objCameraParam == null)
            return;

        int nPhotoSizeWidth = 0;
        int nPhotoSizeHeight = 0;

        List<Camera.Size> arrSizeList = m_objCameraParam.getSupportedPictureSizes();
        // 如果获取支持的尺寸数组中，最后一个大于第一个，使用最后一个尺寸（最大尺寸）
        // 否则，使用第一个（因为尺寸是按照顺序排练的，不是最后一个最大，就是第一个最大）
        if (arrSizeList.get(arrSizeList.size() - 1).width > arrSizeList.get(0).width) {
            nPhotoSizeWidth = arrSizeList.get(arrSizeList.size() - 1).width;
            nPhotoSizeHeight = arrSizeList.get(arrSizeList.size() - 1).height;
        } else {
            nPhotoSizeWidth = arrSizeList.get(0).width;
            nPhotoSizeHeight = arrSizeList.get(0).height;
        }
        m_objCameraParam.setPictureSize(nPhotoSizeWidth, nPhotoSizeHeight);
    }

    /**
     * SurfaceHolder.Callback surfaceView创建回调
     * @param holder 视频流显示对象
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        int cameraId = BaseApplication.s2iClientInitResult.getS2iParam().getCameraId();
        if (cameraId == 0) {//cameraId为0时，使用默认摄像头
            if (openCamera(Camera.CameraInfo.CAMERA_FACING_BACK) == 0) {
                cameraStandBy();
            }
        } else {
            int cameraCount = Camera.getNumberOfCameras();
            if (cameraId < 0 || cameraId >= cameraCount) {//cameraId的范围为 0~cameraCount-1
                if (openCamera(Camera.CameraInfo.CAMERA_FACING_BACK) == 0) {//cameraId无效时使用默认摄像头
                    cameraStandBy();
                }
            } else {
                if (openCamera(cameraId) == 0) {//启动指定摄像头
                    cameraStandBy();
                }
            }
        }
    }

    /**
     * SurfaceHolder.Callback
     * 表面创建完毕的系列配置
     * @param holder The SurfaceHolder whose surface has changed.
     * @param format The new PixelFormat of the surface.
     * @param width The new width of the surface.
     * @param height The new height of the surface.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        initCamera();
        // 对视频流的尺寸对应屏幕进行优化
        // 可以解决视频流尺寸一致，但是会导致View宽度也变化而产生显示不全的问题
        // 避免 camera1下设置后台视频流尺寸，而使用默认的1920x1080，问题可以解决

        int Y = getResources().getDisplayMetrics().heightPixels;
        int X = getResources().getDisplayMetrics().widthPixels;
        if (height*1.0/width >= 1812/1080f) {
            initPreview();
        }
        // 启动预览
        startPreviewWithFocusAndFlash();
    }

    /**
     * 失去表面时调用该回调
     * @param holder The SurfaceHolder whose surface has changed.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (m_objCamera == null) {
            return;
        }
        // 设置预览标识
        m_bIsRun = false;
        m_eCameraState = CameraThreadState.STATE_STAN_BY;
        synchronized (m_objCameraThread) {
            m_objCameraThread.notify();
        }
        // 停止预览
        stopPreview();
    }

    /**
     * 启动视频流及闪光灯, 并处理对焦模式
     */
    public void startPreviewWithFocusAndFlash() {
        // 启动预览
        if (m_objCamera != null) {
            m_objCamera.startPreview();
            // 开启视频流回调
            setPreviewCallStart();
            isPreviewStart = true;
            controllState = ControllState.CAMERA_PREVIEW;
            managerUtils.sendMsg(CAMERA_PREVIEW);
        }

        initFocusMode();
    }

    /**
     * 停止视频流
     */
    public void stopPreview() {
        if (mMediaRecorder != null) {
            try {
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                }
                mMediaRecorder.stop();//暂停录制
                mMediaRecorder.release();
                mMediaRecorder = null;
            } catch (Exception e) {
                mMediaRecorder.reset();//重置,将MediaRecorder调整为空闲状态
            }
        }

        stopContinuFocus();
        if (m_objCamera != null) {
            closeFlash();
            setPreviewCallStop();
            isPreviewStart = false;
        }
    }

    /**
     * 当拍摄完照片后，操控控件显示状态
     */
    private void stopContinuFocus() {
        if (m_objAlwaysTime != null) {
            m_objAlwaysTime.cancel();
            m_objAlwaysTime = null;
        }
    }

    /**
     * 视频流回调开启
     */
    public void setPreviewCallStart() {
        if (m_objCamera != null) {
            m_objCamera.setPreviewCallback(this);
        }
    }

    /**
     * 视频流回调关闭
     */
    public void setPreviewCallStop() {
        if (m_objCamera != null) {
            m_objCamera.setPreviewCallback(null);
        }
    }

    /**
     * Camera.PreviewCallback 视频流回调方法，交给Activity进行处理图像
     * @param data 返回的YUV420格式数据
     * @param camera 照相机对象
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        try {
            demoCameraBaseInterface.receivePreviewImageData(data,
                    camera.getParameters().getPreviewSize().width,
                    camera.getParameters().getPreviewSize().height,
                    1);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Camera.PictureCallback 拍照回调
     * @param data JPEG 格式数据
     * @param camera 照相机对象
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        cameraStandBy();
    }

    /**
     * Camera.onError 照相机错误回调 callback
     * @param error 错误代码
     * @param camera 照相机对象
     */
    @Override
    public void onError(int error, Camera camera) {
        cameraStandBy();
    }

    /**
     * 闪光灯开启
     */
    public void openFlash() {
        try {
            if (m_objCameraParam != null && m_objCamera != null) {
                m_objCameraParam.setFlashMode(Parameters.FLASH_MODE_TORCH);
                m_objCamera.setParameters(m_objCameraParam);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭闪光灯
     */
    public void closeFlash() {
        if (m_objCameraParam == null || m_objCamera == null) {
            return;
        }
        m_objCameraParam.setFlashMode(Parameters.FLASH_MODE_OFF);
        try {
            m_objCamera.setParameters(m_objCameraParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 线程控制函数
     */
    @SuppressWarnings("incomplete-switch")
    @Override
    public void run() {
        Looper.prepare();
        while (m_bIsRun) {
            synchronized (m_objCameraThread) {
                try {
                    m_objCameraThread.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                m_objCamera.cancelAutoFocus();
                m_objCamera.autoFocus(this);
            } catch (Exception e) {
                e.printStackTrace();
                if (m_objCamera != null) {
                    m_objCamera.cancelAutoFocus();
                    m_objCamera.autoFocus(this);
                }
            }
        }
        Looper.loop();
    }

    /**
     * 重新调整View解码的长宽比例（translationX）
     */
    private void initPreview() {
        //利用布局，重设宽高来撑开界面，超出的部分直接撑出屏幕，至于为什么用这两个布局，自己去谷歌了！
        if (m_objCamera != null && m_objHolder.getSurface() != null) {

            if (!cameraConfigured) {

                Camera.Size size = getBestPreviewSize();

                if (size != null) {
                    m_objCameraParam.setPreviewSize(size.width, size.height);
                    m_objCamera.setParameters(m_objCameraParam);
                    cameraConfigured = true;
                    // Setting up correctly the view
                    int Y = getResources().getDisplayMetrics().heightPixels;
                    int X = getResources().getDisplayMetrics().widthPixels;
                    if (frame != null) {
                        ViewGroup.LayoutParams params = frame.getLayoutParams();

                        params.width = (int) ( Y * 1f / size.width * size.height );

                        frame.setLayoutParams(params);
                        int deslocationX = (int) (params.width / 2.0 - X / 2.0);
                        frame.animate().translationX(-deslocationX);
                    }
                }
            }

            try {
                m_objCamera.setPreviewDisplay(m_objHolder);
                m_objCamera.setDisplayOrientation(90);
            } catch (Throwable t) {
                //错误处理，自己做相应的逻辑
                setVisibility(GONE);
            }
        }
    }

    /**
     * 设置最佳视频流尺寸
     *
     * @return 返回选取的视频流尺寸
     */
    private Camera.Size getBestPreviewSize() {
        Camera.Size result = null;

        for (Camera.Size size : m_objCameraParam.getSupportedPreviewSizes()) {
            if (result == null) {
                float ratio = 0.5625f;
                if (size.height * 1f / size.width == ratio) {
                    result = size;
                }
            } else {
                int resultArea = result.width * result.height;
                int newArea = size.width * size.height;

                // use 0.75!!!!
                if (newArea > resultArea && size.height * 1f / size.width == 0.75) {
                    result = size;
                }
            }
        }

        return result;
    }

    public void setFrame(DrawerLayout frame) {
        this.frame = frame;
    }

    public void setS2iSwitch(boolean b) {
        this.isS2iOpen = b;
    }

    public void recordStart() {

        if (!controllState.equals(ControllState.CAMERA_PREVIEW)) {
            return;
        }

        controllState = ControllState.VIDEO;
        managerUtils.sendMsg(VIDEO);


        if (mMediaRecorder == null) {
            createVideo();
        }
        mMediaRecorder.start();

        mTimer = new Timer();

        mStartTime = System.currentTimeMillis();

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                int second = Math.round((System.currentTimeMillis() - mStartTime) / 1000f);
                managerUtils.sendMsg(VIDEO_TIME, second + getContext().getString(R.string.video_s));

                if (second == VIDEO_MAX_TIME)
                    recordStop();

                Log.d(TAG, String.valueOf(second));
            }
        }, 0, 1000);

    }

    public void recordStop() {
        int second = Math.round((System.currentTimeMillis() - mStartTime) / 1000f);

        if (second < VIDEO_MIN_TIME) {
            managerUtils.sendMsg(VIDEO_MIN);
            return;
        }

        destroyVideo();
        controllState = ControllState.VIDEO_SUCCESS;
        managerUtils.sendMsg(VIDEO_SUCCESS);
    }
}
