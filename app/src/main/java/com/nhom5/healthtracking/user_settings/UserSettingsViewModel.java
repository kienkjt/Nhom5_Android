package com.nhom5.healthtracking.user_settings;

import com.nhom5.healthtracking.data.repository.AuthRepository;
import com.nhom5.healthtracking.data.repository.UserRepository;

public class UserSettingsViewModel {
  private final UserRepository userRepository;
  private final AuthRepository authRepository;

  public UserSettingsViewModel(UserRepository userRepository, AuthRepository authRepository) {
    this.userRepository = userRepository;
    this.authRepository = authRepository;
  }

  public void logout() {
    authRepository.logout();
  }
}
