package com.rave.yunwang.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.rave.yunwang.R;
import com.rave.yunwang.bean.UserInfoBean;
import com.rave.yunwang.contract.RecordContract;
import com.rave.yunwang.presenter.RecordPresenter;
import com.rave.yunwang.utils.FilePathUtils;
import com.rave.yunwang.utils.ImageUtils;
import com.rave.yunwang.utils.SPUtils;
import com.rave.yunwang.utils.StringUtils;
import com.rave.yunwang.utils.WindowUtils;
import com.rave.yunwang.widget.Camera1ApiView;
import com.rave.yunwang.widget.camera.DemoCameraBaseInterface;
import com.rave.yunwang.widget.camera.InitCallback;
import com.s2icode.dao.S2iCodeResult;
import com.s2icode.dao.S2iCodeResultBase;
import com.s2icode.main.S2iCodeModule;
import com.s2icode.main.S2iCodeResultInterface;

import java.io.File;
import java.util.Stack;

import static com.rave.yunwang.application.BaseApplication.s2iClientInitResult;

public class Camera1RecordActivity extends AppCompatActivity implements RecordContract.View, DemoCameraBaseInterface, S2iCodeResultInterface, InitCallback {
    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_VIN_CODE = "extra_vin_code";
    public static final int TYPE_CAR_HEAD = 1;
    public static final int TYPE_STORE_LOGO = 2;
    public static final int TYPE_TAG = 3;
    public static final int TYPE_SIDE_GLASS = 4;
    public static final int TYPE_VIN_CODE = 5;
    public static final int TYPE_S2I_CODE = 6;
    public static final int TYPE_VIDEO = 7;

    private int currentType = TYPE_VIN_CODE;
    private int order;
    private Stack<Integer> orderStack = new Stack<>();

    private TextView tvCurrentType;
    private Camera1ApiView cameraView;
    private ImageView ivRecordOperator;
    private int ScreenWidth;
    private int taskId;
    private String vinCode, s2iCode1, s2iCode2;
    private FrameLayout layoutScanFrame;
    private int s2iSuNum = 2;//扫描二维码成功次数
    private UserInfoBean userInfo;
    private RecordContract.Presenter presenter;
    private String vinResult;
    private String vinCodeResult;
    private boolean skipDecode = false;//跳过解码
    private S2iDetectAsyncTask detectAsyncTask;
    private Bitmap vinBitmap;
    private boolean isValidVinCode = false;
    private Dialog progressDialog;
    private ProgressBar progressBar;
    private Toast toast;
    private boolean isUploadVinPic;
    private View maskView;
    private ImageView startImage;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        ScreenWidth = dm.widthPixels;
        presenter = new RecordPresenter();
        presenter.attachView(this);
        setContentView(R.layout.activity_record_camera1);
        S2iCodeModule.setS2iCodeResultInterface(this);
        ImageView ivBack = findViewById(R.id.iv_back);
        TextView tvTitle = findViewById(R.id.tv_title);
        tvCurrentType = findViewById(R.id.tv_current_type);
        cameraView = findViewById(R.id.drawer_layout);
        ivRecordOperator = cameraView.findViewById(R.id.iv_record_operator);
        ivRecordOperator.setVisibility(View.VISIBLE);

        cameraView.setDemoCameraBaseInterface(this);
        cameraView.setInitCallback(this);

