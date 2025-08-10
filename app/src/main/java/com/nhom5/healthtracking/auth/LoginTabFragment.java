package com.nhom5.healthtracking.auth;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.nhom5.healthtracking.MainActivity;
import com.nhom5.healthtracking.R;

public class LoginTabFragment extends Fragment {

    TextInputEditText emailEditText, passwordEditText;
    TextView forgotPasswordTextView;
    Button loginButton;
    LoginTabViewModel mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_login_tab, container, false);

        initViews(root);
        animateViews();
        setupClickListeners();

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LoginTabViewModel.Factory factory = new LoginTabViewModel.Factory(requireActivity().getApplication());
        mViewModel = new ViewModelProvider(this, factory).get(LoginTabViewModel.class);
        
        observeViewModel();
        checkIfAlreadyLoggedIn();
    }

    void initViews(ViewGroup root) {
        emailEditText = root.findViewById(R.id.email_edit_text);
        passwordEditText = root.findViewById(R.id.password_edit_text);
        loginButton = root.findViewById(R.id.login_button);
        forgotPasswordTextView = root.findViewById(R.id.forgot_password_text);
    }

    void setupClickListeners() {
        loginButton.setOnClickListener(v -> performLogin());
        
        forgotPasswordTextView.setOnClickListener(v -> {
            // Handle forgot password - for now just show a message
            Toast.makeText(getContext(), "Forgot password functionality will be implemented soon", Toast.LENGTH_SHORT).show();
        });
    }

    void observeViewModel() {
        // Observe loading state
        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                loginButton.setEnabled(!isLoading);
                loginButton.setText(isLoading ? "Logging in..." : "Login");
            }
        });

        // Observe error messages
        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                mViewModel.clearError(); // Clear error after showing
            }
        });

        // Observe login success
        mViewModel.getLoginSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                navigateToMainActivity();
            }
        });

        // Observe logged in user
        mViewModel.getLoggedInUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                String welcomeMessage = "Welcome " + (user.name != null && !user.name.isEmpty() ? user.name : user.email) + "!";
                Toast.makeText(getContext(), welcomeMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    void checkIfAlreadyLoggedIn() {
        // Check if user is already logged in
        if (mViewModel.isUserLoggedIn()) {
            navigateToMainActivity();
        }
    }

    void performLogin() {
        String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
        String password = passwordEditText.getText() != null ? passwordEditText.getText().toString() : "";

        mViewModel.loginUser(email, password);
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

    void clearForm() {
        emailEditText.setText("");
        passwordEditText.setText("");
    }

    void animateViews() {
        float translationX = 800;
        View[] views = {
                emailEditText,
                passwordEditText,
                forgotPasswordTextView,
                loginButton
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
}
