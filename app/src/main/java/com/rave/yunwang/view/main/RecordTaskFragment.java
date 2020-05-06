package com.rave.yunwang.view.main;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rave.yunwang.R;
import com.rave.yunwang.adapter.RecordTaskListAdapter;
import com.rave.yunwang.base.SimpleFragment;
import com.rave.yunwang.bean.IndexBean;
import com.rave.yunwang.bean.UserInfoBean;
import com.rave.yunwang.contract.RecordTaskContract;
import com.rave.yunwang.presenter.RecordTaskPresenter;
import com.rave.yunwang.utils.SPUtils;
import com.rave.yunwang.view.taskoverview.TaskOverviewActivity;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：tianrenzheng on 2019/12/18 08:49
 * 邮箱：317642600@qq.com
 */
public class RecordTaskFragment extends SimpleFragment implements RecordTaskContract.View {//左》今日任务
    private ImageView ivBack;
    private TextView tvTitle;
    private SmartRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private RecordTaskListAdapter adapter;
    private List<IndexBean.TaskBean> data = new ArrayList<>();

    private UserInfoBean userInfoBean;
    private RecordTaskContract.Presenter presenter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_record_task;
    }

    @Override
    protected void initView(View view) {
        presenter = new RecordTaskPresenter();
        presenter.attachView(this);

        ivBack = view.findViewById(R.id.iv_back);
        tvTitle = view.findViewById(R.id.tv_title);
        ivBack.setVisibility(View.GONE);
        tvTitle.setText(R.string.record_task_fragment_title);

        refreshLayout = view.findViewById(R.id.refreshLayout);
        recyclerView = view.findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void initEventAndData() {
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                presenter.getRecordTaskList(true);
            }
        });

        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                presenter.getRecordTaskList(false);
            }
        });

        adapter = new RecordTaskListAdapter(data);
        adapter.setClickListener(new RecordTaskListAdapter.ClickListener() {
            @Override
            public void OnItemClicked(IndexBean.TaskBean itemData) {
                gotoTaskOverviewActivity(itemData);
            }

            @Override
            public void onRecordVideoClicked(IndexBean.TaskBean itemData) {

            }
        });

        this.recyclerView.setAdapter(adapter);

        userInfoBean = SPUtils.getObject(getContext(), SPUtils.LOGIN_USER, UserInfoBean.class);
        presenter.getRecordTaskList(true);
    }

    @Override
    public UserInfoBean getUserInfo() {
        return userInfoBean;
    }

    @Override
    public void initData(IndexBean indexBean) {
        this.data.clear();
        this.data.addAll(indexBean.getToday());
        this.data.addAll(indexBean.getOther());
        this.adapter.refreshData(data);
    }

    public void addData(IndexBean indexBean) {
        this.data.addAll(indexBean.getToday());
        this.adapter.refreshData(data);
    }

    @Override
    public void stopRefreshLayout() {
        this.refreshLayout.finishRefresh();
        this.refreshLayout.finishLoadMore();
    }

    @Override
    public void gotoTaskOverviewActivity(IndexBean.TaskBean taskBean) {
        Intent intent = new Intent(getActivity(), TaskOverviewActivity.class);
        intent.putExtra(TaskOverviewActivity.EXTRA_TASK_BEAN, taskBean);
        startActivity(intent);
    }

    @Override
    public void showErrorTips(int tips) {
        Toast.makeText(getContext(), getString(tips), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showErrorTips(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setCallBackCount() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }
}
