package com.example.myhealth.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myhealth.R;
import com.example.myhealth.models.WeightRecord;
import java.util.List;

public class WeightHistoryAdapter extends RecyclerView.Adapter<WeightHistoryAdapter.WeightViewHolder> {
    private List<WeightRecord> weightList;

    public WeightHistoryAdapter(List<WeightRecord> weightList) {
        this.weightList = weightList;
    }

    @NonNull
    @Override
    public WeightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weight_record, parent, false);
        return new WeightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeightViewHolder holder, int position) {
        WeightRecord record = weightList.get(position);
        holder.tvWeight.setText(record.getWeight() + " kg");
        holder.tvDate.setText(record.getRecordedDate().toString());
    }

    @Override
    public int getItemCount() {
        return weightList.size();
    }

    static class WeightViewHolder extends RecyclerView.ViewHolder {
        TextView tvWeight, tvDate;

        WeightViewHolder(View itemView) {
            super(itemView);
            tvWeight = itemView.findViewById(R.id.tvWeightItem);
            tvDate = itemView.findViewById(R.id.tvDateItem);
        }
    }
}
