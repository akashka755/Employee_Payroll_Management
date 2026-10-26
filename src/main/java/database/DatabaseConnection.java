package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages JDBC connection to MySQL database.
 * Demonstrates Module 5 syllabus concept: Developing Database Applications using JDBC.
 */
public class DatabaseConnection {

    // =========================================================================
    // DATABASE CONFIGURATION CONSTANTS
    // Replace these values if your MySQL setup uses a different host, port, or user/password
    // =========================================================================
    public static final String DB_URL = "jdbc:mysql://localhost:3306/employee_payroll_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    public static final String DB_USER = "root";       // Change if using a dedicated user, e.g. "payroll_user"
    public static final String DB_PASSWORD = "";       // Enter your MySQL password here (leave empty if none)

    // Load JDBC Driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] MySQL JDBC Driver not found on classpath: " + e.getMessage());
        }
    }

    /**
     * Establishes and returns a live connection to the MySQL database.
     * @return active java.sql.Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Diagnostic helper method to verify if database connectivity is working.
     * @return true if connection succeeds, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Connection test failed: " + e.getMessage());
            return false;
        }
    }
}
