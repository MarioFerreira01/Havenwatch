package com.havenwatch.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Executors;

/**
 * Robust database connection management with automatic reconnection
 * This solves the "No operations allowed after connection closed" error
 */
public class DatabaseConnection {
    // Database configuration
    private static final String DATABASE_URL = "jdbc:mysql://localhost/HavenWatch?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8";
    private static final String DATABASE_USER = "root";
    private static final String DATABASE_PASSWORD = "";

    // Connection management settings
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private static final int CONNECTION_TIMEOUT = 10; // seconds
    private static final int VALIDATION_TIMEOUT = 5;  // seconds

    private static DatabaseConnection instance;

    private DatabaseConnection() {
        // Private constructor to enforce singleton pattern
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Get a fresh, validated database connection
     * This method creates a NEW connection for each call to avoid connection timeout issues
     */
    public Connection getConnection() throws SQLException {
        return createNewConnection();
    }

    /**
     * Create a new database connection with retry logic
     */
    private Connection createNewConnection() throws SQLException {
        SQLException lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                System.out.println("Attempting database connection (attempt " + attempt + ")");

                // Create new connection with timeout settings
                Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);

                // Set connection properties
                connection.setAutoCommit(true);

                // Fix: Use a proper executor instead of null
                // Alternative: Remove this line entirely as it's not essential
                // connection.setNetworkTimeout(Executors.newSingleThreadExecutor(), CONNECTION_TIMEOUT * 1000);

                // Validate the connection
                if (connection.isValid(VALIDATION_TIMEOUT)) {
                    System.out.println("Database connection established successfully (attempt " + attempt + ")");
                    return connection;
                } else {
                    connection.close();
                    throw new SQLException("Connection validation failed");
                }

            } catch (SQLException e) {
                lastException = e;
                System.err.println("Database connection attempt " + attempt + " failed: " + e.getMessage());

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Connection attempt interrupted", ie);
                    }
                }
            }
        }

        throw new SQLException("Failed to establish database connection after " + MAX_RETRY_ATTEMPTS + " attempts", lastException);
    }

    /**
     * Test database connectivity
     */
    public boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection.isValid(VALIDATION_TIMEOUT);
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Execute a simple query to verify database is accessible
     */
    public boolean isDbAccessible() {
        try (Connection connection = getConnection();
             var stmt = connection.createStatement();
             var rs = stmt.executeQuery("SELECT 1")) {
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Database accessibility test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cleanup method for application shutdown
     * Since we create fresh connections per request, there's no persistent connection to close
     */
    public void closeConnection() {
        // No-op: connections are managed per-request and closed by calling code
        System.out.println("Database connection manager shutdown");
    }
}