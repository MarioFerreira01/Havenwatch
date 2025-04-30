package com.havenwatch.services;

import com.havenwatch.database.AlertDAO;
import com.havenwatch.database.HealthDataDAO;
import com.havenwatch.database.EnvironmentDataDAO;
import com.havenwatch.database.ResidentDAO;
import com.havenwatch.models.Alert;
import com.havenwatch.models.HealthData;
import com.havenwatch.models.EnvironmentData;
import com.havenwatch.models.Resident;
import com.havenwatch.utils.SessionManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for dashboard data preparation
 */
public class DashboardService {
    private final ResidentDAO residentDAO;
    private final HealthDataDAO healthDataDAO;
    private final EnvironmentDataDAO environmentDataDAO;
    private final AlertDAO alertDAO;

    public DashboardService() {
        this.residentDAO = new ResidentDAO();
        this.healthDataDAO = new HealthDataDAO();
        this.environmentDataDAO = new EnvironmentDataDAO();
        this.alertDAO = new AlertDAO();
    }

    /**
     * Get all residents accessible to the current user
     * @return List of accessible residents
     */
    public List<Resident> getAccessibleResidents() {
        int userId = SessionManager.getInstance().getCurrentUser().getUserId();

        // If admin, return all residents
        if (SessionManager.getInstance().getCurrentUser().getRole() == com.havenwatch.models.User.UserRole.ADMIN) {
            return residentDAO.getAllResidents();
        }

        // Otherwise, return only residents accessible to this user
        return residentDAO.getResidentsByUser(userId);
    }

    /**
     * Get latest health data for all accessible residents
     * @return Map of resident ID to latest health data
     */
    public Map<Integer, HealthData> getLatestHealthData() {
        Map<Integer, HealthData> latestData = new HashMap<>();

        for (Resident resident : getAccessibleResidents()) {
            HealthData healthData = healthDataDAO.getLatestHealthData(resident.getResidentId());
            if (healthData != null) {
                latestData.put(resident.getResidentId(), healthData);
            }
        }

        return latestData;
    }

    /**
     * Get latest environment data for all accessible residents
     * @return Map of resident ID to latest environment data
     */
    public Map<Integer, EnvironmentData> getLatestEnvironmentData() {
        Map<Integer, EnvironmentData> latestData = new HashMap<>();

        for (Resident resident : getAccessibleResidents()) {
            EnvironmentData environmentData = environmentDataDAO.getLatestEnvironmentData(resident.getResidentId());
            if (environmentData != null) {
                latestData.put(resident.getResidentId(), environmentData);
            }
        }

        return latestData;
    }

    /**
     * Get active alerts for all accessible residents
     * @return Map of resident ID to list of active alerts
     */
    public Map<Integer, List<Alert>> getActiveAlerts() {
        Map<Integer, List<Alert>> activeAlerts = new HashMap<>();

        for (Resident resident : getAccessibleResidents()) {
            List<Alert> alerts = alertDAO.getActiveAlertsByResident(resident.getResidentId());
            if (!alerts.isEmpty()) {
                activeAlerts.put(resident.getResidentId(), alerts);
            }
        }

        return activeAlerts;
    }

    /**
     * Get list of critical alerts that need immediate attention
     * @return List of critical alerts
     */
    public List<Alert> getCriticalAlerts() {
        List<Alert> criticalAlerts = new ArrayList<>();

        for (Resident resident : getAccessibleResidents()) {
            List<Alert> alerts = alertDAO.getActiveAlertsByResident(resident.getResidentId());
            for (Alert alert : alerts) {
                if (alert.getSeverity() == Alert.AlertSeverity.CRITICAL ||
                        alert.getSeverity() == Alert.AlertSeverity.HIGH) {
                    criticalAlerts.add(alert);
                }
            }
        }

        return criticalAlerts;
    }

    /**
     * Get health data for a resident within a time range
     * @param residentId The resident's ID
     * @param days Number of days to look back
     * @return List of health data records
     */
    public List<HealthData> getHealthDataHistory(int residentId, int days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        return healthDataDAO.getHealthDataInRange(residentId, startTime, endTime);
    }

    /**
     * Get environment data for a resident within a time range
     * @param residentId The resident's ID
     * @param days Number of days to look back
     * @return List of environment data records
     */
    public List<EnvironmentData> getEnvironmentDataHistory(int residentId, int days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        return environmentDataDAO.getEnvironmentDataInRange(residentId, startTime, endTime);
    }

    /**
     * Get the total number of active alerts
     * @return Total number of active alerts
     */
    public int getTotalActiveAlerts() {
        int total = 0;

        for (List<Alert> alerts : getActiveAlerts().values()) {
            total += alerts.size();
        }

        return total;
    }

    /**
     * Get the counts of active alerts by severity
     * @return Array of counts (index 0=LOW, 1=MEDIUM, 2=HIGH, 3=CRITICAL)
     */
    public int[] getAlertCountsBySeverity() {
        return alertDAO.countAlertsBySeverity();
    }
}