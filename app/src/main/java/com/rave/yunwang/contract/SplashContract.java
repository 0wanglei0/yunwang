package com.rave.yunwang.contract;

import com.rave.yunwang.base.BaseContract;
import com.rave.yunwang.bean.UserInfoBean;

public class SplashContract {

    public interface View extends BaseContract.BaseView {

        void setCountDownText(String countDownText);

        void gotoLoginActivity();

        void gotoMainActivity();

        void showUpgradeDialog();

        UserInfoBean getLoginUser();
    }

    public interface Presenter<T> extends BaseContract.BasePresenter<T> {
        void startCountDown();

        void checkVersion(String versionName);
    }
}
