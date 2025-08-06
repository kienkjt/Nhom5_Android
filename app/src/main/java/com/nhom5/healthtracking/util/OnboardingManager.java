package com.nhom5.healthtracking.util;

import android.content.Context;
import android.content.SharedPreferences;

public class OnboardingManager {
    
    private static final String PREF_NAME = "health_tracking_onboarding";
    private static final String KEY_CURRENT_STEP = "current_step";
    private static final String KEY_STEP_1_COMPLETED = "step_1_completed";
    private static final String KEY_STEP_2_COMPLETED = "step_2_completed";
    private static final String KEY_STEP_3_COMPLETED = "step_3_completed";
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
    
    // Personal Info Data (Step 1)
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_DATE_OF_BIRTH = "date_of_birth";
    
    // Body Metrics Data (Step 2)
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_WEIGHT = "weight";
    private static final String KEY_BMI = "bmi";
    
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    
    public OnboardingManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    
    // Step Management
    public void setCurrentStep(int step) {
        editor.putInt(KEY_CURRENT_STEP, step);
        editor.apply();
    }
    
    public int getCurrentStep() {
        return sharedPreferences.getInt(KEY_CURRENT_STEP, 1);
    }
    
    public void markStepCompleted(int step) {
        switch (step) {
            case 1:
                editor.putBoolean(KEY_STEP_1_COMPLETED, true);
                break;
            case 2:
                editor.putBoolean(KEY_STEP_2_COMPLETED, true);
                break;
            case 3:
                editor.putBoolean(KEY_STEP_3_COMPLETED, true);
                break;
        }
        editor.apply();
    }
    
    public boolean isStepCompleted(int step) {
        switch (step) {
            case 1:
                return sharedPreferences.getBoolean(KEY_STEP_1_COMPLETED, false);
            case 2:
                return sharedPreferences.getBoolean(KEY_STEP_2_COMPLETED, false);
            case 3:
                return sharedPreferences.getBoolean(KEY_STEP_3_COMPLETED, false);
            default:
                return false;
        }
    }
    
    public void setOnboardingCompleted(boolean completed) {
        editor.putBoolean(KEY_ONBOARDING_COMPLETED, completed);
        if (completed) {
            setCurrentStep(4); // Onboarding finished
        }
        editor.apply();
    }
    
    public boolean isOnboardingCompleted() {
        return sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false);
    }
    
    // Personal Info Data Methods (Step 1)
    public void savePersonalInfo(String fullName, String gender, String dateOfBirth) {
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_GENDER, gender);
        editor.putString(KEY_DATE_OF_BIRTH, dateOfBirth);
        editor.apply();
        
        // Mark step 1 as completed
        markStepCompleted(1);
    }
    
    public String getFullName() {
        return sharedPreferences.getString(KEY_FULL_NAME, "");
    }
    
    public String getGender() {
        return sharedPreferences.getString(KEY_GENDER, "");
    }
    
    public String getDateOfBirth() {
        return sharedPreferences.getString(KEY_DATE_OF_BIRTH, "");
    }
    
    // Body Metrics Data Methods (Step 2)
    public void saveBodyMetrics(String height, String weight, double bmi) {
        editor.putString(KEY_HEIGHT, height);
        editor.putString(KEY_WEIGHT, weight);
        editor.putFloat(KEY_BMI, (float) bmi);
        editor.apply();
        
        // Mark step 2 as completed
        markStepCompleted(2);
    }
    
    public String getHeight() {
        return sharedPreferences.getString(KEY_HEIGHT, "");
    }
    
    public String getWeight() {
        return sharedPreferences.getString(KEY_WEIGHT, "");
    }
    
    public float getBMI() {
        return sharedPreferences.getFloat(KEY_BMI, 0.0f);
    }
    
    // Validation Methods
    public boolean canProceedToStep(int step) {
        switch (step) {
            case 1:
                return true; // Always can start from step 1
            case 2:
                return isStepCompleted(1);
            case 3:
                return isStepCompleted(1) && isStepCompleted(2);
            default:
                return false;
        }
    }
    
    public int getNextAvailableStep() {
        if (!isStepCompleted(1)) {
            return 1;
        } else if (!isStepCompleted(2)) {
            return 2;
        } else if (!isStepCompleted(3)) {
            return 3;
        } else {
            return 4; // Onboarding completed
        }
    }
    
    // Clear all onboarding data (for testing or reset)
    public void clearOnboardingData() {
        editor.clear();
        editor.apply();
    }
    
    // Debug methods
    public void logOnboardingStatus() {
        android.util.Log.d("OnboardingManager", "Current Step: " + getCurrentStep());
        android.util.Log.d("OnboardingManager", "Step 1 Completed: " + isStepCompleted(1));
        android.util.Log.d("OnboardingManager", "Step 2 Completed: " + isStepCompleted(2));
        android.util.Log.d("OnboardingManager", "Step 3 Completed: " + isStepCompleted(3));
        android.util.Log.d("OnboardingManager", "Onboarding Completed: " + isOnboardingCompleted());
    }
} 