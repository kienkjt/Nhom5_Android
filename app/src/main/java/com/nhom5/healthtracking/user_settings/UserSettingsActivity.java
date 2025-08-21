package com.nhom5.healthtracking.user_settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.HealthTrackingApp;
import com.nhom5.healthtracking.auth.AuthActivity;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.util.AuthState;

public class UserSettingsActivity extends AppCompatActivity {
    private static final String TAG = "UserSettingsActivity";

    private MaterialSwitch switchNotifications;
    private MaterialSwitch switchVibrate;
    private MaterialButton btnEditProfile;
    private MaterialButton logoutButton;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private UserSettingsViewModel viewModel;
    private HealthTrackingApp app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        app = (HealthTrackingApp) getApplication();
        viewModel = new UserSettingsViewModel(app.getUserRepository(), app.getAuthRepository());
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_settings);

        setupToolbar();
        
        initViews();
        
        setupClickListeners();
        
        // Check auth state AFTER views are initialized
        checkAuthState();
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        observeAuthState();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.user_settings);
        }
    }

    private void initViews() {
        switchNotifications = findViewById(R.id.switch_notifications);
        switchVibrate = findViewById(R.id.switch_vibrate);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        logoutButton = findViewById(R.id.logout_button);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        
        Log.d(TAG, "initViews() - Views initialized:");
        Log.d(TAG, "tvUserName: " + (tvUserName != null ? "OK" : "NULL"));
        Log.d(TAG, "tvUserEmail: " + (tvUserEmail != null ? "OK" : "NULL"));
        
        // Set initial text to see if views are working
        if (tvUserName != null) {
            tvUserName.setText("Loading...");
        }
        if (tvUserEmail != null) {
            tvUserEmail.setText("Loading...");
        }
    }

    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> {
            // TODO: Navigate to edit profile screen
        });

        logoutButton.setOnClickListener(v -> {
            viewModel.logout();
            redirectToAuth();
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Save notification preference
        });

        switchVibrate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Save vibrate preference
        });
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

    private void checkAuthState() {
        AuthState currentState = app.getAuthState().getValue();
        Log.d(TAG, "checkAuthState() - Current state: " + currentState);
        
        if (currentState != null && currentState.isAuthenticated()) {
            Log.d(TAG, "User already authenticated, updating UI...");
            handleAuthenticatedUser((AuthState.Authenticated) currentState);
        } else {
            Log.d(TAG, "User not authenticated or state is null");
            // Set default values
            tvUserName.setText("Guest User");
            tvUserEmail.setText("No email");
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
        Log.d(TAG, "handleAuthenticatedUser() - Profile: " + profile);

        String name = profile.getName();
        String email = profile.getEmail();

        // Set user name
        if (name != null && !name.isEmpty()) {
            tvUserName.setText(name);
            Log.d(TAG, "Set user name: " + name);
        } else {
            tvUserName.setText("No name");
        }

        // Set user email
        if (email != null && !email.isEmpty()) {
            tvUserEmail.setText(email);
            Log.d(TAG, "Set user email: " + email);
        } else {
            tvUserEmail.setText("No email");
        }
    }

    private void redirectToAuth() {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}