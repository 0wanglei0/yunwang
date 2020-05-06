package com.rave.yunwang.base;

import androidx.annotation.StringRes;

public interface BaseContract {
    interface BaseView {
        void showErrorTips(@StringRes int tips);

        void showErrorTips(String text);
        void setCallBackCount();
    }

    interface BasePresenter<T> {

        void attachView(T view);

        void detachView();

    }

}