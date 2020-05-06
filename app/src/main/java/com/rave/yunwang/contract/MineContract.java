package com.rave.yunwang.contract;

import com.rave.yunwang.base.BaseContract;
import com.rave.yunwang.bean.UserInfoBean;

/**
 * 作者：tianrenzheng on 2019/12/21 15:11
 * 邮箱：317642600@qq.com
 */
public class MineContract {

    public interface View extends BaseContract.BaseView {

        UserInfoBean getUserInfo();

        void gotoAlterPassword();

        void gotoAlterEmail();

        void gotoAlterPhoneNumber();

        void clearUserInfo();

        void gotoLoginActivity();

        void showErrorTips(String tips);

        void fillUserInfo();

    }

    public interface Presenter<T> extends BaseContract.BasePresenter<T> {

        void logout();

    }
}
