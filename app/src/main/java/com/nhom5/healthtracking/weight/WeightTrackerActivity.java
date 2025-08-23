package com.nhom5.healthtracking.weight;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.nhom5.healthtracking.HealthTrackingApp;
import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.auth.AuthActivity;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.data.local.entity.WeightRecord;
import com.nhom5.healthtracking.data.repository.UserRepository;
import com.nhom5.healthtracking.data.repository.WeightRecordRepository;

import java.util.ArrayList;
import java.util.List;

public class WeightTrackerActivity extends AppCompatActivity {
    private UserRepository userRepo;
    private WeightRecordRepository weightRepo;

    private EditText etWeight;
    private TextView tvCurrentWeight, tvWeightChange, tvBMI, tvBMICategory;
    private ProgressBar progressBMI;
    private RecyclerView rvWeightHistory;
    private LineChart chartWeight;
    private Button btnAddWeight;

    private HealthTrackingApp app;

    private String currentUserId; // UID thực từ Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weight_tracker);
        setupToolbar();
        app = (HealthTrackingApp) getApplication();

        // --- Ánh xạ view ---
        etWeight = findViewById(R.id.etWeight);
        tvCurrentWeight = findViewById(R.id.tvCurrentWeight);
        tvWeightChange = findViewById(R.id.tvWeightChange);
        tvBMI = findViewById(R.id.tvBMI);
        tvBMICategory = findViewById(R.id.tvBMICategory);
        progressBMI = findViewById(R.id.progressBMI);
        rvWeightHistory = findViewById(R.id.rvWeightHistory);
        chartWeight = findViewById(R.id.chartWeight);
        btnAddWeight = findViewById(R.id.btnAddWeight);

        rvWeightHistory.setLayoutManager(new LinearLayoutManager(this));

        // --- Lấy thông tin user từ Room (đồng bộ với Firebase) ---
        userRepo = app.getUserRepository();
        weightRepo = app.getWeightRecordRepository();

        userRepo.observeCurrentUser().observe(this, user -> {
            if (user != null) {
                currentUserId = user.getUid();
                loadWeightData();
            } else {
                startActivity(new Intent(this, AuthActivity.class));
                finish();
            }
        });

        // --- Xử lý click nút Lưu cân nặng ---
        btnAddWeight.setOnClickListener(v -> {
            if (currentUserId == null) return;

            String weightStr = etWeight.getText().toString().trim();
            if (!weightStr.isEmpty()) {
                double weight = Double.parseDouble(weightStr);
                weightRepo.insertAsync(currentUserId, weight, "", () -> {
                    runOnUiThread(() -> {
                        etWeight.setText("");
                        loadWeightData();
                    });
                });
            }
        });
    }

    private void loadWeightData() {
        if (currentUserId == null) return;

        weightRepo.getAllByUserIdAsync(currentUserId, records -> {
            runOnUiThread(() -> {
                // Hiển thị cân nặng hiện tại & thay đổi
                if (!records.isEmpty()) {
                    double currentWeight = records.get(0).getWeight();
                    tvCurrentWeight.setText(currentWeight + " kg");

                    if (records.size() > 1) {
                        double diff = currentWeight - records.get(1).getWeight();
                        if (diff > 0) {
                            tvWeightChange.setText("↑ " + Math.abs(diff) + " kg so với lần trước");
                            tvWeightChange.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        } else if (diff < 0) {
                            tvWeightChange.setText("↓ " + Math.abs(diff) + " kg so với lần trước");
                            tvWeightChange.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        } else {
                            tvWeightChange.setText("Không thay đổi so với lần trước");
                        }
                    }

                    // Lấy chiều cao từ User
                    User user = userRepo.observeCurrentUser().getValue();
                    float heightMeters = user != null && user.getHeight() != null ? user.getHeight() / 100.0f : 1.70f; // Chuyển cm sang m
                    calculateBMI(currentWeight, heightMeters);
                }

                // Lịch sử
                rvWeightHistory.setAdapter(new WeightHistoryAdapter(records));

                // Biểu đồ
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
            });
        });
    }

    private void calculateBMI(double weight, float heightMeters) {
        double bmi = weight / (heightMeters * heightMeters);
        tvBMI.setText(String.format("%.1f", bmi));

        String category;
        if (bmi < 18.5) category = "Gầy";
        else if (bmi < 24.9) category = "Bình thường";
        else category = "Thừa cân";
        tvBMICategory.setText(category);

        progressBMI.setMax(40);
        progressBMI.setProgress((int) bmi);
    }

    // --- Adapter hiển thị lịch sử ---
    public static class WeightHistoryAdapter extends RecyclerView.Adapter<WeightHistoryAdapter.WeightViewHolder> {
        private final List<WeightRecord> weightList;

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


    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Theo dõi cân nặng");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
