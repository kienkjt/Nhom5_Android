package com.nhom5.healthtracking.user_settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.data.repository.UserRepository;
import com.nhom5.healthtracking.util.Formatter;
import com.nhom5.healthtracking.util.FirebaseModule;

import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class EditProfileViewModel extends ViewModel {

    private static final Executor IO = Executors.newSingleThreadExecutor();
    private final UserRepository userRepository;
    
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSaveSuccessful = new MutableLiveData<>();

    public EditProfileViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
        isLoading.setValue(false);
        loadCurrentUser();
    }

    public void loadCurrentUser() {
        isLoading.setValue(true);
        
        FirebaseUser firebaseUser = FirebaseModule.auth().getCurrentUser();
        if (firebaseUser == null) {
            errorMessage.setValue("User not authenticated");
            isLoading.setValue(false);
            return;
        }

        IO.execute(() -> {
            try {
                User user = userRepository.getCurrentUser();
                currentUser.postValue(user);
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Error loading user data: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void updateProfile(String fullName, String gender, String dateOfBirth, String height, SaveCallback callback) {
        if (!validateInput(fullName, gender, dateOfBirth, height)) {
            callback.onError("Invalid input data");
            return;
        }

        isLoading.setValue(true);
        
        FirebaseUser firebaseUser = FirebaseModule.auth().getCurrentUser();
        if (firebaseUser == null) {
            callback.onError("User not authenticated");
            isLoading.setValue(false);
            return;
        }

        IO.execute(() -> {
            try {
                User user = userRepository.getCurrentUser();
                if (user == null) {
                    callback.onError("User not found in database");
                    return;
                }

                // Update user information
                user.name = fullName.trim();
                user.gender = gender;
                user.dateOfBirth = Formatter.stringToDate(dateOfBirth, "dd/MM/yyyy");
                
                // Update height if provided
                if (height != null && !height.trim().isEmpty()) {
                    try {
                        double heightValue = Double.parseDouble(height.trim());
                        if (isValidHeight(heightValue)) {
                            user.height = (long) heightValue;
                        } else {
                            callback.onError("Invalid height value");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        callback.onError("Invalid height format");
                        return;
                    }
                }
                
                user.updatedAt = new Date();
                user.isSynced = false; // Mark as not synced to trigger Firebase sync

                // Save updated user
                userRepository.upsertSync(user);
                
                // Update current user in ViewModel
                currentUser.postValue(user);
                isLoading.postValue(false);
                isSaveSuccessful.postValue(true);

                // Call success callback
                callback.onSuccess();

            } catch (Exception e) {
                isLoading.postValue(false);
                callback.onError("Error saving profile: " + e.getMessage());
            }
        });
    }

    private boolean validateInput(String fullName, String gender, String dateOfBirth, String height) {
        // Validate full name
        if (fullName == null || fullName.trim().isEmpty()) {
            errorMessage.setValue("Full name is required");
            return false;
        }
        
        if (fullName.trim().length() < 2) {
            errorMessage.setValue("Full name must be at least 2 characters");
            return false;
        }

        // Validate gender
        if (gender == null || gender.trim().isEmpty()) {
            errorMessage.setValue("Gender is required");
            return false;
        }

        // Validate date of birth
        if (dateOfBirth == null || dateOfBirth.trim().isEmpty()) {
            errorMessage.setValue("Date of birth is required");
            return false;
        }

        // Validate height if provided
        if (height != null && !height.trim().isEmpty()) {
            try {
                double heightValue = Double.parseDouble(height.trim());
                if (!isValidHeight(heightValue)) {
                    errorMessage.setValue("Height must be between 50 and 250 cm");
                    return false;
                }
            } catch (NumberFormatException e) {
                errorMessage.setValue("Invalid height format");
                return false;
            }
        }

        return true;
    }

    private boolean isValidHeight(double height) {
        return height >= 50 && height <= 250;
    }

    public boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2;
    }

    public boolean isValidGender(String gender) {
        return gender != null && !gender.trim().isEmpty();
    }

    public boolean isValidDateOfBirth(String dateOfBirth) {
        return dateOfBirth != null && !dateOfBirth.trim().isEmpty();
    }

    public boolean isValidHeightString(String heightStr) {
        if (heightStr == null || heightStr.trim().isEmpty()) {
            return true; // Height is optional
        }
        
        try {
            double height = Double.parseDouble(heightStr.trim());
            return isValidHeight(height);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Getters for LiveData
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsSaveSuccessful() {
        return isSaveSuccessful;
    }

    // Clear error message
    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }

    // Clear save success flag
    public void clearSaveSuccessFlag() {
        isSaveSuccessful.setValue(false);
    }

    public interface SaveCallback {
        void onSuccess();
        void onError(String error);
    }
}
