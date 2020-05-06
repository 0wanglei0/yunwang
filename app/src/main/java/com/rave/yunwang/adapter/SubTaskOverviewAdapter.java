package com.rave.yunwang.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rave.yunwang.R;
import com.rave.yunwang.bean.TaskOverviewListBean;

import java.util.List;

public class SubTaskOverviewAdapter extends RecyclerView.Adapter<SubTaskOverviewAdapter.MyViewHolder> {
    private Context mContext;
    private ClickListener clickListener;
    private List<TaskOverviewListBean.DataBean> data;

    public SubTaskOverviewAdapter(List<TaskOverviewListBean.DataBean> data) {
        this.data = data;
    }

    public void refreshData(List<TaskOverviewListBean.DataBean> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_sub_record_task, parent, false);
        final MyViewHolder viewHolder = new MyViewHolder(view);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
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
        TaskOverviewListBean.DataBean taskBean = this.data.get(position);
        holder.tvVinCode.setText(mContext.getString(R.string.vin_code_prefix, taskBean.getVin()));
        String status = mContext.getString(R.string.record_status_prefix, taskBean.getStatus());
        holder.tvRecordStatus.setText(Html.fromHtml(status));
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
        private TextView tvVinCode;
        private TextView tvRecordStatus;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.view = itemView;
            this.tvVinCode = itemView.findViewById(R.id.tv_vin_code);
            this.tvRecordStatus = itemView.findViewById(R.id.tv_record_status);
        }
    }

    public interface ClickListener {

        void OnItemClicked(TaskOverviewListBean.DataBean itemData);

    }
}
