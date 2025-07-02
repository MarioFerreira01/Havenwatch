package com.havenwatch.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class for database connection management
 */
public class DatabaseConnection {
//    private static final String DATABASE_URL = "jdbc:mysql://195.235.211.197:3306/pii2_HavenWatch";
//    private static final String DATABASE_USER = "db_DavIAN";  // Change to your MySQL username
//    private static final String DATABASE_PASSWORD = "davidiancu123";  // Change to your MySQL password

    private static final String DATABASE_URL = "jdbc:mysql://localhost/HavenWatch";
    private static final String DATABASE_USER = "root";  // Change to your MySQL username
    private static final String DATABASE_PASSWORD = "";

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
            System.out.println("Database connection established");
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("Error refreshing database connection: " + e.getMessage());
            throw new RuntimeException("Failed to refresh database connection", e);
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}