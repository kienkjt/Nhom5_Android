package com.nhom5.healthtracking.water;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.nhom5.healthtracking.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class water_volume_chart extends AppCompatActivity {
    private BarChart bc_waterChart;
    // Lưu mảng lượng nước theo ngày (7 phần tử: thứ Hai đến Chủ nhật)
    private int[] weeklyWater = new int[7];
    Button btnback ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_volume_chart);

        bc_waterChart = findViewById(R.id.waterChart);
        btnback = findViewById(R.id.btn_back);

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Nhận mảng từ MainActivity
        int[] data = getIntent().getIntArrayExtra("weeklyWater");
        if (data != null && data.length == 7) {
            weeklyWater = data;
        }

        setupChart();
    }

    private void setupChart() {
        List<BarEntry> entries = new ArrayList<>();
        // Xác định ngày hiện tại để đẩy tuần đến khi Thứ Hai
        Calendar cal = Calendar.getInstance();
        int todayDow = cal.get(Calendar.DAY_OF_WEEK); // 1=Chủ nhật, 2=Thứ Hai...
        int startOffset = (todayDow == Calendar.MONDAY) ? 0 :
                (Calendar.SATURDAY < 8 ?
                        (Calendar.MONDAY - todayDow + 7) % 7 :
                        (Calendar.MONDAY + 7 - todayDow) % 7);
        // Nhưng đơn giản hơn: ta giả định gửi mảng thứ Hai–Chủ nhật từ Main
        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, weeklyWater[i]));
        }

        BarDataSet set = new BarDataSet(entries, "mL uống");
        set.setColor(0xFF00B8D4);
        set.setValueTextSize(12f);

        BarData barData = new BarData(set);
        barData.setBarWidth(0.9f);
        bc_waterChart.setData(barData);

        String[] labels = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
        XAxis xAxis = bc_waterChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                if (idx >= 0 && idx < labels.length) return labels[idx];
                else return "";
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        bc_waterChart.getDescription().setEnabled(false);
        bc_waterChart.setFitBars(true);
        bc_waterChart.invalidate();
    }
}
