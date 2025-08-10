package com.nhom5.healthtracking.auth;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Patterns;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.nhom5.healthtracking.data.local.AppDatabase;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.data.repository.UserRepository;
import com.nhom5.healthtracking.constant.AuthConstant;
import com.nhom5.healthtracking.util.SessionManager;

public class LoginTabViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    
    // LiveData for UI updates
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>(false);
    private MutableLiveData<User> loggedInUser = new MutableLiveData<>();

    public LoginTabViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getDatabase(application);
        this.userRepository = new UserRepository(database.userDao());
        this.sessionManager = SessionManager.getInstance(application);
    }

    public LoginTabViewModel(Application application, UserRepository userRepository) {
        super(application);
        this.userRepository = userRepository;
        this.sessionManager = SessionManager.getInstance(application);
    }

    public void loginUser(String email, String password) {
        String validationError = validateInputs(email, password);
        if (validationError != null) {
            errorMessage.setValue(validationError);
            return;
        }

        isLoading.setValue(true);

        userRepository.login(email, password)
                .addOnSuccessListener(user -> {
                    isLoading.setValue(false);
                    loginSuccess.setValue(true);
                    errorMessage.setValue(null);
                    sessionManager.saveUserSession(user);
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(e.getMessage());
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

    public User getLoggedInUserFromPrefs() {
        if (!isUserLoggedIn()) {
            return null;
        }
        return sessionManager.getLoggedInUser();
    }

    public void logoutUser() {
        sessionManager.clearSession();
        
        loggedInUser.setValue(null);
        loginSuccess.setValue(false);
    }

    public boolean isSessionValid() {
        return sessionManager.isSessionValid();
    }

    public void refreshSession() {
        sessionManager.updateSessionTime();
    }

    public String getSessionToken() {
        return sessionManager.getSessionToken();
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

    public MutableLiveData<User> getLoggedInUser() {
        return loggedInUser;
    }

    public void clearError() {
        errorMessage.setValue(null);
    }

    public static class Factory extends ViewModelProvider.AndroidViewModelFactory {
        private final Application application;
        private UserRepository userRepository;

        public Factory(@NonNull Application application) {
            super(application);
            this.application = application;
        }

        public Factory(@NonNull Application application, UserRepository userRepository) {
            super(application);
            this.application = application;
            this.userRepository = userRepository;
        }

        @NonNull
        @Override
        public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(LoginTabViewModel.class)) {
                if (userRepository != null) {
                    return (T) new LoginTabViewModel(application, userRepository);
                } else {
                    return (T) new LoginTabViewModel(application);
                }
            }
            return super.create(modelClass);
        }
    }
}
