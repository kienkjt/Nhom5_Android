package com.nhom5.healthtracking.sleep;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class HistorySleep extends AppCompatActivity {

    ListView listView;
    Button btnback ;
    ArrayList<String> sleepHistory;
    SharedPreferences prefs;
    private static final String PREFS_NAME = "SleepTrackerPrefs";
    private static final String KEY_HISTORY = "sleepHistory";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_sleep);

        btnback = findViewById(R.id.btnBack);
        listView = findViewById(R.id.lv_history);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);


        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        String savedData = prefs.getString(KEY_HISTORY, "");
        sleepHistory = new ArrayList<>();

        if (!savedData.isEmpty()) {
            String[] items = savedData.split(";;");
            for (String item : items) {
                sleepHistory.add(item);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, sleepHistory
        );
        listView.setAdapter(adapter);
    }
}
