package com.nhom5.healthtracking;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.nhom5.healthtracking.data.local.AppDatabase;
import com.nhom5.healthtracking.data.local.dao.UserDao;
import com.nhom5.healthtracking.data.repository.AuthRepository;
import com.nhom5.healthtracking.data.repository.UserRepository;
import com.nhom5.healthtracking.util.AuthState;

public class HealthTrackingApp extends Application {
    private AuthRepository authRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        UserDao userDao = AppDatabase.getDatabase(this).userDao();

        authRepository = AuthRepository
                .create(
                        FirebaseAuth.getInstance(),
                        UserRepository.getInstance(userDao)
                );
    }

    public AuthRepository getAuthRepository() {
        return authRepository;
    }

    public LiveData<AuthState> getAuthState() {
        return authRepository.getAuthState();
    }
}