package com.nhom5.healthtracking.Blood;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.nhom5.healthtracking.R;

import java.util.ArrayList;

public class BloodPressureHistoryActivity extends AppCompatActivity {

    private BloodPressureViewModel viewModel;
    private BloodPressureAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bp_history);
        setupToolbar();

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


    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Lịch sử huyết áp");
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
