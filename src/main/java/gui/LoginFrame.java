package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Administrator Login Window.
 * Demonstrates:
 * - Module 4 syllabus concept: Swing fundamentals (JFrame, JPanel, JLabel, JButton, JTextField, JPasswordField).
 * - Event Handling: ActionListener and Delegation Event Model.
 * - Simple layout management using BorderLayout and GridLayout.
 */
public class LoginFrame extends JFrame {

    // Predefined Administrator credentials (for academic demonstration)
    public static final String DEMO_USERNAME = "admin";
    public static final String DEMO_PASSWORD = "admin123";

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnExit;

    public LoginFrame() {
        setTitle("Employee Payroll System - Administrator Login");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        // Main panel with border layout and padding
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Header Title
        JLabel lblHeader = new JLabel("Admin Login", SwingConstants.CENTER);
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 20));
        mainPanel.add(lblHeader, BorderLayout.NORTH);

        // Form Center Panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 15));

        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtUsername = new JTextField();
        txtUsername.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JLabel lblDemoHint = new JLabel("Default Demo Credentials:");
        lblDemoHint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblDemoHint.setForeground(Color.GRAY);

        JLabel lblDemoCreds = new JLabel("admin / admin123");
        lblDemoCreds.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 11));
        lblDemoCreds.setForeground(new Color(0, 102, 204));

        formPanel.add(lblUsername);
        formPanel.add(txtUsername);
        formPanel.add(lblPassword);
        formPanel.add(txtPassword);
        formPanel.add(lblDemoHint);
        formPanel.add(lblDemoCreds);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Button Bottom Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnLogin = new JButton("Login");
        btnExit = new JButton("Exit");

        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnExit.setFont(new Font("SansSerif", Font.PLAIN, 13));

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnExit);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Attach Event Listeners
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });

        btnExit.addActionListener(e -> System.exit(0));

        // Allow pressing Enter in password field to login
        txtPassword.addActionListener(e -> performLogin());

        add(mainPanel);
    }

    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (DEMO_USERNAME.equals(username) && DEMO_PASSWORD.equals(password)) {
            JOptionPane.showMessageDialog(this,
                    "Login successful! Welcome to the Payroll Management System.",
                    "Authentication Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Close login frame
            new DashboardFrame().setVisible(true); // Open dashboard
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password. Please try again.\n(Hint: admin / admin123)",
                    "Authentication Failed",
                    JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
            txtPassword.requestFocus();
        }
    }
}
