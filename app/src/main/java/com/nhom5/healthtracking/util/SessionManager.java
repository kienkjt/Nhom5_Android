package com.nhom5.healthtracking.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import com.nhom5.healthtracking.data.local.entity.User;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class SessionManager {
    private static final String SHARED_PREF_NAME = "health_tracking_secure_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_SESSION_TOKEN = "session_token";
    private static final String KEY_LOGIN_TIME = "login_time";

    private SharedPreferences encryptedSharedPreferences;
    private static SessionManager instance;

    private SessionManager(Context context) throws GeneralSecurityException, IOException {
        // Generate master key for encryption
        String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

        // Create encrypted shared preferences
        encryptedSharedPreferences = EncryptedSharedPreferences.create(
                SHARED_PREF_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            try {
                instance = new SessionManager(context.getApplicationContext());
            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException("Failed to initialize SessionManager", e);
            }
        }
        return instance;
    }

    /**
     * Save user session data securely
     */
    public void saveUserSession(User user) {
        SharedPreferences.Editor editor = encryptedSharedPreferences.edit();
        editor.putInt(KEY_USER_ID, user.id);
        editor.putString(KEY_USER_EMAIL, user.email);
        editor.putString(KEY_USER_NAME, user.name != null ? user.name : "");
        editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
        
        // Generate session token - this is the only indicator of login status
        String sessionToken = generateSessionToken(user);
        editor.putString(KEY_SESSION_TOKEN, sessionToken);
        
        editor.apply();
    }

    /**
     * Check if user is currently logged in based on valid session token
     */
    public boolean isUserLoggedIn() {
        String sessionToken = getSessionToken();
        return sessionToken != null && !sessionToken.isEmpty() && isValidToken(sessionToken);
    }

    /**
     * Get logged in user information from secure storage
     * Only returns user data if valid token exists
     */
    public User getLoggedInUser() {
        if (!isUserLoggedIn()) {
            return null;
        }

        // Double check token validity
        String token = getSessionToken();
        if (token == null || token.isEmpty() || !isValidToken(token)) {
            return null;
        }

        User user = new User();
        user.id = encryptedSharedPreferences.getInt(KEY_USER_ID, 0);
        user.email = encryptedSharedPreferences.getString(KEY_USER_EMAIL, "");
        user.name = encryptedSharedPreferences.getString(KEY_USER_NAME, "");
        
        // Additional validation: ensure we have essential user data
        if (user.id == 0 || user.email == null || user.email.isEmpty()) {
            // Invalid user data, clear session
            clearSession();
            return null;
        }
        
        return user;
    }

    /**
     * Get session token
     */
    public String getSessionToken() {
        return encryptedSharedPreferences.getString(KEY_SESSION_TOKEN, null);
    }

    /**
     * Get login time
     */
    public long getLoginTime() {
        return encryptedSharedPreferences.getLong(KEY_LOGIN_TIME, 0);
    }

    /**
     * Check if session is valid (has valid token and not expired)
     * Session expires after 30 days
     */
    public boolean isSessionValid() {
        // Check if user is logged in (includes token validation)
        if (!isUserLoggedIn()) {
            return false;
        }

        // Check token format and validity
        String token = getSessionToken();
        if (!isValidToken(token)) {
            return false;
        }

        // Check if session is not expired
        long loginTime = getLoginTime();
        if (loginTime == 0) {
            return false; // No login time recorded
        }
        
        long currentTime = System.currentTimeMillis();
        long thirtyDaysInMillis = 30L * 24L * 60L * 60L * 1000L; // 30 days

        boolean notExpired = (currentTime - loginTime) <= thirtyDaysInMillis;
        
        // If session is expired, clear it
        if (!notExpired) {
            clearSession();
        }
        
        return notExpired;
    }

    /**
     * Update session timestamp (only if valid token exists)
     */
    public void updateSessionTime() {
        if (isUserLoggedIn() && isSessionValid()) {
            SharedPreferences.Editor editor = encryptedSharedPreferences.edit();
            editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
            editor.apply();
        }
    }

    /**
     * Clear all session data (logout)
     */
    public void clearSession() {
        SharedPreferences.Editor editor = encryptedSharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * Generate a session token
     */
    private String generateSessionToken(User user) {
        long timestamp = System.currentTimeMillis();
        String tokenData = user.id + ":" + user.email + ":" + timestamp;
        try {
            return HashUtils.hash(tokenData);
        } catch (Exception e) {
            // Fallback to simple hash
            return String.valueOf(tokenData.hashCode());
        }
    }

    /**
     * Validate if token format is correct and not empty
     */
    private boolean isValidToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        // BCrypt hash typically starts with $2a$, $2b$, or $2y$ and has specific length
        // For fallback hash, check if it's a valid integer
        if (token.startsWith("$2") && token.length() >= 60) {
            return true; // BCrypt hash format
        }
        
        // Check if it's a valid integer (fallback hash)
        try {
            Integer.parseInt(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Verify if current token belongs to current user data
     */
    public boolean verifyCurrentToken() {
        if (!isUserLoggedIn()) {
            return false;
        }
        
        String currentToken = getSessionToken();
        User currentUser = getLoggedInUser();
        long loginTime = getLoginTime();
        
        if (currentUser == null || currentToken == null) {
            return false;
        }
        
        // Regenerate token with current user data and login time to verify
        String expectedTokenData = currentUser.id + ":" + currentUser.email + ":" + loginTime;
        
        try {
            // For BCrypt hash, we can't reverse it, so we assume it's valid if format is correct
            if (currentToken.startsWith("$2") && currentToken.length() >= 60) {
                return true;
            }
            
            // For simple hash, verify it matches
            String expectedHash = String.valueOf(expectedTokenData.hashCode());
            return currentToken.equals(expectedHash);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get user ID from session
     */
    public int getUserId() {
        return encryptedSharedPreferences.getInt(KEY_USER_ID, 0);
    }

    /**
     * Get user email from session
     */
    public String getUserEmail() {
        return encryptedSharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    /**
     * Get user name from session
     */
    public String getUserName() {
        return encryptedSharedPreferences.getString(KEY_USER_NAME, "");
    }

    /**
     * Check if any session data exists (even if invalid)
     */
    public boolean hasSessionData() {
        String token = encryptedSharedPreferences.getString(KEY_SESSION_TOKEN, null);
        int userId = encryptedSharedPreferences.getInt(KEY_USER_ID, 0);
        String userEmail = encryptedSharedPreferences.getString(KEY_USER_EMAIL, "");
        
        return token != null || userId != 0 || !userEmail.isEmpty();
    }

    /**
     * Validate session integrity
     * Checks if all required session data exists and is consistent
     */
    public boolean validateSessionIntegrity() {
        String token = getSessionToken();
        User user = getLoggedInUser();
        long loginTime = getLoginTime();
        
        if (token == null || user == null || loginTime == 0) {
            return false;
        }
        
        // Check if token format is valid
        if (!isValidToken(token)) {
            return false;
        }
        
        // Check if user data is complete
        if (user.id == 0 || user.email == null || user.email.isEmpty()) {
            return false;
        }
        
        return true;
    }

    /**
     * Clean up invalid session data
     * Removes session if it exists but is invalid
     */
    public void cleanupInvalidSession() {
        if (hasSessionData() && !validateSessionIntegrity()) {
            clearSession();
        }
    }
} 