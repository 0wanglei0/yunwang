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
import com.rave.yunwang.contract.AlterPhoneNumberContract;
import com.rave.yunwang.presenter.AlterPhoneNumberPresenter;
import com.rave.yunwang.utils.SPUtils;
import com.rave.yunwang.widget.UserInfoEditText;


public class AlterPhoneNumActivity extends AppCompatActivity implements AlterPhoneNumberContract.View {
    private ImageView ivBack;
    private TextView tvTitle;
    private UserInfoEditText etPhoneNumber;
    private Button btnSubmit;

    private UserInfoBean userInfoBean;
    private AlterPhoneNumberContract.Presenter presenter;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new AlterPhoneNumberPresenter();
        presenter.attachView(this);
        setContentView(R.layout.activity_alter_phone_num);

        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(R.string.alter_phone_num_activity_title);

        etPhoneNumber = findViewById(R.id.et_phone_number);
        etPhoneNumber.setTvLeftText(R.string.input_phone_num_item_title);
        etPhoneNumber.setEtRightHint(R.string.input_phone_num_item_hint);
        etPhoneNumber.setShowArrow(false);
        etPhoneNumber.setShowDividerDashLine(false);

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
                presenter.alterPhoneNumber();
            }
        });

        userInfoBean = SPUtils.getObject(AlterPhoneNumActivity.this, SPUtils.LOGIN_USER, UserInfoBean.class);
    }

    @Override
    public String getNewPhoneNumber() {
        return etPhoneNumber.getEtRightText();
    }

    @Override
    public UserInfoBean getUserInfo() {
        return this.userInfoBean;
    }

    @Override
    public void refreshUserInfo(UserInfoBean newUserInfo) {
        this.userInfoBean.setUser_id(newUserInfo.getUser_id());
        this.userInfoBean.setPhone(newUserInfo.getPhone());
        SPUtils.putObject(AlterPhoneNumActivity.this, SPUtils.LOGIN_USER, this.userInfoBean);
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
    public void showErrorTips(String tips) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(getApplicationContext(), tips, Toast.LENGTH_SHORT);
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
