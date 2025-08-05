package com.nhom5.healthtracking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    
    private FloatingActionButton fabAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        fabAuth = findViewById(R.id.fab_auth);
    }

    private void setupClickListeners() {
        if (fabAuth != null) {
            fabAuth.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                startActivity(intent);
            });
        }
    }
}