package com.rave.yunwang.contract;

import com.rave.yunwang.base.BaseContract;
import com.rave.yunwang.bean.TodayRecordVideoBean;
import com.rave.yunwang.bean.UserInfoBean;

import java.util.List;

/**
 * 作者：tianrenzheng on 2019/12/16 19:51
 * 邮箱：317642600@qq.com
 */
public interface RecordVideoContract {

    public interface View extends BaseContract.BaseView {

        UserInfoBean getUserInfo();

        void initData(List<TodayRecordVideoBean> indexBean);

        void addData(List<TodayRecordVideoBean> indexBean);

        void removeOnTheWayItem(int taskId);

        void stopRefreshLayout();

        void gotoRecordActivity(TodayRecordVideoBean recordVideoBean);

    }

    public interface Presenter<T> extends BaseContract.BasePresenter<T> {

        void getRecordVideoList(boolean isPullRefresh);

        void getSearchResult(String keyWord);

        void requestOnTheWay(int taskId);
    }
}
