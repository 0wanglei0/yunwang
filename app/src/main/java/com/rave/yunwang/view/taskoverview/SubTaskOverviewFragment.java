package com.rave.yunwang.view.taskoverview;

import android.Manifest;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rave.yunwang.R;
import com.rave.yunwang.adapter.SubTaskOverviewAdapter;
import com.rave.yunwang.application.BaseApplication;
import com.rave.yunwang.base.SimpleFragment;
import com.rave.yunwang.bean.IndexBean;
import com.rave.yunwang.bean.TaskOverviewListBean;
import com.rave.yunwang.bean.UserInfoBean;
import com.rave.yunwang.contract.SubTaskOverviewContract;
import com.rave.yunwang.presenter.SubTaskOverviewPresenter;
import com.rave.yunwang.utils.SPUtils;
import com.rave.yunwang.utils.permission.RxPermissions;
import com.rave.yunwang.view.Camera1RecordActivity;
import com.rave.yunwang.view.RecordActivity;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;

public class SubTaskOverviewFragment extends SimpleFragment implements SubTaskOverviewContract.View {
    private final static String TIME_DIVIDER = "-";
    private int pageType;
    private IndexBean.TaskBean taskBean;
    private SmartRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private SubTaskOverviewAdapter adapter;
    private List<TaskOverviewListBean.DataBean> data = new ArrayList<>();
    private UserInfoBean userInfoBean;
    private SubTaskOverviewContract.Presenter presenter;

    public SubTaskOverviewFragment(int pageType, IndexBean.TaskBean taskBean) {
        this.pageType = pageType;
        this.taskBean = taskBean;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_sub_task_overview;
    }

    @Override
    protected void initView(View view) {
        presenter = new SubTaskOverviewPresenter();
        presenter.attachView(this);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        recyclerView = view.findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        userInfoBean = SPUtils.getObject(getContext(), SPUtils.LOGIN_USER, UserInfoBean.class);
    }

    @Override
    protected void initEventAndData() {
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                requestData(true);
            }
        });

        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                requestData(false);
            }
        });

        adapter = new SubTaskOverviewAdapter(data);
        adapter.setClickListener(new SubTaskOverviewAdapter.ClickListener() {
            @Override
            public void OnItemClicked(final TaskOverviewListBean.DataBean itemData) {
//                gotoTaskOverviewActivity(itemData);
                new RxPermissions(getActivity())
                        .request(Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                Intent intent = new Intent(getContext(), RecordActivity.class);
                                if (BaseApplication.s2iClientInitResult == null) {
                                    showErrorTips("请等待初始化完成");
                                    return;
                                }
                                if (!BaseApplication.s2iClientInitResult.getS2iParam().isCamera2Api()) {
                                    intent = new Intent(getContext(), Camera1RecordActivity.class);
                                }
                                intent.putExtra(RecordActivity.EXTRA_TASK_ID, 45919);
                                intent.putExtra(RecordActivity.EXTRA_VIN_CODE, itemData.getVin());
                                startActivity(intent);
                            }
                        });
            }
        });

        this.recyclerView.setAdapter(adapter);
        requestData(true);
    }

    @Override
    public void requestData(boolean isPullRefresh) {
        if (this.pageType == TaskOverviewActivity.PAGE_RECORDED) {
            presenter.getSubRecordedTaskList(isPullRefresh);
        } else if (this.pageType == TaskOverviewActivity.PAGE_NOT_RECORD) {
            presenter.getSubNotRecordedTaskList(isPullRefresh);
        } else if (this.pageType == TaskOverviewActivity.PAGE_VALID_FAIL) {
            presenter.getSubVerifyFailTaskList(isPullRefresh);
        }
    }

    @Override
    public UserInfoBean getUserInfo() {
        return userInfoBean;
    }

    @Override
    public void initData(TaskOverviewListBean indexBean) {
        this.data = indexBean.getData();
        this.adapter.refreshData(data);
    }

    @Override
    public void addData(TaskOverviewListBean indexBean) {
        this.data.addAll(indexBean.getData());
        this.adapter.refreshData(data);
    }

    @Override
    public void stopRefreshLayout() {
        refreshLayout.finishRefresh();
        refreshLayout.finishLoadMore();
    }

    @Override
    public void gotoTaskOverviewActivity(IndexBean.TaskBean taskBean) {

    }

    @Override
    public String getTime() {
        StringBuilder sb = new StringBuilder();
        sb.append(taskBean.getYear()).append(TIME_DIVIDER).append(taskBean.getMonth()).append(TIME_DIVIDER).append(taskBean.getDay());
        return sb.toString();
    }

    @Override
    public String getDisPhone() {
        return this.taskBean.getPhone();
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
}
