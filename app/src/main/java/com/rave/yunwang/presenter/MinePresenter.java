package com.rave.yunwang.presenter;

import com.rave.yunwang.base.RxBasePresenter;
import com.rave.yunwang.bean.BaseBean;
import com.rave.yunwang.bean.UserInfoBean;
import com.rave.yunwang.contract.MineContract;
import com.rave.yunwang.model.RemoteRepository;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 作者：tianrenzheng on 2019/12/16 20:08
 * 邮箱：317642600@qq.com
 */
public class MinePresenter extends RxBasePresenter<MineContract.View> implements MineContract.Presenter<MineContract.View> {

    @Override
    public void logout() {
        UserInfoBean userInfoBean = mView.getUserInfo();
        if (userInfoBean == null) {
            return;
        }

        final Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("user_id", userInfoBean.getUser_id());
        addSubscrebe(RemoteRepository.getInstance().logout(requestMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean>() {
                    @Override
                    public void accept(BaseBean baseBean) throws Exception {
                        if (baseBean.getCode() == 0) {
                            mView.clearUserInfo();
                            mView.gotoLoginActivity();
                        } else {
                            mView.showErrorTips(baseBean.getMessage());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mView.showErrorTips(throwable.getMessage());
                    }
                })
        );
    }
}
