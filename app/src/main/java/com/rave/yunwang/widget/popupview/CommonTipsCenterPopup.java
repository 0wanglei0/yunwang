package com.rave.yunwang.widget.popupview;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lxj.xpopup.core.CenterPopupView;
import com.rave.yunwang.R;

/**
 * 作者：tianrenzheng on 2020/1/15 22:18
 * 邮箱：317642600@qq.com
 */
public class CommonTipsCenterPopup extends CenterPopupView {
    private TextView tvTitle;
    private TextView tvTipsContent;
    private Button btnConfirm;
    private Button btnCancel;

    private PositiveButtonClickListener positiveButtonClickListener;
    private NegativeButtonClickListener negativeButtonClickListener;

    public CommonTipsCenterPopup(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.popup_common_tips;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        btnConfirm = findViewById(R.id.btn_sure);
        btnCancel = findViewById(R.id.btn_cancel);

//        btnConfirm.setVisibility(GONE);
//        btnCancel.setVisibility(GONE);

        btnConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (positiveButtonClickListener != null) {
                    positiveButtonClickListener.onPositiveButtonClicked();
                }
                dismiss();
            }
        });

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (negativeButtonClickListener != null) {
                    negativeButtonClickListener.onNegativeButtonClicked();
                }
                dismiss();
            }
        });
    }

    public void setPositiveButtonClickListener(PositiveButtonClickListener positiveButtonClickListener) {
//        this.btnConfirm.setVisibility(VISIBLE);
        this.positiveButtonClickListener = positiveButtonClickListener;
    }

    public void setNegativeButtonClickListener(NegativeButtonClickListener negativeButtonClickListener) {
//        this.btnCancel.setVisibility(VISIBLE);
        this.negativeButtonClickListener = negativeButtonClickListener;
    }

    public interface PositiveButtonClickListener {
        void onPositiveButtonClicked();
    }

    public interface NegativeButtonClickListener {
        void onNegativeButtonClicked();
    }


}
