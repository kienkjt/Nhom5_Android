package com.nhom5.healthtracking.auth;

import android.app.Application;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.nhom5.healthtracking.HealthTrackingApp;
import com.nhom5.healthtracking.constant.AuthConstant;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.data.repository.AuthRepository;
import com.nhom5.healthtracking.util.AuthState;

public class RegisterTabViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final LiveData<AuthState> authState;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public RegisterTabViewModel(@NonNull Application application) {
        super(application);
        HealthTrackingApp app = (HealthTrackingApp) application;
        this.authRepository = app.getAuthRepository();
        this.authState = app.getAuthState();
    }

    public void registerUser(String email, String password, String confirmPassword, boolean acceptedTerms) {
        String validationError = validateInputs(email, password, confirmPassword, acceptedTerms);
        if (validationError != null) {
            setErrorMessage(validationError);
            return;
        }

        setLoading(true);
        authRepository.register(email, password)
                .addOnSuccessListener(user -> {
                    setLoading(false);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    setErrorMessage(e.getMessage());
                });
    }

    public void registerWithGoogle(String idToken) {
        if (idToken == null || idToken.isEmpty()) {
            setErrorMessage("Google sign-in failed: Invalid token");
            return;
        }

        setLoading(true);
        authRepository.registerWithGoogle(idToken)
                .addOnSuccessListener(user -> {
                    setLoading(false);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    setErrorMessage(e.getMessage());
                });
    }

    private String validateInputs(String email, String password, String confirmPassword, boolean acceptedTerms) {
        if (email == null || email.trim().isEmpty()) {
            return "Email is required";
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Please enter a valid email address";
        }

        if (password == null || password.trim().isEmpty()) {
            return "Password is required";
        }

        if (!password.matches(AuthConstant.PASSWORD_PATTERN)) {
            return "Password must be at least 8 characters and contain at least 1 uppercase letter, 1 lowercase letter, 1 number, 1 special character, and no whitespace";
        }

        if (!password.equals(confirmPassword)) {
            return "Passwords do not match";
        }

        if (!acceptedTerms) {
            return "Please accept the Terms and Conditions";
        }

        return null; // No validation errors
    }

    private void setLoading(boolean value) {
        isLoading.postValue(value);
    }

    public void setErrorMessage(String value) {
        errorMessage.postValue(value);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<AuthState> getAuthState() {
        return authState;
    }

    public void clearError() {
        setErrorMessage(null);
    }
}
