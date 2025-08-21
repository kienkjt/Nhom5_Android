package com.nhom5.healthtracking.onboarding.step_one;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nhom5.healthtracking.HealthTrackingApp;
import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.util.AuthState;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PersonalInfoFragment extends Fragment {

    private PersonalInfoViewModel mViewModel;

    private TextInputLayout tilFullName;
    private TextInputEditText etFullName;
    private RadioGroup rgGender;
    private TextInputLayout tilDateOfBirth;
    private TextInputEditText etDateOfBirth;
    private MaterialButton btnContinue;
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;

    public static PersonalInfoFragment newInstance() {
        return new PersonalInfoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        selectedDate = Calendar.getInstance();

        HealthTrackingApp app = (HealthTrackingApp) requireActivity().getApplication();
        mViewModel = new PersonalInfoViewModel(app.getUserRepository());

        setupListeners();
        observeAuthState();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void initViews(View view) {
        tilFullName = view.findViewById(R.id.til_full_name);
        etFullName = view.findViewById(R.id.et_full_name);
        rgGender = view.findViewById(R.id.rg_gender);
        tilDateOfBirth = view.findViewById(R.id.til_date_of_birth);
        etDateOfBirth = view.findViewById(R.id.et_date_of_birth);
        btnContinue = view.findViewById(R.id.btn_continue);
    }

    private void observeAuthState() {
        // Observe AuthState to load existing data when available
        HealthTrackingApp app = (HealthTrackingApp) requireActivity().getApplication();
        app.getAuthState().observe(getViewLifecycleOwner(), authState -> {
            if (authState.isAuthenticated()) {
                User user = ((AuthState.Authenticated) authState).getProfile();
                loadExistingUserData(user);
            }
        });
    }

    private void loadExistingUserData(User user) {
        if (user == null) return;

        // Load name
        if (user.getName() != null && !user.getName().isEmpty()) {
            etFullName.setText(user.getName());
        }

        // Load gender
        if (user.getGender() != null) {
            switch (user.getGender()) {
                case "Male":
                case "Nam":
                    rgGender.check(R.id.rb_male);
                    break;
                case "Female":
                case "Nữ":
                    rgGender.check(R.id.rb_female);
                    break;
                case "Other":
                case "Khác":
                    rgGender.check(R.id.rb_other);
                    break;
            }
        }

        // Load date of birth
        if (user.getDateOfBirth() != null) {
            selectedDate.setTime(user.getDateOfBirth());
            etDateOfBirth.setText(dateFormat.format(user.getDateOfBirth()));
        }
    }

    private void setupListeners() {
        etDateOfBirth.setOnClickListener(v -> showDatePicker());

        btnContinue.setOnClickListener(v -> {
            if (validateForm()) {
                savePersonalInfo();
            }
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            etDateOfBirth.setText(dateFormat.format(selectedDate.getTime()));
            tilDateOfBirth.setError(null);
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -100);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -10);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.show();
    }

    private boolean validateForm() {
        boolean isValid = true;

        tilFullName.setError(null);
        tilDateOfBirth.setError(null);

        String fullName = etFullName.getText().toString().trim();
        if (fullName.isEmpty()) {
            tilFullName.setError(getString(R.string.error_name_required));
            isValid = false;
        } else if (fullName.length() < 2) {
            tilFullName.setError("Name must be at least 2 characters");
            isValid = false;
        }

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            isValid = false;
        }

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

        if (mViewModel != null) {
            mViewModel.savePersonalInfo(fullName, gender, dateOfBirth, new PersonalInfoViewModel.SaveCallback() {
                @Override
                public void onSuccess() {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Đã lưu thông tin cá nhân thành công!", Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Lỗi khi lưu: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
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
}