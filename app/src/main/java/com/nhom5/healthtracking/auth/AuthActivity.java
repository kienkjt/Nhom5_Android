package com.nhom5.healthtracking.auth;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.nhom5.healthtracking.R;

public class AuthActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager viewPager;
    AuthAdapter authAdapter;
    float v = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);

        initViews();
        setupViewPager();
        setupAnimations();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
    }

    private void setupViewPager() {
        authAdapter = new AuthAdapter(getSupportFragmentManager());
        viewPager.setAdapter(authAdapter);
        tabLayout.setupWithViewPager(viewPager);

    }
    private void setupAnimations() {
        tabLayout.setTranslationY(300);
        tabLayout.setAlpha(v);
        tabLayout.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(100).start();
    }
}