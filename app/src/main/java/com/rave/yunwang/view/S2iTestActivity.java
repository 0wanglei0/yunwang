package com.rave.yunwang.view;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rave.yunwang.R;
import com.rave.yunwang.bean.UserInfoBean;
import com.rave.yunwang.contract.SplashContract;
import com.rave.yunwang.presenter.SplashPresenter;
import com.rave.yunwang.utils.SPUtils;
import com.rave.yunwang.utils.permission.RxPermissions;
import com.rave.yunwang.view.main.MainActivity;
import com.s2icode.dao.S2iClientInitBase;
import com.s2icode.dao.S2iClientInitResult;
import com.s2icode.dao.S2iCodeResult;
import com.s2icode.dao.S2iCodeResultBase;
import com.s2icode.main.S2iClientInitInterface;
import com.s2icode.main.S2iCodeModule;
import com.s2icode.main.S2iCodeResultInterface;

import io.reactivex.functions.Consumer;

public class S2iTestActivity extends AppCompatActivity implements SplashContract.View, S2iClientInitInterface, S2iCodeResultInterface {

    private TextView tvCountdown;
    private SplashContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_s2i_test);
        tvCountdown = findViewById(R.id.tv_s2i);

        presenter = new SplashPresenter();
        presenter.attachView(this);

        tvCountdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RxPermissions(S2iTestActivity.this)
                        .request(Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.READ_PHONE_STATE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                if (aBoolean) {
                                    S2iCodeModule.setS2iClientInitInterface(S2iTestActivity.this);
                                    S2iCodeModule.startS2iClient();
                                }
                            }
                        });
            }
        });

//        presenter.startCountDown();
//        presenter.checkVersion("1.0.1");
    }

    @Override
    public void setCountDownText(String countDownText) {
        tvCountdown.setText(countDownText);
    }

    @Override
    public void gotoLoginActivity() {
        Intent intent = new Intent(S2iTestActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void gotoMainActivity() {
        Intent intent = new Intent(S2iTestActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void showUpgradeDialog() {
        Toast.makeText(S2iTestActivity.this, "当前不是最新版本", Toast.LENGTH_SHORT).show();
    }

    @Override
    public UserInfoBean getLoginUser() {
        return SPUtils.getObject(S2iTestActivity.this, SPUtils.LOGIN_USER, UserInfoBean.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    @Override
    public void showErrorTips(int tips) {
        Toast.makeText(this, getString(tips), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showErrorTips(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setCallBackCount() {

    }

    @Override
    public void onS2iClientInit(S2iClientInitResult s2iClientInitResult) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("S2iClientInitResult", s2iClientInitResult);
        intent.putExtra("bundle", bundle);
        intent.setClass(this, S2IDemoCameraDemoActivity.class);
        startActivity(intent);
    }

    @Override
    public void onS2iClientInitError(S2iClientInitBase s2iClientInitBase) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("S2iClientInitBase", s2iClientInitBase);
        intent.putExtra("bundle",bundle);
        intent.setClass(this, ShowErrorResultActivity.class);
        startActivity(intent);

    }

    @Override
    public void onS2iCodeResult(S2iCodeResult s2iCodeResult) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("S2iCodeResult", s2iCodeResult);
        intent.putExtra("bundle", bundle);
        intent.setClass(this, ShowResultActivity.class);
        startActivity(intent);
    }

    @Override
    public void onS2iCodeError(S2iCodeResultBase errorResult) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("S2iCodeResult",errorResult);
        intent.putExtra("bundle",bundle);
        intent.setClass(this,ShowErrorResultActivity.class);
        startActivity(intent);

    }
}
