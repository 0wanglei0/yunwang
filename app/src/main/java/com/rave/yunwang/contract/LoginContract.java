package com.rave.yunwang.contract;

import com.amap.api.location.AMapLocationClientOption;
import com.rave.yunwang.base.BaseContract;
import com.rave.yunwang.bean.UserInfoBean;

/**
 * 作者：tianrenzheng on 2019/12/16 19:51
 * 邮箱：317642600@qq.com
 */
public interface LoginContract {

    public interface View extends BaseContract.BaseView {

        void initLocationSdk();

        AMapLocationClientOption getLocationConfig();

        void startLocation();

        void stopLocation();

        void destroyLocation();

        void showValidateFailTips();

        void gotoMainActivity();

        void saveLoginUserInfo(UserInfoBean userInfoBean);

        void setPasswordEncryption(boolean encryption);

        void showAlterDefaultPasswordPopup();

        void showUploadLocationPopup();

        int getAccountId();

        String getUserName();

        String getPassword();

        String getIMEI();

        String getAddress();

        String getToken();

        void alertInsertAddress();
        void dismissAlert();
    }

    public interface Presenter<T> extends BaseContract.BasePresenter<T> {

        void changePasswordEncryption();

        void alterDefaultPassword(String newPassword);

        void login();

        void insertAddress(int userId);
    }
}
