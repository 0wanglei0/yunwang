package com.rave.yunwang.view;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.rave.yunwang.R;
import com.s2icode.dao.S2iCodeResult;
import com.s2icode.dao.S2iCodeResultBase;

public class ShowErrorResultActivity extends Activity {
    protected S2iCodeResultBase s2iCodeResultBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_result);
        //S2iCodeModule.exitModule();
        Bundle bundle = getIntent().getBundleExtra("bundle");
        s2iCodeResultBase = (S2iCodeResultBase) bundle.getSerializable("S2iCodeResult");

        TextView gpsTextView = findViewById(R.id.gpsTextView);
        if (s2iCodeResultBase != null) {
            String outputString = "GPS信息：\n" + s2iCodeResultBase.latitude + "\n" +
                    s2iCodeResultBase.longitude + "\n" +
                    s2iCodeResultBase.country + "\n" +
                    s2iCodeResultBase.province + "\n" +
                    s2iCodeResultBase.city + "\n" +
                    s2iCodeResultBase.district + "\n" +
                    s2iCodeResultBase.address + "\n" + "" +
                    s2iCodeResultBase.errorCode + "\n" + "" +
                    s2iCodeResultBase.errorInfo + "\n" + "";
            if (s2iCodeResultBase instanceof S2iCodeResult) {
                outputString = outputString + "\n" +
                        "statusCode: " + "\n" + s2iCodeResultBase.statusCode;
            }
            gpsTextView.setText(outputString);

        } else {
            gpsTextView.setText("GPS信息无效");
        }

    }
}
