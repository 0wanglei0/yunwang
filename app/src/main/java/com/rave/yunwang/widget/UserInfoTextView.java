package com.rave.yunwang.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.rave.yunwang.R;


public class UserInfoTextView extends ConstraintLayout {
    private TextView tvLeft;
    private TextView tvRight;
    private ImageView ivArrow;
    private View dividerDashLine;

    public UserInfoTextView(Context context) {
        super(context);
        initView(context);
    }

    public UserInfoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public UserInfoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_user_info_text, this);
        tvLeft = view.findViewById(R.id.tv_left);
        tvRight = view.findViewById(R.id.tv_right);
        ivArrow = view.findViewById(R.id.iv_arrow);
        dividerDashLine = view.findViewById(R.id.divider_dash_line);
    }

    public void setTvLeftText(@StringRes int text) {
        this.tvLeft.setText(text);
    }

    public void setTvLeftText(String text) {
        this.tvLeft.setText(text);
    }

    public String getTvLeftText() {
        return this.tvLeft.getText().toString();
    }

    public void setTvRightText(@StringRes int text) {
        this.tvRight.setText(text);
    }

    public void setTvRightText(String text) {
        this.tvRight.setText(text);
    }

    public String getTvRightText() {
        return this.tvRight.getText().toString();
    }

    public void setShowArrow(boolean showArrow) {
        this.ivArrow.setVisibility(showArrow ? VISIBLE : GONE);
    }

    public void setShowDividerDashLine(boolean showDividerDashLine) {
        this.dividerDashLine.setVisibility(showDividerDashLine ? VISIBLE : GONE);
    }

}
