package com.nhom5.healthtracking.onboarding.step_one;

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

public class PersonalInfoViewModel extends ViewModel {

    private static final Executor IO = Executors.newSingleThreadExecutor();
    private final MutableLiveData<String> fullName = new MutableLiveData<>();
    private final MutableLiveData<String> gender = new MutableLiveData<>();
    private final MutableLiveData<String> dateOfBirth = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFormValid = new MutableLiveData<>();
    private UserRepository userRepository;

    public PersonalInfoViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
        isFormValid.setValue(false);
    }

    public void savePersonalInfo(String fullName, String gender, String dateOfBirth, SaveCallback callback) {
        this.fullName.setValue(fullName);
        this.gender.setValue(gender);
        this.dateOfBirth.setValue(dateOfBirth);
        validateForm();

        FirebaseUser firebaseUser = FirebaseModule.auth().getCurrentUser();
        if (firebaseUser == null) {
            callback.onError("User not authenticated");
            return;
        }

        if (userRepository == null) {
            callback.onError("UserRepository not initialized");
            return;
        }

        IO.execute(() -> {
            try {
                // Get current user from database
                User currentUser = userRepository.getCurrentUser();
                if (currentUser == null) {
                    callback.onError("User not found in database");
                    return;
                }

                // Update user information
                currentUser.name = fullName;
                currentUser.gender = gender;
                currentUser.dateOfBirth = Formatter.stringToDate(dateOfBirth, "dd/MM/yyyy");
                currentUser.onboardingStep = 2L; // Move to step 2
                currentUser.updatedAt = new Date();
                currentUser.isSynced = false; // Mark as not synced to trigger Firebase sync

                // Save updated user
                userRepository.upsertSync(currentUser);

                // Call success on main thread
                callback.onSuccess();

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    private void validateForm() {
        String name = fullName.getValue();
        String genderValue = gender.getValue();
        String dob = dateOfBirth.getValue();

        boolean isValid = name != null && !name.trim().isEmpty() && genderValue != null && !genderValue.trim().isEmpty() && dob != null && !dob.trim().isEmpty();

        isFormValid.setValue(isValid);
    }

    public interface SaveCallback {
        void onSuccess();

        void onError(String error);
    }
}