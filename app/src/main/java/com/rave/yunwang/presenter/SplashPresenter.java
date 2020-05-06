package com.rave.yunwang.presenter;

import com.rave.yunwang.base.RxBasePresenter;
import com.rave.yunwang.bean.BaseBean;
import com.rave.yunwang.contract.SplashContract;
import com.rave.yunwang.model.RemoteRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class SplashPresenter extends RxBasePresenter<SplashContract.View> implements SplashContract.Presenter<SplashContract.View> {

    @Override
    public void startCountDown() {
        addSubscrebe(Observable.interval(0, 300, TimeUnit.MILLISECONDS)
                .take(5)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        if (mView.getLoginUser() == null) {
                            mView.gotoLoginActivity();
                        } else {
                            mView.gotoMainActivity();
                        }
                    }
                })
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        mView.setCountDownText(String.valueOf(aLong));
                    }
                })

        );
    }

    @Override
    public void checkVersion(String versionName) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("version", "1.0.1");
        addSubscrebe(RemoteRepository.getInstance().checkVersion(requestMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean>() {
                    @Override
                    public void accept(BaseBean responseBean) throws Exception {
                        if (responseBean.getCode() == 1) {
                            mView.showUpgradeDialog();
                        }
                    }
                })
        );
    }
}
