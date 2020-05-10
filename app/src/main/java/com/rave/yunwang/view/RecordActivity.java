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
import android.util.Log;
import android.view.LayoutInflater;
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
import com.rave.yunwang.widget.CameraView;
import com.rave.yunwang.widget.camera.DemoCameraBaseInterface;
import com.rave.yunwang.widget.camera.InitCallback;
import com.s2icode.dao.S2iCodeResult;
import com.s2icode.dao.S2iCodeResultBase;
import com.s2icode.main.S2iCodeModule;
import com.s2icode.main.S2iCodeResultInterface;

import java.io.File;
import java.util.Stack;

import static com.rave.yunwang.application.BaseApplication.s2iClientInitResult;

public class RecordActivity extends AppCompatActivity implements RecordContract.View, DemoCameraBaseInterface, S2iCodeResultInterface, InitCallback {
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
    private Stack<Integer> orderStack = new Stack<>();//顺序栈

    private ImageView ivBack;
    private TextView tvTitle;
    private TextView tvCurrentType;
    private CameraView cameraView;
    private ImageView ivRecordOperator;
    private int ScreenWidth;
    private int taskId;
    private String vinCode, s2iCode1, s2iCode2;
    private UserInfoBean userInfo;
    private FrameLayout layoutScanFrame;
    private RecordContract.Presenter presenter;
    private int s2iSuNum = 2;//扫描二维码成功次数
    private String vinResult;
    private String vinCodeResult;
    private boolean skipDecode = false;//跳过解码
    private S2iDetectAsyncTask detectAsyncTask;
    private Bitmap vinBitmap;
    private boolean isValidVinCode = false;
    private Dialog progressDialog;
    private ProgressBar progressBar;
    private TessBaseAPI baseApi;
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
        setContentView(R.layout.activity_record);
        S2iCodeModule.setS2iCodeResultInterface(this);
        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        tvCurrentType = findViewById(R.id.tv_current_type);
        cameraView = findViewById(R.id.cameraView);
        maskView = findViewById(R.id.first_mask);
        startImage = findViewById(R.id.iv_start);
        tvCurrentType.setText("录制视频中请按照提示进行拍摄");
        startImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maskView.setVisibility(View.GONE);
                startImage.setVisibility(View.GONE);
                cameraView.setCameraCallBack(new CameraView.CameraCallBack() {
                    @Override
                    public void uploadFile(String path, boolean fromPreview) {
                        switch (currentType) {
                            case TYPE_VIN_CODE://Vin码
                                if (fromPreview) {
//                            try {
                                    if (StringUtils.isNotEmpty(path) && isValidVinCode) {
                                        presenter.uploadImage(true, path);
                                    }
//                            } catch (Exception e) {
//                                showErrorTips("您的手机不支持自动识别Vin码，请联系供应商");
//                            }
//                                    if (count > 0) {
//                                        count--;
//                                        vinCodeOcr(path);
//                                    }
                                }
                                break;
                            case TYPE_VIDEO://上传视频
                                ivRecordOperator.setImageResource(R.mipmap.ic_take_photo);
                                presenter.uploadVideo(path);//上传视频
                                break;
                            case TYPE_CAR_HEAD://车头
                            case TYPE_STORE_LOGO://门店logo
                            case TYPE_TAG://标签
                            case TYPE_SIDE_GLASS://侧面玻璃
                                if (fromPreview) {
                                    break;
                                }

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
        ivRecordOperator = cameraView.findViewById(R.id.iv_record_operator);
        ivRecordOperator.setVisibility(View.GONE);
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
                cameraView.closeCamera();
                finish();
            }
        });

        userInfo = SPUtils.getObject(this, SPUtils.LOGIN_USER, UserInfoBean.class);
        initTessBaseAPI();
    }

