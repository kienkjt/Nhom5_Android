package com.example.water;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class water_monitoring extends AppCompatActivity {

    TextView tvnhatky, edtGoalWater, tvmuctieu;
    EditText edtTodayWater;
    TextView tvProgressCircle;
    ProgressBar pbwater;

    int totalWater = 0;
    int goalWater = 2000;
    int[] weeklyWater = new int[7]; // Lưu lượng nước mỗi ngày trong tuần

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
        edtGoalWater = findViewById(R.id.edtwater);
        edtTodayWater = findViewById(R.id.edttodaywater);
        tvProgressCircle = findViewById(R.id.tv_pbwater);
        tvmuctieu = findViewById(R.id.tv_muctieu);

        pbwater.setMax(100);

        tvnhatky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, bieudoluongnuoc.class);
                intent.putExtra("weeklyWater", weeklyWater);
                startActivity(intent);
            }
        });

        updateProgress();
    }

    private void updateGoalFromInput() {
        String goalInput = edtGoalWater.getText().toString();
        if (!goalInput.isEmpty()) {
            goalWater = Integer.parseInt(goalInput);
        } else {
            goalWater = 2000; // fallback nếu không nhập gì
        }
    }

    private void updateProgress() {
        int percent = (int) ((totalWater * 100.0f) / goalWater);
        pbwater.setProgress(percent);
        tvProgressCircle.setText(totalWater + " mL / " + goalWater + " mL");
    }

    private void updateWeeklyData() {
        int currentDayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;
        if (currentDayIndex < 0) currentDayIndex = 6; // Chủ nhật => 6
        weeklyWater[currentDayIndex] = totalWater;
    }

    public void btnMinusWater(View view) {
        updateGoalFromInput();

        String input = edtTodayWater.getText().toString();
        if (!input.isEmpty()) {
            int removed = Integer.parseInt(input);
            totalWater = Math.max(0, totalWater - removed);
            updateWeeklyData();
            updateProgress();
            if(totalWater >= 2000)
            {
                tvmuctieu.setText("Bạn đã hoàn thành!");
            }
            else{
                tvmuctieu.setText("Uống thêm nước nhé!");
            }
        }
    }

    public void btnAddWater(View view) {
        updateGoalFromInput();

        String input = edtTodayWater.getText().toString();
        if (!input.isEmpty()) {
            int added = Integer.parseInt(input);
            totalWater += added;
            updateWeeklyData();
            updateProgress();
            if(totalWater >= 2000)
            {
                tvmuctieu.setText("Bạn đã hoàn thành!");
            }
            else{
                tvmuctieu.setText("Uống thêm nước nhé!");
            }
        }
    }
}
