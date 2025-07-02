package com.havenwatch.database;

import com.havenwatch.models.HealthData;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HealthDataDAO {

    /**
     * Get a fresh database connection for each operation
     */
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Get the latest health data for a resident
     * @param residentId The resident's ID
     * @return HealthData object if found, null otherwise
     */
    public HealthData getLatestHealthData(int residentId) {
        String query = "SELECT * FROM health_data WHERE resident_id = ? ORDER BY timestamp DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, residentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractHealthDataFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting latest health data: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get health data for a resident within a time range
     * @param residentId The resident's ID
     * @param startTime The start time of the range
     * @param endTime The end time of the range
     * @return List of health data records
     */
    public List<HealthData> getHealthDataInRange(int residentId, LocalDateTime startTime, LocalDateTime endTime) {
        List<HealthData> healthDataList = new ArrayList<>();
        String query = "SELECT * FROM health_data WHERE resident_id = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, residentId);
            stmt.setTimestamp(2, Timestamp.valueOf(startTime));
            stmt.setTimestamp(3, Timestamp.valueOf(endTime));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    healthDataList.add(extractHealthDataFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting health data in range: " + e.getMessage());
            e.printStackTrace();
        }
        return healthDataList;
    }

    /**
     * Insert new health data for a resident
     * @param healthData The health data to insert
     * @return true if successful, false otherwise
     */
    public boolean insertHealthData(HealthData healthData) {
        String query = "INSERT INTO health_data (resident_id, heart_rate, blood_pressure, blood_oxygen) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, healthData.getResidentId());
            stmt.setInt(2, healthData.getHeartRate());
            stmt.setString(3, healthData.getBloodPressure());
            stmt.setInt(4, healthData.getBloodOxygen());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    healthData.setHealthId(generatedKeys.getInt(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting health data: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Delete health data for a resident
     * @param healthId The ID of the health data to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteHealthData(int healthId) {
        String query = "DELETE FROM health_data WHERE health_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, healthId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting health data: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get the average health metrics for a resident over a time period
     * @param residentId The resident's ID
     * @param days Number of days to look back
     * @return HealthData object with averages or null if no data
     */
    public HealthData getAverageHealthData(int residentId, int days) {
        String query = "SELECT AVG(heart_rate) as heart_rate, AVG(blood_oxygen) as blood_oxygen " +
                "FROM health_data " +
                "WHERE resident_id = ? AND timestamp >= DATE_SUB(NOW(), INTERVAL ? DAY)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, residentId);
            stmt.setInt(2, days);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getDouble("heart_rate") > 0) {  // Check if we got results
                    HealthData avgData = new HealthData();
                    avgData.setResidentId(residentId);
                    avgData.setHeartRate((int)Math.round(rs.getDouble("heart_rate")));
                    avgData.setBloodOxygen((int)Math.round(rs.getDouble("blood_oxygen")));
                    // Blood pressure doesn't make sense to average
                    avgData.setBloodPressure("N/A");
                    return avgData;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting average health data: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Extract a HealthData object from a ResultSet
     */
    private HealthData extractHealthDataFromResultSet(ResultSet rs) throws SQLException {
        HealthData healthData = new HealthData();
        healthData.setHealthId(rs.getInt("health_id"));
        healthData.setResidentId(rs.getInt("resident_id"));
        healthData.setHeartRate(rs.getInt("heart_rate"));
        healthData.setBloodPressure(rs.getString("blood_pressure"));
        healthData.setBloodOxygen(rs.getInt("blood_oxygen"));
        healthData.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        return healthData;
    }
}