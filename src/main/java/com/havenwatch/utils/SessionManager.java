package com.havenwatch.utils;

import com.havenwatch.models.User;

/**
 * Singleton class to manage the current user session
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {
        // Private constructor for singleton
    }

    /**
     * Get the singleton instance
     * @return SessionManager instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Set the current logged in user
     * @param user The logged in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Get the current logged in user
     * @return The current user or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Clear the current session
     */
    public void clearSession() {
        this.currentUser = null;
    }

    /**
     * Check if the current user has a specific role
     * @param role The role to check
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(User.UserRole role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    /**
     * Check if the current user is an admin
     * @return true if admin, false otherwise
     */
    public boolean isAdmin() {
        return hasRole(User.UserRole.ADMIN);
    }
}