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

import java.util.List;

/**
 * Service for managing alerts and alert logic
 */
public class AlertService {
    private final AlertDAO alertDAO;
    private final HealthDataDAO healthDataDAO;
    private final EnvironmentDataDAO environmentDataDAO;
    private final ResidentDAO residentDAO;

    // Thresholds for alerts
    private static final int HEART_RATE_HIGH = 100;
    private static final int HEART_RATE_LOW = 55;
    private static final double TEMPERATURE_HIGH = 38.0;
    private static final double TEMPERATURE_LOW = 35.5;
    private static final int BLOOD_OXYGEN_LOW = 92;
    private static final int GLUCOSE_HIGH = 180;
    private static final int GLUCOSE_LOW = 70;

    private static final double ROOM_TEMP_HIGH = 30.0;
    private static final double ROOM_TEMP_LOW = 15.0;
    private static final int HUMIDITY_HIGH = 70;
    private static final int HUMIDITY_LOW = 30;
    private static final int AIR_QUALITY_POOR = 50;
    private static final int GAS_LEVEL_HIGH = 50;

    public AlertService() {
        this.alertDAO = new AlertDAO();
        this.healthDataDAO = new HealthDataDAO();
        this.environmentDataDAO = new EnvironmentDataDAO();
        this.residentDAO = new ResidentDAO();
    }

    /**
     * Get all active alerts for the system
     * @return List of active alerts
     */
    public List<Alert> getAllActiveAlerts() {
        return alertDAO.getAllActiveAlerts();
    }

    /**
     * Get active alerts for a specific resident
     * @param residentId The resident's ID
     * @return List of active alerts
     */
    public List<Alert> getActiveAlertsForResident(int residentId) {
        return alertDAO.getActiveAlertsByResident(residentId);
    }

    /**
     * Create a new alert
     * @param alert The alert to create
     * @return true if successful, false otherwise
     */
    public boolean createAlert(Alert alert) {
        return alertDAO.insertAlert(alert);
    }

    /**
     * Acknowledge an alert
     * @param alertId The alert's ID
     * @return true if successful, false otherwise
     */
    public boolean acknowledgeAlert(int alertId) {
        return alertDAO.updateAlertStatus(alertId, Alert.AlertStatus.ACKNOWLEDGED, null);
    }

    /**
     * Resolve an alert
     * @param alertId The alert's ID
     * @return true if successful, false otherwise
     */
    public boolean resolveAlert(int alertId) {
        int currentUserId = SessionManager.getInstance().getCurrentUser().getUserId();
        return alertDAO.updateAlertStatus(alertId, Alert.AlertStatus.RESOLVED, currentUserId);
    }

    /**
     * Check health data for alert conditions and create alerts if needed
     * @param healthData The health data to check
     * @return true if any alerts were created, false otherwise
     */
    public boolean checkHealthDataForAlerts(HealthData healthData) {
        boolean alertsCreated = false;
        int residentId = healthData.getResidentId();
        Resident resident = residentDAO.getResidentById(residentId);

        // Check heart rate
        if (healthData.getHeartRate() > HEART_RATE_HIGH) {
            Alert alert = new Alert(
                    residentId,
                    Alert.AlertType.HEALTH,
                    Alert.AlertSeverity.HIGH,
                    "High heart rate detected for " + resident.getFullName() + ": " + healthData.getHeartRate() + " bpm"
            );
            alertsCreated |= alertDAO.insertAlert(alert);
        } else if (healthData.getHeartRate() < HEART_RATE_LOW) {
            Alert alert = new Alert(
                    residentId,
                    Alert.AlertType.HEALTH,
                    Alert.AlertSeverity.HIGH,
                    "Low heart rate detected for " + resident.getFullName() + ": " + healthData.getHeartRate() + " bpm"
            );
            alertsCreated |= alertDAO.insertAlert(alert);
        }

        // Check temperature
        if (healthData.getTemperature() > TEMPERATURE_HIGH) {
            Alert alert = new Alert(
                    residentId,
                    Alert.AlertType.HEALTH,
                    Alert.AlertSeverity.HIGH,
                    "High body temperature detected for " + resident.getFullName() + ": " + healthData.getTemperature() + "°C"
            );
            alertsCreated |= alertDAO.insertAlert(alert);
        } else if (healthData.getTemperature() < TEMPERATURE_LOW) {
            Alert alert = new Alert(
                    residentId,
                    Alert.AlertType.HEALTH,
                    Alert.AlertSeverity.MEDIUM,
                    "Low body temperature detected for " + resident.getFullName() + ": " + healthData.getTemperature() + "°C"
            );
            alertsCreated |= alertDAO.insertAlert(alert);
        }

        // Check blood oxygen
        if (healthData.getBloodOxygen() < BLOOD_OXYGEN_LOW) {
            Alert alert = new Alert(
                    residentId,
                    Alert.AlertType.HEALTH,
                    Alert.AlertSeverity.CRITICAL,
                    "Low blood oxygen detected for " + resident.getFullName() + ": " + healthData.getBloodOxygen() + "%"
            );
            alertsCreated |= alertDAO.insertAlert(alert);
        }

        // Check glucose level
        if (healthData.getGlucoseLevel() > GLUCOSE_HIGH) {
            Alert alert = new Alert(
                    residentId,
                    Alert.AlertType.HEALTH,
                    Alert.AlertSeverity.MEDIUM,
                    "High glucose level detected for " + resident.getFullName() + ": " + healthData.getGlucoseLevel() + " mg/dL"
            );
            alertsCreated |= alertDAO.insertAlert(alert);
        } else if (healthData.getGlucoseLevel() < GLUCOSE_LOW) {
            Alert alert = new Alert(
                    residentId,
                    Alert.AlertType.HEALTH,
                    Alert.AlertSeverity.HIGH,
                    "Low glucose level detected for " + resident.getFullName() + ": " + healthData.getGlucoseLevel() + " mg/dL"
            );
            alertsCreated |= alertDAO.insertAlert(alert);
        }

        return alertsCreated;
    }

