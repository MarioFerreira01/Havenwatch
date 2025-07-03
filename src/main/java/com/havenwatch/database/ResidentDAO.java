package com.havenwatch.database;

import com.havenwatch.models.Resident;
import com.havenwatch.models.Resident.Gender;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResidentDAO {

    /**
     * Get a fresh database connection for each operation
     */
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Get a resident by their ID
     * @param residentId The resident's ID
     * @return Resident object if found, null otherwise
     */
    public Resident getResidentById(int residentId) {
        String query = "SELECT * FROM residents WHERE resident_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, residentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractResidentFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting resident by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all residents in the system
     * @return List of all residents
     */
    public List<Resident> getAllResidents() {
        List<Resident> residents = new ArrayList<>();
        String query = "SELECT * FROM residents ORDER BY last_name, first_name";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                residents.add(extractResidentFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all residents: " + e.getMessage());
            e.printStackTrace();
        }
        return residents;
    }

    /**
     * Get residents accessible by a specific user
     * @param userId The user's ID
     * @return List of accessible residents
     */
    public List<Resident> getResidentsByUser(int userId) {
        List<Resident> residents = new ArrayList<>();
        String query = "SELECT r.* FROM residents r " +
                "JOIN user_resident_access ura ON r.resident_id = ura.resident_id " +
                "WHERE ura.user_id = ? " +
                "ORDER BY r.last_name, r.first_name";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    residents.add(extractResidentFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting residents by user: " + e.getMessage());
            e.printStackTrace();
        }
        return residents;
    }

    /**
     * Insert a new resident into the database
     * @param resident The resident to insert
     * @return true if successful, false otherwise
     */
    public boolean insertResident(Resident resident) {
        String query = "INSERT INTO residents (first_name, last_name, date_of_birth, gender, " +
                "address, emergency_contact, emergency_phone, medical_conditions, " +
                "medications, allergies) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, resident.getFirstName());
            stmt.setString(2, resident.getLastName());
            stmt.setDate(3, Date.valueOf(resident.getDateOfBirth()));
            stmt.setString(4, resident.getGender().toString());
            stmt.setString(5, resident.getAddress());
            stmt.setString(6, resident.getEmergencyContact());
            stmt.setString(7, resident.getEmergencyPhone());
            stmt.setString(8, resident.getMedicalConditions());
            stmt.setString(9, resident.getMedications());
            stmt.setString(10, resident.getAllergies());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    resident.setResidentId(generatedKeys.getInt(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting resident: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Update an existing resident in the database
     * @param resident The resident to update
     * @return true if successful, false otherwise
     */
    public boolean updateResident(Resident resident) {
        String query = "UPDATE residents SET first_name = ?, last_name = ?, date_of_birth = ?, " +
                "gender = ?, address = ?, emergency_contact = ?, emergency_phone = ?, " +
                "medical_conditions = ?, medications = ?, allergies = ? " +
                "WHERE resident_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, resident.getFirstName());
            stmt.setString(2, resident.getLastName());
            stmt.setDate(3, Date.valueOf(resident.getDateOfBirth()));
            stmt.setString(4, resident.getGender().toString());
            stmt.setString(5, resident.getAddress());
            stmt.setString(6, resident.getEmergencyContact());
            stmt.setString(7, resident.getEmergencyPhone());
            stmt.setString(8, resident.getMedicalConditions());
            stmt.setString(9, resident.getMedications());
            stmt.setString(10, resident.getAllergies());
            stmt.setInt(11, resident.getResidentId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating resident: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Delete a resident from the database
     * @param residentId The ID of the resident to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteResident(int residentId) {
        // First delete related access records
        String deleteAccessQuery = "DELETE FROM user_resident_access WHERE resident_id = ?";
        String deleteResidentQuery = "DELETE FROM residents WHERE resident_id = ?";

        try (Connection conn = getConnection()) {
            // Start transaction
            conn.setAutoCommit(false);

            try {
                // Delete access records first
                try (PreparedStatement stmt = conn.prepareStatement(deleteAccessQuery)) {
                    stmt.setInt(1, residentId);
                    stmt.executeUpdate();
                }

                // Delete resident
                try (PreparedStatement stmt = conn.prepareStatement(deleteResidentQuery)) {
                    stmt.setInt(1, residentId);
                    int affectedRows = stmt.executeUpdate();

                    if (affectedRows > 0) {
                        conn.commit();
                        return true;
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error deleting resident: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Assign a resident to a user for access
     * @param residentId The resident's ID
     * @param userId The user's ID
     * @return true if successful, false otherwise
     */
    public boolean assignResidentToUser(int residentId, int userId) {
        // Check if assignment already exists
        String checkQuery = "SELECT COUNT(*) FROM user_resident_access WHERE user_id = ? AND resident_id = ?";
        String insertQuery = "INSERT INTO user_resident_access (user_id, resident_id) VALUES (?, ?)";

        try (Connection conn = getConnection()) {
            // Check if assignment already exists
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, userId);
                checkStmt.setInt(2, residentId);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        // Assignment already exists
                        return true;
                    }
                }
            }

            // Create new assignment
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, residentId);

                int affectedRows = insertStmt.executeUpdate();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error assigning resident to user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Remove a resident assignment from a user
     * @param residentId The resident's ID
     * @param userId The user's ID
     * @return true if successful, false otherwise
     */
    public boolean removeResidentFromUser(int residentId, int userId) {
        String query = "DELETE FROM user_resident_access WHERE user_id = ? AND resident_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, residentId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error removing resident from user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get a list of users who have access to a specific resident
     * @param residentId The resident's ID
     * @return List of user IDs
     */
    public List<Integer> getUsersWithAccessToResident(int residentId) {
        List<Integer> userIds = new ArrayList<>();
        String query = "SELECT user_id FROM user_resident_access WHERE resident_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, residentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getInt("user_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting users with access: " + e.getMessage());
            e.printStackTrace();
        }
        return userIds;
    }

    /**
     * Check if a user has access to a specific resident
     * @param userId The user's ID
     * @param residentId The resident's ID
     * @return true if user has access, false otherwise
     */
    public boolean userHasAccessToResident(int userId, int residentId) {
        String query = "SELECT COUNT(*) FROM user_resident_access WHERE user_id = ? AND resident_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, residentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking user access: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get residents by multiple IDs (for batch operations)
     * @param residentIds List of resident IDs
     * @return List of residents
     */
    public List<Resident> getResidentsByIds(List<Integer> residentIds) {
        if (residentIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Resident> residents = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM residents WHERE resident_id IN (");

        for (int i = 0; i < residentIds.size(); i++) {
            if (i > 0) queryBuilder.append(",");
            queryBuilder.append("?");
        }
        queryBuilder.append(") ORDER BY last_name, first_name");

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < residentIds.size(); i++) {
                stmt.setInt(i + 1, residentIds.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    residents.add(extractResidentFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting residents by IDs: " + e.getMessage());
            e.printStackTrace();
        }
        return residents;
    }

    /**
     * Extract a Resident object from a ResultSet
     */
    private Resident extractResidentFromResultSet(ResultSet rs) throws SQLException {
        Resident resident = new Resident();
        resident.setResidentId(rs.getInt("resident_id"));
        resident.setFirstName(rs.getString("first_name"));
        resident.setLastName(rs.getString("last_name"));
        resident.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        resident.setGender(Gender.valueOf(rs.getString("gender")));
        resident.setAddress(rs.getString("address"));
        resident.setEmergencyContact(rs.getString("emergency_contact"));
        resident.setEmergencyPhone(rs.getString("emergency_phone"));
        resident.setMedicalConditions(rs.getString("medical_conditions"));
        resident.setMedications(rs.getString("medications"));
        resident.setAllergies(rs.getString("allergies"));
        resident.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return resident;
    }
}