package com.nhom5.healthtracking.Blood;

import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.nhom5.healthtracking.R;

import java.util.ArrayList;

public class BloodPressureHistoryActivity extends ComponentActivity {

    private BloodPressureViewModel viewModel;
    private BloodPressureAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bp_history);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rvAllHistory = findViewById(R.id.rvAllBPHistory);
        adapter = new BloodPressureAdapter(this, new ArrayList<>());
        rvAllHistory.setLayoutManager(new LinearLayoutManager(this));
        rvAllHistory.setAdapter(adapter);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(BloodPressureViewModel.class);
        String userId = viewModel.getCurrentUserId();
        if (userId == null) {
            finish();
            return;
        }

        // Quan sát toàn bộ lịch sử
        viewModel.getAllRecords(userId).observe(this, records -> {
            if (records != null) {
                adapter.updateData(records);
            }
        });
    }
}
