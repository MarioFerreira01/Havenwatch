package com.havenwatch.services;

import com.havenwatch.database.ResidentDAO;
import com.havenwatch.database.AlertDAO;
import com.havenwatch.models.User;
import com.havenwatch.models.Resident;
import com.havenwatch.models.Alert;
import com.havenwatch.utils.SessionManager;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to manage user access control and data filtering
 * Ensures users only see data they are authorized to access
 */
public class AccessControlService {
    private final ResidentDAO residentDAO;
    private final AlertDAO alertDAO;

    public AccessControlService() {
        this.residentDAO = new ResidentDAO();
        this.alertDAO = new AlertDAO();
    }

    /**
     * Get residents that the current user can access
     * @return List of accessible residents
     */
    public List<Resident> getAccessibleResidents() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("No user logged in");
        }

        if (currentUser.getRole() == User.UserRole.ADMIN) {
            // Admins can see all residents
            return residentDAO.getAllResidents();
        } else {
            // Other users can only see residents assigned to them
            return residentDAO.getResidentsByUser(currentUser.getUserId());
        }
    }

    /**
     * Check if the current user can access a specific resident
     * @param residentId The resident ID to check
     * @return true if accessible, false otherwise
     */
    public boolean canAccessResident(int residentId) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        if (currentUser.getRole() == User.UserRole.ADMIN) {
            return true; // Admins can access all residents
        }

        // Check if the resident is assigned to this user
        List<Integer> accessibleResidentIds = residentDAO.getResidentsByUser(currentUser.getUserId())
                .stream()
                .map(Resident::getResidentId)
                .collect(Collectors.toList());

        return accessibleResidentIds.contains(residentId);
    }

    /**
     * Get alerts that the current user can access (filtered by resident access)
     * @return List of accessible alerts
     */
    public List<Alert> getAccessibleAlerts() {
        List<Alert> allAlerts = alertDAO.getAllActiveAlerts();
        List<Integer> accessibleResidentIds = getAccessibleResidents()
                .stream()
                .map(Resident::getResidentId)
                .collect(Collectors.toList());

        return allAlerts.stream()
                .filter(alert -> accessibleResidentIds.contains(alert.getResidentId()))
                .collect(Collectors.toList());
    }

    /**
     * Get alerts for a specific resident if the user has access
     * @param residentId The resident ID
     * @return List of alerts for the resident, or empty list if no access
     */
    public List<Alert> getAccessibleAlertsForResident(int residentId) {
        if (!canAccessResident(residentId)) {
            return List.of(); // Return empty list if no access
        }

        return alertDAO.getActiveAlertsByResident(residentId);
    }

    /**
     * Check if the current user can manage users (admin only)
     * @return true if user management is allowed
     */
    public boolean canManageUsers() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        return currentUser != null && currentUser.getRole() == User.UserRole.ADMIN;
    }

    /**
     * Check if the current user can assign residents to users (admin only)
     * @return true if resident assignment is allowed
     */
    public boolean canAssignResidents() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        return currentUser != null && currentUser.getRole() == User.UserRole.ADMIN;
    }

    /**
     * Check if the current user can create new residents
     * @return true if resident creation is allowed
     */
    public boolean canCreateResidents() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        // Admins and caregivers can create residents
        return currentUser.getRole() == User.UserRole.ADMIN ||
                currentUser.getRole() == User.UserRole.CAREGIVER;
    }

    /**
     * Check if the current user can edit a specific resident
     * @param residentId The resident ID
     * @return true if editing is allowed
     */
    public boolean canEditResident(int residentId) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        // Admins can edit any resident
        if (currentUser.getRole() == User.UserRole.ADMIN) {
            return true;
        }

        // Caregivers and healthcare workers can edit residents they have access to
        if (currentUser.getRole() == User.UserRole.CAREGIVER ||
                currentUser.getRole() == User.UserRole.HEALTHCARE) {
            return canAccessResident(residentId);
        }

        // Family members cannot edit residents
        return false;
    }

    /**
     * Check if the current user can delete a specific resident
     * @param residentId The resident ID
     * @return true if deletion is allowed
     */
    public boolean canDeleteResident(int residentId) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        // Only admins can delete residents
        return currentUser.getRole() == User.UserRole.ADMIN;
    }

    /**
     * Validate that the current user has the required role
     * @param requiredRole The required role
     * @throws SecurityException if user doesn't have the required role
     */
    public void requireRole(User.UserRole requiredRole) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("No user logged in");
        }

        if (currentUser.getRole() != requiredRole) {
            throw new SecurityException("Insufficient privileges. Required role: " + requiredRole);
        }
    }

    /**
     * Validate that the current user has admin role
     * @throws SecurityException if user is not admin
     */
    public void requireAdmin() {
        requireRole(User.UserRole.ADMIN);
    }

    /**
     * Get the current user's role display name
     * @return Role display name
     */
    public String getCurrentUserRoleDisplay() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return "Guest";
        }

        switch (currentUser.getRole()) {
            case ADMIN:
                return "Administrator";
            case CAREGIVER:
                return "Caregiver";
            case HEALTHCARE:
                return "Healthcare Provider";
            case FAMILY:
                return "Family Member";
            default:
                return "User";
        }
    }
}