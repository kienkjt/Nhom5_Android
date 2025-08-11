package com.nhom5.healthtracking.weight;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.util.DatabaseHelper;
import com.nhom5.healthtracking.data.local.entity.WeightRecord;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class WeightTrackerActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private EditText etWeight;
    private TextView tvCurrentWeight, tvWeightChange, tvBMI, tvBMICategory;
    private ProgressBar progressBMI;
    private RecyclerView rvWeightHistory;
    private LineChart chartWeight;

    // Tạm thời giả lập userId, sau này lấy từ Firebase
    private String currentUserId = "demo-user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weight_tracker); // XML bạn đã gửi

        // Khởi tạo database helper
        dbHelper = new DatabaseHelper(this);

        // Ánh xạ view
        etWeight = findViewById(R.id.etWeight);
        tvCurrentWeight = findViewById(R.id.tvCurrentWeight);
        tvWeightChange = findViewById(R.id.tvWeightChange);
        tvBMI = findViewById(R.id.tvBMI);
        tvBMICategory = findViewById(R.id.tvBMICategory);
        progressBMI = findViewById(R.id.progressBMI);
        rvWeightHistory = findViewById(R.id.rvWeightHistory);
        chartWeight = findViewById(R.id.chartWeight);

        Button btnAddWeight = findViewById(R.id.btnAddWeight);

        // Cấu hình RecyclerView
        rvWeightHistory.setLayoutManager(new LinearLayoutManager(this));

        // Xử lý click nút Lưu cân nặng
        btnAddWeight.setOnClickListener(v -> {
            String weightStr = etWeight.getText().toString().trim();
            if (!weightStr.isEmpty()) {
                double weight = Double.parseDouble(weightStr);
                dbHelper.addWeight(currentUserId, weight, "");
                etWeight.setText("");
                loadWeightData();
            }
        });

        // Load dữ liệu ban đầu
        loadWeightData();
    }

    private void loadWeightData() {
        List<WeightRecord> records = dbHelper.getAllWeights(currentUserId);

        // Hiển thị cân nặng hiện tại & thay đổi so với trước
        if (!records.isEmpty()) {
            double currentWeight = records.get(0).getWeight();
            tvCurrentWeight.setText(currentWeight + " kg");

            if (records.size() > 1) {
                double diff = currentWeight - records.get(1).getWeight();
                if (diff > 0) {
                    tvWeightChange.setText("↑ " + Math.abs(diff) + " kg so với tháng trước");
                    tvWeightChange.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else if (diff < 0) {
                    tvWeightChange.setText("↓ " + Math.abs(diff) + " kg so với tháng trước");
                    tvWeightChange.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                } else {
                    tvWeightChange.setText("Không thay đổi so với tháng trước");
                }
            }

            // Tính BMI với chiều cao giả định 1.70m
            calculateBMI(currentWeight, 1.70f);
        }

        // Cập nhật lịch sử vào RecyclerView
        rvWeightHistory.setAdapter(new WeightHistoryAdapter(records));

        // Cập nhật biểu đồ
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            float weight = (float) records.get(i).getWeight();
            entries.add(new Entry(i, weight));
        }
        LineDataSet dataSet = new LineDataSet(entries, "Cân nặng (kg)");
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);
        chartWeight.setData(new LineData(dataSet));
        chartWeight.invalidate();
    }

    private void calculateBMI(double weight, float heightMeters) {
        double bmi = weight / (heightMeters * heightMeters);
        tvBMI.setText(String.format("%.1f", bmi));

        String category;
        if (bmi < 18.5) category = "Gầy";
        else if (bmi < 24.9) category = "Bình thường";
        else category = "Thừa cân";
        tvBMICategory.setText(category);

        // Cập nhật thanh tiến trình BMI (giả định max 40)
        progressBMI.setMax(40);
        progressBMI.setProgress((int) bmi);
    }

    public static class WeightHistoryAdapter extends RecyclerView.Adapter<WeightHistoryAdapter.WeightViewHolder> {
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
            holder.tvDate.setText(record.getRecordedAt().toString());
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
}
