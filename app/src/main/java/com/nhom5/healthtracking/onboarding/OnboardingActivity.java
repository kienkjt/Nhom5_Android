package com.nhom5.healthtracking.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;

import com.nhom5.healthtracking.HealthTrackingApp;
import com.nhom5.healthtracking.MainActivity;
import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.onboarding.step_one.PersonalInfoFragment;
import com.nhom5.healthtracking.onboarding.step_two.BodyMetricsFragment;
import com.nhom5.healthtracking.util.AuthState;

import java.util.Date;

public class OnboardingActivity extends AppCompatActivity {

    private static final String TAG = "OnboardingActivity";
    private MutableLiveData<Integer> currentStep = new MutableLiveData<>(1);
    private static final int TOTAL_STEPS = 3;
    private HealthTrackingApp app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        app = (HealthTrackingApp) getApplication();
        
        // Observe AuthState to get current user and onboarding step
        observeAuthState();
    }

    private void observeAuthState() {
        app.getAuthState().observe(this, authState -> {
            Log.d(TAG, "AuthState changed: " + authState);
            
            if (authState.isAuthenticated()) {
                User user = ((AuthState.Authenticated) authState).getProfile();
                if (user != null) {
                    handleUserOnboardingStep(user);
                } else {
                    Log.e(TAG, "User profile is null");
                    finish();
                }
            } else if (authState.isUnauthenticated()) {
                Log.e(TAG, "User not authenticated, finishing onboarding");
                finish();
            } else if (authState.isError()) {
                Log.e(TAG, "Auth error: " + ((AuthState.Error) authState).getMessage());
                finish();
            }
            // Loading state - do nothing, wait for final state
        });
    }

    private void handleUserOnboardingStep(User user) {
        Long onboardingStep = user.getOnboardingStep();
        if (onboardingStep == null) {
            onboardingStep = 1L;
        }

        // Check if user has already completed onboarding
        if (user.hasCompletedOnboarding()) {
            Log.d(TAG, "User has completed onboarding, redirecting to main app");
            redirectToMainApp();
            return;
        }

        currentStep.setValue(onboardingStep.intValue());
        Log.d(TAG, "Setting current step to: " + currentStep.getValue());
        
        // Load appropriate fragment based on current step
        loadFragmentForStep(currentStep.getValue());
        updateProgressIndicator(currentStep.getValue());
    }

    private void loadFragmentForStep(int step) {
        Fragment fragment;
        switch (step) {
            case 1:
                fragment = PersonalInfoFragment.newInstance();
                break;
            case 2:
                fragment = BodyMetricsFragment.newInstance();
                break;
            case 3:
                // TODO: Create Step 3 fragment
                fragment = PersonalInfoFragment.newInstance(); // Temporary
                break;
            default:
                Log.e(TAG, "Unknown step: " + step);
                fragment = PersonalInfoFragment.newInstance();
                break;
        }
        loadFragment(fragment);
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void redirectToMainApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateProgressIndicator(int step) {
        currentStep.setValue(step);

        // Update progress bars
        findViewById(R.id.progress_step_1).setBackgroundColor(
                getResources().getColor(step >= 1 ? R.color.progress_active : R.color.progress_inactive)
        );
        findViewById(R.id.progress_step_2).setBackgroundColor(
                getResources().getColor(step >= 2 ? R.color.progress_active : R.color.progress_inactive)
        );
        findViewById(R.id.progress_step_3).setBackgroundColor(
                getResources().getColor(step >= 3 ? R.color.progress_active : R.color.progress_inactive)
        );
    }

    public void goToNextStep() {
        if (currentStep.getValue() < TOTAL_STEPS) {
            int nextStep = currentStep.getValue() + 1;
            // Update user onboarding step in database - UI will be updated via AuthState observer
            updateUserOnboardingStep(nextStep);
        } else {
            // Finish onboarding
            finishOnboarding();
        }
    }

    public void goToPreviousStep() {
        if (currentStep.getValue() > 1) {
            int previousStep = currentStep.getValue() - 1;
            // Update user onboarding step in database - UI will be updated via AuthState observer
            updateUserOnboardingStep(previousStep);
        }
    }

    private void updateUserOnboardingStep(int step) {
        // Update user onboarding step in background
        new Thread(() -> {
            try {
                User currentUser = app.getUserRepository().getCurrentUser();
                if (currentUser != null) {
                    Log.d(TAG, "Current user: " + currentUser);
                    currentUser.onboardingStep = (long) step;
                    currentUser.updatedAt = new Date();
                    currentUser.isSynced = false;
                    app.getUserRepository().upsertSync(currentUser);
                    Log.d(TAG, "Updated user onboarding step to: " + step);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to update user onboarding step", e);
            }
        }).start();
    }

    private void finishOnboarding() {
        // Mark onboarding as completed (step 4) and navigate to main app
        new Thread(() -> {
            try {
                User currentUser = app.getUserRepository().getCurrentUser();
                if (currentUser != null) {
                    currentUser.onboardingStep = 4L; // Completed
                    currentUser.updatedAt = new Date();
                    currentUser.isSynced = false;
                    app.getUserRepository().upsertSync(currentUser);
                    Log.d(TAG, "Onboarding completed for user");
                    // Navigate to main app on UI thread
                    runOnUiThread(this::redirectToMainApp);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to finish onboarding", e);
                runOnUiThread(this::redirectToMainApp);
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        if (currentStep.getValue() > 1) {
            goToPreviousStep();
        } else {
            super.onBackPressed();
        }
    }
}