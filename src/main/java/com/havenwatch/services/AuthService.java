package com.havenwatch.services;

import com.havenwatch.database.UserDAO;
import com.havenwatch.models.User;
import com.havenwatch.utils.SessionManager;

/**
 * Service for handling user authentication and authorization
 */
public class AuthService {
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Attempt to login with username and password
     * @param username The username
     * @param password The password
     * @return true if login successful, false otherwise
     */
    public boolean login(String username, String password) {
        User user = userDAO.authenticate(username, password);
        if (user != null) {
            // Store user in session
            SessionManager.getInstance().setCurrentUser(user);
            return true;
        }
        return false;
    }

    /**
     * Log out the current user
     */
    public void logout() {
        SessionManager.getInstance().clearSession();
    }

    /**
     * Check if a user is logged in
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return SessionManager.getInstance().getCurrentUser() != null;
    }

    /**
     * Get the currently logged in user
     * @return User object or null if not logged in
     */
    public User getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser();
    }

    /**
     * Register a new user
     * @param user The user to register
     * @return true if registration successful, false otherwise
     */
    public boolean registerUser(User user) {
        // Check if username already exists
        if (userDAO.usernameExists(user.getUsername())) {
            return false;
        }

        // Ensure that self-registration doesn't create admins
        if (user.getRole() == User.UserRole.ADMIN && !isAdmin()) {
            // Only existing admins can create other admins
            return false;
        }

        // Insert the new user
        return userDAO.insertUser(user);
    }

    /**
     * Check if the current user has admin role
     * @return true if admin, false otherwise
     */
    public boolean isAdmin() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        return currentUser != null && currentUser.getRole() == User.UserRole.ADMIN;
    }

    /**
     * Check if the current user has caregiver role
     * @return true if caregiver, false otherwise
     */
    public boolean isCaregiver() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        return currentUser != null && currentUser.getRole() == User.UserRole.CAREGIVER;
    }

    /**
     * Check if the current user has healthcare role
     * @return true if healthcare, false otherwise
     */
    public boolean isHealthcare() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        return currentUser != null && currentUser.getRole() == User.UserRole.HEALTHCARE;
    }

    /**
     * Check if the current user has family role
     * @return true if family, false otherwise
     */
    public boolean isFamily() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        return currentUser != null && currentUser.getRole() == User.UserRole.FAMILY;
    }
}