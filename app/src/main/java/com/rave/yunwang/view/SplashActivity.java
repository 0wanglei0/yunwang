package com.rave.yunwang.view;

import android.content.Intent;
import android.os.Bundle;
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
import com.rave.yunwang.view.main.MainActivity;

public class SplashActivity extends AppCompatActivity implements SplashContract.View {

    private TextView tvCountdown;
    private SplashContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);
        tvCountdown = findViewById(R.id.tv_countdown);

        presenter = new SplashPresenter();
        presenter.attachView(this);

        presenter.startCountDown();
        presenter.checkVersion("1.0.1");
    }

    @Override
    public void setCountDownText(String countDownText) {
        tvCountdown.setText(countDownText);
    }

    @Override
    public void gotoLoginActivity() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void gotoMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void showUpgradeDialog() {
        Toast.makeText(SplashActivity.this, "当前不是最新版本", Toast.LENGTH_SHORT).show();
    }

    @Override
    public UserInfoBean getLoginUser() {
        return SPUtils.getObject(SplashActivity.this, SPUtils.LOGIN_USER, UserInfoBean.class);
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
}
