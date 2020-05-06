package com.rave.yunwang.contract;

import com.rave.yunwang.base.BaseContract;
import com.rave.yunwang.bean.IndexBean;
import com.rave.yunwang.bean.UserInfoBean;

/**
 * 作者：tianrenzheng on 2019/12/16 19:51
 * 邮箱：317642600@qq.com
 */
public interface RecordTaskContract {

    public interface View extends BaseContract.BaseView {

        UserInfoBean getUserInfo();

        void initData(IndexBean indexBean);

        void addData(IndexBean indexBean);

        void stopRefreshLayout();

        void gotoTaskOverviewActivity(IndexBean.TaskBean taskBean);
    }

    public interface Presenter<T> extends BaseContract.BasePresenter<T> {

        void getRecordTaskList(boolean isPullRefresh);
    }
}