        this.taskId = getIntent().getExtras().getInt(EXTRA_TASK_ID, -1);
        this.vinCode = getIntent().getExtras().getString(EXTRA_VIN_CODE);
        tvTitle.setText(String.format("对%s车辆进行拍摄", getVinCode()));
        layoutScanFrame = findViewById(R.id.layout_scan_frame);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });
        maskView = findViewById(R.id.first_mask);
        startImage = findViewById(R.id.iv_start);
        tvCurrentType.setText("录制视频中请按照提示进行拍摄");
        startImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maskView.setVisibility(View.GONE);
                startImage.setVisibility(View.GONE);
                cameraView.setCameraCallBack(new Camera1ApiView.CameraCallBack() {
                    @Override
                    public void uploadFile(String path, boolean fromPreview) {
                        switch (currentType) {
                            case TYPE_VIN_CODE:
                                if (count > 0) {
                                    count--;
                                    vinCodeOcr(path);
                                }
                                break;
                            case TYPE_VIDEO:
                                ivRecordOperator.setImageResource(R.mipmap.ic_take_photo);
                                presenter.uploadVideo(path);
                                break;
                            case TYPE_CAR_HEAD:
                            case TYPE_STORE_LOGO:
                            case TYPE_TAG:
                            case TYPE_SIDE_GLASS:
                                ivRecordOperator.setImageResource(R.mipmap.ic_take_photo);
                                presenter.uploadImage(false, path);//上传相片
                                break;
                            default:
                                ivRecordOperator.setImageResource(R.mipmap.ic_take_photo);
                                break;
                        }
                    }
                });
                tvCurrentType.setText(getProcessTips());
            }
        });

        userInfo = SPUtils.getObject(this, SPUtils.LOGIN_USER, UserInfoBean.class);
