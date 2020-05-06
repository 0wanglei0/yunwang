package com.rave.yunwang.contract;

import com.rave.yunwang.base.BaseContract;

/**
 * 作者：tianrenzheng on 2019/12/16 19:51
 * 邮箱：317642600@qq.com
 */
public interface MainContract {

    public interface View extends BaseContract.BaseView {

        void showRecordTaskListFragment();

        void refreshRecordTaskListFragment();

        void showRecordVideoFragment();

        void refreshRecordVideoFragment();

        void showMineFragment();

        void cleanTabSelectStatus();

        void setTabSelectStatus(int selectedTab);

    }

    public interface Presenter<T> extends BaseContract.BasePresenter<T> {


    }
}
