package com.nhom5.healthtracking.user_settings;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nhom5.healthtracking.HealthTrackingApp;
import com.nhom5.healthtracking.R;
import com.nhom5.healthtracking.data.local.entity.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    private EditProfileViewModel viewModel;
    private ProgressDialog progressDialog;
    
    // UI Components
    private TextInputLayout tilFullName, tilDateOfBirth, tilHeight;
    private TextInputEditText etFullName, etDateOfBirth, etHeight;
    private RadioGroup rgGender;
    private MaterialButton btnSave, btnCancel;
    
    // Date handling
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        
        setupToolbar();
        initViews();
        setupViewModel();
        setupListeners();
        setupInputFilters();
        observeViewModel();
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Chỉnh sửa hồ sơ");
        }
    }

    private void initViews() {
        // TextInputLayouts
        tilFullName = findViewById(R.id.til_full_name);
        tilDateOfBirth = findViewById(R.id.til_date_of_birth);
        tilHeight = findViewById(R.id.til_height);
        
        // TextInputEditTexts
        etFullName = findViewById(R.id.et_full_name);
        etDateOfBirth = findViewById(R.id.et_date_of_birth);
        etHeight = findViewById(R.id.et_height);
        
        // Other components
        rgGender = findViewById(R.id.rg_gender);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        
        // Date setup
        selectedDate = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        
        // Progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang lưu thông tin...");
        progressDialog.setCancelable(false);
    }

    private void setupViewModel() {
        HealthTrackingApp app = (HealthTrackingApp) getApplication();
        viewModel = new EditProfileViewModel(app.getUserRepository());
    }

    private void setupListeners() {
        etDateOfBirth.setOnClickListener(v -> showDatePicker());
        
        btnSave.setOnClickListener(v -> saveProfile());
        
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void setupInputFilters() {
        // Full name input filter - only letters and spaces
        InputFilter nameFilter = new InputFilter.LengthFilter(50);
        InputFilter[] nameFilters = {nameFilter, new NameInputFilter()};
        etFullName.setFilters(nameFilters);
        
        // Height input filter - only digits and decimal point
        InputFilter heightFilter = new InputFilter.LengthFilter(6);
        InputFilter[] heightFilters = {heightFilter, new DecimalInputFilter()};
        etHeight.setFilters(heightFilters);
    }

    private void observeViewModel() {
        viewModel.getCurrentUser().observe(this, this::loadUserData);
        
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                if (isLoading) {
                    progressDialog.show();
                } else {
                    progressDialog.dismiss();
                }
            }
        });
        
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                viewModel.clearErrorMessage();
            }
        });
        
        viewModel.getIsSaveSuccessful().observe(this, isSuccessful -> {
            if (isSuccessful != null && isSuccessful) {
                Toast.makeText(this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void loadUserData(User user) {
        if (user == null) return;
        
        // Load name
        if (user.getName() != null) {
            etFullName.setText(user.getName());
        }

        // Load gender
        if (user.getGender() != null) {
            switch (user.getGender()) {
                case "Nam":
                case "Male":
                    rgGender.check(R.id.rb_male);
                    break;
                case "Nữ":
                case "Female":
                    rgGender.check(R.id.rb_female);
                    break;
                case "Khác":
                case "Other":
                    rgGender.check(R.id.rb_other);
                    break;
            }
        }
        
        // Load date of birth
        if (user.getDateOfBirth() != null) {
            selectedDate.setTime(user.getDateOfBirth());
            etDateOfBirth.setText(dateFormat.format(user.getDateOfBirth()));
        }
        
        // Load height
        if (user.getHeight() != null) {
            etHeight.setText(String.valueOf(user.getHeight()));
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                
                etDateOfBirth.setText(dateFormat.format(selectedDate.getTime()));
                tilDateOfBirth.setError(null);
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set constraints
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -100);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -10);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        
        datePickerDialog.show();
    }

    private void saveProfile() {
        if (!validateForm()) {
            return;
        }
        
        String fullName = etFullName.getText().toString().trim();
        String gender = getSelectedGender();
        String dateOfBirth = etDateOfBirth.getText().toString().trim();
        String height = etHeight.getText().toString().trim();
        
        viewModel.updateProfile(fullName, gender, dateOfBirth, height, new EditProfileViewModel.SaveCallback() {
            @Override
            public void onSuccess() {
                // Success handling is done in observeViewModel()
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(EditProfileActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;
        
        // Clear previous errors
        tilFullName.setError(null);
        tilDateOfBirth.setError(null);
        tilHeight.setError(null);
        
        String fullName = etFullName.getText().toString().trim();
        String dateOfBirth = etDateOfBirth.getText().toString().trim();
        String height = etHeight.getText().toString().trim();
        
        // Validate name
        if (!viewModel.isValidName(fullName)) {
            tilFullName.setError("Tên phải có ít nhất 2 ký tự");
            isValid = false;
        }
        
        // Validate gender
        if (rgGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        // Validate date of birth
        if (!viewModel.isValidDateOfBirth(dateOfBirth)) {
            tilDateOfBirth.setError("Vui lòng chọn ngày sinh");
            isValid = false;
        }
        
        // Validate height (optional but if provided must be valid)
        if (!viewModel.isValidHeightString(height)) {
            tilHeight.setError("Chiều cao không hợp lệ (50-250 cm)");
            isValid = false;
        }
        
        return isValid;
    }

    private String getSelectedGender() {
        int selectedId = rgGender.getCheckedRadioButtonId();
        if (selectedId == R.id.rb_male) {
            return "Nam";
        } else if (selectedId == R.id.rb_female) {
            return "Nữ";
        } else if (selectedId == R.id.rb_other) {
            return "Khác";
        }
        return "";
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
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    // Input filter for names - only letters and spaces
    private static class NameInputFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                 android.text.Spanned dest, int dstart, int dend) {
            StringBuilder builder = new StringBuilder();
            
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                // Allow letters (both Vietnamese and English), spaces
                if (Character.isLetter(c) || Character.isWhitespace(c)) {
                    builder.append(c);
                }
            }
            
            boolean allValid = builder.length() == (end - start);
            return allValid ? null : builder.toString();
        }
    }

    // Input filter for decimal numbers
    private static class DecimalInputFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, 
                                 android.text.Spanned dest, int dstart, int dend) {
            StringBuilder builder = new StringBuilder();
            String existing = dest.toString();
            boolean hasDecimal = existing.contains(".");
            
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (Character.isDigit(c)) {
                    builder.append(c);
                } else if (c == '.' && !hasDecimal) {
                    builder.append(c);
                    hasDecimal = true;
                }
            }
            
            boolean allValid = builder.length() == (end - start);
            return allValid ? null : builder.toString();
        }
    }
}