//        initTessBaseAPI();
    }

    private TessBaseAPI baseApi;
    public void initTessBaseAPI() {
        baseApi = new TessBaseAPI();
        String fileName = "eng";
        File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        boolean initResult = baseApi.init(dir.getAbsolutePath(), fileName);
        if (!initResult) {
            showErrorTips("初始化失败");
        }
        baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
    }

    private String validateVinCodeByC(Bitmap bitmap) {
        //记得要在对应的文件夹里放上要识别的图片文件，比如我这里就在sd卡根目录放了img.png
        String result = "";
        try {
            baseApi.setImage(bitmap);
            final String result1 = baseApi.getUTF8Text();
            //这里，你可以把result的值赋值给你的TextView
//            baseApi.end();
            result = result1;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showErrorTips("识别结果: " + result1);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void setScanAreaFrame(int width, int height) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layoutScanFrame.getLayoutParams();
        layoutParams.width = (int) width;
        layoutParams.height = (int) height;
        layoutScanFrame.setLayoutParams(layoutParams);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public String getRecordTaskId() {
        return String.valueOf(this.taskId);
    }

    @Override
    public UserInfoBean getUserInfo() {
        return this.userInfo;
    }

    @Override
    public String getVinCode() {
        return this.vinCode;
    }

    @Override
    public String getS2iCode1() {
        return this.s2iCode1;
    }

    @Override
    public String getS2iCode2() {
        return this.s2iCode2;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public void initProcess(int order) {
        orderStack.clear();
        orderStack.push(TYPE_VIDEO);
        orderStack.push(TYPE_S2I_CODE);
        int remainder = order;
        while (remainder > 0) {
            int mod = remainder % 10;
            remainder = remainder / 10;
            orderStack.add(mod);
        }
    }

    @Override
    public void nextProcess() {
        if (!orderStack.isEmpty()) {
            currentType = orderStack.pop();
        }

        if (isUploadVinPic) {
            isUploadVinPic = false;
            tvCurrentType.setText(String.format("%s \n（首次录制需拍摄张贴标签过程）", getProcessTips()));
        } else {
            tvCurrentType.setText(getProcessTips());
        }
    }

    @Override
    public String getProcessTips() {
        cameraView.fromPreview = true;
        layoutScanFrame.setVisibility(View.VISIBLE);
        switch (currentType) {
            case TYPE_VIN_CODE:
                cameraView.fromPreview = false;
                cameraView.openS2i();
                cameraView.closeFlash();
                ivRecordOperator.setVisibility(View.GONE);
                setScanAreaFrame(WindowUtils.dip2px(350), WindowUtils.dip2px(100));
                return "正在拍摄Vin码";
            case TYPE_SIDE_GLASS:
                ivRecordOperator.setVisibility(View.VISIBLE);
                cameraView.closeS2i();
                cameraView.closeFlash();
                setScanAreaFrame(ScreenWidth - 40, (int) ((ScreenWidth - 40) / 9f * 6));
                return "正在拍摄侧面玻璃";
            case TYPE_VIDEO:
                ivRecordOperator.setVisibility(View.VISIBLE);
                cameraView.closeS2i();
                cameraView.closeFlash();
                layoutScanFrame.setVisibility(View.GONE);
                return "正在上传视频";
            case TYPE_CAR_HEAD:
                ivRecordOperator.setVisibility(View.VISIBLE);
                cameraView.closeS2i();
                cameraView.openFlash();//打开闪关灯
                setScanAreaFrame(ScreenWidth - 40, (int) ((ScreenWidth - 40) / 9f * 6));
                return "正在拍摄车头";
            case TYPE_STORE_LOGO:
                ivRecordOperator.setVisibility(View.VISIBLE);
                cameraView.closeS2i();
                cameraView.closeFlash();
                setScanAreaFrame(ScreenWidth - 40, (int) ((ScreenWidth - 40) / 9f * 6));
                return "正在拍摄门店Logo";
            case TYPE_TAG:
                ivRecordOperator.setVisibility(View.VISIBLE);
                cameraView.closeS2i();
                cameraView.closeFlash();
                setScanAreaFrame(ScreenWidth - 40, (int) ((ScreenWidth - 40) / 9f * 6));
                return "正在拍摄标签";
            case TYPE_S2I_CODE:
                cameraView.startCapture();
                ivRecordOperator.setVisibility(View.GONE);
                cameraView.openS2i();//
                cameraView.openFlash();//打开闪关灯
                setScanAreaFrame((int) s2iClientInitResult.getS2iParam().getFocusFrameWidth(), (int) s2iClientInitResult.getS2iParam().getFocusFrameHeight());
                return "正在扫描S2i码";
            default:
                break;
        }
        return "";
    }

    @Override
    public void clearLocalFile() {
        File file = new File(FilePathUtils.getDir(FilePathUtils.PATH_TYPE_CAMERA_CACHE));
        FilePathUtils.delFile(file);
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void showErrorTips(int tips) {
        Toast.makeText(this, getString(tips), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showErrorTips(String text) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
        cameraView.closeCamera();
        finish();
    }

    @Override
    public void receivePreviewImageData(byte[] data, int previewWidth, int previewHeight, int imageType) {

    }

    @Override
    public void receivePreviewImageData(Bitmap bitmap, byte[] data, int imageType) {
        if ((detectAsyncTask == null || detectAsyncTask.getStatus() == AsyncTask.Status.FINISHED)) {
            detectAsyncTask = new S2iDetectAsyncTask(bitmap, data, imageType, this);
            detectAsyncTask.execute();
        }
    }

    @Override
    public void initProcess() {
        currentType = TYPE_VIN_CODE;
        tvCurrentType.setText(getProcessTips());
    }

    @Override
    public void showProgress(final int progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog == null) {
                    progressDialog = new Dialog(Camera1RecordActivity.this);
                    View v = LayoutInflater.from(Camera1RecordActivity.this).inflate(R.layout.yw_progress_dialog, null);
                    LinearLayout layout = v.findViewById(R.id.yw_progress_view);
                    progressBar = v.findViewById(R.id.zip_progress);
                    progressDialog.setContentView(layout);
                    ;
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }

                if (progressDialog != null && progressDialog.isShowing()) {
                    progressBar.setProgress(progress);
                    if (progress == 100) {
                        progressDialog.dismiss();
                    }
                }
            }
        });
    }

    protected class S2iDetectAsyncTask extends AsyncTask<Void, Void, Integer> {
        private byte[] data;
        int imageType;
        Camera1RecordActivity thisActivity;
        private Bitmap bitmap;

        S2iDetectAsyncTask(Bitmap bitmap, byte[] data, int imageType, Camera1RecordActivity activity) {
            this.bitmap = bitmap;
            this.data = data;
            this.imageType = imageType;
            thisActivity = activity;
        }

        @Override
        protected void onPostExecute(Integer decodeCode) {
            if (decodeCode == 1) {
                thisActivity.skipDecode = true;
            }

            if (currentType == TYPE_S2I_CODE || currentType == TYPE_VIN_CODE) {
                cameraView.startCapture();
            }
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            if (thisActivity.isFinishing()) {
                return -2;
            }

            if (!thisActivity.skipDecode && currentType == TYPE_S2I_CODE) {
                int imageWidth = bitmap.getWidth();
                int imageHeight = bitmap.getHeight();
                return S2iCodeModule.startS2iDecode(data, imageWidth, imageHeight, imageType, true);
            } else {
                vinBitmap = ImageUtils.rotateBmp(bitmap, 90);
                if (currentType == TYPE_VIN_CODE) {
                    vinBitmap = ImageUtils.cropBitmap(vinBitmap, new Rect(0, vinBitmap.getHeight() / 3, vinBitmap.getWidth(), vinBitmap.getHeight() * 2 / 3));
//                    vinCodeResult = validateVinCodeByC(vinBitmap);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cameraView.getVinPicture(vinBitmap);
                        }
                    });
                }
            }

            return -1;
        }
    }

    public int count = 2;

    @Override
    public void setCallBackCount() {
        count++;
    }

    private void vinCodeOcr(final String filePath) {
        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(true);
        param.setImageFile(new File(filePath));

        // 调用通用文字识别服务
        OCR.getInstance(this).recognizeGeneralBasic(param, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult result) {
                // 调用成功，返回GeneralResult对象
                for (WordSimple wordSimple : result.getWordList()) {
                    // wordSimple不包含位置信息
                    try {
                        if (StringUtils.isNotEmpty(filePath)
                                && getVinCode().equals(wordSimple.getWords())
                        ) {
                            isUploadVinPic = true;
                            presenter.uploadImage(true, filePath);
                            showErrorTips("vin码验证成功");
                            break;
                        } else {
                            showErrorTips("vin码不匹配");
                            break;
                        }
                    } catch (Exception e) {
                        showErrorTips("您的手机不支持自动识别Vin码，请联系供应商");
                    }
                }
                count++;
            }

            @Override
            public void onError(OCRError error) {
                // 调用失败，返回OCRError对象
                count++;
            }
        });
    }

    /**
     * 响应用户触屏事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchEvent(event);
        return super.onTouchEvent(event);
    }

    protected void touchEvent(MotionEvent event) {
        cameraView.pointFocus(event);
    }

    @Override
    public void onS2iCodeResult(S2iCodeResult s2iCodeResult) {
        s2iSuNum = s2iSuNum - 1;
        if (s2iSuNum == 1) {
            s2iCode1 = s2iCodeResult.data;
            showErrorTips("扫描成功,请扫描下一张图");
        }
        if (s2iSuNum == 0) {
            s2iCode2 = s2iCodeResult.data;
            if (s2iCode1.equals(s2iCode2)) {
                showErrorTips("必须是两张不同的二维码");
                s2iCode2 = "";
                s2iSuNum = s2iSuNum + 1;
            } else {
                showErrorTips("扫描结束！");
                cameraView.closeS2i();
                cameraView.closeFlash();
                presenter.uploadS2i();//上传s2i
                cameraView.stopRecord();
                currentType = TYPE_VIDEO;
                tvCurrentType.setText(getProcessTips());
                return;
            }
        }
        skipDecode = false;
        cameraView.startCapture();
    }

    @Override
    public void onS2iCodeError(S2iCodeResultBase s2iCodeResultBase) {
        showErrorTips("s2i扫描失败！");
        skipDecode = false;
        cameraView.startCapture();
    }
}
