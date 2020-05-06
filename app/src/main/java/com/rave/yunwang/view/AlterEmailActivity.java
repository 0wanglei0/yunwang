package com.rave.yunwang.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rave.yunwang.R;
import com.rave.yunwang.bean.UserInfoBean;
import com.rave.yunwang.contract.AlterEmailContract;
import com.rave.yunwang.presenter.AlterEmailPresenter;
import com.rave.yunwang.utils.SPUtils;
import com.rave.yunwang.widget.UserInfoEditText;


public class AlterEmailActivity extends AppCompatActivity implements AlterEmailContract.View {

    private ImageView ivBack;
    private TextView tvTitle;
    private UserInfoEditText etEmail;
    private Button btnSubmit;

    private UserInfoBean userInfoBean;
    private AlterEmailContract.Presenter presenter;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new AlterEmailPresenter();
        presenter.attachView(this);

        setContentView(R.layout.activity_alter_email);
        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(R.string.alter_email_activity_title);

        etEmail = findViewById(R.id.et_email);
        etEmail.setTvLeftText(R.string.input_email_item_title);
        etEmail.setEtRightHint(R.string.input_email_item_hint);
        etEmail.setShowArrow(false);
        etEmail.setShowDividerDashLine(false);

        btnSubmit = findViewById(R.id.btn_submit);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.alterEmail();
            }
        });

        userInfoBean = SPUtils.getObject(AlterEmailActivity.this, SPUtils.LOGIN_USER, UserInfoBean.class);
    }

    @Override
    public String getNewEmail() {
        return etEmail.getEtRightText();
    }

    @Override
    public UserInfoBean getUserInfo() {
        return userInfoBean;
    }

    @Override
    public void refreshUserInfo(UserInfoBean newUserInfo) {
        this.userInfoBean.setUser_id(newUserInfo.getUser_id());
        this.userInfoBean.setEmail(newUserInfo.getEmail());
        SPUtils.putObject(AlterEmailActivity.this, SPUtils.LOGIN_USER, this.userInfoBean);
    }

    @Override
    public void goBack() {
        setResult(Activity.RESULT_OK);
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
    public void setCallBackCount() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }
}