    public void initTessBaseAPI() {
        baseApi = new TessBaseAPI();
        String fileName = "group";
        File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        boolean initResult = baseApi.init(dir.getAbsolutePath(), fileName);
        if (!initResult) {
            showErrorTips("初始化失败");
        }
        baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK);
    }

    private void vinCodeOcr(final String filePath) {
        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(false);
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
                            showErrorTips("vin码验证成功");
                            isUploadVinPic = true;
                            presenter.uploadImage(true, filePath);
                            break;
                        } else {
                            showErrorTips("vin码不匹配： " + wordSimple.getWords());
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
//    private void initVinDetect() {
//        int initKernalCode = vinApi.initVinKernal(this);
//        if (initKernalCode == 0) {
//            String endTime = vinApi.VinGetEndTime();
//            Log.e("endTime", endTime);
//            if (ConstantConfig.isCheckMotorbike) {
//                vinApi.VinSetRecogParam(1);
//            } else {
//                vinApi.VinSetRecogParam(0);
//            }
//        } else {
//            showErrorTips("OCR核心激活失败，ErrorCode:" + initKernalCode + "\r\n错误信息：" + ConstantConfig.getErrorInfo(initKernalCode));
//        }
//    }

    private void validateVinCodeByC(Bitmap bitmap) {
        //记得要在对应的文件夹里放上要识别的图片文件，比如我这里就在sd卡根目录放了img.png
        baseApi.setImage(bitmap);
        final String result = baseApi.getUTF8Text();
        //这里，你可以把result的值赋值给你的TextView
        isValidVinCode = vinCode.equals(result);
        if (isValidVinCode) {
            baseApi.end();
        }
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
    public void nextProcess() {//下一个程序
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
        layoutScanFrame.setVisibility(View.VISIBLE);
        switch (currentType) {
            case TYPE_VIN_CODE:
                cameraView.openS2i();
                cameraView.closeFlash();
                setScanAreaFrame(WindowUtils.dip2px(350), WindowUtils.dip2px(100));
                return "正在拍摄Vin码";
            case TYPE_SIDE_GLASS:
                ivRecordOperator.setVisibility(View.VISIBLE);
                cameraView.closeS2i();
                cameraView.closeFlash();
                setScanAreaFrame(ScreenWidth - 40, (int) ((ScreenWidth - 40) / 9f * 6));
                return "正在拍摄侧面玻璃";
            case TYPE_VIDEO:
                layoutScanFrame.setVisibility(View.GONE);
                ivRecordOperator.setVisibility(View.VISIBLE);
                cameraView.closeS2i();
                cameraView.closeFlash();
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
    public void showErrorTips(final int tips) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RecordActivity.this, getString(tips), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void showErrorTips(String text) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    public int count = 2;

    @Override
    public void setCallBackCount() {
        count++;
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
        if ((detectAsyncTask == null || detectAsyncTask.getStatus() == AsyncTask.Status.FINISHED)) {
            detectAsyncTask = new S2iDetectAsyncTask(data, previewWidth, previewHeight, imageType, this);
            detectAsyncTask.execute();
        }
    }

    @Override
    public void receivePreviewImageData(Bitmap bitmap, byte[] data, int imageType) {

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
                    progressDialog = new Dialog(RecordActivity.this);
                    View v = LayoutInflater.from(RecordActivity.this).inflate(R.layout.yw_progress_dialog, null);
                    LinearLayout layout = v.findViewById(R.id.yw_progress_view);
                    progressBar = v.findViewById(R.id.zip_progress);
                    progressDialog.setContentView(layout);
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
        protected byte[] data;
        protected int imageWidth;
        protected int imageHeight;
        protected int imageType;
        protected RecordActivity thisActivity;

        S2iDetectAsyncTask(byte[] data, int imageWidth, int imageHeight, int imageType, RecordActivity activity) {
            this.data = data;
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
            this.imageType = imageType;
            thisActivity = activity;
        }

        @Override
        protected void onPostExecute(Integer decodeCode) {//解码代码
            if (currentType == TYPE_VIN_CODE) {
                if (isValidVinCode) {
                    showErrorTips("vin码验证成功");
                } else {
                    showErrorTips("vin码验证失败");
                }
            }

            if (decodeCode == 1) {
                thisActivity.skipDecode = true;
            }
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            if (currentType == TYPE_VIN_CODE) {
                vinBitmap = ImageUtils.rotateBmp(ImageUtils.convertDataToBitmap(data, imageWidth, imageHeight), 90);

                float widthScale = (float) WindowUtils.dip2px(350f) / ScreenWidth;
                float heightScale = (float) WindowUtils.dip2px(80f) / WindowUtils.getScreenHeight(RecordActivity.this);
                float cropWidth = vinBitmap.getWidth() * widthScale;
                float cropHeight = vinBitmap.getHeight() * heightScale;
                int startX = (int) ((vinBitmap.getWidth() - cropWidth) / 2);
                int startY = (int) ((vinBitmap.getHeight() - cropHeight) / 2);
                Rect cropRect = new Rect(startX, startY, (int) cropWidth + startX, (int) cropHeight + startY);
                if (cropWidth <= 0 || cropHeight <= 0) {
                    cropRect = new Rect(0, vinBitmap.getHeight() / 3, vinBitmap.getWidth(), vinBitmap.getHeight() * 2 / 3);
                }
                vinBitmap = ImageUtils.cropBitmap(vinBitmap, cropRect);
//                FilePathUtils.saveImage2(vinBitmap);
                validateVinCodeByC(vinBitmap);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        cameraView.getVinPicture(vinBitmap);
//                    }
//                });

                return -1;
            }

            if (!thisActivity.skipDecode) {
                int result = S2iCodeModule.startS2iDecode(data, imageWidth, imageHeight, imageType, true);
                Log.e("test", "result: " + result);
                return result;
            }

            return -1;
        }
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
//            }  if(!s2iCode1.equals(s2iCode2)){
            } else {
                showErrorTips("扫描结束！");
                cameraView.closeS2i();
                cameraView.closeFlash();
                presenter.uploadS2i();//上传s2i
                cameraView.stopRecord();
                currentType = TYPE_VIDEO;
                tvCurrentType.setText(getProcessTips());
            }
        }
        skipDecode = false;
    }

    @Override
    public void onS2iCodeError(S2iCodeResultBase s2iCodeResultBase) {
        showErrorTips("s2i扫描失败！");
        skipDecode = false;
    }
}
