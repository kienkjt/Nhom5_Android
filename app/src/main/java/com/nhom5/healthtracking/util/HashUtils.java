package com.nhom5.healthtracking.util;

import java.security.NoSuchAlgorithmException;
import org.mindrot.jbcrypt.BCrypt;

public class HashUtils {
  public static String hashPassword(String password) throws NoSuchAlgorithmException {
    return BCrypt.hashpw(password, BCrypt.gensalt());
  }

  public static boolean verifyPassword(String password, String hashedPassword) {
    return BCrypt.checkpw(password, hashedPassword);
  }
}
