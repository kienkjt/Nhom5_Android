package com.nhom5.healthtracking.step;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.data.local.entity.StepRecord;

import java.util.List;

public class StepHistoryAdapter extends RecyclerView.Adapter<StepHistoryAdapter.ViewHolder> {
    private List<StepRecord> records;

    public StepHistoryAdapter(List<StepRecord> records) {
        this.records = records;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_step_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StepRecord record = records.get(position);
        holder.tvDate.setText(record.getDate());
        holder.tvSteps.setText(record.getStepCount() + " bước");
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public void updateData(List<StepRecord> newRecords) {
        this.records = newRecords;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvSteps;

        ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvSteps = itemView.findViewById(R.id.tvSteps);
        }
    }
}
