package com.rave.yunwang.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rave.yunwang.R;
import com.rave.yunwang.bean.TodayRecordVideoBean;

import java.util.List;

public class RecordVideoListAdapter extends RecyclerView.Adapter<RecordVideoListAdapter.MyViewHolder>  {//中》录制适配器
    private Context mContext;
    private ClickListener clickListener;
    private List<TodayRecordVideoBean> data;

    public RecordVideoListAdapter(List<TodayRecordVideoBean> data) {
        this.data = data;
    }

    public void refreshData(List<TodayRecordVideoBean> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_today_record_task, parent, false);
        final MyViewHolder viewHolder = new MyViewHolder(view);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.OnItemClicked(data.get(viewHolder.getLayoutPosition()));
                }
            }
        });

        viewHolder.btnOnTheWay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.OnTheWayButtonClicked(data.get(viewHolder.getLayoutPosition()));
                }
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TodayRecordVideoBean taskBean = this.data.get(position);
        holder.tvVinCode.setText(taskBean.getVin());
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
        private TextView tvIndex;
        private TextView tvVinCode;
        private Button btnOnTheWay;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.view = itemView;
            this.tvVinCode = itemView.findViewById(R.id.tv_vin_code);
            this.btnOnTheWay = itemView.findViewById(R.id.btn_on_the_way);
        }
    }

    public interface ClickListener {

        void OnItemClicked(TodayRecordVideoBean itemData);

        void OnTheWayButtonClicked(TodayRecordVideoBean itemData);

    }
}
