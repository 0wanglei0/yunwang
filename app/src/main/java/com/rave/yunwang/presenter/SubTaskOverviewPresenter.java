package com.rave.yunwang.presenter;

import com.rave.yunwang.R;
import com.rave.yunwang.base.RxBasePresenter;
import com.rave.yunwang.bean.BaseBean;
import com.rave.yunwang.bean.TaskOverviewListBean;
import com.rave.yunwang.contract.SubTaskOverviewContract;
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
public class SubTaskOverviewPresenter extends RxBasePresenter<SubTaskOverviewContract.View> implements SubTaskOverviewContract.Presenter<SubTaskOverviewContract.View> {

    private int page = 0;

    @Override
    public void getSubRecordedTaskList(final boolean isPullRefresh) {
        if (isPullRefresh) {
            page = 0;
        }
        final Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("user_id", mView.getUserInfo().getUser_id());
        requestMap.put("page", page);
        requestMap.put("time", mView.getTime());
        requestMap.put("dis_phone", mView.getDisPhone());

        addSubscrebe(RemoteRepository.getInstance().getRecordedTaskList(requestMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<TaskOverviewListBean>>() {
                    @Override
                    public void accept(BaseBean<TaskOverviewListBean> indexBeanBaseBean) throws Exception {
                        if (indexBeanBaseBean.getCode() == 0) {
                            if (indexBeanBaseBean.getResult().getData().size() > 0) {
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
                        mView.showErrorTips(R.string.fail_request_overview_data);
                        mView.stopRefreshLayout();
                    }
                })
        );
    }

    @Override
    public void getSubNotRecordedTaskList(final boolean isPullRefresh) {
        if (isPullRefresh) {
            page = 0;
        }
        final Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("user_id", mView.getUserInfo().getUser_id());
        requestMap.put("page", page);
        requestMap.put("time", mView.getTime());
        requestMap.put("dis_phone", mView.getDisPhone());

        addSubscrebe(RemoteRepository.getInstance().getNotRecordedTaskList(requestMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<TaskOverviewListBean>>() {
                    @Override
                    public void accept(BaseBean<TaskOverviewListBean> indexBeanBaseBean) throws Exception {
                        if (indexBeanBaseBean.getCode() == 0) {
                            if (indexBeanBaseBean.getResult().getData().size() > 0) {
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
                        mView.showErrorTips(R.string.fail_request_overview_data);
                        mView.stopRefreshLayout();
                    }
                })
        );
    }

    @Override
    public void getSubVerifyFailTaskList(final boolean isPullRefresh) {
        if (isPullRefresh) {
            page = 0;
        }
        final Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("user_id", mView.getUserInfo().getUser_id());
        requestMap.put("page", page);
        requestMap.put("time", mView.getTime());
        requestMap.put("dis_phone", mView.getDisPhone());

        addSubscrebe(RemoteRepository.getInstance().getVerifyFailTaskList(requestMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<TaskOverviewListBean>>() {
                    @Override
                    public void accept(BaseBean<TaskOverviewListBean> indexBeanBaseBean) throws Exception {
                        if (indexBeanBaseBean.getCode() == 0) {
                            if (indexBeanBaseBean.getResult().getData().size() > 0) {
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
                        mView.showErrorTips(R.string.fail_request_overview_data);
                        mView.stopRefreshLayout();
                    }
                })
        );
    }
}
