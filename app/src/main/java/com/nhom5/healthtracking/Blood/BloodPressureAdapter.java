package com.nhom5.healthtracking.Blood;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.data.local.entity.BloodPressureRecord;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BloodPressureAdapter extends RecyclerView.Adapter<BloodPressureAdapter.BPViewHolder> {

    private List<BloodPressureRecord> records;
    private final Context context;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public BloodPressureAdapter(Context context, List<BloodPressureRecord> records) {
        this.context = context;
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
                .inflate(R.layout.activity_bp_history, parent, false);
        return new BPViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BPViewHolder holder, int position) {
        BloodPressureRecord record = records.get(position);

        holder.tvBPValue.setText(record.systolic + "/" + record.diastolic + " mmHg");
        holder.tvDate.setText(record.measuredAt != null ? dateFormat.format(record.measuredAt) : "—");
        holder.tvNote.setText(record.notes != null && !record.notes.isEmpty() ? "Ghi chú: " + record.notes : "Ghi chú:");

        holder.itemView.setOnClickListener(v ->
                Toast.makeText(context, "Đo lần " + (position + 1), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return records == null ? 0 : records.size();
    }

    static class BPViewHolder extends RecyclerView.ViewHolder {
        TextView tvBPValue, tvDate, tvNote;

        BPViewHolder(View itemView) {
            super(itemView);
            tvBPValue = itemView.findViewById(R.id.tvBPValue);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvNote = itemView.findViewById(R.id.tvNote);
        }
    }
}
