package com.nhom5.healthtracking.auth;

import android.app.Application;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nhom5.healthtracking.HealthTrackingApp;
import com.nhom5.healthtracking.data.repository.AuthRepository;
import com.nhom5.healthtracking.util.AuthState;

public class LoginTabViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final LiveData<AuthState> authState;

    // UI state
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LoginTabViewModel(@NonNull Application application) {
        super(application);
        HealthTrackingApp app = (HealthTrackingApp) application;
        this.authRepository = app.getAuthRepository();
        this.authState = app.getAuthState();
        
        // Initialize AuthRepository nếu chưa được init
        authRepository.init();
    }

    public void loginUser(String email, String password) {
        String validationError = validateInputs(email, password);
        if (validationError != null) {
            setErrorMessage(validationError);
            return;
        }

        setLoading(true);
        authRepository.login(email, password)
                .addOnSuccessListener(user -> {
                    setLoading(false);
                    // AuthState sẽ tự động update qua AuthStateListener
                    // UI sẽ observe AuthState để navigate
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    setErrorMessage(e.getMessage());
                });
    }

    public void loginWithGoogle(String idToken) {
        if (idToken == null || idToken.isEmpty()) {
            setErrorMessage("Google sign-in failed: Invalid token");
            return;
        }

        setLoading(true);
        authRepository.loginWithGoogle(idToken)
                .addOnSuccessListener(user -> {
                    setLoading(false);
                    // AuthState sẽ tự động update qua AuthStateListener
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    setErrorMessage(e.getMessage());
                });
    }

    private String validateInputs(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            return "Email is required";
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Please enter a valid email address";
        }

        if (password == null || password.trim().isEmpty()) {
            return "Password is required";
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
