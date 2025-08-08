package com.example.myhealth.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myhealth.R;
import com.example.myhealth.adapters.WeightHistoryAdapter;
import com.example.myhealth.database.DatabaseHelper;
import com.example.myhealth.models.WeightRecord;
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
                float weight = Float.parseFloat(weightStr);
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
            float currentWeight = records.get(0).getWeight();
            tvCurrentWeight.setText(currentWeight + " kg");

            if (records.size() > 1) {
                float diff = currentWeight - records.get(1).getWeight();
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
            entries.add(new Entry(i, records.get(i).getWeight()));
        }
        LineDataSet dataSet = new LineDataSet(entries, "Cân nặng (kg)");
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);
        chartWeight.setData(new LineData(dataSet));
        chartWeight.invalidate();
    }

    private void calculateBMI(float weight, float heightMeters) {
        float bmi = weight / (heightMeters * heightMeters);
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
}