    /**
     * Check environment data for alert conditions and create alerts if needed
     * @param environmentData The environment data to check
     * @return true if any alerts were created, false otherwise
     */
    public boolean checkEnvironmentDataForAlerts(EnvironmentData environmentData) {
        boolean alertsCreated = false;
        int residentId = environmentData.getResidentId();
        Resident resident = residentDAO.getResidentById(residentId);

        // Check room temperature
        if (environmentData.getRoomTemperature() > ROOM_TEMP_HIGH) {
            Alert alert = new Alert(
                    residentId,
                    Alert.AlertType.ENVIRONMENT,
                    Alert.AlertSeverity.MEDIUM,
                    "High room temperature detected for " + resident.getFullName() + ": " + environmentData.getRoomTemperature() + "°C"
            );
            alertsCreated |= alertDAO.insertAlert(alert);
        } else if (environmentData.getRoomTemperature() < ROOM_TEMP_LOW) {
            Alert alert = new Alert(
                    residentId,
                    Alert.AlertType.ENVIRONMENT,
                    Alert.AlertSeverity.MEDIUM,
                    "Low room temperature detected for " + resident.getFullName() + ": " + environmentData.getRoomTemperature() + "°C"
            );
            alertsCreated |= alertDAO.insertAlert(alert);
        }

        // Check humidity
        if (environmentData.getHumidity() > HUMIDITY_HIGH) {
            Alert alert = new Alert(
                    residentId,
                    Alert.AlertType.ENVIRONMENT,
                    Alert.AlertSeverity.LOW,
                    "High humidity detected for " + resident.getFullName() + ": " + environmentData.getHumidity() + "%"
            );
            alertsCreated |= alertDAO.insertAlert(alert);
        } else if (environmentData.getHumidity() < HUMIDITY_LOW) {
            Alert alert = new Alert(
                    residentId,
                    Alert.AlertType.ENVIRONMENT,
                    Alert.AlertSeverity.LOW,
                    "Low humidity detected for " + resident.getFullName() + ": " + environmentData.getHumidity() + "%"
            );
            alertsCreated |= alertDAO.insertAlert(alert);
        }

        // Check air quality
        if (environmentData.getAirQuality() < AIR_QUALITY_POOR) {
            Alert alert = new Alert(
                    residentId,
                    Alert.AlertType.ENVIRONMENT,
                    Alert.AlertSeverity.MEDIUM,
                    "Poor air quality detected for " + resident.getFullName() + ": " + environmentData.getAirQuality() + "/100"
            );
            alertsCreated |= alertDAO.insertAlert(alert);
        }

        // Check gas level
        if (environmentData.getGasLevel() > GAS_LEVEL_HIGH) {
            Alert alert = new Alert(
                    residentId,
                    Alert.AlertType.ENVIRONMENT,
                    Alert.AlertSeverity.CRITICAL,
                    "High gas level detected for " + resident.getFullName() + ": " + environmentData.getGasLevel() + "%"
            );
            alertsCreated |= alertDAO.insertAlert(alert);
        }

        return alertsCreated;
    }

    /**
     * Get alert counts by severity
     * @return Array of counts indexed by severity (0=LOW, 1=MEDIUM, 2=HIGH, 3=CRITICAL)
     */
    public int[] getAlertCountsBySeverity() {
        return alertDAO.countAlertsBySeverity();
    }
}