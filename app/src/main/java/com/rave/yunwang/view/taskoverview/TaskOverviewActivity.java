package com.rave.yunwang.view.taskoverview;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.rave.yunwang.R;
import com.rave.yunwang.adapter.MainFragmentPagerAdapter;
import com.rave.yunwang.bean.IndexBean;
import com.rave.yunwang.contract.TaskOverviewContract;
import com.rave.yunwang.widget.smarttablayout.SmartTabLayout;

import java.util.ArrayList;

public class TaskOverviewActivity extends AppCompatActivity implements TaskOverviewContract.View {
    public static final String EXTRA_TASK_BEAN = "extra_task_bean";
    public static final int PAGE_RECORDED = 0;
    public static final int PAGE_NOT_RECORD = 1;
    public static final int PAGE_VALID_FAIL = 2;

    private ImageView ivBack;
    private TextView tvTitle;
    private SmartTabLayout tabLayout;
    private ViewPager viewPager;

    private IndexBean.TaskBean taskBean;
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private SubTaskOverviewFragment subRecordedTaskFragment;
    private SubTaskOverviewFragment subNotRecordTaskFragment;
    private SubTaskOverviewFragment subValidFailTaskFragment;
    private MainFragmentPagerAdapter fragmentPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_overview);

        taskBean = (IndexBean.TaskBean) getIntent().getSerializableExtra(EXTRA_TASK_BEAN);

        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(R.string.task_overview_activity_title);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewpager);

        subRecordedTaskFragment = new SubTaskOverviewFragment(PAGE_RECORDED, taskBean);
        subNotRecordTaskFragment = new SubTaskOverviewFragment(PAGE_NOT_RECORD, taskBean);
        subValidFailTaskFragment = new SubTaskOverviewFragment(PAGE_VALID_FAIL, taskBean);
        fragments.add(subRecordedTaskFragment);
        fragments.add(subNotRecordTaskFragment);
        fragments.add(subValidFailTaskFragment);

        String[] tabTitles = new String[3];
        tabTitles[0] = getString(R.string.tab_record_title, taskBean.getRecorded());
        tabTitles[1] = getString(R.string.tab_not_record_title, taskBean.getNorecorded());
        tabTitles[2] = getString(R.string.tab_valid_fail_title, taskBean.getVer_fail());
        fragmentPagerAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager(), fragments, tabTitles);

        viewPager.setAdapter(fragmentPagerAdapter);
        tabLayout.setViewPager(viewPager);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void showErrorTips(int tips) {

    }

    @Override
    public void showErrorTips(String text) {

    }

    @Override
    public void setCallBackCount() {

    }
}
