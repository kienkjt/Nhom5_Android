package com.nhom5.healthtracking.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.nhom5.healthtracking.R;

public class AuthActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager viewPager;
    AuthAdapter authAdapter;
    FloatingActionButton fabGoogle;
    float v = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);

        initViews();
        setupViewPager();
        setupAnimations();
        setupClickListeners();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        fabGoogle = findViewById(R.id.fab_google);
    }

    private void setupViewPager() {
        authAdapter = new AuthAdapter(getSupportFragmentManager());
        viewPager.setAdapter(authAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupAnimations() {
        // Animation for TabLayout
        tabLayout.setTranslationY(300);
        tabLayout.setAlpha(v);
        tabLayout.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(100).start();

        // Animation for FAB
        fabGoogle.setTranslationY(300);
        fabGoogle.setAlpha(v);
        fabGoogle.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(300).start();
    }

    private void setupClickListeners() {
        fabGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement Google Sign In
                Toast.makeText(AuthActivity.this, "Google Sign In coming soon!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}