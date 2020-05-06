package com.rave.yunwang.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.lxj.xpopup.XPopup;
import com.rave.yunwang.R;
import com.rave.yunwang.bean.UserInfoBean;
import com.rave.yunwang.contract.LoginContract;
import com.rave.yunwang.presenter.LoginPresenter;
import com.rave.yunwang.utils.SPUtils;
import com.rave.yunwang.utils.StringUtils;
import com.rave.yunwang.utils.permission.RxPermissions;
import com.rave.yunwang.view.main.MainActivity;
import com.rave.yunwang.widget.popupview.AlterPasswordCenterPopup;

import java.util.UUID;

import io.reactivex.functions.Consumer;


public class LoginActivity extends AppCompatActivity implements LoginContract.View {
    private static final String LOCATION_SAPERATOR = ",";

    private EditText etUsername;
    private EditText etPassword;
    private ImageView ivPasswordInvisible;
    private Button btnLogin;

    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private String locationString = "";

    private AlterPasswordCenterPopup alterPasswordCenterPopup;
    private UserInfoBean mUserInfo;

    private LoginContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initLocationSdk();
        startLocation();
        presenter = new LoginPresenter();
        presenter.attachView(this);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password1);
        ivPasswordInvisible = findViewById(R.id.iv_password_invisible1);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RxPermissions(LoginActivity.this).request(Manifest.permission.READ_PHONE_STATE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean success) throws Exception {
                                if (success) {
                                    presenter.login();
                                } else {
                                    Toast.makeText(LoginActivity.this, "该应用需要获取手机唯一标识码才能登录", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        ivPasswordInvisible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.changePasswordEncryption();
            }
        });
    }

    @Override
    public void initLocationSdk() {
        //初始化client
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = getLocationConfig();
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation location) {
                if (null != location) {
                    StringBuffer sb = new StringBuffer();
                    //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
                    if (location.getErrorCode() == 0) {
                        sb.append(location.getLatitude());
                        sb.append(LOCATION_SAPERATOR);
                        sb.append(location.getLongitude());
                        locationString = sb.toString();
                        Log.d("LoginActivity", locationString);
                    } else {
                        //定位失败之后不做处理，等登录按钮点击的时候响应
                        Log.d("tianrenzheng", "定位失败");
                    }
                } else {
                    //定位失败之后不做处理，等登录按钮点击的时候响应
                    Log.d("tianrenzheng", "定位失败");
                }
            }
        });
    }

    @Override
    public AMapLocationClientOption getLocationConfig() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        mOption.setGeoLanguage(AMapLocationClientOption.GeoLanguage.DEFAULT);//可选，设置逆地理信息的语言，默认值为默认语言（根据所在地区选择语言）
        return mOption;
    }

    @Override
    public void startLocation() {
        new RxPermissions(LoginActivity.this)
                .request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            // 启动定位
                            locationClient.startLocation();
                        } else {
                            finish();
                        }
                    }
                });
    }

    @Override
    public void setPasswordEncryption(boolean encryption) {
        etPassword.setTransformationMethod(encryption ? PasswordTransformationMethod.getInstance() : HideReturnsTransformationMethod.getInstance());
        if (encryption) {
            ivPasswordInvisible.setImageResource(R.mipmap.ic_password_invisible);
        } else {
            ivPasswordInvisible.setImageResource(R.mipmap.ic_password_visible);
        }
        etPassword.setSelection(etPassword.length());
    }

    @Override
    public void showErrorTips(int tips) {
        Toast.makeText(LoginActivity.this, getString(tips), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showErrorTips(String text) {
        Toast.makeText(LoginActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setCallBackCount() {

    }

    @Override
    public void showAlterDefaultPasswordPopup() {
        if (alterPasswordCenterPopup == null) {
            alterPasswordCenterPopup = new AlterPasswordCenterPopup(LoginActivity.this);
            alterPasswordCenterPopup.setClickListener(new AlterPasswordCenterPopup.PopupViewClickListener() {
                @Override
                public void onConfirmClicked(String password1, String password2) {
                    if (StringUtils.isBlank(password1) || StringUtils.isBlank(password2)) {
                        showErrorTips(R.string.input_password_item_hint2);
                    }
                    if (!password1.equals(password2)) {
                        showErrorTips(R.string.alter_default_password_error_tip);
                    }
                    presenter.alterDefaultPassword(password1);
                }
            });
        }

        if (!alterPasswordCenterPopup.isShow()) {
            new XPopup.Builder(LoginActivity.this)
                    .moveUpToKeyboard(false)
                    .asCustom(alterPasswordCenterPopup)
                    .show();
        }
    }

    @Override
    public void showUploadLocationPopup() {

    }

    @Override
    public void showValidateFailTips() {
        Toast.makeText(LoginActivity.this, "登录验证失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void gotoMainActivity() {
        dismissAlert();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void saveLoginUserInfo(UserInfoBean userInfoBean) {
        mUserInfo = userInfoBean;
        SPUtils.putObject(LoginActivity.this, SPUtils.LOGIN_USER, userInfoBean);
    }

    @Override
    public void alertInsertAddress() {
        createTipDialog(this);
    }

    private static AlertDialog sAlertDialog;
    private void createTipDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.dialog_yunwang_insert_address, new RelativeLayout(context), false);
        RelativeLayout relayout = layout.findViewById(R.id.rl_exit);// 加载布局
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        sAlertDialog = builder.create();
        sAlertDialog.show();
        sAlertDialog.setContentView(relayout);
        sAlertDialog.setCanceledOnTouchOutside(false);
        sAlertDialog.setCancelable(true);
        Button btn_ok = relayout.findViewById(R.id.grain_tip_btn_confirm);
        Button btn_cancel = relayout.findViewById(R.id.grain_tip_btn_cancel);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.insertAddress(mUserInfo.getUser_id());
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sAlertDialog.dismiss();
                sAlertDialog = null;
            }
        });

    }

    @Override
    public String getAddress() {
        return locationString;
    }

    @Override
    public String getToken() {
        return null;
    }

    @Override
    public int getAccountId() {
        return this.mUserInfo.getUser_id();
    }

    @Override
    public String getUserName() {
        return this.etUsername.getText().toString();
    }

    @Override
    public String getPassword() {
        return this.etPassword.getText().toString();
    }

    private static final String UUID_KEY = "applicationUniqueId";
    private static final String UUID_MATCHER = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Override
    public String getIMEI() {
        String uuid = UUID.randomUUID().toString();//随机生成uuid
        //将生成的uuid保存到sp中
        //android q存储方式发生变化
        if (Build.VERSION.SDK_INT >= 29) {
            return uuid;
        } else {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            @SuppressLint("MissingPermission")
            String imei = telephonyManager.getDeviceId();
            return imei;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocation();
        destroyLocation();
        presenter.detachView();
    }

    @Override
    public void stopLocation() {
        // 停止定位
        locationClient.stopLocation();
    }

    @Override
    public void destroyLocation() {
        if (null != locationClient) {
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }

    @Override
    public void dismissAlert() {
        if (sAlertDialog != null && sAlertDialog.isShowing()) {
            sAlertDialog.dismiss();
            sAlertDialog = null;
        }
    }
}
