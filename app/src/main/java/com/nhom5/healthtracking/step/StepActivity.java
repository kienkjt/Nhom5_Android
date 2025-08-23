package com.nhom5.healthtracking.step;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.model.GradientColor;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.SensorsClient;
import com.google.android.gms.fitness.RecordingClient;
import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.data.local.entity.StepRecord;
import com.nhom5.healthtracking.util.StepDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class StepActivity extends AppCompatActivity {

    private static final String TAG = "StepActivity";
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 100;

    private TextView tvStepCount, tvStepGoal, tvDistance, tvCalories;
    private TextView tvBestDay, tvConsecutiveDays, tvWeeklyAverage;
    private ProgressBar progressSteps;
    private BarChart chartSteps;
    private RecyclerView rvStepHistory;

    private StepDatabaseHelper dbHelper;
    private FitnessOptions fitnessOptions;
    private StepHistoryAdapter historyAdapter;

    private static final int DAILY_GOAL = 1000;
    private static final int USER_ID = 1;

    private int todaySteps = 0;
    private boolean goalReachedShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.step_counter);
        setupToolbar();

        dbHelper = new StepDatabaseHelper(this);
        initViews();

        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();

        // Xin quyền activity recognition nếu Android Q+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
        } else {
            signInToGoogleFit();
        }
    }

    /** ---------------- Google Fit ---------------- */

    private void signInToGoogleFit() {
        GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(this);
        if (!GoogleSignIn.hasPermissions(acc, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    acc,
                    fitnessOptions
            );
        } else {
            accessGoogleFit();
        }
    }

    private void accessGoogleFit() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        // Đảm bảo đã subscribe
        RecordingClient recordingClient = Fitness.getRecordingClient(this, account);
        recordingClient.subscribe(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(unused -> Log.i(TAG, "Subscribed to step count!"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to subscribe.", e));

        // Lấy tổng số bước hôm nay từ Google Fit
        Fitness.getHistoryClient(this, account)
                .readDailyTotal(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .addOnSuccessListener(dataSet -> {
                    if (dataSet.isEmpty() || dataSet.getDataPoints().isEmpty()) {
                        todaySteps = dbHelper.getTodaySteps(USER_ID);
                        Log.w(TAG, "Google Fit trả về 0 → lấy từ DB: " + todaySteps);
                    } else {
                        todaySteps = dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                        saveStepData(todaySteps);
                    }

                    updateUI(todaySteps);
                    showChart();
                    loadStepHistory();
                    updateAchievements();

                    registerRealtimeListener();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to read daily steps.", e);
                    todaySteps = dbHelper.getTodaySteps(USER_ID);
                    updateUI(todaySteps);
                    showChart();
                    loadStepHistory();
                    updateAchievements();
                });
    }


    private void registerRealtimeListener() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        SensorsClient sensorsClient = Fitness.getSensorsClient(this, account);

        sensorsClient.add(
                        new SensorRequest.Builder()
                                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                .setSamplingRate(5, TimeUnit.SECONDS)
                                .build(),
                        (DataPoint dp) -> {
                            for (Field f : dp.getDataType().getFields()) {
                                int steps = dp.getValue(f).asInt();
                                todaySteps += steps;
                                runOnUiThread(() -> {
                                    saveStepData(todaySteps);
                                    updateUI(todaySteps);
                                    showChart();
                                    loadStepHistory();
                                    updateAchievements();
                                    Log.i(TAG, "Realtime +" + steps + " → total: " + todaySteps);
                                });
                            }
                        }
                ).addOnSuccessListener(unused -> Log.i(TAG, "Registered realtime step sensor"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to track realtime steps", e));
    }

    /** ---------------- UI + DB ---------------- */

    private void initViews() {
        tvStepCount = findViewById(R.id.tvStepCount);
        tvStepGoal = findViewById(R.id.tvStepGoal);
        tvDistance = findViewById(R.id.tvDistance);
        tvCalories = findViewById(R.id.tvCalories);
        progressSteps = findViewById(R.id.progressSteps);
        chartSteps = findViewById(R.id.chartSteps);
        rvStepHistory = findViewById(R.id.rvStepHistory);
        tvBestDay = findViewById(R.id.tvBestDay);
        tvConsecutiveDays = findViewById(R.id.tvConsecutiveDays);
        tvWeeklyAverage = findViewById(R.id.tvWeeklyAverage);

        rvStepHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new StepHistoryAdapter(new ArrayList<>());
        rvStepHistory.setAdapter(historyAdapter);
    }

    private void saveStepData(int steps) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        double distance = steps * 0.00055; // 1 bước ≈ 0.55m
        double calories = steps * 0.05;   // 1 bước ≈ 0.05 kcal

        StepRecord record = new StepRecord(String.valueOf(USER_ID), today, steps, distance, calories);
        dbHelper.insertOrUpdateStep(record);
    }


    @SuppressLint("SetTextI18n")
    private void updateUI(int steps) {
        tvStepCount.setText(String.valueOf(steps));
        tvStepGoal.setText("/" + DAILY_GOAL + " bước");
        progressSteps.setMax(DAILY_GOAL);
        progressSteps.setProgress(steps);

        double distanceKm = steps * 0.55 / 1000;
        tvDistance.setText(String.format(Locale.getDefault(), "%.2f km", distanceKm));

        double calories = steps * 0.05;
        tvCalories.setText(String.format(Locale.getDefault(), "%.0f kcal", calories));

        if (steps >= DAILY_GOAL && !goalReachedShown) {
            Toast.makeText(this, "🎉 Chúc mừng! Bạn đã đạt mục tiêu hôm nay!", Toast.LENGTH_LONG).show();
            goalReachedShown = true;
        }
    }

    private void showChart() {
        List<StepRecord> last7Days = dbHelper.getLast7Days(USER_ID);

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        last7Days.sort((r1, r2) -> {
            try {
                Date d1 = inputFormat.parse(r1.getDate());
                Date d2 = inputFormat.parse(r2.getDate());
                return d1.compareTo(d2);
            } catch (Exception e) {
                return 0;
            }
        });

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

        for (int i = 0; i < last7Days.size(); i++) {
            StepRecord record = last7Days.get(i);
            entries.add(new BarEntry(i, record.getStepCount()));

            String formattedDate = record.getDate();
            try {
                Date d = inputFormat.parse(record.getDate());
                formattedDate = outputFormat.format(d);
            } catch (Exception ignored) {}
            labels.add(formattedDate);
        }

        // Dataset
        BarDataSet dataSet = new BarDataSet(entries, "Bước chân");

        // Gradient cho từng cột dựa theo stepCount
        List<GradientColor> gradientColors = new ArrayList<>();
        for (BarEntry entry : entries) {
            if (entry.getY() < DAILY_GOAL / 2f) {
                // <50% mục tiêu: xanh lá nhạt -> xanh lá đậm
                gradientColors.add(new GradientColor(
                        Color.rgb(200, 255, 200),
                        Color.rgb(0, 200, 83)
                ));
            } else if (entry.getY() < DAILY_GOAL) {
                // <100% mục tiêu: xanh dương nhạt -> xanh dương đậm
                gradientColors.add(new GradientColor(
                        Color.rgb(187, 222, 251),
                        Color.rgb(33, 150, 243)
                ));
            } else {
                // >= mục tiêu: vàng nhạt -> cam đậm
                gradientColors.add(new GradientColor(
                        Color.rgb(255, 236, 179),
                        Color.rgb(255, 152, 0)
                ));
            }
        }
        dataSet.setGradientColors(gradientColors);

        // Hiển thị số bước trên đầu cột
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setDrawValues(true);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.25f);

        // Chart config
        chartSteps.setData(data);
        chartSteps.setFitBars(true);
        chartSteps.getDescription().setEnabled(false);
        chartSteps.setExtraOffsets(10, 10, 10, 10);

        // X Axis
        XAxis xAxis = chartSteps.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-25);

        // Y Axis
        YAxis leftAxis = chartSteps.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);

        // Thêm dòng mục tiêu rõ ràng hơn
        LimitLine goalLine = new LimitLine(DAILY_GOAL, "Mục tiêu: " + DAILY_GOAL + " bước");
        goalLine.setLineColor(Color.RED);
        goalLine.setLineWidth(3f);
        goalLine.setTextSize(12f);
        goalLine.setTextColor(Color.RED);
        goalLine.enableDashedLine(15f, 10f, 0f);
        leftAxis.addLimitLine(goalLine);

        chartSteps.getAxisRight().setEnabled(false);

        // Animation
        chartSteps.animateY(1500, Easing.EaseInOutQuad);

        chartSteps.invalidate();
    }



    private void loadStepHistory() {
        List<StepRecord> history = dbHelper.getLast7Days(USER_ID);
        Collections.reverse(history);
        historyAdapter.updateData(history);
    }

    @SuppressLint("SetTextI18n")
    private void updateAchievements() {
        List<StepRecord> last7Days = dbHelper.getLast7Days(USER_ID);

        if (last7Days.isEmpty()) {
            tvBestDay.setText("Kỷ lục: 0 bước");
            tvConsecutiveDays.setText("Chuỗi ngày đạt mục tiêu: 0 ngày");
            tvWeeklyAverage.setText("Trung bình tuần này: 0 bước/ngày");
            return;
        }

        int bestSteps = 0;
        for (StepRecord r : last7Days) {
            bestSteps = Math.max(bestSteps, r.getStepCount());
        }
        tvBestDay.setText("Kỷ lục: " + bestSteps + " bước");

        int streak = 0, currentStreak = 0;
        for (StepRecord r : last7Days) {
            if (r.getStepCount() >= DAILY_GOAL) {
                currentStreak++;
                streak = Math.max(streak, currentStreak);
            } else {
                currentStreak = 0;
            }
        }
        tvConsecutiveDays.setText("Chuỗi ngày đạt mục tiêu: " + streak + " ngày");

        int total = 0;
        for (StepRecord r : last7Days) total += r.getStepCount();
        int avg = total / last7Days.size();
        tvWeeklyAverage.setText("Trung bình tuần này: " + avg + " bước/ngày");
    }

    /** ---------------- Permission Callback ---------------- */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                accessGoogleFit();
            } else {
                Toast.makeText(this, "Bạn đã từ chối quyền Google Fit", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                signInToGoogleFit();
            } else {
                Toast.makeText(this, "Cần quyền ACTIVITY_RECOGNITION để đếm bước", Toast.LENGTH_SHORT).show();
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
