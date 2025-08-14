package com.nhom5.healthtracking.step;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.data.local.entity.StepRecord;
import com.nhom5.healthtracking.util.GoogleFitHelper;
import com.nhom5.healthtracking.util.StepDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StepActivity extends AppCompatActivity {

    private TextView tvStepCount, tvStepGoal, tvDistance, tvCalories;
    private ProgressBar progressSteps;
    private BarChart chartSteps;
    private RecyclerView rvStepHistory;

    private StepDatabaseHelper dbHelper;
    private GoogleFitHelper googleFitHelper;

    private static final int DAILY_GOAL = 10000;
    private static final int USER_ID = 1;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    requestGoogleFit();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.step_counter);

        dbHelper = new StepDatabaseHelper(this);
        googleFitHelper = new GoogleFitHelper(this);
        initViews();

        // Kiểm tra quyền nhận bước chân
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION);
                return;
            }
        }

        requestGoogleFit();
    }

    private void requestGoogleFit() {
        if (!googleFitHelper.hasPermission()) {
            googleFitHelper.requestPermission();
        } else {
            readStepsFromGoogleFit();
        }
    }

    private void readStepsFromGoogleFit() {
        googleFitHelper.readStepsToday(new GoogleFitHelper.OnStepsReadListener() {
            @Override
            public void onStepsRead(int steps) {
                saveStepData(steps);
                updateUI(steps);
                showChart();
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void initViews() {
        tvStepCount = findViewById(R.id.tvStepCount);
        tvStepGoal = findViewById(R.id.tvStepGoal);
        tvDistance = findViewById(R.id.tvDistance);
        tvCalories = findViewById(R.id.tvCalories);
        progressSteps = findViewById(R.id.progressSteps);
        chartSteps = findViewById(R.id.chartSteps);
        rvStepHistory = findViewById(R.id.rvStepHistory);
    }

    private void saveStepData(int steps) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        StepRecord record = new StepRecord(USER_ID, today, steps, 0);
        dbHelper.insertOrUpdateStep(record);
    }

    private void updateUI(int steps) {
        tvStepCount.setText(String.valueOf(steps));
        tvStepGoal.setText("/" + DAILY_GOAL + " bước");
        progressSteps.setMax(DAILY_GOAL);
        progressSteps.setProgress(steps);

        double distanceKm = steps * 0.8 / 1000;
        tvDistance.setText(String.format(Locale.getDefault(), "%.2f km", distanceKm));

        double calories = steps * 0.04;
        tvCalories.setText(String.format(Locale.getDefault(), "%.0f kcal", calories));
    }

    private void showChart() {
        List<StepRecord> last7Days = dbHelper.getLast7Days(USER_ID);
        List<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < last7Days.size(); i++) {
            entries.add(new BarEntry(i, last7Days.get(i).getStepCount()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Bước chân");
        BarData data = new BarData(dataSet);
        chartSteps.setData(data);
        chartSteps.invalidate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GoogleFitHelper.REQUEST_OAUTH_REQUEST_CODE) {
            if (googleFitHelper.hasPermission()) {
                readStepsFromGoogleFit();
            }
        }
    }
}
