package com.rave.yunwang.presenter;

import com.rave.yunwang.R;
import com.rave.yunwang.base.RxBasePresenter;
import com.rave.yunwang.bean.BaseBean;
import com.rave.yunwang.bean.UserInfoBean;
import com.rave.yunwang.contract.AlterEmailContract;
import com.rave.yunwang.contract.AlterPhoneNumberContract;
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
public class AlterPhoneNumberPresenter extends RxBasePresenter<AlterPhoneNumberContract.View> implements AlterPhoneNumberContract.Presenter<AlterPhoneNumberContract.View> {

    @Override
    public void alterPhoneNumber() {
        String newPhoneNum = mView.getNewPhoneNumber();
        if (StringUtils.isBlank(newPhoneNum)) {
            return;
        }

        final Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("user_id", mView.getUserInfo().getUser_id());
        requestMap.put("phone", newPhoneNum);

        addSubscrebe(RemoteRepository.getInstance().modifyPhoneNum(requestMap)
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
                        mView.showErrorTips(R.string.fail_modify_phone_num_tips);
                    }
                })
        );

    }
}
