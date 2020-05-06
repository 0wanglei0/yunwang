package com.rave.yunwang.widget.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.rave.yunwang.R;
import com.rave.yunwang.application.BaseApplication;
import com.rave.yunwang.utils.MediaUtils;
import com.rave.yunwang.utils.WindowUtils;
import com.s2icode.dao.S2iClientInitResult;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraViewV2 extends ATextureView implements TextureView.SurfaceTextureListener {

    private static final String TAG = "CameraViewV2";
    private static final int M_MIN_PREVIEW_PIX = 1228800;

    private Context context;

    private Integer width;

    private Integer height;

    private HandlerThread mBackgroundThread;

    private Handler mBackgroundHandler;
    private CaptureRequest mPreviewRequest;
    private Size mPreviewSize;

    private Size mPictureSize;

    private Size mVideoSize;

    private String mCameraId;
    private CameraCharacteristics characteristics;

    private CameraManager mCameraManager;

    private CameraCaptureSession mCaptureSession;

    private CameraDevice mCameraDevice;

    private CaptureRequest.Builder mVideoBuilder;

    private MediaRecorder mMediaRecorder;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private Timer mTimer;

    private long mStartTime;

    private ImageReader mImageReader; // 预览回调的接收者
    private ImageReader mPreviewImageReader;
    private DemoCameraBaseInterface demoCameraBaseInterface;
    private S2iClientInitResult s2iClientInitResult = BaseApplication.s2iClientInitResult;

    private final Comparator comparatorA = new Comparator<Size>() {
        @Override
        public int compare(Size o1, Size o2) {
            return o1.getWidth() * o1.getHeight() - o2.getWidth() * o2.getHeight();
        }
    };

    private final Comparator comparatorD = new Comparator<Size>() {
        @Override
        public int compare(Size o1, Size o2) {
            return o2.getWidth() * o2.getHeight() - o1.getWidth() * o1.getHeight();
        }
    };

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            startVideoSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            handleError("camera disconnected");
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;

        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            handleError("camera error");
        }

    };
    public int maxZoom;
    private Surface recorderSurface;

    public CameraViewV2(Context context) {
        super(context);
        init(context);
    }

    public CameraViewV2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CameraViewV2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setDemoCameraBaseInterface(DemoCameraBaseInterface demoCameraBaseInterface) {
        this.demoCameraBaseInterface = demoCameraBaseInterface;
    }

    @Override
    public void startPreview() {
        startVideoSession();
    }

    @Override
    public void capture() {
        try {
            controllState = ControllState.PICTURE;
            managerUtils.sendMsg(PICTURE);
            savePicture(width, height);

        } catch (Exception e) {
            e.printStackTrace();
            cameraState = CameraState.CLOSE;
            controllState = ControllState.FAIL;
            managerUtils.sendMsg(FAIL);
        }
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

    @Override
    public void recordStart() {

        if (!controllState.equals(ControllState.CAMERA_PREVIEW)) {
            Log.d(TAG, context.getString(R.string.camera_busy));
            return;
        }

        controllState = ControllState.VIDEO;
        managerUtils.sendMsg(VIDEO);

        mMediaRecorder.start();

        mTimer = new Timer();

        mStartTime = System.currentTimeMillis();

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                int second = Math.round((System.currentTimeMillis() - mStartTime) / 1000);

                managerUtils.sendMsg(VIDEO_TIME, second + context.getString(R.string.video_s));

                if (second == VIDEO_MAX_TIME)
                    recordStop();

                Log.d(TAG, String.valueOf(second));
            }
        }, 0, 1 * 1000);

    }

    @Override
    public void recordStop() {
        int second = Math.round((System.currentTimeMillis() - mStartTime) / 1000);

        if (second < VIDEO_MIN_TIME) {
            managerUtils.sendMsg(VIDEO_MIN);
            return;
        }
        stopPreview();

        controllState = ControllState.VIDEO_SUCCESS;
        managerUtils.sendMsg(VIDEO_SUCCESS);

    }

    @Override
    public void release() {

        closeCamera();

        stopBackgroundThread();

    }

    @Override
    public void zoomArea(boolean isZoom) {
        try {
            characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            Rect rect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            Float maxRatio = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
            Rect cropRect = mVideoBuilder.get(CaptureRequest.SCALER_CROP_REGION);

            int minWidth = (int) (rect.width() / maxRatio);
            int minHeight = (int) (rect.height() / maxRatio);
            int maxWidth = rect.width();
            int maxHeight = rect.height();

            int zoomWidth = 0;
            int zoomHeight = 0;

            if (cropRect == null) {
                zoomWidth = rect.width();
                zoomHeight = rect.height();
            } else {
                zoomWidth = cropRect.width();
                zoomHeight = cropRect.height();
            }


            if (isZoom) {

                zoomWidth -= 50;
                zoomHeight -= 1.0f * 50 * rect.width() / rect.height();

                if (zoomWidth < minWidth || zoomHeight < minHeight)
                    return;

            } else {

                zoomWidth += 50;
                zoomHeight += 1.0f * 50 * rect.width() / rect.height();

                if (zoomWidth > maxWidth || zoomHeight > maxHeight)
                    return;

            }

            Rect zoomRect = new Rect(0, 0, zoomWidth, zoomHeight);

            mVideoBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomRect);
            mCaptureSession.setRepeatingRequest(mVideoBuilder.build(), null, mBackgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "zoom fail");
        }
    }

    @Override
    public void setZoom(float nZoom) {
        if (characteristics == null) {
            return;
        }
        if (maxZoom == 0) {
            maxZoom = 1;
        }

        float z = s2iClientInitResult.getS2iParam().getZoom();
        if (z > maxZoom) {
            z = maxZoom;
        }
        if (z < 1) {
            z = 1;
        }
        try {
            Rect m = characteristics.get(CameraCharacteristics
                    .SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            //z = 1.5f;
            Rect zoomRect;
            int cropW = (int) (m.width() * 1.0f / z);
            int cropH = (int) (m.height() * 1.0f / z);
            zoomRect = new Rect((m.width() - cropW) / 2,
                    (m.height() - cropH) / 2,
                    (m.width() - cropW) / 2 + cropW,
                    (m.height() - cropH) / 2 + cropH);

            if (mVideoBuilder != null) {
                mVideoBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomRect);
            }

            if (mVideoBuilder != null && mCaptureSession != null) {
                mCaptureSession.setRepeatingRequest(mVideoBuilder.build(), null, mBackgroundHandler);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开闪光灯
     */
    @Override
    public void openFlash() {
        try {
            if (mVideoBuilder != null && mCaptureSession != null) {
                mVideoBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                mCaptureSession.setRepeatingRequest(mVideoBuilder.build(), null, mBackgroundHandler);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭闪光灯
     */
    @Override
    public void closeFlash() {
        try {
            if (mVideoBuilder != null && mCaptureSession != null) {
                mVideoBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                mCaptureSession.setRepeatingRequest(mVideoBuilder.build(), null, mBackgroundHandler);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "close flash fail");
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        startBackgroundThread();
        openCamera(CameraState.BACK);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        Log.e(TAG, "onSurfaceTextureDestroyed");
        if (mCameraDevice != null) {
            release();
            mCameraDevice = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        try {

            Integer preWidth = width;
            Integer preHeight = height;
            width = right - left;
            height = bottom - top;

            if (preWidth != null && preHeight != null && (!preWidth.equals(width) || !preHeight.equals(height))) {
                setTextureSize(mPreviewSize.getHeight(), mPreviewSize.getWidth(), width, height);
            }

        } catch (Exception e) {
            e.printStackTrace();
            handleError("onLayout fail");
        }

    }

    private void init(Context context) {
        controllState = ControllState.CAMERA_PREPARE;
        managerUtils.sendMsg(CAMERA_PREPARE);

        this.context = context;
        setSurfaceTextureListener(this);
    }

    private void openCamera(CameraState state) {
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            int cameraIdInt = s2iClientInitResult.getS2iParam().getCameraId();//相机id
            boolean cameraIdValid = false;
            if (cameraIdInt != 0) {//cameraId不使用默认时，读取支持摄像头的列表
                for (String cameraId : mCameraManager.getCameraIdList()) {
                    CameraCharacteristics c = mCameraManager.getCameraCharacteristics(cameraId);
                    Integer facing = c.get(CameraCharacteristics.LENS_FACING);
                    if (facing != null
                            && CameraCharacteristics.LENS_FACING_BACK == facing
                            && cameraId.equals(String.valueOf(cameraIdInt))) {//判断后台设置的相机是否可用
                        cameraIdValid = true;
                        break;
                    }
                }
            }
            for (String cameraId : mCameraManager.getCameraIdList()) {
                characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                Integer facting = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cameraIdValid) {//后台设置的摄像头可用，使用后台摄像头
                    if (facting != null
                            && facting != CameraCharacteristics.LENS_FACING_BACK
                            || !cameraId.equals(String.valueOf(cameraIdInt))) {
                        continue;
                    }
                } else {//后台设置为0即默认摄像头，或者后台设置的摄像头不可用
                    if (facting != null && facting == CameraCharacteristics.LENS_FACING_FRONT) {
                        continue;
                    }
                }

                mCameraId = cameraId;
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics
                        .SCALER_STREAM_CONFIGURATION_MAP);

                Size largest = new Size(0, 0);

                if (largest.getHeight() <= 0 || largest.getWidth() <= 0) {
                    if (map != null) {
                        largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG))
                                , new CompareSizesByArea());
                    }
                }
                if (s2iClientInitResult.getS2iParam().getPreviewImageWidth() != 0) {
                    mPreviewSize = new Size(s2iClientInitResult.getS2iParam().getPreviewImageWidth(),
                            s2iClientInitResult.getS2iParam().getPreviewImageHeight());
                } else {
                    mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                            this.getWidth(), this.getHeight());
                }

                mPreviewImageReader = ImageReader.newInstance(mPreviewSize.getWidth(),
                        mPreviewSize.getHeight(),
                        ImageFormat.YUV_420_888, /*maxImages*/2);

                mPreviewImageReader.setOnImageAvailableListener(mOnImageAvailableListener,
                        mBackgroundHandler);

                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, /*maxImages*/1);

                mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        Image image = reader.acquireLatestImage();
                        int imageWidth = image.getWidth();
                        int imageHeight = image.getHeight();
                        if (image.getPlanes() == null) {
                            return;
                        }

                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        image.close();
                        demoCameraBaseInterface.receivePreviewImageData(bytes,
                                imageWidth,
                                imageHeight, 8);
                    }
                }, mBackgroundHandler);

                maxZoom = characteristics.get(CameraCharacteristics
                        .SCALER_AVAILABLE_MAX_DIGITAL_ZOOM).intValue();
                break;
            }
            setStabilization(true);
        } catch (CameraAccessException ex) {
            ex.printStackTrace();
        }

        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        getVideoSize(map);
        setTextureSize(mPreviewSize.getHeight(), mPreviewSize.getWidth(), width, height);

        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mCameraOpenCloseLock.acquire();

            mCameraManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (Exception e) {
            handleError("camera fail open");
            mCameraOpenCloseLock.release();
        }
    }

    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight) {
        Size bestPreviewSize;
        List<Size> bigEnough = new ArrayList<>();
        List<Size> ratioEnough = new ArrayList<>();
        for (Size option : choices) {
            if (option.getWidth() * option.getHeight() >= M_MIN_PREVIEW_PIX) {
                bigEnough.add(option);
                double aspectPreviewRatio = (double) option.getHeight() / (double) option.getWidth();
                double screenAspectRatio = (double) (textureViewWidth) / (double) (textureViewHeight);
                double distortion = Math.abs(aspectPreviewRatio - screenAspectRatio);

                if (distortion < 0.15 && option.getWidth() < textureViewWidth) {
                    ratioEnough.add(option);
                }
            }
        }
        if (ratioEnough.size() > 0) {
            Collections.sort(ratioEnough, new SizeComparator(textureViewHeight, textureViewWidth));
            bestPreviewSize = ratioEnough.get(ratioEnough.size() - 1);
        } else if (bigEnough.size() > 0) {
            Collections.sort(bigEnough, new SizeRatioComparator(textureViewHeight, textureViewWidth));
            bestPreviewSize = bigEnough.get(0);
        } else {
            bestPreviewSize = new Size(1440, 1080);
        }

        return bestPreviewSize;
    }


    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    public void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();

            stopVideoSession();

            if (mImageReader != null) {
                mImageReader.close();
                mImageReader = null;
            }

            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }

            closeFlash();
            destroyVideo();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    @Override
    public void stopPreview() {
        closeFlash();
        stopVideoSession();
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }

        destroyVideo();
    }

    private void getPreviewSize(StreamConfigurationMap map) {

        int width = WindowUtils.getDisplayHeight(context);
        int height = WindowUtils.getWindowWidth(context);


        Size[] supportSizes = map.getOutputSizes(SurfaceTexture.class);
        List<Size> chooseSizes = new ArrayList<>();

        for (Size size :
                supportSizes) {

            if (size.getWidth() == width && size.getHeight() == height) {
                mPreviewSize = size;
                return;
            }

            if (isRange(width, size.getWidth()) && isRange(height, size.getHeight())) {
                chooseSizes.add(size);
            }
        }


        if (chooseSizes.isEmpty()) {
            List<Size> list = Arrays.asList(supportSizes);
            Collections.sort(list, comparatorD);
            mPreviewSize = list.get(0);
        } else {
            Collections.sort(chooseSizes, comparatorA);
            mPreviewSize = chooseSizes.get(0);

        }
    }

    private void getPictureSize(StreamConfigurationMap map) {

        int width = WindowUtils.getDisplayHeight(context);
        int height = WindowUtils.getWindowWidth(context);


        Size[] supportSizes = map.getOutputSizes(ImageFormat.JPEG);
        List<Size> chooseSizes = new ArrayList<>();

        for (Size size :
                supportSizes) {

            if (size.getWidth() == width && size.getHeight() == height) {

                mPictureSize = size;

                return;
            }

            if (isRange(width, size.getWidth()) && isRange(height, size.getHeight())) {
                chooseSizes.add(size);
            }

        }

        for (Size size :
                chooseSizes) {

            if (size.getWidth() == mPreviewSize.getWidth() && size.getHeight() == mPreviewSize.getHeight()) {

                mPictureSize = size;

                return;
            }
        }


        if (chooseSizes.isEmpty()) {
            List<Size> list = Arrays.asList(supportSizes);
            Collections.sort(list, comparatorD);
            mPictureSize = list.get(0);
        } else {
            Collections.sort(chooseSizes, comparatorA);
            mPictureSize = chooseSizes.get(0);

        }

    }

    private void getVideoSize(StreamConfigurationMap map) {

        int width = WindowUtils.getDisplayHeight(context);
        int height = WindowUtils.getWindowWidth(context);

        Size[] supportSizes = map.getOutputSizes(MediaRecorder.class);
        List<Size> chooseSizes = new ArrayList<>();

        for (Size size : supportSizes) {
            if (isRange(width, size.getWidth()) && isRange(height, size.getHeight())) {
                chooseSizes.add(size);
            }
        }

        for (Size size : supportSizes) {
            if (size.getWidth() == mPreviewSize.getWidth() && size.getHeight() == mPreviewSize.getHeight()) {
                mVideoSize = size;
                mVideoSize = new Size(600, 450);
                return;
            }
        }

        if (chooseSizes.isEmpty()) {
            List<Size> list = Arrays.asList(supportSizes);
            Collections.sort(list, comparatorD);
            mVideoSize = list.get(0);
        } else {
            Collections.sort(chooseSizes, comparatorA);
            mVideoSize = chooseSizes.get(0);
        }

    }

    private void startVideoSession() {

        if (mCameraDevice == null) {
            handleError("camera device fail");
            return;
        }

        try {
            mVideoBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);

            createVideo();
            SurfaceTexture texture = getSurfaceTexture();
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(texture);
            mMediaRecorder.setPreviewDisplay(previewSurface);
            mMediaRecorder.prepare();
            recorderSurface = mMediaRecorder.getSurface();

            mVideoBuilder.addTarget(previewSurface);
            mVideoBuilder.addTarget(mPreviewImageReader.getSurface());
            mVideoBuilder.addTarget(recorderSurface);
            mCameraDevice.createCaptureSession(
                    Arrays.asList(previewSurface, mPreviewImageReader.getSurface(), recorderSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // 相机已经关闭
                            if (null == mCameraDevice) {
                                return;
                            }
                            mCaptureSession = cameraCaptureSession;

                            try {
                                setStabilization(true);
                                mVideoBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                mVideoBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                        CaptureRequest.CONTROL_AE_MODE_ON);
                                mVideoBuilder.set(CaptureRequest.FLASH_MODE,
                                        CameraMetadata.FLASH_MODE_OFF);

                                mPreviewRequest = mVideoBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        null, mBackgroundHandler);

                                mCaptureSession.setRepeatingRequest(mVideoBuilder.build(),
                                        null, mBackgroundHandler);

                                controllState = ControllState.CAMERA_PREVIEW;
                                managerUtils.sendMsg(CAMERA_PREVIEW);

                                Log.d(TAG, " camera preview success");

                            } catch (Exception e) {
                                e.printStackTrace();
                                handleError(e.toString());
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            handleError("camera session fail");
                        }
                    }, mBackgroundHandler);

            setZoom(BaseApplication.s2iClientInitResult.getS2iParam().getZoom());
        } catch (Exception e) {
            e.printStackTrace();
            handleError("camera session fail");
        }

    }

    private void stopVideoSession() {
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }

        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
    }

    private void createVideo() {

        try {
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            //输出路径
            videoFile = MediaUtils.saveTakeVideo();
            mMediaRecorder.setOutputFile(videoFile.getAbsolutePath());

            mMediaRecorder.setVideoEncodingBitRate(900*1024);
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setOrientationHint(ROTATION_90);
        } catch (Exception error) {
            error.printStackTrace();
            handleError("media fail");
        }
    }

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

    private void startBackgroundThread() {
        try {
            mBackgroundThread = new HandlerThread("CameraBackground");
            mBackgroundThread.start();
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        } catch (OutOfMemoryError e) {
            mBackgroundThread = null;
            mBackgroundHandler = null;
            e.printStackTrace();
            handleError("oom");
        } catch (Exception e) {
            mBackgroundThread = null;
            mBackgroundHandler = null;
            e.printStackTrace();
            handleError(e.toString());
        }
    }

    private void stopBackgroundThread() {
        try {
            if (mBackgroundThread != null) {
                mBackgroundThread.quitSafely();
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            handleError("oom");
        } catch (Exception e) {
            e.printStackTrace();
            handleError(e.toString());
        }
    }

    private void handleError(String error) {
        cameraState = CameraState.CLOSE;
        controllState = ControllState.FAIL;
        managerUtils.sendMsg(FAIL);
        Log.d(TAG, error);
    }

    private Image img;
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            try {
                img = reader.acquireLatestImage();
                byte[] yuv;
                if (img != null) {
                    yuv = getImageYUV420YBytes(img);
                    if (isS2iOpen) {
                        demoCameraBaseInterface.receivePreviewImageData(yuv,
                                mPreviewSize.getWidth(),
                                mPreviewSize.getHeight(), 3);
                    }
                    img.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (img != null) {
                    img.close();
                }
            }
        }
    };

    public static byte[] getImageYUV420YBytes(Image image) {
        if (!isImageFormatSupported(image)) {
            throw new RuntimeException("can't convert Image to byte array, format " + image.getFormat());
        }
        Rect crop = image.getCropRect();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height];
        byte[] rowData = new byte[planes[0].getRowStride()];
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < 1; i++) {
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();
            int shift = 0;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return data;
    }

    private static boolean isImageFormatSupported(Image image) {
        int format = image.getFormat();
        switch (format) {
            case ImageFormat.YUV_420_888:
            case ImageFormat.NV21:
            case ImageFormat.YV12:
                return true;
        }
        return false;
    }

    private void setStabilization(boolean stabilizationOn) {
        try {
            int[] stabilizations = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION);
            if (stabilizations == null || stabilizations.length == 0) {
            } else {
                boolean support = false;
                for (int mode : stabilizations) {
                    if (mode == CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_ON) {
                        support = true;
                    }
                }
                if (support) {
                    mVideoBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON);
                }
            }
            if (mCaptureSession != null) {
                mCaptureSession.setRepeatingRequest(mVideoBuilder.build(), null, mBackgroundHandler);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}


