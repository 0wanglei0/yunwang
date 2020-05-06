package com.rave.yunwang.view;

import android.os.Bundle;
import android.widget.TextView;

import com.rave.yunwang.R;
import com.s2icode.dao.S2iCodeResult;

public class ShowResultActivity extends ShowErrorResultActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView tvResult = findViewById(R.id.resultTextView);
        if (s2iCodeResultBase != null && s2iCodeResultBase instanceof S2iCodeResult && tvResult != null) {
            tvResult.setText("解码结果：\n" + ((S2iCodeResult)s2iCodeResultBase).serialNumber + "\n" +
                    ((S2iCodeResult)s2iCodeResultBase).data + "\n" +
                    ((S2iCodeResult)s2iCodeResultBase).marketingUrl + "\n" +
                    ((S2iCodeResult)s2iCodeResultBase).timestamp);
        } else {
            tvResult.setText("解码失败");
        }
    }
}