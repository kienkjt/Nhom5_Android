package com.nhom5.healthtracking.onboarding.step_two;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.onboarding.OnboardingActivity;
import com.nhom5.healthtracking.util.OnboardingManager;

public class BodyMetricsFragment extends Fragment {

    private BodyMetricsViewModel mViewModel;
    private OnboardingManager onboardingManager;
    
    // Views
    private TextView tvStep;
    private TextInputLayout tilHeight;
    private TextInputEditText etHeight;
    private TextInputLayout tilWeight;
    private TextInputEditText etWeight;
    private MaterialButton btnContinue;
    
    // BMI Display
    private CardView cardBmiInfo;
    private TextView tvBmiValue;
    private TextView tvBmiCategory;

    public static BodyMetricsFragment newInstance() {
        return new BodyMetricsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_body_metrics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize OnboardingManager
        if (getActivity() instanceof OnboardingActivity) {
            onboardingManager = ((OnboardingActivity) getActivity()).getOnboardingManager();
        }
        
        // Initialize views
        initViews(view);
        
        // Setup ViewModel
        mViewModel = new ViewModelProvider(this).get(BodyMetricsViewModel.class);
        
        // Setup listeners
        setupListeners();
        
        // Observe ViewModel
        observeViewModel();
        
        // Load saved data if exists
        loadSavedData();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    
    private void initViews(View view) {
        tvStep = view.findViewById(R.id.tv_step);
        tilHeight = view.findViewById(R.id.til_height);
        etHeight = view.findViewById(R.id.et_height);
        tilWeight = view.findViewById(R.id.til_weight);
        etWeight = view.findViewById(R.id.et_weight);
        btnContinue = view.findViewById(R.id.btn_continue);
        cardBmiInfo = view.findViewById(R.id.card_bmi_info);
        tvBmiValue = view.findViewById(R.id.tv_bmi_value);
        tvBmiCategory = view.findViewById(R.id.tv_bmi_category);
    }
    
    private void loadSavedData() {
        if (onboardingManager == null) return;
        
        // Load saved body metrics
        String savedHeight = onboardingManager.getHeight();
        String savedWeight = onboardingManager.getWeight();
        
        if (!savedHeight.isEmpty()) {
            etHeight.setText(savedHeight);
        }
        
        if (!savedWeight.isEmpty()) {
            etWeight.setText(savedWeight);
        }
        
        // Update ViewModel with saved data
        if (!savedHeight.isEmpty() || !savedWeight.isEmpty()) {
            mViewModel.saveBodyMetrics(savedHeight, savedWeight);
        }
    }
    
    private void setupListeners() {
        // Height text watcher for BMI calculation
        etHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mViewModel.setHeight(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Weight text watcher for BMI calculation
        etWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mViewModel.setWeight(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Continue button click listener
        btnContinue.setOnClickListener(v -> {
            if (validateAndProceed()) {
                saveDataAndProceed();
            }
        });
    }
    
    private void observeViewModel() {
        // Observe BMI value changes
        mViewModel.getBmiValue().observe(getViewLifecycleOwner(), bmiValue -> {
            if (bmiValue != null && bmiValue > 0) {
                tvBmiValue.setText(String.valueOf(bmiValue));
                cardBmiInfo.setVisibility(View.VISIBLE);
            } else {
                cardBmiInfo.setVisibility(View.GONE);
            }
        });
        
        // Observe BMI category changes
        mViewModel.getBmiCategory().observe(getViewLifecycleOwner(), category -> {
            if (category != null) {
                tvBmiCategory.setText(category);
                
                // Update BMI card color based on category
                updateBmiCardColor(category);
            }
        });
        
        // Observe form validation
        mViewModel.getIsFormValid().observe(getViewLifecycleOwner(), isValid -> {
            // You can add visual feedback here if needed
            // For now, validation is done only when Continue is pressed
        });
    }
    
    private void updateBmiCardColor(String category) {
        if (getContext() == null) return;
        
        int backgroundColor;
        int textColor;
        
        switch (category) {
            case "Underweight":
                backgroundColor = getResources().getColor(R.color.light_blue);
                textColor = getResources().getColor(R.color.primary_blue);
                break;
            case "Normal weight":
                backgroundColor = getResources().getColor(R.color.light_green);
                textColor = getResources().getColor(R.color.primary_green);
                break;
            case "Overweight":
            case "Obese":
                backgroundColor = getResources().getColor(R.color.light_blue);
                textColor = getResources().getColor(R.color.text_secondary);
                break;
            default:
                backgroundColor = getResources().getColor(R.color.light_green);
                textColor = getResources().getColor(R.color.primary_green);
                break;
        }
        
        cardBmiInfo.setCardBackgroundColor(backgroundColor);
        tvBmiValue.setTextColor(textColor);
    }
    
    private boolean validateAndProceed() {
        boolean isValid = true;
        
        // Clear previous errors
        tilHeight.setError(null);
        tilWeight.setError(null);
        
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        
        // Validate height
        if (heightStr.isEmpty()) {
            tilHeight.setError(getString(R.string.error_height_required));
            isValid = false;
        } else if (!mViewModel.isValidHeight(heightStr)) {
            tilHeight.setError(getString(R.string.error_height_invalid));
            isValid = false;
        }
        
        // Validate weight
        if (weightStr.isEmpty()) {
            tilWeight.setError(getString(R.string.error_weight_required));
            isValid = false;
        } else if (!mViewModel.isValidWeight(weightStr)) {
            tilWeight.setError(getString(R.string.error_weight_invalid));
            isValid = false;
        }
        
        if (!isValid) {
            Toast.makeText(getContext(), "Please check your input and try again", Toast.LENGTH_SHORT).show();
        }
        
        return isValid;
    }
    
    private void saveDataAndProceed() {
        String height = etHeight.getText().toString().trim();
        String weight = etWeight.getText().toString().trim();
        
        // Save to ViewModel
        mViewModel.saveBodyMetrics(height, weight);
        
        // Get BMI value for saving
        Double bmiValue = mViewModel.getBmiValue().getValue();
        double bmi = bmiValue != null ? bmiValue : 0.0;
        
        // Save to OnboardingManager (SharedPreferences)
        if (onboardingManager != null) {
            onboardingManager.saveBodyMetrics(height, weight, bmi);
        }
        
        // Show success message
        Toast.makeText(getContext(), "Body metrics saved successfully!", Toast.LENGTH_SHORT).show();
        
        // Proceed to next step
        proceedToNextStep();
    }
    
    private void proceedToNextStep() {
        // Navigate to next step
        if (getActivity() != null && getActivity() instanceof com.nhom5.healthtracking.onboarding.OnboardingActivity) {
            ((com.nhom5.healthtracking.onboarding.OnboardingActivity) getActivity()).goToNextStep();
        }
    }
}