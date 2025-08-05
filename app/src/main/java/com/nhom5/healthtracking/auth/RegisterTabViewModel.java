package com.nhom5.healthtracking.auth;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Patterns;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.nhom5.healthtracking.data.local.AppDatabase;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.data.repository.UserRepository;

public class RegisterTabViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    
    // LiveData for UI updates
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> registrationSuccess = new MutableLiveData<>(false);

    public RegisterTabViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getDatabase(application);
        this.userRepository = new UserRepository(database.userDao());
    }

    public RegisterTabViewModel(Application application, UserRepository userRepository) {
        super(application);
        this.userRepository = userRepository;
    }

    // Register new user
    public void registerUser(String email, String password, String confirmPassword, boolean acceptedTerms) {
        // Validate inputs
        String validationError = validateInputs(email, password, confirmPassword, acceptedTerms);
        if (validationError != null) {
            errorMessage.setValue(validationError);
            return;
        }

        isLoading.setValue(true);
        
        // Execute registration in background thread
        new RegisterUserTask().execute(email, password);
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
        
        if (password.length() < 6) {
            return "Password must be at least 6 characters";
        }
        
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match";
        }
        
        if (!acceptedTerms) {
            return "Please accept the Terms and Conditions";
        }
        
        return null; // No validation errors
    }

    // AsyncTask for database operations
    private class RegisterUserTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String email = params[0];
                String password = params[1];
                
                // Check if email already exists
                if (userRepository.checkEmailExists(email)) {
                    return "Email already registered";
                }
                
                // Create and insert new user
                User newUser = new User(email, password);
                userRepository.insert(newUser);
                
                return "SUCCESS";
            } catch (Exception e) {
                return "Registration failed: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            isLoading.setValue(false);
            
            if ("SUCCESS".equals(result)) {
                registrationSuccess.setValue(true);
                errorMessage.setValue(null);
            } else {
                errorMessage.setValue(result);
                registrationSuccess.setValue(false);
            }
        }
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

    public UserRepository getUserRepository() {
        return userRepository;
    }

    // Clear error message
    public void clearError() {
        errorMessage.setValue(null);
    }

    // Factory class for creating RegisterTabViewModel with dependencies
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
            if (modelClass.isAssignableFrom(RegisterTabViewModel.class)) {
                if (userRepository != null) {
                    return (T) new RegisterTabViewModel(application, userRepository);
                } else {
                    return (T) new RegisterTabViewModel(application);
                }
            }
            return super.create(modelClass);
        }
    }
}
