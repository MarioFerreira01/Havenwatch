package com.havenwatch.database;

import com.havenwatch.models.Alert;
import com.havenwatch.models.Alert.AlertType;
import com.havenwatch.models.Alert.AlertSeverity;
import com.havenwatch.models.Alert.AlertStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AlertDAO {

    // Remove the stored connection - get fresh connections per method call
    public AlertDAO() {
        // Constructor now empty - no connection stored
    }

    /**
     * Get an alert by its ID
     * @param alertId The alert's ID
     * @return Alert object if found, null otherwise
     */
    public Alert getAlertById(int alertId) {
        String query = "SELECT * FROM alerts WHERE alert_id = ?";

        // Get fresh connection for this operation
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, alertId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractAlertFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting alert by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all active alerts for a resident
     * @param residentId The resident's ID
     * @return List of active alerts
     */
    public List<Alert> getActiveAlertsByResident(int residentId) {
        List<Alert> alerts = new ArrayList<>();
        String query = "SELECT * FROM alerts WHERE resident_id = ? AND status != 'RESOLVED' ORDER BY created_at DESC";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, residentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alerts.add(extractAlertFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting active alerts by resident: " + e.getMessage());
        }
        return alerts;
    }

    /**
     * Get all active alerts in the system
     * @return List of active alerts
     */
    public List<Alert> getAllActiveAlerts() {
        List<Alert> alerts = new ArrayList<>();
        String query = "SELECT * FROM alerts WHERE status != 'RESOLVED' ORDER BY severity DESC, created_at DESC";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                alerts.add(extractAlertFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all active alerts: " + e.getMessage());
        }
        return alerts;
    }

    /**
     * Get alerts by type and status
     * @param type The alert type
     * @param status The alert status
     * @return List of matching alerts
     */
    public List<Alert> getAlertsByTypeAndStatus(AlertType type, AlertStatus status) {
        List<Alert> alerts = new ArrayList<>();
        String query = "SELECT * FROM alerts WHERE alert_type = ? AND status = ? ORDER BY created_at DESC";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, type.toString());
            stmt.setString(2, status.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alerts.add(extractAlertFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting alerts by type and status: " + e.getMessage());
        }
        return alerts;
    }

    /**
     * Insert a new alert into the database
     * @param alert The alert to insert
     * @return true if successful, false otherwise
     */
    public boolean insertAlert(Alert alert) {
        String query = "INSERT INTO alerts (resident_id, alert_type, severity, message, status) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, alert.getResidentId());
            stmt.setString(2, alert.getAlertType().toString());
            stmt.setString(3, alert.getSeverity().toString());
            stmt.setString(4, alert.getMessage());
            stmt.setString(5, alert.getStatus().toString());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    alert.setAlertId(generatedKeys.getInt(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting alert: " + e.getMessage());
        }
        return false;
    }

    /**
     * Update the status of an alert
     * @param alertId The alert's ID
     * @param status The new status
     * @param resolvedBy The ID of the user who resolved the alert (if applicable)
     * @return true if successful, false otherwise
     */
    public boolean updateAlertStatus(int alertId, AlertStatus status, Integer resolvedBy) {
        String query;
        if (status == AlertStatus.RESOLVED) {
            query = "UPDATE alerts SET status = ?, resolved_at = NOW(), resolved_by = ? WHERE alert_id = ?";
        } else {
            query = "UPDATE alerts SET status = ? WHERE alert_id = ?";
        }

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, status.toString());

            if (status == AlertStatus.RESOLVED) {
                stmt.setObject(2, resolvedBy);
                stmt.setInt(3, alertId);
            } else {
                stmt.setInt(2, alertId);
            }

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating alert status: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete an alert from the database
     * @param alertId The ID of the alert to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteAlert(int alertId) {
        String query = "DELETE FROM alerts WHERE alert_id = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, alertId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting alert: " + e.getMessage());
        }
        return false;
    }

    /**
     * Count alerts by severity
     * @return Array of counts indexed by severity (0=LOW, 1=MEDIUM, 2=HIGH, 3=CRITICAL)
     */
    public int[] countAlertsBySeverity() {
        int[] counts = new int[4];
        String query = "SELECT severity, COUNT(*) as count FROM alerts WHERE status != 'RESOLVED' " +
                "GROUP BY severity ORDER BY FIELD(severity, 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL')";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String severity = rs.getString("severity");
                int count = rs.getInt("count");

                switch (AlertSeverity.valueOf(severity)) {
                    case LOW:
                        counts[0] = count;
                        break;
                    case MEDIUM:
                        counts[1] = count;
                        break;
                    case HIGH:
                        counts[2] = count;
                        break;
                    case CRITICAL:
                        counts[3] = count;
                        break;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting alerts by severity: " + e.getMessage());
        }
        return counts;
    }

    /**
     * Extract an Alert object from a ResultSet
     */
    private Alert extractAlertFromResultSet(ResultSet rs) throws SQLException {
        Alert alert = new Alert();
        alert.setAlertId(rs.getInt("alert_id"));
        alert.setResidentId(rs.getInt("resident_id"));
        alert.setAlertType(AlertType.valueOf(rs.getString("alert_type")));
        alert.setSeverity(AlertSeverity.valueOf(rs.getString("severity")));
        alert.setMessage(rs.getString("message"));
        alert.setStatus(AlertStatus.valueOf(rs.getString("status")));
        alert.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        Timestamp resolvedAt = rs.getTimestamp("resolved_at");
        if (resolvedAt != null) {
            alert.setResolvedAt(resolvedAt.toLocalDateTime());
        }

        Object resolvedBy = rs.getObject("resolved_by");
        if (resolvedBy != null) {
            alert.setResolvedBy((Integer) resolvedBy);
        }

        return alert;
    }
}