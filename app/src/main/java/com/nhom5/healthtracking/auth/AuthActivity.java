package com.nhom5.healthtracking.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.nhom5.healthtracking.HealthTrackingApp;
import com.nhom5.healthtracking.MainActivity;
import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.onboarding.OnboardingActivity;
import com.nhom5.healthtracking.util.AuthState;

public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "AuthActivity";
    TabLayout tabLayout;
    ViewPager viewPager;
    AuthAdapter authAdapter;
    float v = 0;
    private HealthTrackingApp app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);

        app = (HealthTrackingApp) getApplication();
        
        // Check if user is already authenticated
        checkAuthState();
        
        initViews();
        setupViewPager();
        setupAnimations();
        observeAuthState();
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
    
    private void checkAuthState() {
        AuthState currentState = app.getAuthState().getValue();
        if (currentState != null && currentState.isAuthenticated()) {
            Log.d(TAG, "User already authenticated, redirecting...");
            handleAuthenticatedUser((AuthState.Authenticated) currentState);
        }
    }
    
    private void observeAuthState() {
        app.getAuthState().observe(this, authState -> {
            Log.d(TAG, "AuthState changed: " + authState);
            
            if (authState.isAuthenticated()) {
                handleAuthenticatedUser((AuthState.Authenticated) authState);
            }
        });
    }
    
    private void handleAuthenticatedUser(AuthState.Authenticated authState) {
        User profile = authState.getProfile();
        Log.d(TAG, "User authenticated: " + profile.toString());
        
        if (profile.hasCompletedOnboarding()) {
            Log.d(TAG, "User completed onboarding, redirecting to main");
            redirectToMain();
        } else {
            Log.d(TAG, "User needs onboarding, redirecting to onboarding");
            redirectToOnboarding();
        }
    }
    
    private void redirectToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void redirectToOnboarding() {
        Intent intent = new Intent(this, OnboardingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}