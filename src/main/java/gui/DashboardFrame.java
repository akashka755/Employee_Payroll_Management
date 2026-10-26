package gui;

import javax.swing.*;
import java.awt.*;

/**
 * Main Administrator Dashboard.
 * Provides intuitive navigation to all core modules:
 * 1. Employee Management
 * 2. Department Management
 * 3. Attendance Management
 * 4. Payroll Processing
 * 5. Salary Slips
 * 6. Monthly Payroll Reports
 * 7. Logout
 */
public class DashboardFrame extends JFrame {

    public DashboardFrame() {
        setTitle("Employee Payroll Management System - Dashboard");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Header Section
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        JLabel lblTitle = new JLabel("Employee Payroll Management System", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitle.setForeground(new Color(25, 55, 105));

        JLabel lblSubtitle = new JLabel("Semester 3 OOP Project (PBCST304) | Administrator Console", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblSubtitle.setForeground(Color.DARK_GRAY);

        headerPanel.add(lblTitle);
        headerPanel.add(lblSubtitle);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Grid for Navigation Buttons (3 rows, 2 columns)
        JPanel gridPanel = new JPanel(new GridLayout(3, 2, 20, 20));

        JButton btnEmployee = createDashboardButton("1. Employee Management", "Add, edit, search, and delete employee records");
        JButton btnDepartment = createDashboardButton("2. Department Management", "Maintain company departments and organization units");
        JButton btnAttendance = createDashboardButton("3. Attendance Management", "Log monthly present/absent days and overtime hours");
        JButton btnPayroll = createDashboardButton("4. Payroll Processing", "Calculate gross, deductions, and net monthly salaries");
        JButton btnSalarySlip = createDashboardButton("5. Salary Slips", "View detailed historical salary breakdown cards");
        JButton btnReports = createDashboardButton("6. Monthly Payroll Reports", "Summarize monthly payroll across all employees");

        gridPanel.add(btnEmployee);
        gridPanel.add(btnDepartment);
        gridPanel.add(btnAttendance);
        gridPanel.add(btnPayroll);
        gridPanel.add(btnSalarySlip);
        gridPanel.add(btnReports);

        mainPanel.add(gridPanel, BorderLayout.CENTER);

        // Footer / Logout Section
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnLogout.setBackground(new Color(220, 53, 69));
        btnLogout.setForeground(Color.WHITE);

        footerPanel.add(btnLogout);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        // Event Listeners for Navigation
        btnEmployee.addActionListener(e -> new EmployeeFrame().setVisible(true));
        btnDepartment.addActionListener(e -> new DepartmentFrame().setVisible(true));
        btnAttendance.addActionListener(e -> new AttendanceFrame().setVisible(true));
        btnPayroll.addActionListener(e -> new PayrollFrame().setVisible(true));
        btnSalarySlip.addActionListener(e -> new SalarySlipFrame().setVisible(true));
        btnReports.addActionListener(e -> new ReportFrame().setVisible(true));

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to log out of the system?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        add(mainPanel);
    }

    private JButton createDashboardButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(280, 60));
        return button;
    }
}
