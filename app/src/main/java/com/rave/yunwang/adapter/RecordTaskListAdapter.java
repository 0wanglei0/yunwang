package com.rave.yunwang.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rave.yunwang.R;
import com.rave.yunwang.bean.IndexBean;

import java.util.List;

public class RecordTaskListAdapter extends RecyclerView.Adapter<RecordTaskListAdapter.MyViewHolder> {//左》适配器
    private Context mContext;
    private ClickListener clickListener;
    private List<IndexBean.TaskBean> data;

    public RecordTaskListAdapter(List<IndexBean.TaskBean> data) {
        this.data = data;
    }

    public void refreshData(List<IndexBean.TaskBean> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_record_task, parent, false);
        final MyViewHolder viewHolder = new MyViewHolder(view);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.OnItemClicked(data.get(viewHolder.getLayoutPosition()));
                }
            }
        });

        viewHolder.layoutRecordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.OnItemClicked(data.get(viewHolder.getLayoutPosition()));
                }
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        IndexBean.TaskBean taskBean = this.data.get(position);
        holder.tvToday.setText(taskBean.getJr());
        holder.tvDate.setText(taskBean.getMonth() + "." + taskBean.getDay());
        String content = mContext.getString(R.string.record_task_content, taskBean.getAll_num());
        holder.tvTaskContent.setText(Html.fromHtml(content));
        String taskTime = mContext.getString(R.string.record_task_time, taskBean.getTask_status());
        holder.tvTaskTime.setText(Html.fromHtml(taskTime));
        String distributive = mContext.getString(R.string.record_distributive,
                taskBean.getName(),
                taskBean.getPhone());
        holder.tvDistributive.setText(Html.fromHtml(distributive));
        String taskOverview = mContext.getString(R.string.record_overview,
                taskBean.getNorecorded(),
                taskBean.getRecorded(),
                taskBean.getVer_fail());
        holder.tvTaskOverview.setText(Html.fromHtml(taskOverview));
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private LinearLayout layoutDate;
        private TextView tvToday;
        private TextView tvDate;
        private LinearLayout layoutRecordVideo;
        private TextView tvTaskContent;
        private TextView tvTaskTime;
        private TextView tvDistributive;
        private TextView tvTaskOverview;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.view = itemView;
            this.layoutDate = itemView.findViewById(R.id.layout_date);
            this.tvToday = itemView.findViewById(R.id.tv_today);
            this.tvDate = itemView.findViewById(R.id.tv_date);
            this.layoutRecordVideo = itemView.findViewById(R.id.layout_record_video);
            this.tvTaskContent = itemView.findViewById(R.id.tv_task_content);
            this.tvTaskTime = itemView.findViewById(R.id.tv_task_time);
            this.tvDistributive = itemView.findViewById(R.id.tv_distributive);
            this.tvTaskOverview = itemView.findViewById(R.id.tv_task_overview);
        }
    }

    public interface ClickListener {

        void OnItemClicked(IndexBean.TaskBean itemData);

        void onRecordVideoClicked(IndexBean.TaskBean itemData);

    }
}
