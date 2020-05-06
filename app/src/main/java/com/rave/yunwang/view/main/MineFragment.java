package com.rave.yunwang.view.main;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.rave.yunwang.R;
import com.rave.yunwang.base.SimpleFragment;
import com.rave.yunwang.bean.UserInfoBean;
import com.rave.yunwang.contract.MineContract;
import com.rave.yunwang.presenter.MinePresenter;
import com.rave.yunwang.utils.SPUtils;
import com.rave.yunwang.view.AlterEmailActivity;
import com.rave.yunwang.view.AlterPasswordActivity;
import com.rave.yunwang.view.AlterPhoneNumActivity;
import com.rave.yunwang.view.LoginActivity;
import com.rave.yunwang.widget.UserInfoTextView;

/**
 * 作者：tianrenzheng on 2019/12/18 08:49
 * 邮箱：317642600@qq.com
 */
public class MineFragment extends SimpleFragment implements MineContract.View {
    private static final int REQUEST_ALTER_PASSWORD = 1;
    private static final int REQUEST_ALTER_EMAIL = 2;
    private static final int REQUEST_ALTER_PHONE_NUM = 3;

    private ImageView ivBack;
    private TextView tvTitle;
    private UserInfoTextView tvNickname;
    private UserInfoTextView tvUsername;
    private UserInfoTextView tvEmail;
    private UserInfoTextView tvPhoneNumber;
    private Button btnLogout;

    private UserInfoBean userInfoBean;
    private MineContract.Presenter presenter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_user_center;
    }

    @Override
    protected void initView(View view) {
        ivBack = view.findViewById(R.id.iv_back);
        tvTitle = view.findViewById(R.id.tv_title);
        tvNickname = view.findViewById(R.id.tv_nickname);
        tvUsername = view.findViewById(R.id.tv_username);
        tvEmail = view.findViewById(R.id.tv_email);
        tvPhoneNumber = view.findViewById(R.id.tv_phone_number);
        btnLogout = view.findViewById(R.id.btn_login);

        ivBack.setVisibility(View.GONE);
        tvTitle.setText(R.string.user_center_fragment_title);
        tvNickname.setShowArrow(false);
        tvNickname.setShowDividerDashLine(false);
        tvUsername.setShowArrow(true);
        tvUsername.setShowDividerDashLine(true);
        tvEmail.setShowArrow(true);
        tvEmail.setShowDividerDashLine(true);
        tvPhoneNumber.setShowArrow(true);
        tvPhoneNumber.setShowDividerDashLine(true);
    }

    @Override
    protected void initEventAndData() {
        presenter = new MinePresenter();
        presenter.attachView(this);
        tvUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAlterPassword();
            }
        });
        tvEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAlterEmail();
            }
        });
        tvPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAlterPhoneNumber();
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.logout();
            }
        });

        fillUserInfo();
    }

    @Override
    public void fillUserInfo() {
        userInfoBean = SPUtils.getObject(getContext(), SPUtils.LOGIN_USER, UserInfoBean.class);
        tvNickname.setTvLeftText(getResources().getString(R.string.username_item_title, userInfoBean.getUsername()));
        tvUsername.setTvLeftText(getResources().getString(R.string.account_item_title, userInfoBean.getAccount_num()));
        tvUsername.setTvRightText(R.string.account_item_redirect_desc);
        tvEmail.setTvLeftText(getResources().getString(R.string.email_item_title, userInfoBean.getEmail()));
        tvEmail.setTvRightText(R.string.email_item_redirect_desc);
        tvPhoneNumber.setTvLeftText(getResources().getString(R.string.phone_num_item_title, userInfoBean.getPhone()));
        tvPhoneNumber.setTvRightText(R.string.phone_num_item_redirect_desc);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
    }

    @Override
    public UserInfoBean getUserInfo() {
        return userInfoBean;
    }

    @Override
    public void gotoAlterPassword() {
        Intent intent = new Intent(getContext(), AlterPasswordActivity.class);
        startActivityForResult(intent, REQUEST_ALTER_PASSWORD);
    }

    @Override
    public void gotoAlterEmail() {
        Intent intent = new Intent(getContext(), AlterEmailActivity.class);
        startActivityForResult(intent, REQUEST_ALTER_EMAIL);
    }

    @Override
    public void gotoAlterPhoneNumber() {
        Intent intent = new Intent(getContext(), AlterPhoneNumActivity.class);
        startActivityForResult(intent, REQUEST_ALTER_PHONE_NUM);
    }

    @Override
    public void clearUserInfo() {
        SPUtils.removeValue(getContext(), SPUtils.LOGIN_USER);
    }

    @Override
    public void gotoLoginActivity() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void showErrorTips(int tips) {
        Toast.makeText(getContext(), getString(tips), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showErrorTips(String tips) {
        Toast.makeText(getContext(), tips, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setCallBackCount() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_ALTER_PASSWORD:
                case REQUEST_ALTER_EMAIL:
                case REQUEST_ALTER_PHONE_NUM:
                    fillUserInfo();
                    break;
            }
        }
    }
}
