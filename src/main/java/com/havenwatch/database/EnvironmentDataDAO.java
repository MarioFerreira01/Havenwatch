package com.havenwatch.database;

import com.havenwatch.models.EnvironmentData;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EnvironmentDataDAO {

    /**
     * Get a fresh database connection for each operation
     */
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Get the latest environment data for a resident
     * @param residentId The resident's ID
     * @return EnvironmentData object if found, null otherwise
     */
    public EnvironmentData getLatestEnvironmentData(int residentId) {
        String query = "SELECT * FROM environment_data WHERE resident_id = ? ORDER BY timestamp DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, residentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractEnvironmentDataFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting latest environment data: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get environment data for a resident within a time range
     * @param residentId The resident's ID
     * @param startTime The start time of the range
     * @param endTime The end time of the range
     * @return List of environment data records
     */
    public List<EnvironmentData> getEnvironmentDataInRange(int residentId, LocalDateTime startTime, LocalDateTime endTime) {
        List<EnvironmentData> environmentDataList = new ArrayList<>();
        String query = "SELECT * FROM environment_data WHERE resident_id = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, residentId);
            stmt.setTimestamp(2, Timestamp.valueOf(startTime));
            stmt.setTimestamp(3, Timestamp.valueOf(endTime));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    environmentDataList.add(extractEnvironmentDataFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting environment data in range: " + e.getMessage());
            e.printStackTrace();
        }
        return environmentDataList;
    }

    /**
     * Insert new environment data for a resident
     * @param environmentData The environment data to insert
     * @return true if successful, false otherwise
     */
    public boolean insertEnvironmentData(EnvironmentData environmentData) {
        String query = "INSERT INTO environment_data (resident_id, room_temperature, humidity, air_quality, gas_level) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, environmentData.getResidentId());
            stmt.setDouble(2, environmentData.getRoomTemperature());
            stmt.setInt(3, environmentData.getHumidity());
            stmt.setInt(4, environmentData.getAirQuality());
            stmt.setInt(5, environmentData.getGasLevel());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    environmentData.setEnvironmentId(generatedKeys.getInt(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting environment data: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Delete environment data for a resident
     * @param environmentId The ID of the environment data to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteEnvironmentData(int environmentId) {
        String query = "DELETE FROM environment_data WHERE environment_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, environmentId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting environment data: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get the average environment metrics for a resident over a time period
     * @param residentId The resident's ID
     * @param days Number of days to look back
     * @return EnvironmentData object with averages or null if no data
     */
    public EnvironmentData getAverageEnvironmentData(int residentId, int days) {
        String query = "SELECT AVG(room_temperature) as room_temperature, " +
                "AVG(humidity) as humidity, " +
                "AVG(air_quality) as air_quality, " +
                "AVG(gas_level) as gas_level " +
                "FROM environment_data " +
                "WHERE resident_id = ? AND timestamp >= DATE_SUB(NOW(), INTERVAL ? DAY)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, residentId);
            stmt.setInt(2, days);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getDouble("room_temperature") > 0) {  // Check if we got results
                    EnvironmentData avgData = new EnvironmentData();
                    avgData.setResidentId(residentId);
                    avgData.setRoomTemperature(rs.getDouble("room_temperature"));
                    avgData.setHumidity((int)Math.round(rs.getDouble("humidity")));
                    avgData.setAirQuality((int)Math.round(rs.getDouble("air_quality")));
                    avgData.setGasLevel((int)Math.round(rs.getDouble("gas_level")));
                    return avgData;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting average environment data: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Extract an EnvironmentData object from a ResultSet
     */
    private EnvironmentData extractEnvironmentDataFromResultSet(ResultSet rs) throws SQLException {
        EnvironmentData environmentData = new EnvironmentData();
        environmentData.setEnvironmentId(rs.getInt("environment_id"));
        environmentData.setResidentId(rs.getInt("resident_id"));
        environmentData.setRoomTemperature(rs.getDouble("room_temperature"));
        environmentData.setHumidity(rs.getInt("humidity"));
        environmentData.setAirQuality(rs.getInt("air_quality"));
        environmentData.setGasLevel(rs.getInt("gas_level"));
        environmentData.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        return environmentData;
    }
}