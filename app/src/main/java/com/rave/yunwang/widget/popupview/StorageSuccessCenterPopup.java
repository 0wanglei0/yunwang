package com.rave.yunwang.widget.popupview;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lxj.xpopup.core.CenterPopupView;
import com.rave.yunwang.R;

public class StorageSuccessCenterPopup extends CenterPopupView {
    private TextView tvTitle;
    private TextView tvTipsContent;
    public StorageSuccessCenterPopup(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.popup_storage;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
//        new Handler().postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//              dismiss();
//            }
//        }, 3000);    //延时3s关闭

    }
}
