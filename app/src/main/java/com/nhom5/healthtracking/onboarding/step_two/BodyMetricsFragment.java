package com.nhom5.healthtracking.onboarding.step_two;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import com.nhom5.healthtracking.data.repository.UserRepository;
import com.nhom5.healthtracking.data.repository.WeightRecordRepository;
import com.nhom5.healthtracking.onboarding.OnboardingActivity;
import com.nhom5.healthtracking.util.AuthState;

public class BodyMetricsFragment extends Fragment {

    private BodyMetricsViewModel mViewModel;


    private TextView tvStep;
    private TextInputLayout tilHeight;
    private TextInputEditText etHeight;
    private TextInputLayout tilWeight;
    private TextInputEditText etWeight;
    private MaterialButton btnContinue;
    private MaterialButton btnPrevious;

    public static BodyMetricsFragment newInstance() {
        return new BodyMetricsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_body_metrics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        HealthTrackingApp app = (HealthTrackingApp) requireActivity().getApplication();
        UserRepository userRepository = app.getUserRepository();
        WeightRecordRepository weightRecordRepository = app.getWeightRecordRepository();
        mViewModel = new BodyMetricsViewModel(userRepository, weightRecordRepository);

        setupListeners();

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
        btnPrevious = view.findViewById(R.id.btn_skip);

        setInputFilters();
    }

    private void setInputFilters() {
        InputFilter heightFilter = new InputFilter.LengthFilter(3);
        InputFilter[] heightFilters = {heightFilter, new NumericInputFilter()};
        etHeight.setFilters(heightFilters);

        InputFilter weightFilter = new InputFilter.LengthFilter(5);
        InputFilter[] weightFilters = {weightFilter, new DecimalInputFilter()};
        etWeight.setFilters(weightFilters);
    }

    private void loadSavedData() {
        HealthTrackingApp app = (HealthTrackingApp) requireActivity().getApplication();
        app.getAuthState().observe(getViewLifecycleOwner(), authState -> {
            if (authState.isAuthenticated()) {
                User user = ((AuthState.Authenticated) authState).getProfile();

                if (user != null && user.getHeight() != null) {
                    etHeight.setText(String.valueOf(user.getHeight()));
                    mViewModel.setHeight(String.valueOf(user.getHeight()));
                }
            }
        });
    }

    private void setupListeners() {
        etHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mViewModel.setHeight(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mViewModel.setWeight(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        btnContinue.setOnClickListener(v -> {
            if (validateAndProceed()) {
                saveDataAndProceed();
            }
        });


        btnPrevious.setOnClickListener(v -> {
            if (getActivity() instanceof OnboardingActivity) {
                ((OnboardingActivity) getActivity()).goToPreviousStep();
            }
        });
    }

    private boolean validateAndProceed() {
        boolean isValid = true;

        tilHeight.setError(null);
        tilWeight.setError(null);

        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();

        if (heightStr.isEmpty()) {
            tilHeight.setError(getString(R.string.error_height_required));
            isValid = false;
        } else if (!mViewModel.isValidHeight(heightStr)) {
            tilHeight.setError(getString(R.string.error_height_invalid));
            isValid = false;
        }


        if (weightStr.isEmpty()) {
            tilWeight.setError(getString(R.string.error_weight_required));
            isValid = false;
        } else if (!mViewModel.isValidWeight(weightStr)) {
            tilWeight.setError(getString(R.string.error_weight_invalid));
            isValid = false;
        }


        if (!isValid) {
            Toast.makeText(getContext(), "Vui lòng kiểm tra thông tin nhập và thử lại", Toast.LENGTH_SHORT).show();
        }

        return isValid;
    }

    private void saveDataAndProceed() {
        String height = etHeight.getText().toString().trim();
        String weight = etWeight.getText().toString().trim();


        mViewModel.saveBodyMetrics(height, weight, new BodyMetricsViewModel.SaveCallback() {
            @Override
            public void onSuccess() {

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Đã lưu thông tin cơ thể thành công!", Toast.LENGTH_SHORT).show();
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

    private static class NumericInputFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, android.text.Spanned dest, int dstart, int dend) {
            StringBuilder builder = new StringBuilder();
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (Character.isDigit(c)) {
                    builder.append(c);
                }
            }

            boolean allValid = builder.length() == (end - start);
            return allValid ? null : builder.toString();
        }
    }

    private static class DecimalInputFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, android.text.Spanned dest, int dstart, int dend) {
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