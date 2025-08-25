package com.nhom5.healthtracking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.nhom5.healthtracking.Blood.BloodPressureActivity;
import com.nhom5.healthtracking.auth.AuthActivity;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.data.repository.UserRepository;
import com.nhom5.healthtracking.data.repository.WeightRecordRepository;
import com.nhom5.healthtracking.onboarding.OnboardingActivity;
import com.nhom5.healthtracking.sleep.SleepTracker;
import com.nhom5.healthtracking.step.StepActivity;
import com.nhom5.healthtracking.user_settings.UserSettingsActivity;
import com.nhom5.healthtracking.util.AuthState;
import com.nhom5.healthtracking.util.BMICal;
import com.nhom5.healthtracking.util.StepDatabaseHelper;
import com.nhom5.healthtracking.water.WaterDataManager;
import com.nhom5.healthtracking.water.water_Monitoring;
import com.nhom5.healthtracking.weight.WeightTrackerActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    TextView tvUserName, tvUserAge, tvUserGender, tvUserHeight, tvUserWeight, tvUserBMIStatus, tvUserStepsStats, tvUserWaterStats;
    MaterialCardView cardWater, cardSleep, cardBlood, cardWeight, cardSteps, cardProfile;
    private HealthTrackingApp app;
    private WeightRecordRepository weightRepo;
    private User currentUser;
    private StepDatabaseHelper stepDbHelper;
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
        weightRepo = app.getWeightRecordRepository();
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
                currentUser = ((AuthState.Authenticated) authState).getProfile();
                Log.d(TAG, "Profile: " + currentUser);
                if (!currentUser.hasCompletedOnboarding()) {
                    redirectToOnboarding();
                } else {
                    setupMainUI();
                }
            } else if (authState.isError()) {
                AuthState.Error error = (AuthState.Error) authState;
                Log.e(TAG, "Auth error: " + error.getMessage());
                redirectToAuth();
            }
        });
    }

    private void setupMainUI() {
        initViews();
        setupClickListeners();
        setupUserInfo();
        setupStepsInfo();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserAge = findViewById(R.id.tv_user_age);
        tvUserGender = findViewById(R.id.tv_user_gender);
        tvUserHeight = findViewById(R.id.tv_user_height);
        tvUserWeight = findViewById(R.id.tv_user_weight);
        tvUserBMIStatus = findViewById(R.id.tv_bmi_status);
        tvUserStepsStats = findViewById(R.id.tv_steps_stats);
        tvUserWaterStats = findViewById(R.id.tv_water_stats);

        cardWater = findViewById(R.id.card_water);
        cardSleep = findViewById(R.id.card_sleep);
        cardBlood = findViewById(R.id.card_blood);
        cardWeight = findViewById(R.id.card_weight);
        cardSteps = findViewById(R.id.card_steps);
        cardProfile = findViewById(R.id.card_profile);
    }

    private void setupUserInfo() {
        tvUserName.setText(currentUser.getName());
        tvUserAge.setText(String.valueOf(currentUser.getAge()));
        tvUserGender.setText(currentUser.getGender());
        tvUserHeight.setText(String.valueOf(currentUser.getHeight()));
        setupWeightInfo();
        setupWaterInfo();
    }

    private void setupWeightInfo() {
        weightRepo.getLatestByUserIdLiveData(currentUser.getUid()).observe(this, weightRecord -> {
            if (weightRecord != null) {
                tvUserWeight.setText(String.valueOf(weightRecord.getWeight()));

                // Calculate BMI using height in cm
                double bmi = BMICal.calculateBMIFromCm(weightRecord.getWeight(), currentUser.getHeight());
                String bmiStatus = BMICal.getBMICategory(bmi);
                tvUserBMIStatus.setText(bmiStatus);
            }
        });
    }

    private void setupWaterInfo() {
        int totalWaterToday = WaterDataManager.getTotalWater(this);
        Log.d(TAG, "Total water today: " + totalWaterToday);
        tvUserWaterStats.setText(String.valueOf(totalWaterToday));
    }
    private void setupStepsInfo() {
        stepDbHelper = new StepDatabaseHelper(this);
        int todaySteps = stepDbHelper.getTodaySteps(1);
        tvUserStepsStats.setText(String.valueOf(todaySteps));
    }
    private void setupClickListeners() {
        if (cardWater != null) {
            cardWater.setOnClickListener(v -> redirectToWaterMonitoring());
        }
        if (cardSleep != null) {
            cardSleep.setOnClickListener(v -> redirectToSleepTracker());
        }
        if (cardBlood != null) {
            cardBlood.setOnClickListener(v -> redirectToBloodPressure());
        }
        if (cardWeight != null) {
            cardWeight.setOnClickListener(v -> redirectToWeightTracker());
        }
        if (cardSteps != null) {
            cardSteps.setOnClickListener(v -> redirectToStepActivity());
        }
        if (cardProfile != null) {
            cardProfile.setOnClickListener(v -> redirectToUserSettings());
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

    private void redirectToWaterMonitoring() {
        Intent intent = new Intent(this, water_Monitoring.class);
        startActivity(intent);
    }

    private void redirectToSleepTracker() {
        Intent intent = new Intent(this, SleepTracker.class);
        startActivity(intent);
    }

    private void redirectToBloodPressure() {
        Intent intent = new Intent(this, BloodPressureActivity.class);
        startActivity(intent);
    }

    private void redirectToStepActivity() {
        Intent intent = new Intent(this, StepActivity.class);
        startActivity(intent);
    }

    private void redirectToWeightTracker() {
        Intent intent = new Intent(this, WeightTrackerActivity.class);
        startActivity(intent);
    }

}