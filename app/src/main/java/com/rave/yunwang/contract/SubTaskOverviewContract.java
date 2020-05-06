package com.rave.yunwang.contract;

import com.rave.yunwang.base.BaseContract;
import com.rave.yunwang.bean.IndexBean;
import com.rave.yunwang.bean.TaskOverviewListBean;
import com.rave.yunwang.bean.UserInfoBean;

/**
 * 作者：tianrenzheng on 2019/12/16 19:51
 * 邮箱：317642600@qq.com
 */
public interface SubTaskOverviewContract {

    public interface View extends BaseContract.BaseView {

        UserInfoBean getUserInfo();

        void requestData(boolean isPullRefresh);

        void initData(TaskOverviewListBean indexBean);

        void addData(TaskOverviewListBean indexBean);

        void stopRefreshLayout();

        void gotoTaskOverviewActivity(IndexBean.TaskBean taskBean);

        String getTime();

        String getDisPhone();
    }

    public interface Presenter<T> extends BaseContract.BasePresenter<T> {

        void getSubRecordedTaskList(boolean isPullRefresh);

        void getSubNotRecordedTaskList(boolean isPullRefresh);

        void getSubVerifyFailTaskList(boolean isPullRefresh);

    }
}
