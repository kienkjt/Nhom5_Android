package com.nhom5.healthtracking.auth;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.nhom5.healthtracking.data.local.AppDatabase;
import com.nhom5.healthtracking.data.repository.UserRepository;

public class LoginTabViewModel extends AndroidViewModel {
    private final UserRepository userRepository;

    public LoginTabViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getDatabase(application);
        this.userRepository = new UserRepository(database.userDao());
    }

    public LoginTabViewModel(Application application, UserRepository userRepository) {
        super(application);
        this.userRepository = userRepository;
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

    public UserRepository getUserRepository() {
        return userRepository;
    }
}
