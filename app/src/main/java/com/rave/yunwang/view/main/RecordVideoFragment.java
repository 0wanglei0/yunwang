package com.rave.yunwang.view.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rave.yunwang.R;
import com.rave.yunwang.adapter.RecordVideoListAdapter;
import com.rave.yunwang.application.BaseApplication;
import com.rave.yunwang.application.MyApplication;
import com.rave.yunwang.base.SimpleFragment;
import com.rave.yunwang.bean.TodayRecordVideoBean;
import com.rave.yunwang.bean.UserInfoBean;
import com.rave.yunwang.contract.RecordVideoContract;
import com.rave.yunwang.presenter.RecordVideoPresenter;
import com.rave.yunwang.utils.SPUtils;
import com.rave.yunwang.utils.StringUtils;
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

/**
 * 作者：tianrenzheng on 2019/12/18 08:49
 * 邮箱：317642600@qq.com
 */
public class RecordVideoFragment extends SimpleFragment implements RecordVideoContract.View {
    private ImageView ivBack;
    private TextView tvTitle;
    private SmartRefreshLayout refreshLayout;
    private EditText etSearchKeyWord;
    private RecyclerView recyclerView;
    private RecordVideoListAdapter adapter;
    private List<TodayRecordVideoBean> data = new ArrayList<>();

    private UserInfoBean userInfoBean;
    private RecordVideoContract.Presenter presenter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_record_video;
    }

    @Override
    protected void initView(View view) {
        presenter = new RecordVideoPresenter();
        presenter.attachView(this);

        ivBack = view.findViewById(R.id.iv_back);
        tvTitle = view.findViewById(R.id.tv_title);
        ivBack.setVisibility(View.GONE);
        tvTitle.setText("选择拍摄车辆");

        refreshLayout = view.findViewById(R.id.refreshLayout);
        etSearchKeyWord = view.findViewById(R.id.et_search_key_word);
        recyclerView = view.findViewById(R.id.recyclerView);
        ImageView deleteInput = view.findViewById(R.id.iv_cancel);
        deleteInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearchKeyWord.setText("");
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

    }

    @Override
    protected void initEventAndData() {
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                presenter.getRecordVideoList(true);
            }
        });

        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                presenter.getRecordVideoList(false);
            }
        });

        etSearchKeyWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (StringUtils.isEmpty(etSearchKeyWord.getText())) {
                    presenter.getRecordVideoList(true);
                    refreshLayout.setEnableRefresh(true);
                    refreshLayout.setEnableLoadMore(true);
                } else {
                    refreshLayout.setEnableRefresh(false);
                    refreshLayout.setEnableLoadMore(false);
                    presenter.getSearchResult(s.toString());
                }
            }
        });

        adapter = new RecordVideoListAdapter(data);
        adapter.setClickListener(new RecordVideoListAdapter.ClickListener() {
            @Override
            public void OnItemClicked(TodayRecordVideoBean itemData) {
                gotoRecordActivity(itemData);
            }

            @Override
            public void OnTheWayButtonClicked(TodayRecordVideoBean itemData) {
                presenter.requestOnTheWay(itemData.getTask_id());
            }
        });

        this.recyclerView.setAdapter(adapter);

        userInfoBean = SPUtils.getObject(getContext(), SPUtils.LOGIN_USER, UserInfoBean.class);
        presenter.getRecordVideoList(true);
    }

    @Override
    public UserInfoBean getUserInfo() {
        return userInfoBean;
    }

    @Override
    public void initData(List<TodayRecordVideoBean> indexBean) {
        this.data = indexBean;
        this.adapter.refreshData(data);
    }

    @Override
    public void addData(List<TodayRecordVideoBean> indexBean) {
        this.data.addAll(indexBean);
        this.adapter.refreshData(data);
    }

    @Override
    public void removeOnTheWayItem(int taskId) {
        for (TodayRecordVideoBean temp : this.data) {
            if (temp.getTask_id() == taskId) {
                this.data.remove(temp);
                break;
            }
        }
        this.adapter.refreshData(data);
    }

    @Override
    public void gotoRecordActivity(final TodayRecordVideoBean recordVideoBean) {
        if (!MyApplication.getInstance().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            showErrorTips("no camera");
            return;
        }

        new RxPermissions(getActivity())
                .request(Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE)
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
                        intent.putExtra(RecordActivity.EXTRA_TASK_ID, recordVideoBean.getTask_id());
                        intent.putExtra(RecordActivity.EXTRA_VIN_CODE, recordVideoBean.getVin());
                        startActivity(intent);
//                                                if (BaseApplication.s2iClientInitResult == null) {
//                            showErrorTips("请等待初始化完成");
//                            return;
//                        }
//                        if (!BaseApplication.s2iClientInitResult.getS2iParam().isCamera2Api()) {
//                            intent = new Intent(getContext(), Camera1RecordActivity.class);
//                        }
//                        intent.putExtra(RecordActivity.EXTRA_TASK_ID, recordVideoBean.getTask_id());
//                        intent.putExtra(RecordActivity.EXTRA_VIN_CODE, recordVideoBean.getVin());
//                        startActivity(intent);
                    }
                });
    }

    @Override
    public void stopRefreshLayout() {
        refreshLayout.finishRefresh();
        refreshLayout.finishLoadMore();
    }

    @Override
    public void showErrorTips(int tips) {

    }

    @Override
    public void showErrorTips(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setCallBackCount() {

    }
}
