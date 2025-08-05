package com.nhom5.healthtracking.util;

import java.security.NoSuchAlgorithmException;
import org.mindrot.jbcrypt.BCrypt;

public class HashUtils {
  private static final int SALT_ROUNDS = 12;

  public static String hash(String password) throws NoSuchAlgorithmException {
    return BCrypt.hashpw(password, BCrypt.gensalt(SALT_ROUNDS));
  }

  public static boolean verify(String password, String hashedPassword) {
    return BCrypt.checkpw(password, hashedPassword);
  }
}
