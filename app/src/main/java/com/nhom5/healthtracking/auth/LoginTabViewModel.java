package com.nhom5.healthtracking.auth;

import android.app.Application;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.nhom5.healthtracking.data.local.AppDatabase;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.data.repository.UserRepository;
import com.nhom5.healthtracking.util.SessionManager;

public class LoginTabViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    // LiveData for UI updates
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>(false);

    public LoginTabViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getDatabase(application);
        this.userRepository = new UserRepository(database.userDao());
        this.sessionManager = SessionManager.getInstance(application);
    }

    public void loginUser(String email, String password) {
        String validationError = validateInputs(email, password);
        if (validationError != null) {
            setErrorMessage(validationError);
            return;
        }

        setLoading(true);
        performLogin(userRepository.login(email, password));
    }

    public void loginWithGoogle(String idToken) {
        if (idToken == null || idToken.isEmpty()) {
            setErrorMessage("Google sign-in failed: Invalid token");
            return;
        }

        setLoading(true);
        performLogin(userRepository.loginWithGoogle(idToken));
    }

    private void performLogin(Task<User> loginTask) {
        loginTask.addOnSuccessListener(user -> {
                    setLoading(false);
                    setLoginSuccess(true);
                    sessionManager.saveUserSession(user);
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

    public boolean isUserLoggedIn() {
        return sessionManager.isUserLoggedIn() && sessionManager.isSessionValid();
    }

    private void setLoading(boolean value) {
        isLoading.postValue(value);
    }

    public void setLoginSuccess(boolean value) {
        loginSuccess.postValue(value);
    }

    public void setErrorMessage(String value) {
        errorMessage.postValue(value);
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public void clearError() {
        setErrorMessage(null);
    }
}
