package com.nhom5.healthtracking.constant;

public class AuthConstant {
  /**
   * Password pattern:
   * - At least 8 characters
   * - At least 1 uppercase letter
   * - At least 1 lowercase letter
   * - At least 1 number
   * - At least 1 special character
   * - No whitespace
   */
  public static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
  
  /**
   * SharedPreferences key for is logged in
   */
  public static final String SP_IS_LOGGED_IN_KEY = "is_logged_in";
}
