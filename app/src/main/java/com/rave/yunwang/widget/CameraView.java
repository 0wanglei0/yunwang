package com.rave.yunwang.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.hw.videoprocessor.VideoProcessor;
import com.hw.videoprocessor.util.VideoProgressListener;
import com.rave.yunwang.R;
import com.rave.yunwang.application.MyApplication;
import com.rave.yunwang.utils.StringUtils;
import com.rave.yunwang.widget.camera.ATextureView;
import com.rave.yunwang.widget.camera.CameraViewV2;
import com.rave.yunwang.widget.camera.DemoCameraBaseInterface;
import com.rave.yunwang.widget.camera.FoucsImageView;
import com.rave.yunwang.widget.camera.InitCallback;

import java.io.File;

public class CameraView extends FrameLayout {
    private static final String TAG = CameraView.class.getSimpleName();
    private boolean isRecordStarted = false;
    private Context mContext;

    private boolean fromPreview;
    private View viewCameraPrepareMask;
    private FrameLayout layoutSurface;
    private CustomVideoView viewVideoPreview;
    private TextView tvVideoRecordTimer;
    private ImageView ivRecordOperator;
    private LinearLayout layoutUpload;
    private ImageButton btnReShoot;
    private ImageButton btnUpload;
    private CameraCallBack cameraCallBack;
    private ATextureView cameraView;
    private FoucsImageView mFoucsImageView;
    private DemoCameraBaseInterface demoCameraBaseInterface;
    private InitCallback initCallback;
    private String filePath = "";
    private VideoCompress videoCompress;

    public CameraView(Context context) {
        super(context);
        initView(context);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        this.mContext = context;
        View view = LayoutInflater.from(mContext).inflate(R.layout.chat_board_tab_camera_fragment, this);
        viewCameraPrepareMask = view.findViewById(R.id.view_camera_prepare_mask);
        layoutSurface = view.findViewById(R.id.layout_surface);
        viewVideoPreview = view.findViewById(R.id.view_video_preview);
        tvVideoRecordTimer = view.findViewById(R.id.tv_video_record_timer);
        ivRecordOperator = view.findViewById(R.id.iv_record_operator);
        layoutUpload = view.findViewById(R.id.layout_upload);
        btnReShoot = view.findViewById(R.id.btn_Re_shoot);
        btnUpload = view.findViewById(R.id.btn_upload);

        initClickListener();

        mFoucsImageView = new FoucsImageView(context);
        addView(mFoucsImageView, new LayoutParams(80, 80));

        cameraView = new CameraViewV2(context);
        cameraView.setDemoCameraBaseInterface(demoCameraBaseInterface);

        cameraView.getManagerUtils().addListener(new ATextureView.OnCameraListener() {

            @Override
            public void onFail() {
                initStatus();
            }

            @Override
            public void onCameraPrepare() {
                viewCameraPrepareMask.setVisibility(VISIBLE);
            }

            @Override
            public void onCameraPreview() {
                initStatus();
                viewCameraPrepareMask.setVisibility(INVISIBLE);
                cameraView.setVisibility(VISIBLE);
                cameraView.recordStart();
            }

            @Override
            public void onPicture() {
                startTakePhoto();
            }

            @Override
            public void onPictureSuccess() {
                Log.d(TAG, "拍照成功" + cameraView.getPhotoFile());
                finishTakePhoto(cameraView.getPhotoFile());
            }

            @Override
            public void onVideo() {
                startVideoRecord();
            }

            @Override
            public void onVideoSuccess() {
                Log.d(TAG, "摄像成功" + cameraView.getVideoFile());
                cameraView.stopPreview();
                cameraView.closeFlash();
                playBackgroundVideo(cameraView.getVideoFile().getAbsolutePath());
                finishVideoRecord();
            }

            @Override
            public void onVideoTime(String count) {
                tvVideoRecordTimer.setText(count);
            }

            @Override
            public void onVideoMin() {
                Toast.makeText(MyApplication.getInstance(), mContext.getString(R.string.video_max), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFoucs(Point point) {
                mFoucsImageView.startFocus(point);
            }
        });

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutSurface.addView(cameraView, layoutParams);
        startPreview();
        initStatus();
    }

    public void setInitCallback(InitCallback callback) {
        this.initCallback = callback;
    }

    public void setDemoCameraBaseInterface(DemoCameraBaseInterface demoCameraBaseInterface) {
        this.demoCameraBaseInterface = demoCameraBaseInterface;
        this.cameraView.setDemoCameraBaseInterface(demoCameraBaseInterface);
    }

    private void initClickListener() {

        ivRecordOperator.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fromPreview = false;
                cameraView.capture();
            }
        });

        btnReShoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraView.getVideoFile() != null) {
                    cameraView.getVideoFile().delete();
                    cameraView.setVideoFile(null);
                }

                initStatus();
                startPreview();
                initCallback.initProcess();
            }
        });

        btnUpload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StringUtils.isEmpty(filePath)) {
                    Toast.makeText(mContext, "视频压缩中，请等待", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (cameraCallBack != null) {
                    if (StringUtils.isEmpty(filePath) || StringUtils.isEmpty(new File(filePath).getAbsolutePath())) {
                        Toast.makeText(mContext, "视频不存在，请重新拍摄", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, "视频正在上传，请等待", Toast.LENGTH_SHORT).show();
                        cameraCallBack.uploadFile(new File(filePath).getAbsolutePath(), fromPreview);
                    }
                }
            }
        });
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void playBackgroundVideo(String videoPath) {
        String outputPath = new File(videoPath).getParent();
        //设置播放加载路径
        cameraView.stopPreview();
        cameraView.setVisibility(INVISIBLE);
        viewVideoPreview.setVideoPath(videoPath);
        MediaController mediaController = new MediaController(getContext());
        mediaController.setMediaPlayer(viewVideoPreview);
        mediaController.setAnchorView(viewVideoPreview.getRootView());
        mediaController.setAlpha(0.5f);
        mediaController.show();
        viewVideoPreview.setMediaController(mediaController);
        //播放
        viewVideoPreview.setVisibility(VISIBLE);
        //播放
        viewVideoPreview.start();
        //循环播放
        viewVideoPreview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                viewVideoPreview.start();
            }
        });
        try {
            if ((videoCompress == null || videoCompress.getStatus() == AsyncTask.Status.FINISHED)) {
                videoCompress = new VideoCompress(videoPath, outputPath);
                videoCompress.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected class VideoCompress extends AsyncTask<Void, Void, Void> {
        protected String originFilePath;
        protected String outputPath;

        VideoCompress(String originFilePath, String outputPath) {
            this.originFilePath = originFilePath;
            this.outputPath = outputPath;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                VideoProcessor.processor(mContext)
                        .input(originFilePath)
                        .outWidth(1440)
                        .outHeight(1080)
                        .output(outputPath + "test.mp4")
                        .progressListener(new VideoProgressListener() {
                            @Override
                            public void onProgress(float progress) {
                                Log.i("Process", " progress "+ progress + "");
                                initCallback.showProgress((int) (progress * 100));
                            }
                        })
                        .process();
                filePath = outputPath + "test.mp4";
            } catch (Exception e) {
                filePath = "";
                e.printStackTrace();
            }

            return null;
        }
    }

    private void startPreview() {
        if (cameraView.getVisibility() == INVISIBLE) {
            cameraView.setVisibility(VISIBLE);
        }
        cameraView.startPreview();
    }

    public void stopRecord() {
        cameraView.recordStop();
    }

    public void openFlash(){
        cameraView.openFlash();
    }

    public void setZoom(float z){
        cameraView.setZoom(z);
    }

    private void initStatus() {
        ivRecordOperator.setImageResource(R.mipmap.ic_take_photo);
        ivRecordOperator.setVisibility(GONE);
        layoutUpload.setVisibility(GONE);
        tvVideoRecordTimer.setVisibility(VISIBLE);
        viewVideoPreview.setVisibility(GONE);
        viewVideoPreview.pause();
    }

    private void startTakePhoto() {
        Log.d(TAG, "开始拍照");
    }

    private void finishTakePhoto(File photoFile) {
        if (cameraCallBack != null) {
            cameraCallBack.uploadFile(photoFile.getAbsolutePath(), fromPreview);
        }
    }

    private void startVideoRecord() {
        layoutUpload.setVisibility(GONE);
        viewVideoPreview.setVisibility(GONE);
        tvVideoRecordTimer.setVisibility(VISIBLE);
        viewVideoPreview.pause();
    }

    private void finishVideoRecord() {
        ivRecordOperator.setVisibility(GONE);
        tvVideoRecordTimer.setVisibility(GONE);
        layoutUpload.setVisibility(VISIBLE);
        viewVideoPreview.setVisibility(VISIBLE);
    }

    private void setShowStatus(int status, View... views) {
        if (views == null) return;

        for (View view :
                views) {
            if (view.getVisibility() != status)
                view.setVisibility(status);
        }
    }

    public void getVinPicture(Bitmap bitmap) {
        fromPreview = true;
        cameraView.getVinPicture(bitmap);
    }

    public void setCameraCallBack(CameraCallBack cameraCallBack) {
        this.cameraCallBack = cameraCallBack;
    }

    public void closeFlash() {
        cameraView.closeFlash();
    }
    public void openS2i() {
        cameraView.setS2iSwitch(true);
    }

    public void closeS2i() {
        cameraView.setS2iSwitch(false);
    }

    public interface CameraCallBack {
        void uploadFile(String path, boolean fromPreview);
    }

    public void closeCamera() {
        cameraView.closeCamera();
    }
}
