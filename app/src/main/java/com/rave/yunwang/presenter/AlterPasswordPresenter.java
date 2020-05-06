package com.rave.yunwang.presenter;

import com.rave.yunwang.R;
import com.rave.yunwang.base.RxBasePresenter;
import com.rave.yunwang.bean.BaseBean;
import com.rave.yunwang.bean.UserInfoBean;
import com.rave.yunwang.contract.AlterPasswordContract;
import com.rave.yunwang.model.RemoteRepository;
import com.rave.yunwang.utils.MD5Util;
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
public class AlterPasswordPresenter extends RxBasePresenter<AlterPasswordContract.View> implements AlterPasswordContract.Presenter<AlterPasswordContract.View> {

    @Override
    public void alterPassword() {

        String oldPassword = mView.getOldPassword();
        String newPassword = mView.getNewPassword();
        String repeatPassword = mView.getRepeatPassword();
        if (StringUtils.isBlank(mView.getOldPassword())) {
            mView.showErrorTips(R.string.empty_old_password_tips);
            return;
        }
        if (StringUtils.isBlank(mView.getNewPassword())) {
            mView.showErrorTips(R.string.empty_new_password_tips);
            return;
        }

        if (StringUtils.isBlank(mView.getRepeatPassword())) {
            mView.showErrorTips(R.string.empty_repeat_password_tips);
            return;
        }

        if (!newPassword.equals(repeatPassword)) {
            mView.showErrorTips(R.string.error_repeat_password_tips);
            return;
        }

        if (!mView.getUserInfo().getPassword().equals(MD5Util.MD5(oldPassword))) {
            mView.showErrorTips(R.string.error_old_password_tips);
            return;
        }


        final Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("user_id", mView.getUserInfo().getUser_id());
        requestMap.put("password", newPassword);
        addSubscrebe(RemoteRepository.getInstance().modifyPassword(requestMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<UserInfoBean>>() {
                    @Override
                    public void accept(BaseBean<UserInfoBean> userInfoBeanBaseBean) throws Exception {
                        if (userInfoBeanBaseBean.getCode() == 0) {
                            mView.refreshUserInfo(userInfoBeanBaseBean.getResult());
                            mView.goBack();
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
}
