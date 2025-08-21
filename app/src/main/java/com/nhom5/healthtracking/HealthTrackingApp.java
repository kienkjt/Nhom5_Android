package com.nhom5.healthtracking;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.nhom5.healthtracking.data.local.AppDatabase;
import com.nhom5.healthtracking.data.local.dao.UserDao;
import com.nhom5.healthtracking.data.local.dao.WeightRecordDao;
import com.nhom5.healthtracking.data.repository.AuthRepository;
import com.nhom5.healthtracking.data.repository.UserRepository;
import com.nhom5.healthtracking.data.repository.WeightRecordRepository;
import com.nhom5.healthtracking.util.AuthState;

public class HealthTrackingApp extends Application {
    private AuthRepository authRepository;
    private UserRepository userRepository;
    private WeightRecordRepository weightRecordRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        AppDatabase database = AppDatabase.getDatabase(this);
        UserDao userDao = database.userDao();
        WeightRecordDao weightRecordDao = database.weightRecordDao();
        
        userRepository = UserRepository.getInstance(userDao);
        weightRecordRepository = new WeightRecordRepository(weightRecordDao);
        authRepository = AuthRepository
                .create(
                        FirebaseAuth.getInstance(),
                        userRepository
                );
    }

    public AuthRepository getAuthRepository() {
        return authRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public WeightRecordRepository getWeightRecordRepository() {
        return weightRecordRepository;
    }

    public LiveData<AuthState> getAuthState() {
        return authRepository.getAuthState();
    }
}