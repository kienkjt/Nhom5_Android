package com.nhom5.healthtracking.onboarding.step_two;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.data.repository.UserRepository;
import com.nhom5.healthtracking.data.repository.WeightRecordRepository;
import com.nhom5.healthtracking.util.FirebaseModule;

import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BodyMetricsViewModel extends ViewModel {

    private static final Executor IO = Executors.newSingleThreadExecutor();
    private final MutableLiveData<String> height = new MutableLiveData<>();
    private final MutableLiveData<String> weight = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFormValid = new MutableLiveData<>();
    private final UserRepository userRepository;
    private final WeightRecordRepository weightRecordRepository;

    public BodyMetricsViewModel(UserRepository userRepository, WeightRecordRepository weightRecordRepository) {
        this.userRepository = userRepository;
        this.weightRecordRepository = weightRecordRepository;
        isFormValid.setValue(false);
    }

    public void saveBodyMetrics(String height, String weight, SaveCallback callback) {
        this.height.setValue(height);
        this.weight.setValue(weight);

        FirebaseUser firebaseUser = FirebaseModule.auth().getCurrentUser();
        if (firebaseUser == null) {
            callback.onError("User not authenticated");
            return;
        }

        if (userRepository == null || weightRecordRepository == null) {
            callback.onError("Repositories not initialized");
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

                // Parse and validate height
                double heightValue = Double.parseDouble(height.trim());
                if (heightValue < 50 || heightValue > 250) {
                    callback.onError("Invalid height range");
                    return;
                }

                // Parse and validate weight
                double weightValue = Double.parseDouble(weight.trim());
                if (weightValue < 20 || weightValue > 300) {
                    callback.onError("Invalid weight range");
                    return;
                }

                // Save height to User (convert cm to Long for storage)
                currentUser.height = (long) heightValue;
                currentUser.onboardingStep = 3L;
                currentUser.updatedAt = new Date();
                currentUser.isSynced = false;
                userRepository.upsertSync(currentUser);

                // Save weight record (keep as double)
                weightRecordRepository.insert(firebaseUser.getUid(), weightValue, "Onboarding weight record");

                // Call success on main thread
                callback.onSuccess();
            } catch (NumberFormatException e) {
                callback.onError("Invalid number format");
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    private void validateForm() {
        String heightValue = height.getValue();
        String weightValue = weight.getValue();

        boolean isValid = heightValue != null && !heightValue.trim().isEmpty() && weightValue != null && !weightValue.trim().isEmpty();

        if (isValid) {
            try {
                double h = Double.parseDouble(heightValue.trim());
                double w = Double.parseDouble(weightValue.trim());
                isValid = h >= 50 && h <= 250 && w >= 20 && w <= 300;
            } catch (NumberFormatException e) {
                isValid = false;
            }
        }

        isFormValid.setValue(isValid);
    }

    public boolean isValidHeight(String heightStr) {
        try {
            if (heightStr == null || heightStr.trim().isEmpty()) {
                return false;
            }
            double height = Double.parseDouble(heightStr.trim());
            return height >= 50 && height <= 250;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isValidWeight(String weightStr) {
        try {
            if (weightStr == null || weightStr.trim().isEmpty()) {
                return false;
            }
            double weight = Double.parseDouble(weightStr.trim());
            return weight >= 20 && weight <= 300;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Getters
    public MutableLiveData<String> getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height.setValue(height);
        validateForm();
    }

    public MutableLiveData<String> getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight.setValue(weight);
        validateForm();
    }

    public MutableLiveData<Boolean> getIsFormValid() {
        return isFormValid;
    }

    public interface SaveCallback {
        void onSuccess();

        void onError(String error);
    }
}