package com.rave.yunwang.widget.popupview;

import android.content.Context;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.lxj.xpopup.core.CenterPopupView;
import com.lxj.xpopup.util.XPopupUtils;
import com.rave.yunwang.R;

/**
 * 作者：tianrenzheng on 2020/1/13 20:55
 * 邮箱：317642600@qq.com
 */
public class AlterPasswordCenterPopup extends CenterPopupView {

    private EditText etPassword1;
    private EditText etPassword2;
    private ImageView ivPasswordInvisible1;
    private ImageView ivPasswordInvisible2;
    private Button btnConfirm;
    public PopupViewClickListener clickListener;

    private boolean isPassword1Encryption = true;
    private boolean isPassword2Encryption = true;

    public AlterPasswordCenterPopup(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.popup_alter_password;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        etPassword1 = findViewById(R.id.et_password1);
        etPassword2 = findViewById(R.id.et_password2);
        ivPasswordInvisible1 = findViewById(R.id.iv_password_invisible1);
        ivPasswordInvisible2 = findViewById(R.id.iv_password_invisible2);
        btnConfirm = findViewById(R.id.btn_confirm);

        ivPasswordInvisible1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isPassword1Encryption = !isPassword1Encryption;
                etPassword1.setTransformationMethod(isPassword1Encryption ? PasswordTransformationMethod.getInstance() : HideReturnsTransformationMethod.getInstance());
                if (isPassword1Encryption) {
                    ivPasswordInvisible1.setImageResource(R.mipmap.ic_password_invisible);
                } else {
                    ivPasswordInvisible1.setImageResource(R.mipmap.ic_password_visible);
                }
                etPassword1.setSelection(etPassword1.length());

            }
        });

        ivPasswordInvisible2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isPassword2Encryption = !isPassword2Encryption;
                etPassword2.setTransformationMethod(isPassword2Encryption ? PasswordTransformationMethod.getInstance() : HideReturnsTransformationMethod.getInstance());
                if (isPassword2Encryption) {
                    ivPasswordInvisible2.setImageResource(R.mipmap.ic_password_invisible);
                } else {
                    ivPasswordInvisible2.setImageResource(R.mipmap.ic_password_visible);
                }
                etPassword2.setSelection(etPassword2.length());
            }
        });

        btnConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onConfirmClicked(etPassword1.getText().toString(), etPassword2.getText().toString());
                }
                dismiss();
            }
        });
    }

    public void setClickListener(PopupViewClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    protected int getMaxHeight() {
        return (int) (XPopupUtils.getWindowHeight(getContext()) * .65f);
    }

    public interface PopupViewClickListener {
        void onConfirmClicked(String password1, String password2);
    }
}
