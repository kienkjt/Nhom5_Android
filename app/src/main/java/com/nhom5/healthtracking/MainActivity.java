package com.nhom5.healthtracking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nhom5.healthtracking.auth.AuthActivity;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.onboarding.OnboardingActivity;
import com.nhom5.healthtracking.user_settings.UserSettingsActivity;
import com.nhom5.healthtracking.util.AuthState;

public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "MainActivity";
    
    private FloatingActionButton fabAuth;
    private TextView textView;
    private HealthTrackingApp app;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        setupWindowInsets();
        initApp();
        observeAuthState();
    }
    
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    
    private void initApp() {
        app = (HealthTrackingApp) getApplication();
        app.getAuthRepository().init();
    }
    
    private void observeAuthState() {
        app.getAuthState().observe(this, authState -> {
            Log.d(TAG, "AuthState changed: " + authState);

            if (authState.isLoading()) {
                Log.d(TAG, "Authentication loading...");
            } else if (authState.isUnauthenticated()) {
                Log.d(TAG, "User not authenticated, redirecting to auth");
                redirectToAuth();
                
            } else if (authState.isAuthenticated()) {
                User profile = ((AuthState.Authenticated) authState).getProfile();

                Log.d(TAG, "User authenticated: " + profile.toString());
                
                if (profile.hasCompletedOnboarding()) {
                    Log.d(TAG, "User authenticated and onboarded, setting up main UI");
                    setupMainUI(profile);
                } else {
                    Log.d(TAG, "User needs onboarding, redirecting");
                    redirectToOnboarding();
                }
                
            } else if (authState.isError()) {
                AuthState.Error error = (AuthState.Error) authState;
                Log.e(TAG, "Auth error: " + error.getMessage());
                redirectToAuth();
            }
        });
    }
    
    private void setupMainUI(User user) {
        initViews();
        setupClickListeners();
        displayUserInfo(user);
    }
    
    private void initViews() {
        fabAuth = findViewById(R.id.fab_auth);
        textView = findViewById(R.id.text_view);
    }
    
    private void displayUserInfo(User user) {
        if (user != null) {
            String displayText = user.name != null && !user.name.isEmpty() 
                ? "Xin chào, " + user.name 
                : user.email != null ? user.email : "Unknown User";
            textView.setText(displayText);
        }
    }
    
    private void setupClickListeners() {
        if (fabAuth != null) {
            fabAuth.setOnClickListener(v -> redirectToUserSettings());
        }
    }
    
    private void redirectToAuth() {
        Intent intent = new Intent(this, AuthActivity.class);
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

    private void redirectToUserSettings() {
        Intent intent = new Intent(this, UserSettingsActivity.class);
        startActivity(intent);
    }
}