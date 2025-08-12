package com.nhom5.healthtracking.auth;

import android.app.Application;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.Task;
import com.nhom5.healthtracking.data.local.AppDatabase;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.data.repository.UserRepository;
import com.nhom5.healthtracking.constant.AuthConstant;
import com.nhom5.healthtracking.util.SessionManager;

public class RegisterTabViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    // LiveData for UI updates
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> registrationSuccess = new MutableLiveData<>(false);

    public RegisterTabViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getDatabase(application);
        this.userRepository = new UserRepository(database.userDao());
        this.sessionManager = SessionManager.getInstance(application);
    }

    public void registerUser(String email, String password, String confirmPassword, boolean acceptedTerms) {
        String validationError = validateInputs(email, password, confirmPassword, acceptedTerms);
        if (validationError != null) {
            setErrorMessage(validationError);
            return;
        }

        setLoading(true);
        performRegistration(userRepository.register(email, password));
    }


    public void registerWithGoogle(String idToken) {
        if (idToken == null || idToken.isEmpty()) {
            setErrorMessage("Google sign-in failed: Invalid token");
            return;
        }

        setLoading(true);
        performRegistration(userRepository.registerWithGoogle(idToken));
    }

    private void performRegistration(Task<User> registerTask) {
        registerTask.addOnSuccessListener(user -> {
                    setLoading(false);
                    setRegistrationSuccess(true);
                    setErrorMessage(null);
                    sessionManager.saveUserSession(user);
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

    public void setLoading(boolean value) {
        isLoading.postValue(value);
    }

    public void setErrorMessage(String value) {
        errorMessage.postValue(value);
    }

    public void setRegistrationSuccess(boolean value) {
        registrationSuccess.postValue(value);
    }

    // Getters for LiveData
    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<Boolean> getRegistrationSuccess() {
        return registrationSuccess;
    }

    // Clear error message
    public void clearError() {
        setErrorMessage(null);
    }
}
