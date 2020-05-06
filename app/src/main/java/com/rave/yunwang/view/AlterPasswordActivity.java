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
import com.rave.yunwang.contract.AlterPasswordContract;
import com.rave.yunwang.presenter.AlterPasswordPresenter;
import com.rave.yunwang.utils.SPUtils;
import com.rave.yunwang.widget.UserInfoEditText;


public class AlterPasswordActivity extends AppCompatActivity implements AlterPasswordContract.View {
    private ImageView ivBack;
    private TextView tvTitle;

    private UserInfoEditText etOldPassword;
    private UserInfoEditText etNewPassword;
    private UserInfoEditText etRepeatPassword;
    private Button btnSubmit;

    private UserInfoBean userInfoBean;
    private AlterPasswordContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new AlterPasswordPresenter();
        presenter.attachView(this);
        setContentView(R.layout.activity_alter_password);

        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(R.string.alter_password_activity_title);

        etOldPassword = findViewById(R.id.et_old_password);
        etOldPassword.setTvLeftText(R.string.input_old_password_item_title);
        etOldPassword.setEtRightHint(R.string.input_old_password_item_hint);
        etOldPassword.setShowArrow(false);
        etOldPassword.setShowDividerDashLine(true);

        etNewPassword = findViewById(R.id.et_new_password);
        etNewPassword.setTvLeftText(R.string.input_new_password_item_title);
        etNewPassword.setEtRightHint(R.string.input_new_password_item_hint);
        etNewPassword.setShowArrow(false);
        etNewPassword.setShowDividerDashLine(true);

        etRepeatPassword = findViewById(R.id.et_repeat_password);
        etRepeatPassword.setTvLeftText(R.string.input_repeat_password_item_title);
        etRepeatPassword.setEtRightHint(R.string.input_repeat_password_item_hint);
        etRepeatPassword.setShowArrow(false);
        etRepeatPassword.setShowDividerDashLine(true);

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
                presenter.alterPassword();
            }
        });

        userInfoBean = SPUtils.getObject(AlterPasswordActivity.this, SPUtils.LOGIN_USER, UserInfoBean.class);
    }

    @Override
    public UserInfoBean getUserInfo() {
        return userInfoBean;
    }

    @Override
    public void refreshUserInfo(UserInfoBean newUserInfo) {
        this.userInfoBean.setUser_id(newUserInfo.getUser_id());
        this.userInfoBean.setPassword(newUserInfo.getPassword());
        SPUtils.putObject(AlterPasswordActivity.this, SPUtils.LOGIN_USER, this.userInfoBean);
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
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setCallBackCount() {

    }

    @Override
    public String getOldPassword() {
        return etOldPassword.getEtRightText();
    }

    @Override
    public String getNewPassword() {
        return etNewPassword.getEtRightText();
    }

    @Override
    public String getRepeatPassword() {
        return etRepeatPassword.getEtRightText();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }


}
