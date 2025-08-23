package com.nhom5.healthtracking.Blood;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.data.local.entity.BloodPressureRecord;
import com.nhom5.healthtracking.step.StepActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BloodPressureActivity extends AppCompatActivity {
    private BloodPressureViewModel viewModel;

    private TextView tvCurrentBP, tvBPCategory, tvHistoryTitle;
    private TextInputEditText etSystolic, etDiastolic, etNote;
    private MaterialButton btnAddBP;
    private LineChart chartBP;
    private Spinner spinnerMode;

    private List<BloodPressureRecord> allRecords = new ArrayList<>();
    private String chartMode = "Cả hai"; // mặc định

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blood_pressure);

        // Toolbar
        setupToolbar();

        // Views
        tvCurrentBP = findViewById(R.id.tvCurrentBP);
        tvBPCategory = findViewById(R.id.tvBPCategory);
        tvHistoryTitle = findViewById(R.id.tvHistoryTitle);
        etSystolic = findViewById(R.id.etSystolic);
        etDiastolic = findViewById(R.id.etDiastolic);
        etNote = findViewById(R.id.etNote);
        btnAddBP = findViewById(R.id.btnAddBP);
        chartBP = findViewById(R.id.chartBP);
        spinnerMode = findViewById(R.id.spinnerMode);

        // Spinner chế độ hiển thị
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Cả hai", "Chỉ Sys", "Chỉ Dia"});
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMode.setAdapter(modeAdapter);
        spinnerMode.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                chartMode = parent.getItemAtPosition(position).toString();
                updateChart(allRecords);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        // ViewModel
        viewModel = new ViewModelProvider(this).get(BloodPressureViewModel.class);

        // Lấy userId từ Firebase Auth
        String userId = viewModel.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Quan sát dữ liệu -> cập nhật chỉ số mới nhất + biểu đồ
        viewModel.getAllRecords(userId).observe(this, records -> {
            if (records != null && !records.isEmpty()) {
                allRecords = records;

                BloodPressureRecord latest = records.get(0);
                tvCurrentBP.setText(latest.systolic + "/" + latest.diastolic + " mmHg");
                tvBPCategory.setText(getBPCategory(latest.systolic, latest.diastolic));

                updateChart(records);
            } else {
                allRecords = new ArrayList<>();
                tvCurrentBP.setText("—");
                tvBPCategory.setText("");
                updateChart(allRecords);
            }
        });

        // Nhấn "Lịch sử huyết áp" -> trang lịch sử đầy đủ
        tvHistoryTitle.setOnClickListener(v -> {
            redirectToHistoryActivity();
        });

        // Thêm bản ghi
        btnAddBP.setOnClickListener(v -> {
            String sysStr = etSystolic.getText() == null ? "" : etSystolic.getText().toString().trim();
            String diaStr = etDiastolic.getText() == null ? "" : etDiastolic.getText().toString().trim();
            String note = etNote.getText() == null ? "" : etNote.getText().toString().trim();

            if (sysStr.isEmpty() || diaStr.isEmpty()) {
                Toast.makeText(this, "Nhập đủ chỉ số", Toast.LENGTH_SHORT).show();
                return;
            }

            int systolic, diastolic;
            try {
                systolic = Integer.parseInt(sysStr);
                diastolic = Integer.parseInt(diaStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Chỉ số không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.insert(userId, systolic, diastolic, 70, new Date(), note);
            etSystolic.setText("");
            etDiastolic.setText("");
            etNote.setText("");
        });
    }

    private void updateChart(List<BloodPressureRecord> records) {
        if (records == null || records.isEmpty()) {
            chartBP.clear();
            chartBP.getDescription().setEnabled(false);
            chartBP.invalidate();
            return;
        }

        List<Entry> systolicEntries = new ArrayList<>();
        List<Entry> diastolicEntries = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            BloodPressureRecord r = records.get(i);
            systolicEntries.add(new Entry(i, r.systolic));
            diastolicEntries.add(new Entry(i, r.diastolic));
        }

        LineData lineData = new LineData();

        if (chartMode.equals("Cả hai") || chartMode.equals("Chỉ Sys")) {
            LineDataSet setSys = new LineDataSet(systolicEntries, "Tâm thu");
            setSys.setColor(Color.RED);
            setSys.setCircleColor(Color.RED);
            setSys.setLineWidth(2f);
            setSys.setCircleRadius(3.5f);
            setSys.setDrawValues(false);
            lineData.addDataSet(setSys);
        }

        if (chartMode.equals("Cả hai") || chartMode.equals("Chỉ Dia")) {
            LineDataSet setDia = new LineDataSet(diastolicEntries, "Tâm trương");
            setDia.setColor(Color.BLUE);
            setDia.setCircleColor(Color.BLUE);
            setDia.setLineWidth(2f);
            setDia.setCircleRadius(3.5f);
            setDia.setDrawValues(false);
            lineData.addDataSet(setDia);
        }

        chartBP.setData(lineData);

        // X-Axis
        XAxis xAxis = chartBP.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(Math.min(records.size(), 6), true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int index = (int) value;
                if (index >= 0 && index < records.size()) {
                    return String.valueOf(index + 1); // Lần đo
                }
                return "";
            }
        });

        // Y-Axis
        YAxis left = chartBP.getAxisLeft();
        left.setAxisMinimum(50f);
        left.setAxisMaximum(200f);
        left.removeAllLimitLines();
        left.addLimitLine(new LimitLine(120f, "Sys 120"));
        left.addLimitLine(new LimitLine(80f, "Dia 80"));
        chartBP.getAxisRight().setEnabled(false);

        chartBP.getDescription().setEnabled(false);
        chartBP.getLegend().setTextSize(12f);
        chartBP.animateX(600);
        chartBP.invalidate();
    }

    private String getBPCategory(int sys, int dia) {
        if (sys < 90 || dia < 60) return "Thấp huyết áp";
        if (sys < 120 && dia < 80) return "Bình thường";
        if (sys <= 139 || dia <= 89) return "Tiền cao huyết áp";
        return "Cao huyết áp";
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Theo dõi huyết áp");
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
    private void redirectToHistoryActivity() {
        Intent intent = new Intent(this, BloodPressureHistoryActivity.class);
        startActivity(intent);
    }
}
