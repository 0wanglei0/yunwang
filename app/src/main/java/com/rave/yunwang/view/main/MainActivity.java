package com.rave.yunwang.view.main;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.rave.yunwang.R;
import com.rave.yunwang.adapter.MainFragmentPagerAdapter;
import com.rave.yunwang.application.BaseApplication;
import com.rave.yunwang.contract.MainContract;
import com.rave.yunwang.widget.NoScrollViewPager;
import com.s2icode.dao.S2iClientInitBase;
import com.s2icode.dao.S2iClientInitResult;
import com.s2icode.main.S2iClientInitInterface;
import com.s2icode.main.S2iCodeModule;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements MainContract.View, S2iClientInitInterface {

    private static final int PAGE_RECORD_TASK = 0;
    private static final int PAGE_RECORD_VIDEO = 1;
    private static final int PAGE_MINE = 2;
    private int currentSelectedPage = PAGE_RECORD_TASK;

    private LinearLayout tabRecordVideo;
    private ImageView ivRecordVideo;
    private TextView tvRecordVideo;
    private LinearLayout tabRecordTask;
    private ImageView ivRecordTask;
    private TextView tvRecordTask;
    private LinearLayout tabMine;
    private ImageView ivMine;
    private TextView tvMine;
//    private LinearLayout llNoINfo;

    private NoScrollViewPager viewPager;
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private RecordTaskFragment recordTaskFragment;//左边》》今日任务页
    private RecordVideoFragment recordVideoFragment;//中间》》录制视频页
    private MineFragment mineFragment;//
    private MainFragmentPagerAdapter fragmentPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        S2iCodeModule.setS2iClientInitInterface(this);
        S2iCodeModule.startS2iClient();
        viewPager = findViewById(R.id.viewpager);
        recordTaskFragment = new RecordTaskFragment();
        recordVideoFragment = new RecordVideoFragment();
        mineFragment = new MineFragment();
        fragments.add(recordTaskFragment);
        fragments.add(recordVideoFragment);
        fragments.add(mineFragment);

        fragmentPagerAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager(), fragments, getResources().getStringArray(R.array.main_activity_tab));
        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(fragmentPagerAdapter);
//        llNoINfo = findViewById(R.id.ll_no_info);
        tabRecordTask = findViewById(R.id.tab_record_task);
        ivRecordTask = findViewById(R.id.iv_record_task);
        tvRecordTask = findViewById(R.id.tv_record_task);

        tabRecordVideo = findViewById(R.id.tab_record_video);
        ivRecordVideo = findViewById(R.id.iv_record_video);
        tvRecordVideo = findViewById(R.id.tv_record_video);

        tabMine = findViewById(R.id.tab_mine);
        ivMine = findViewById(R.id.iv_mine);
        tvMine = findViewById(R.id.tv_mine);

        tabRecordTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSelectedPage != PAGE_RECORD_TASK) {
                    setTabSelectStatus(PAGE_RECORD_TASK);
                    showRecordTaskListFragment();
                } else {
                    refreshRecordTaskListFragment();
                }
            }
        });

        tabRecordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSelectedPage != PAGE_RECORD_VIDEO) {
                    setTabSelectStatus(PAGE_RECORD_VIDEO);
                    showRecordVideoFragment();
                } else {
                    refreshRecordVideoFragment();
                }
            }
        });

        tabMine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSelectedPage != PAGE_MINE) {
                    setTabSelectStatus(PAGE_MINE);
                    showMineFragment();
                }
            }
        });

        setTabSelectStatus(PAGE_RECORD_TASK);
    }

    @Override
    public void showRecordTaskListFragment() {

    }

    @Override
    public void refreshRecordTaskListFragment() {

    }

    @Override
    public void showRecordVideoFragment() {

    }

    @Override
    public void refreshRecordVideoFragment() {

    }

    @Override
    public void showMineFragment() {

    }

    @Override
    public void cleanTabSelectStatus() {
        ivRecordTask.setImageResource(R.mipmap.ic_record_task_unselected);
        tvRecordTask.setTextColor(getResources().getColor(R.color.textColorGrey));

        ivRecordVideo.setImageResource(R.mipmap.ic_record_video_unselected);
        tvRecordVideo.setTextColor(getResources().getColor(R.color.textColorGrey));

        ivMine.setImageResource(R.mipmap.ic_mine_unselected);
        tvMine.setTextColor(getResources().getColor(R.color.textColorGrey));
    }

    @Override
    public void setTabSelectStatus(int selectedTab) {
        cleanTabSelectStatus();
        this.currentSelectedPage = selectedTab;
        switch (selectedTab) {
            case PAGE_RECORD_TASK:
                ivRecordTask.setImageResource(R.mipmap.ic_record_task_selected);
                tvRecordTask.setTextColor(getResources().getColor(R.color.textColorMain));
                break;
            case PAGE_RECORD_VIDEO:
                ivRecordVideo.setImageResource(R.mipmap.ic_record_video_selected);
                tvRecordVideo.setTextColor(getResources().getColor(R.color.textColorMain));
                break;
            case PAGE_MINE:
                ivMine.setImageResource(R.mipmap.ic_mine_selected);
                tvMine.setTextColor(getResources().getColor(R.color.textColorMain));
                break;
        }

        viewPager.setCurrentItem(selectedTab, false);
    }

    @Override
    public void showErrorTips(final int tips) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, getString(tips), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void showErrorTips(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void setCallBackCount() {

    }


    @Override
    public void onS2iClientInit(S2iClientInitResult s2iClientInitResult) {
        BaseApplication.s2iClientInitResult = s2iClientInitResult;
    }

    @Override
    public void onS2iClientInitError(S2iClientInitBase s2iClientInitBase) {
        showErrorTips("S2i扫描SDK初始化失败");
    }
}
