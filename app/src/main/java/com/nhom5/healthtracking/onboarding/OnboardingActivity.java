package com.nhom5.healthtracking.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.nhom5.healthtracking.MainActivity;
import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.onboarding.step_one.PersonalInfoFragment;
import com.nhom5.healthtracking.onboarding.step_two.BodyMetricsFragment;
import com.nhom5.healthtracking.util.OnboardingManager;

public class OnboardingActivity extends AppCompatActivity {

    private int currentStep = 1;
    private static final int TOTAL_STEPS = 3;
    private OnboardingManager onboardingManager;

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
        
        // Initialize OnboardingManager
        onboardingManager = new OnboardingManager(this);
        
        // Check if onboarding is already completed
        if (onboardingManager.isOnboardingCompleted()) {
            navigateToMainApp();
            return;
        }
        
        // Load appropriate step
        if (savedInstanceState == null) {
            currentStep = onboardingManager.getCurrentStep();
            loadStepFragment(currentStep);
            updateProgressIndicator(currentStep);
        }
        
        // Debug log
        onboardingManager.logOnboardingStatus();
    }
    
    private void loadStepFragment(int step) {
        Fragment fragment = null;
        
        switch (step) {
            case 1:
                fragment = PersonalInfoFragment.newInstance();
                break;
            case 2:
                if (onboardingManager.canProceedToStep(2)) {
                    fragment = BodyMetricsFragment.newInstance();
                } else {
                    // Force back to step 1 if step 1 not completed
                    currentStep = 1;
                    fragment = PersonalInfoFragment.newInstance();
                    Toast.makeText(this, "Please complete Step 1 first", Toast.LENGTH_SHORT).show();
                }
                break;
            case 3:
                if (onboardingManager.canProceedToStep(3)) {
                    // Load Step 3 fragment (will be implemented later)
                    // fragment = HealthGoalsFragment.newInstance();
                    Toast.makeText(this, "Step 3 will be implemented soon", Toast.LENGTH_SHORT).show();
                    fragment = PersonalInfoFragment.newInstance();
                    currentStep = 1;
                } else {
                    // Force back to appropriate step
                    currentStep = onboardingManager.getNextAvailableStep();
                    loadStepFragment(currentStep);
                    return;
                }
                break;
            default:
                fragment = PersonalInfoFragment.newInstance();
                currentStep = 1;
                break;
        }
        
        if (fragment != null) {
            loadFragment(fragment);
            onboardingManager.setCurrentStep(currentStep);
            updateProgressIndicator(currentStep);
        }
    }
    
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
    
    private void updateProgressIndicator(int step) {
        currentStep = step;
        
        // Update progress bars based on completed steps
        findViewById(R.id.progress_step_1).setBackgroundColor(
            getResources().getColor(onboardingManager.isStepCompleted(1) || step >= 1 ? 
                R.color.progress_active : R.color.progress_inactive)
        );
        findViewById(R.id.progress_step_2).setBackgroundColor(
            getResources().getColor(onboardingManager.isStepCompleted(2) || step >= 2 ? 
                R.color.progress_active : R.color.progress_inactive)
        );
        findViewById(R.id.progress_step_3).setBackgroundColor(
            getResources().getColor(onboardingManager.isStepCompleted(3) || step >= 3 ? 
                R.color.progress_active : R.color.progress_inactive)
        );
    }
    
    public void goToNextStep() {
        if (currentStep < TOTAL_STEPS) {
            int nextStep = currentStep + 1;
            
            // Check if can proceed to next step
            if (onboardingManager.canProceedToStep(nextStep)) {
                currentStep = nextStep;
                loadStepFragment(currentStep);
                
                Toast.makeText(this, "Step " + (currentStep - 1) + " completed successfully!", 
                    Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please complete the current step first", 
                    Toast.LENGTH_SHORT).show();
            }
        } else {
            // All steps completed
            finishOnboarding();
        }
    }
    
    public void goToPreviousStep() {
        if (currentStep > 1) {
            currentStep--;
            loadStepFragment(currentStep);
        } else {
            // Exit onboarding
            finish();
        }
    }
    
    private void finishOnboarding() {
        // Mark onboarding as completed
        onboardingManager.setOnboardingCompleted(true);
        
        Toast.makeText(this, "Onboarding completed! Welcome to Health Tracking!", 
            Toast.LENGTH_LONG).show();
        
        // Navigate to main app
        navigateToMainApp();
    }
    
    private void navigateToMainApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    public OnboardingManager getOnboardingManager() {
        return onboardingManager;
    }
    
    @Override
    public void onBackPressed() {
        if (currentStep > 1) {
            goToPreviousStep();
        } else {
            // Show confirmation dialog before exiting onboarding
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Update progress indicator when resuming
        updateProgressIndicator(currentStep);
    }
}