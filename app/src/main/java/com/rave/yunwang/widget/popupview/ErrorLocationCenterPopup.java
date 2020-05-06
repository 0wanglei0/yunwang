package com.rave.yunwang.widget.popupview;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lxj.xpopup.core.CenterPopupView;
import com.rave.yunwang.R;

public class ErrorLocationCenterPopup extends CenterPopupView {
    private TextView tvTitle;
    private TextView tvTipsContent;
    private Button btnConfirm;
    public ErrorLocationCenterPopup(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.popup_errore_location;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        btnConfirm = findViewById(R.id.btn_error_location);
        btnConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }
}
