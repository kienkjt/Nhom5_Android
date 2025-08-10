package com.nhom5.healthtracking.util;

import android.content.Context;
import com.nhom5.healthtracking.data.local.entity.User;

/**
 * Helper class providing convenient methods for session management
 */
public class SessionHelper {
    
    private SessionManager sessionManager;
    
    public SessionHelper(Context context) {
        this.sessionManager = SessionManager.getInstance(context);
    }
    
    /**
     * Quick check if user is authenticated with valid token
     */
    public boolean isUserAuthenticated() {
        // Cleanup any invalid session first
        sessionManager.cleanupInvalidSession();
        return sessionManager.isUserLoggedIn() && sessionManager.isSessionValid();
    }
    
    /**
     * Get current logged in user if session is valid
     */
    public User getCurrentUser() {
        if (isUserAuthenticated()) {
            return sessionManager.getLoggedInUser();
        }
        return null;
    }
    
    /**
     * Get current user's email if available
     */
    public String getCurrentUserEmail() {
        User user = getCurrentUser();
        return user != null ? user.email : null;
    }
    
    /**
     * Get current user's name if available
     */
    public String getCurrentUserName() {
        User user = getCurrentUser();
        return user != null ? user.name : null;
    }
    
    /**
     * Get current user's ID if available
     */
    public String getCurrentUserUid() {
        User user = getCurrentUser();
        return user != null ? user.uid : null;
    }
    
    /**
     * Login user and save session
     */
    public void loginUser(User user) {
        sessionManager.saveUserSession(user);
    }
    
    /**
     * Logout current user
     */
    public void logoutUser() {
        sessionManager.clearSession();
    }
    
    /**
     * Refresh session timestamp
     */
    public void refreshSession() {
        sessionManager.updateSessionTime();
    }
    
    /**
     * Check if session is about to expire (within 24 hours)
     */
    public boolean isSessionNearExpiry() {
        if (!isUserAuthenticated()) {
            return false;
        }
        
        long loginTime = sessionManager.getLoginTime();
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - loginTime;
        
        // Check if session will expire within 24 hours
        long twentyNineDaysInMillis = 29L * 24L * 60L * 60L * 1000L; // 29 days
        
        return timeDifference >= twentyNineDaysInMillis;
    }
    
    /**
     * Get session token for API calls
     */
    public String getSessionToken() {
        return sessionManager.getSessionToken();
    }
    
    /**
     * Get days since login
     */
    public int getDaysSinceLogin() {
        if (!isUserAuthenticated()) {
            return 0;
        }
        
        long loginTime = sessionManager.getLoginTime();
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - loginTime;
        
        return (int) (timeDifference / (24L * 60L * 60L * 1000L));
    }
    
    /**
     * Static method to quickly check authentication from any context
     */
    public static boolean isAuthenticated(Context context) {
        return new SessionHelper(context).isUserAuthenticated();
    }
    
    /**
     * Static method to quickly get current user from any context
     */
    public static User getCurrentUser(Context context) {
        return new SessionHelper(context).getCurrentUser();
    }
    
    /**
     * Static method to quickly logout from any context
     */
    public static void logout(Context context) {
        new SessionHelper(context).logoutUser();
    }

    /**
     * Validate token integrity
     */
    public boolean validateTokenIntegrity() {
        return sessionManager.validateSessionIntegrity();
    }

    /**
     * Check if session has valid token
     */
    public boolean hasValidToken() {
        String token = sessionManager.getSessionToken();
        return token != null && !token.isEmpty();
    }

    /**
     * Clean up any invalid session data
     */
    public void cleanupSession() {
        sessionManager.cleanupInvalidSession();
    }

    /**
     * Static method to validate authentication with token integrity check
     */
    public static boolean isValidAuthentication(Context context) {
        SessionHelper helper = new SessionHelper(context);
        helper.cleanupSession();
        return helper.isUserAuthenticated() && helper.validateTokenIntegrity();
    }
} 