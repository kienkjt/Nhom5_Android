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

public class LoginTabViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final SharedPreferences sharedPreferences;
    
    // LiveData for UI updates
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>(false);
    private MutableLiveData<User> loggedInUser = new MutableLiveData<>();

    public LoginTabViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getDatabase(application);
        this.userRepository = new UserRepository(database.userDao());
        this.sharedPreferences = application.getSharedPreferences("HealthTracking", Context.MODE_PRIVATE);
    }

    public LoginTabViewModel(Application application, UserRepository userRepository) {
        super(application);
        this.userRepository = userRepository;
        this.sharedPreferences = application.getSharedPreferences("HealthTracking", Context.MODE_PRIVATE);
    }

    // Login user
    public void loginUser(String email, String password) {
        // Validate inputs
        String validationError = validateInputs(email, password);
        if (validationError != null) {
            errorMessage.setValue(validationError);
            return;
        }

        isLoading.setValue(true);
        
        // Execute login in background thread
        new LoginUserTask().execute(email, password);
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

    // AsyncTask for authentication
    private class LoginUserTask extends AsyncTask<String, Void, User> {
        private String errorMsg = null;

        @Override
        protected User doInBackground(String... params) {
            try {
                String email = params[0];
                String password = params[1];
                
                // Authenticate user
                User user = userRepository.authenticate(email, password);
                
                if (user == null) {
                    errorMsg = "Invalid email or password";
                }
                
                return user;
            } catch (Exception e) {
                errorMsg = "Login failed: " + e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            isLoading.setValue(false);
            
            if (user != null) {
                // Save user session
                saveUserSession(user);
                
                loggedInUser.setValue(user);
                loginSuccess.setValue(true);
                errorMessage.setValue(null);
            } else {
                errorMessage.setValue(errorMsg);
                loginSuccess.setValue(false);
            }
        }
    }

    // Save user session to SharedPreferences
    private void saveUserSession(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("user_id", user.id);
        editor.putString("user_email", user.email);
        editor.putString("user_name", user.name != null ? user.name : "");
        editor.putBoolean(AuthConstant.SP_IS_LOGGED_IN_KEY, true);
        editor.apply();
    }

    // Check if user is already logged in
    public boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean(AuthConstant.SP_IS_LOGGED_IN_KEY, false);
    }

    // Get logged in user info from SharedPreferences
    public User getLoggedInUserFromPrefs() {
        if (!isUserLoggedIn()) {
            return null;
        }
        
        User user = new User();
        user.id = sharedPreferences.getInt("user_id", 0);
        user.email = sharedPreferences.getString("user_email", "");
        user.name = sharedPreferences.getString("user_name", "");
        return user;
    }

    // Logout user
    public void logoutUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        
        loggedInUser.setValue(null);
        loginSuccess.setValue(false);
    }

    // Getters for LiveData
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

    // Clear error message
    public void clearError() {
        errorMessage.setValue(null);
    }

    // Factory class for creating LoginTabViewModel with dependencies
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
