package com.rave.yunwang.presenter;

import com.rave.yunwang.R;
import com.rave.yunwang.base.RxBasePresenter;
import com.rave.yunwang.bean.BaseBean;
import com.rave.yunwang.bean.IndexBean;
import com.rave.yunwang.contract.RecordTaskContract;
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
public class RecordTaskPresenter extends RxBasePresenter<RecordTaskContract.View> implements RecordTaskContract.Presenter<RecordTaskContract.View> {

    private int page = 0;

    @Override
    public void getRecordTaskList(final boolean isPullRefresh) {
        if (isPullRefresh) {
            page = 0;
        }
        final Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("page", page);
        requestMap.put("user_id", mView.getUserInfo().getUser_id());
        addSubscrebe(RemoteRepository.getInstance().getTaskList(requestMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<IndexBean>>() {
                    @Override
                    public void accept(BaseBean<IndexBean> indexBeanBaseBean) throws Exception {
                        if (indexBeanBaseBean.getCode() == 0) {
                            if (indexBeanBaseBean.getResult().getToday().size() > 0 || indexBeanBaseBean.getResult().getOther().size() > 0) {
                                if (isPullRefresh) {
                                    mView.initData(indexBeanBaseBean.getResult());
                                } else {
                                    mView.addData(indexBeanBaseBean.getResult());
                                }
                                page++;
                            }
                        } else {
                            mView.showErrorTips(indexBeanBaseBean.getMessage());
                        }
                        mView.stopRefreshLayout();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mView.showErrorTips(R.string.fail_request_index_data);
                        mView.stopRefreshLayout();
                    }
                })
        );
    }
}
