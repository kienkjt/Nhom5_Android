package com.nhom5.healthtracking.Blood;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.data.local.entity.BloodPressureRecord;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BloodPressureAdapter extends RecyclerView.Adapter<BloodPressureAdapter.BPViewHolder> {

    private List<BloodPressureRecord> records;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public BloodPressureAdapter(List<BloodPressureRecord> records) {
        this.records = records;
    }

    public void updateData(List<BloodPressureRecord> newRecords) {
        this.records = newRecords;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BPViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bp_history, parent, false);
        return new BPViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BPViewHolder holder, int position) {
        BloodPressureRecord record = records.get(position);
        holder.tvBP.setText(record.systolic + "/" + record.diastolic + " mmHg");
        holder.tvDate.setText(dateFormat.format(record.measuredAt));
    }

    @Override
    public int getItemCount() {
        return records == null ? 0 : records.size();
    }

    static class BPViewHolder extends RecyclerView.ViewHolder {
        TextView tvBP, tvDate;

        BPViewHolder(View itemView) {
            super(itemView);
            tvBP = itemView.findViewById(R.id.tvBP);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
