package com.rave.yunwang.presenter;

import com.rave.yunwang.R;
import com.rave.yunwang.base.RxBasePresenter;
import com.rave.yunwang.bean.AssociatBean;
import com.rave.yunwang.bean.BaseBean;
import com.rave.yunwang.bean.TodayRecordVideoBean;
import com.rave.yunwang.contract.RecordVideoContract;
import com.rave.yunwang.model.RemoteRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 作者：tianrenzheng on 2019/12/16 20:08
 * 邮箱：317642600@qq.com
 */
public class RecordVideoPresenter extends RxBasePresenter<RecordVideoContract.View> implements RecordVideoContract.Presenter<RecordVideoContract.View> {

    private int page = 0;

    @Override
    public void getRecordVideoList(final boolean isPullRefresh) {
        if (isPullRefresh) {
            page = 0;
        }
        final Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("page", page);
        requestMap.put("user_id", mView.getUserInfo().getUser_id());
        addSubscrebe(RemoteRepository.getInstance().getAllTodayVin(requestMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<List<TodayRecordVideoBean>>>() {
                    @Override
                    public void accept(BaseBean<List<TodayRecordVideoBean>> indexBeanBaseBean) throws Exception {
                        if (indexBeanBaseBean.getCode() == 0) {
                            if (indexBeanBaseBean.getResult().size() > 0) {
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
                        mView.showErrorTips(R.string.fail_request_record_list_data);
                        mView.stopRefreshLayout();
                    }
                })
        );
    }

    @Override
    public void getSearchResult(String keyWord) {
        final Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("vin", keyWord);
        requestMap.put("user_id", mView.getUserInfo().getUser_id());
        addSubscrebe(RemoteRepository.getInstance().getAssociatedList(requestMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<BaseBean<List<AssociatBean>>>() {
                            @Override
                            public void accept(BaseBean<List<AssociatBean>> indexBeanBaseBean) throws Exception {
                                List<TodayRecordVideoBean> lists = new ArrayList<>();
                                if (indexBeanBaseBean.getResult().size() > 0 && indexBeanBaseBean.getCode() == 0) {
                                    for (AssociatBean associatBean : indexBeanBaseBean.getResult()) {
                                        TodayRecordVideoBean todayRecordVideoBean = new TodayRecordVideoBean();
                                        todayRecordVideoBean.setVin(associatBean.getVin());
                                        todayRecordVideoBean.setTask_id(associatBean.getTask_id());
                                        lists.add(todayRecordVideoBean);
                                    }
                                }
                                mView.initData(lists);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                mView.showErrorTips(R.string.fail_search_record_list_data);
                            }
                        })
        );
    }

    @Override
    public void requestOnTheWay(final int taskId) {
        final Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("task_id", taskId);
        addSubscrebe(RemoteRepository.getInstance().requestOnTheWay(requestMap)
                .subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<BaseBean>() {
                    @Override
                    public void accept(BaseBean baseBean) throws Exception {
                        if (baseBean.getCode() == 200) {
                            mView.removeOnTheWayItem(taskId);
                        } else {
                            mView.showErrorTips(baseBean.getMessage());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mView.showErrorTips(R.string.fail_request_on_the_way);
                    }
                }));
    }
}
