package com.rave.yunwang.presenter;

import com.rave.yunwang.R;
import com.rave.yunwang.base.RxBasePresenter;
import com.rave.yunwang.bean.BaseBean;
import com.rave.yunwang.bean.UserInfoBean;
import com.rave.yunwang.contract.LoginContract;
import com.rave.yunwang.model.RemoteRepository;
import com.rave.yunwang.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 作者：tianrenzheng on 2019/12/16 20:08
 * 邮箱：317642600@qq.com
 */
public class LoginPresenter extends RxBasePresenter<LoginContract.View> implements LoginContract.Presenter<LoginContract.View> {
    private boolean isPasswordEncryption = true;

    @Override
    public void changePasswordEncryption() {
        isPasswordEncryption = !isPasswordEncryption;
        mView.setPasswordEncryption(isPasswordEncryption);
    }

    @Override
    public void alterDefaultPassword(String newPassword) {

        final Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("user_id", mView.getAccountId());
        requestMap.put("password", newPassword);
        addSubscrebe(RemoteRepository.getInstance().modifyPassword(requestMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<UserInfoBean>>() {
                    @Override
                    public void accept(BaseBean<UserInfoBean> userInfoBeanBaseBean) throws Exception {
                        if (userInfoBeanBaseBean.getCode() == 0) {
                            mView.showUploadLocationPopup();
                        } else {
                            mView.showErrorTips(userInfoBeanBaseBean.getMessage());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mView.showErrorTips(R.string.fail_modify_password_tips);
                    }
                })
        );
    }

    @Override
    public void login() {
        String userName = mView.getUserName();
        String password = mView.getPassword();
        String imei = mView.getIMEI();
        String address = mView.getAddress();
        String token = mView.getToken();
        if (StringUtils.isBlank(userName)) {
            mView.showErrorTips(R.string.empty_username_tips);
            return;
        }
        if (StringUtils.isBlank(password)) {
            mView.showErrorTips(R.string.empty_password_tips);
            return;
        }

        final Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("account_num", userName);
        requestMap.put("password", password);
        requestMap.put("address", address);
        requestMap.put("uid", imei);
        requestMap.put("token", "");
        addSubscrebe(RemoteRepository.getInstance().login(requestMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<UserInfoBean>>() {
                    @Override
                    public void accept(BaseBean<UserInfoBean> userInfoBean) throws Exception {
                        mView.saveLoginUserInfo(userInfoBean.getResult());
                        if (userInfoBean.getCode() == 0) {
                            mView.showErrorTips("登录成功");
                            mView.gotoMainActivity();
                        } else if (userInfoBean.getCode() == 1) {
                            //弹出提示修改密码
                            mView.saveLoginUserInfo(userInfoBean.getResult());
                            mView.showAlterDefaultPasswordPopup();
                        } else if (userInfoBean.getCode() == 5) {
                            mView.alertInsertAddress();
                        } else {
                            mView.showErrorTips(userInfoBean.getMessage());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mView.showErrorTips(R.string.fail_user_login);
                    }
                })
        );
    }

    @Override
    public void insertAddress(int userId) {
        String address = mView.getAddress();
        if (StringUtils.isEmpty(address)) {
            mView.showErrorTips("未获取到有效地址，请开启定位");
            return;
        }

        final Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("address", address);
        requestMap.put("user_id", userId);
        addSubscrebe(RemoteRepository.getInstance().insertAddress(requestMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<UserInfoBean>>() {
                    @Override
                    public void accept(BaseBean<UserInfoBean> userInfoBean) throws Exception {
                        if (userInfoBean.getCode() == 0) {
                            mView.showErrorTips("登录成功");
                            mView.gotoMainActivity();
                        } else {
                            mView.showErrorTips("上传工作地址失败，请重试");
                            mView.dismissAlert();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mView.showErrorTips("上传工作地址失败，请确认定位是否有效");
                        mView.dismissAlert();
                    }
                })
        );
    }
}
