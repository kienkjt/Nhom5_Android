package com.nhom5.healthtracking.constant;

public class AuthConstant {
  /**
   * Password pattern: ^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$
   * - At least 8 characters
   * - At least 1 letter
   * - At least 1 number
   * - No whitespace
   */
  public static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";
}