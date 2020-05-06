package com.rave.yunwang.contract;

import androidx.annotation.StringRes;

import com.rave.yunwang.base.BaseContract;
import com.rave.yunwang.bean.UserInfoBean;

/**
 * 作者：tianrenzheng on 2019/12/16 19:51
 * 邮箱：317642600@qq.com
 */
public interface AlterPasswordContract {

    public interface View extends BaseContract.BaseView {

        String getOldPassword();

        String getNewPassword();

        String getRepeatPassword();

        UserInfoBean getUserInfo();

        void refreshUserInfo(UserInfoBean newUserInfo);

        void goBack();
    }

    public interface Presenter<T> extends BaseContract.BasePresenter<T> {

        void alterPassword();
    }
}
