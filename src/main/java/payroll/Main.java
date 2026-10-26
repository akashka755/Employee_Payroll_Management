package payroll;

import database.DatabaseConnection;
import gui.LoginFrame;

import javax.swing.*;

/**
 * Main Application Entry Point.
 * Starts the Employee Payroll Management System GUI on the Event Dispatch Thread (EDT).
 * Demonstrates:
 * - Module 1: Structure of a simple Java program, main() method.
 * - Module 4: Swing Event Dispatch Thread (SwingUtilities.invokeLater).
 * - Module 5: Diagnostic check of MySQL JDBC connection at startup.
 */
public class Main {

    public static void main(String[] args) {
        // Set modern system look and feel if available
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to default cross-platform Swing look and feel
        }

        // Launch GUI asynchronously on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            // Verify MySQL database connectivity on startup
            boolean dbOnline = DatabaseConnection.testConnection();
            if (!dbOnline) {
                System.err.println("[Main] Warning: Could not connect to MySQL database.");
                JOptionPane.showMessageDialog(null,
                        "Warning: Cannot establish connection to MySQL database.\n" +
                        "Please ensure MySQL service is running and database 'employee_payroll_db' exists.\n" +
                        "You can configure connection parameters in 'DatabaseConnection.java'.",
                        "Database Connection Notice",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                System.out.println("[Main] Successfully connected to MySQL database: employee_payroll_db");
            }

            // Display Administrator Login Window
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
