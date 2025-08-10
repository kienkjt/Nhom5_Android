package com.nhom5.healthtracking.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.nhom5.healthtracking.MainActivity;
import com.nhom5.healthtracking.R;

public class RegisterTabFragment extends Fragment {

    TextInputEditText emailEditText, passwordEditText, confirmPasswordEditText;
    CheckBox termsCheckBox;
    Button registerButton;
    RegisterTabViewModel mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_register_tab, container, false);

        initViews(root);
        animateViews();
        setupClickListeners();

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RegisterTabViewModel.Factory factory = new RegisterTabViewModel.Factory(requireActivity().getApplication());
        mViewModel = new ViewModelProvider(this, factory).get(RegisterTabViewModel.class);
        
        observeViewModel();
    }

    void initViews(ViewGroup root) {
        emailEditText = root.findViewById(R.id.email_edit_text);
        passwordEditText = root.findViewById(R.id.password_edit_text);
        confirmPasswordEditText = root.findViewById(R.id.confirm_password_edit_text);
        termsCheckBox = root.findViewById(R.id.terms_checkbox);
        registerButton = root.findViewById(R.id.register_button);
    }

    void setupClickListeners() {
        registerButton.setOnClickListener(v -> performRegistration());
    }

    void observeViewModel() {
        // Observe loading state
        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                registerButton.setEnabled(!isLoading);
                registerButton.setText(isLoading ? "Registering..." : "Register");
            }
        });

        // Observe error messages
        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                mViewModel.clearError(); // Clear error after showing
            }
        });

        // Observe registration success
        mViewModel.getRegistrationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                clearForm();
                navigateToMainActivity();
            }
        });
    }

    void performRegistration() {
        String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
        String password = passwordEditText.getText() != null ? passwordEditText.getText().toString() : "";
        String confirmPassword = confirmPasswordEditText.getText() != null ? confirmPasswordEditText.getText().toString() : "";
        boolean acceptedTerms = termsCheckBox.isChecked();

        mViewModel.registerUser(email, password, confirmPassword, acceptedTerms);
    }

    void clearForm() {
        emailEditText.setText("");
        passwordEditText.setText("");
        confirmPasswordEditText.setText("");
        termsCheckBox.setChecked(false);
    }

    void animateViews() {
        float translationX = -800;
        View[] views = {
                emailEditText,
                passwordEditText,
                confirmPasswordEditText,
                termsCheckBox,
                registerButton
        };
        for (View view : views) {
            view.setTranslationX(translationX);
            view.setAlpha(0);
        }
        int delay = 300;
        for (View view : views) {
            view.animate()
                    .translationX(0)
                    .alpha(1)
                    .setDuration(800)
                    .setStartDelay(delay)
                    .start();
            delay += 100;
        }
    }

    void navigateToMainActivity() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        // Finish the auth activity
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
