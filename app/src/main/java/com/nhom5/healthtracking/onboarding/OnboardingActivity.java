package com.nhom5.healthtracking.onboarding;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.onboarding.step_one.PersonalInfoFragment;

public class OnboardingActivity extends AppCompatActivity {

    private int currentStep = 1;
    private static final int TOTAL_STEPS = 3;

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
        
        // Load first step
        if (savedInstanceState == null) {
            loadFragment(PersonalInfoFragment.newInstance());
            updateProgressIndicator(1);
        }
    }
    
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
    
    private void updateProgressIndicator(int step) {
        currentStep = step;
        
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
        if (currentStep < TOTAL_STEPS) {
            currentStep++;
            updateProgressIndicator(currentStep);
            
            // Load next fragment based on current step
            switch (currentStep) {
                case 2:
                    // Load Step 2 fragment (will be implemented later)
                    // loadFragment(HealthGoalsFragment.newInstance());
                    break;
                case 3:
                    // Load Step 3 fragment (will be implemented later)
                    // loadFragment(PreferencesFragment.newInstance());
                    break;
            }
        } else {
            // Finish onboarding
            finishOnboarding();
        }
    }
    
    public void goToPreviousStep() {
        if (currentStep > 1) {
            currentStep--;
            updateProgressIndicator(currentStep);
            
            // Load previous fragment based on current step
            switch (currentStep) {
                case 1:
                    loadFragment(PersonalInfoFragment.newInstance());
                    break;
                case 2:
                    // Load Step 2 fragment (will be implemented later)
                    // loadFragment(HealthGoalsFragment.newInstance());
                    break;
            }
        }
    }
    
    private void finishOnboarding() {
        // Mark onboarding as completed and navigate to main app
        // This can be implemented with SharedPreferences or similar
        finish();
    }
    
    @Override
    public void onBackPressed() {
        if (currentStep > 1) {
            goToPreviousStep();
        } else {
            super.onBackPressed();
        }
    }
}