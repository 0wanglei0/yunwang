package com.rave.yunwang.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.rave.yunwang.R;


public class UserInfoEditText extends ConstraintLayout {
    private TextView tvLeft;
    private EditText etRight;
    private ImageView ivArrow;
    private View dividerDashLine;

    public UserInfoEditText(Context context) {
        super(context);
        initView(context);
    }

    public UserInfoEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public UserInfoEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_user_info_edit, this);
        tvLeft = view.findViewById(R.id.tv_left);
        etRight = view.findViewById(R.id.et_right);
        ivArrow = view.findViewById(R.id.iv_arrow);
        dividerDashLine = view.findViewById(R.id.divider_dash_line);
    }

    public void setTvLeftText(@StringRes int text) {
        this.tvLeft.setText(text);
    }

    public String getTvLeftText() {
        return this.tvLeft.getText().toString();
    }

    public void setEtRightText(String text) {
        this.etRight.setText(text);
    }

    public String getEtRightText() {
        return this.etRight.getText().toString();
    }

    public void setEtRightHint(@StringRes int hintText) {
        this.etRight.setHint(hintText);
    }

    public void setShowArrow(boolean showArrow) {
        this.ivArrow.setVisibility(showArrow ? VISIBLE : GONE);
    }

    public void setShowDividerDashLine(boolean showDividerDashLine) {
        this.dividerDashLine.setVisibility(showDividerDashLine ? VISIBLE : GONE);
    }

}
