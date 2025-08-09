package com.example.myhealth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myhealth.WaterDataManager;

import java.util.Calendar;

public class water_Monitoring extends AppCompatActivity {

    TextView tvnhatky, tvmuctieu, tvProgressCircle, edtGoalWater;
    EditText edtTodayWater;
    ProgressBar pbwater;

    int totalWater = 0;
    int goalWater = 2000;
    int[] weeklyWater = new int[7];

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pbwater = findViewById(R.id.pb_water);
        tvnhatky = findViewById(R.id.tv_nhatky);
        tvmuctieu = findViewById(R.id.tv_muctieu);
        edtTodayWater = findViewById(R.id.edttodaywater);
        edtGoalWater = findViewById(R.id.edtwater);
        tvProgressCircle = findViewById(R.id.tv_pbwater);

        pbwater.setMax(100);

        // Kiểm tra ngày mới và load dữ liệu
        checkNewDayAndLoadData();

        edtGoalWater.setText(String.valueOf(goalWater));
        edtTodayWater.setText(WaterDataManager.getLastInputWater(this));

        updateProgress();

        tvnhatky.setOnClickListener(v -> {
            Intent intent = new Intent(water_Monitoring.this, water_volume_chart.class);
            intent.putExtra("weeklyWater", weeklyWater);
            startActivity(intent);
        });
    }

    private void checkNewDayAndLoadData() {
        Calendar today = Calendar.getInstance();
        int dayOfYear = today.get(Calendar.DAY_OF_YEAR);
        int year = today.get(Calendar.YEAR);

        int savedDay = WaterDataManager.getLastDay(this);
        int savedYear = WaterDataManager.getLastYear(this);

        goalWater = WaterDataManager.getGoalWater(this);
        weeklyWater = WaterDataManager.getWeeklyWater(this);

        if (savedDay != dayOfYear || savedYear != year) {
            // Sang ngày mới -> reset totalWater
            totalWater = 0;

            // Lưu ngày hiện tại
            WaterDataManager.saveLastDay(this, dayOfYear);
            WaterDataManager.saveLastYear(this, year);
            WaterDataManager.saveTotalWater(this, totalWater);
        } else {
            totalWater = WaterDataManager.getTotalWater(this);
        }
    }

    private void updateGoalFromInput() {
        String goalInput = edtGoalWater.getText().toString();
        if (!goalInput.isEmpty()) {
            goalWater = Integer.parseInt(goalInput);
        } else {
            goalWater = 2000;
        }
        WaterDataManager.saveGoalWater(this, goalWater);
    }

    private void updateProgress() {
        int percent = (int) ((totalWater * 100.0f) / goalWater);
        pbwater.setProgress(percent);
        tvProgressCircle.setText(totalWater + " mL / " + goalWater + " mL");

        if (totalWater >= goalWater) {
            tvmuctieu.setText("Bạn đã hoàn thành!");
        } else {
            tvmuctieu.setText("Uống thêm nước nhé!");
        }
    }

    private void updateWeeklyData() {
        int currentDayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;
        if (currentDayIndex < 0) currentDayIndex = 6;
        weeklyWater[currentDayIndex] = totalWater;
        WaterDataManager.saveWeeklyWater(this, weeklyWater);
    }

    public void btnAddWater(View view) {
        updateGoalFromInput();
        String input = edtTodayWater.getText().toString();

        if (!input.isEmpty()) {
            int added = Integer.parseInt(input);
            totalWater += added;
            WaterDataManager.saveTotalWater(this, totalWater);
            WaterDataManager.saveLastInputWater(this, input);
            updateWeeklyData();
            updateProgress();
        }
    }

    public void btnMinusWater(View view) {
        updateGoalFromInput();
        String input = edtTodayWater.getText().toString();

        if (!input.isEmpty()) {
            int removed = Integer.parseInt(input);
            totalWater = Math.max(0, totalWater - removed);
            WaterDataManager.saveTotalWater(this, totalWater);
            WaterDataManager.saveLastInputWater(this, input);
            updateWeeklyData();
            updateProgress();
        }
    }
}
