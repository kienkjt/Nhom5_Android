package com.nhom5.healthtracking.sleep;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
//import android.widget.Button;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.graphics.PorterDuff;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
//import com.nhom5.healthtracking.sleep.HistoryActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import com.nhom5.healthtracking.R;

public class SleepTracker extends AppCompatActivity {

    TextView tvDuration, btnHistory;
    EditText etNotes;
    ImageButton btnSleepTime, btnWakeTime;
    BarChart barChart;
    ImageButton[] stars = new ImageButton[5];

    private Calendar sleepTime, wakeTime;
    private float[] weekData = new float[7];
    private int currentRating = 0;

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "SleepTrackerPrefs";
    private static final String KEY_WEEK_DATA = "weekData";
    private static final String KEY_WEEK_START = "weekStart";
    private static final String KEY_NOTES = "notes";
    private static final String KEY_NOTES_DATE = "notesDate";
    private static final String KEY_HISTORY = "sleepHistory";
    private static final String KEY_SLEEP_TIME = "sleepTimeMillis"; // thêm để lưu giờ Start

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_tracker);
        setupToolbar();

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        tvDuration = findViewById(R.id.tvDuration);
        etNotes = findViewById(R.id.etNotes);
        btnSleepTime = findViewById(R.id.btnSleepTime);
        btnWakeTime = findViewById(R.id.btnWakeTime);
        barChart = findViewById(R.id.barChart);
        btnHistory = findViewById(R.id.btn_History);

        stars[0] = findViewById(R.id.star1);
        stars[1] = findViewById(R.id.star2);
        stars[2] = findViewById(R.id.star3);
        stars[3] = findViewById(R.id.star4);
        stars[4] = findViewById(R.id.star5);

        // Sự kiện bấm sao
        for (int i = 0; i < stars.length; i++) {
            final int index = i;
            stars[i].setOnClickListener(v -> {
                currentRating = index + 1;
                for (int j = 0; j < stars.length; j++) {
                    if (j <= index) {
                        stars[j].setImageResource(R.drawable.ic_star_24);
                    } else {
                        stars[j].setImageResource(R.drawable.ic_star_border_24);
                    }
                }
            });
        }

        // Kiểm tra nếu có sleepTime đã lưu
        long savedSleepTime = prefs.getLong(KEY_SLEEP_TIME, -1);
        if (savedSleepTime != -1) {
            sleepTime = Calendar.getInstance();
            sleepTime.setTimeInMillis(savedSleepTime);
            tvDuration.setText("Started at: " + formatTime(sleepTime));

            // Sleep disable, mờ, xám
            btnSleepTime.setEnabled(false);
            btnSleepTime.setAlpha(0.5f);
            btnSleepTime.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);

            // Wake enable, sáng, vàng
            btnWakeTime.setEnabled(true);
            btnWakeTime.setAlpha(1f);
            btnWakeTime.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);

        } else {
            // Sleep enable, sáng, vàng
            btnSleepTime.setEnabled(true);
            btnSleepTime.setAlpha(1f);
            btnSleepTime.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);

            // Wake disable, mờ, xám
            btnWakeTime.setEnabled(false);
            btnWakeTime.setAlpha(0.5f);
            btnWakeTime.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        }


        btnWakeTime.setOnClickListener(v -> {
            if (sleepTime == null) {
                tvDuration.setText("Please press Start first!");
                return;
            }
            wakeTime = Calendar.getInstance();
            updateDuration();
            updateBarChart();
            saveHistoryRecord();

            prefs.edit().remove(KEY_SLEEP_TIME).apply();

            // Wake disable, mờ, xám
            btnWakeTime.setEnabled(false);
            btnWakeTime.setAlpha(0.5f);
            btnWakeTime.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);

            // Sleep enable, sáng, vàng
            btnSleepTime.setEnabled(true);
            btnSleepTime.setAlpha(1f);
            btnSleepTime.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);
        });

        btnSleepTime.setOnClickListener(v -> {
            sleepTime = Calendar.getInstance();
            prefs.edit().putLong(KEY_SLEEP_TIME, sleepTime.getTimeInMillis()).apply();
            tvDuration.setText("Started at: " + formatTime(sleepTime));

            // Sleep disable, mờ, xám
            btnSleepTime.setEnabled(false);
            btnSleepTime.setAlpha(0.5f);
            btnSleepTime.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);

            // Wake enable, sáng, vàng
            btnWakeTime.setEnabled(true);
            btnWakeTime.setAlpha(1f);
            btnWakeTime.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);
        });

        btnHistory.setOnClickListener(v -> {
            startActivity(new Intent(SleepTracker.this, HistorySleep.class));
        });

        setupBarChart();
        loadWeekData();
        updateBarChart();
        loadNotes();

        etNotes.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                saveNotes(s.toString());
            }
        });
    }


    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Theo dõi giấc ngủ");
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

    private String formatTime(Calendar cal) {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.getTime());
    }

    private String formatDate(Calendar cal) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
    }

    private void updateDuration() {
        long diffMillis = wakeTime.getTimeInMillis() - sleepTime.getTimeInMillis();
        if (diffMillis < 0) diffMillis += 24 * 60 * 60 * 1000;

        int totalMinutes = (int) (diffMillis / (1000 * 60));
        int hours = totalMinutes / 60;
        int mins = totalMinutes % 60;

        tvDuration.setText(formatTime(sleepTime) + " - " + formatTime(wakeTime)
                + " (" + hours + "h " + mins + "m)");

        int dayOfWeek = sleepTime.get(Calendar.DAY_OF_WEEK);
        int index = (dayOfWeek + 5) % 7;
        weekData[index] = hours + mins / 60f;
        saveWeekData();
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                int idx = Math.round(value);
                if (idx >= 0 && idx < days.length) return days[idx];
                return "";
            }
        });
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setAxisMinimum(0f);
    }

    private void updateBarChart() {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, weekData[i]));
        }
        BarDataSet dataSet = new BarDataSet(entries, "Hours Slept");
        dataSet.setColor(Color.parseColor("#2B4C7E"));
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);
        barChart.setData(barData);
        barChart.invalidate();
    }

    private void saveWeekData() {
        SharedPreferences.Editor editor = prefs.edit();
        StringBuilder sb = new StringBuilder();
        for (float v : weekData) sb.append(v).append(",");
        editor.putString(KEY_WEEK_DATA, sb.toString());
        editor.putLong(KEY_WEEK_START, getCurrentWeekStartMillis());
        editor.apply();
    }

    private void loadWeekData() {
        long savedWeekStart = prefs.getLong(KEY_WEEK_START, -1);
        long currentWeekStart = getCurrentWeekStartMillis();
        if (savedWeekStart != currentWeekStart) {
            weekData = new float[7];
            return;
        }
        String saved = prefs.getString(KEY_WEEK_DATA, "");
        if (!saved.isEmpty()) {
            String[] parts = saved.split(",");
            for (int i = 0; i < parts.length && i < 7; i++) {
                try {
                    weekData[i] = Float.parseFloat(parts[i]);
                } catch (NumberFormatException e) {
                    weekData[i] = 0;
                }
            }
        }
    }

    private long getCurrentWeekStartMillis() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private void saveNotes(String note) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_NOTES, note);
        editor.putLong(KEY_NOTES_DATE, System.currentTimeMillis());
        editor.apply();
    }

    private void loadNotes() {
        long savedTime = prefs.getLong(KEY_NOTES_DATE, -1);
        String savedNote = prefs.getString(KEY_NOTES, "");
        if (savedTime != -1) {
            Calendar savedCal = Calendar.getInstance();
            savedCal.setTimeInMillis(savedTime);
            Calendar resetCal = (Calendar) savedCal.clone();
            resetCal.add(Calendar.DAY_OF_MONTH, 1);
            resetCal.set(Calendar.HOUR_OF_DAY, 18);
            resetCal.set(Calendar.MINUTE, 0);
            resetCal.set(Calendar.SECOND, 0);
            if (System.currentTimeMillis() < resetCal.getTimeInMillis()) {
                etNotes.setText(savedNote);
            } else {
                etNotes.setText("");
                saveNotes("");
            }
        }
    }

    private void saveHistoryRecord() {
        long diffMillis = wakeTime.getTimeInMillis() - sleepTime.getTimeInMillis();
        if (diffMillis < 0) diffMillis += 24 * 60 * 60 * 1000;
        int totalMinutes = (int) (diffMillis / (1000 * 60));
        int hours = totalMinutes / 60;
        int mins = totalMinutes % 60;

        String record = formatDate(sleepTime) +
                " | Start: " + formatTime(sleepTime) +
                " | End: " + formatTime(wakeTime) +
                " | " + hours + "h " + mins + "m" +
                " | Note: " + etNotes.getText().toString() +
                " | Rating: " + currentRating;

        String oldData = prefs.getString(KEY_HISTORY, "");
        if (!oldData.isEmpty()) oldData += ";;";
        oldData += record;

        prefs.edit().putString(KEY_HISTORY, oldData).apply();
    }
}
