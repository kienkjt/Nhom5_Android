package com.nhom5.healthtracking.onboarding.step_one;

import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.onboarding.OnboardingActivity;
import com.nhom5.healthtracking.util.OnboardingManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PersonalInfoFragment extends Fragment {

    private PersonalInfoViewModel mViewModel;
    private OnboardingManager onboardingManager;
    
    // Views
    private TextInputLayout tilFullName;
    private TextInputEditText etFullName;
    private RadioGroup rgGender;
    private TextInputLayout tilDateOfBirth;
    private TextInputEditText etDateOfBirth;
    private MaterialButton btnContinue;
    
    // Data
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;

    public static PersonalInfoFragment newInstance() {
        return new PersonalInfoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_info, container, false);
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
        
        // Setup date format
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        selectedDate = Calendar.getInstance();
        
        // Setup listeners
        setupListeners();
        
        // Load saved data if exists
        loadSavedData();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PersonalInfoViewModel.class);
    }
    
    private void initViews(View view) {
        tilFullName = view.findViewById(R.id.til_full_name);
        etFullName = view.findViewById(R.id.et_full_name);
        rgGender = view.findViewById(R.id.rg_gender);
        tilDateOfBirth = view.findViewById(R.id.til_date_of_birth);
        etDateOfBirth = view.findViewById(R.id.et_date_of_birth);
        btnContinue = view.findViewById(R.id.btn_continue);
    }
    
    private void loadSavedData() {
        if (onboardingManager == null) return;
        
        // Load saved personal info
        String savedName = onboardingManager.getFullName();
        String savedGender = onboardingManager.getGender();
        String savedDateOfBirth = onboardingManager.getDateOfBirth();
        
        if (!savedName.isEmpty()) {
            etFullName.setText(savedName);
        }
        
        if (!savedGender.isEmpty()) {
            switch (savedGender) {
                case "Male":
                    rgGender.check(R.id.rb_male);
                    break;
                case "Female":
                    rgGender.check(R.id.rb_female);
                    break;
                case "Other":
                    rgGender.check(R.id.rb_other);
                    break;
            }
        }
        
        if (!savedDateOfBirth.isEmpty()) {
            etDateOfBirth.setText(savedDateOfBirth);
        }
    }
    
    private void setupListeners() {
        // Date of birth click listener
        etDateOfBirth.setOnClickListener(v -> showDatePicker());
        
        // Continue button click listener
        btnContinue.setOnClickListener(v -> {
            if (validateForm()) {
                // Save data and proceed to next step
                savePersonalInfo();
                proceedToNextStep();
            }
        });
    }
    
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        
        // Set max date to today (no future dates)
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                
                etDateOfBirth.setText(dateFormat.format(selectedDate.getTime()));
                // Clear any previous error
                tilDateOfBirth.setError(null);
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set max date to today
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        
        // Set min date to 100 years ago
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -100);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        
        datePickerDialog.show();
    }
    
    private boolean validateForm() {
        boolean isValid = true;
        
        // Clear all previous errors
        tilFullName.setError(null);
        tilDateOfBirth.setError(null);
        
        // Validate full name
        String fullName = etFullName.getText().toString().trim();
        if (fullName.isEmpty()) {
            tilFullName.setError(getString(R.string.error_name_required));
            isValid = false;
        } else if (fullName.length() < 2) {
            tilFullName.setError("Name must be at least 2 characters");
            isValid = false;
        }
        
        // Validate gender selection
        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            // You can show a toast or snackbar for gender validation
            isValid = false;
        }
        
        // Validate date of birth
        String dateOfBirth = etDateOfBirth.getText().toString().trim();
        if (dateOfBirth.isEmpty()) {
            tilDateOfBirth.setError(getString(R.string.error_dob_required));
            isValid = false;
        }
        
        return isValid;
    }

    private void savePersonalInfo() {
        String fullName = etFullName.getText().toString().trim();
        String gender = getSelectedGender();
        String dateOfBirth = etDateOfBirth.getText().toString().trim();
        
        // Save to ViewModel
        if (mViewModel != null) {
            mViewModel.savePersonalInfo(fullName, gender, dateOfBirth);
        }
        
        // Save to OnboardingManager (SharedPreferences)
        if (onboardingManager != null) {
            onboardingManager.savePersonalInfo(fullName, gender, dateOfBirth);
        }
    }
    
    private String getSelectedGender() {
        int selectedId = rgGender.getCheckedRadioButtonId();
        if (selectedId == R.id.rb_male) {
            return "Male";
        } else if (selectedId == R.id.rb_female) {
            return "Female";
        } else if (selectedId == R.id.rb_other) {
            return "Other";
        }
        return "";
    }
    
    private void proceedToNextStep() {
        // Navigate to next step
        if (getActivity() != null && getActivity() instanceof com.nhom5.healthtracking.onboarding.OnboardingActivity) {
            ((com.nhom5.healthtracking.onboarding.OnboardingActivity) getActivity()).goToNextStep();
        }
    }
}