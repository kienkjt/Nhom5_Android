package com.nhom5.healthtracking.Blood;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.data.local.entity.BloodPressureRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BloodPressureActivity extends ComponentActivity {

    private BloodPressureViewModel viewModel;
    private BloodPressureAdapter adapter;

    private TextView tvCurrentBP, tvBPCategory;
    private TextInputEditText etSystolic, etDiastolic;
    private Button btnAddBP;
    private LineChart chartBP;
    private RecyclerView rvBPHistory;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blood_pressure);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvCurrentBP = findViewById(R.id.tvCurrentBP);
        tvBPCategory = findViewById(R.id.tvBPCategory);
        etSystolic = findViewById(R.id.etSystolic);
        etDiastolic = findViewById(R.id.etDiastolic);
        btnAddBP = findViewById(R.id.btnAddBP);
        chartBP = findViewById(R.id.chartBP);
        rvBPHistory = findViewById(R.id.rvBPHistory);

        // RecyclerView
        adapter = new BloodPressureAdapter(new ArrayList<>());
        rvBPHistory.setLayoutManager(new LinearLayoutManager(this));
        rvBPHistory.setAdapter(adapter);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(BloodPressureViewModel.class);

        // Lấy userId từ Firebase Auth
        String userId = viewModel.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Quan sát dữ liệu
        viewModel.getAllRecords(userId).observe(this, records -> {
            if (records != null && !records.isEmpty()) {
                BloodPressureRecord latest = records.get(0);
                tvCurrentBP.setText(latest.systolic + "/" + latest.diastolic + " mmHg");
                tvBPCategory.setText(getBPCategory(latest.systolic, latest.diastolic));
                adapter.updateData(records);
                updateChart(records);
            }
        });

        // Thêm huyết áp
        btnAddBP.setOnClickListener(v -> {
            String sysStr = etSystolic.getText().toString();
            String diaStr = etDiastolic.getText().toString();

            if (sysStr.isEmpty() || diaStr.isEmpty()) {
                Toast.makeText(this, "Nhập đủ chỉ số", Toast.LENGTH_SHORT).show();
                return;
            }

            int systolic = Integer.parseInt(sysStr);
            int diastolic = Integer.parseInt(diaStr);

            viewModel.insert(userId, systolic, diastolic, 70, new Date(), "");
            etSystolic.setText("");
            etDiastolic.setText("");
        });
    }

    private void updateChart(List<BloodPressureRecord> records) {
        List<Entry> systolicEntries = new ArrayList<>();
        List<Entry> diastolicEntries = new ArrayList<>();

        for (int i = 0; i < records.size(); i++) {
            BloodPressureRecord r = records.get(i);
            systolicEntries.add(new Entry(i, r.systolic));
            diastolicEntries.add(new Entry(i, r.diastolic));
        }

        LineDataSet setSys = new LineDataSet(systolicEntries, "Tâm thu");
        LineDataSet setDia = new LineDataSet(diastolicEntries, "Tâm trương");

        LineData lineData = new LineData(setSys, setDia);
        chartBP.setData(lineData);
        chartBP.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chartBP.invalidate();
    }

    private String getBPCategory(int sys, int dia) {
        if (sys < 90 || dia < 60) return "Thấp huyết áp";
        if (sys < 120 && dia < 80) return "Bình thường";
        if (sys <= 139 || dia <= 89) return "Tiền cao huyết áp";
        return "Cao huyết áp";
    }
}





