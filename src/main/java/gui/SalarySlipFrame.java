package gui;

import database.DatabaseHelper;
import payroll.Employee;
import payroll.Payroll;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Salary Slip Viewer Window.
 * Retrieves and presents historical payroll snapshots for a selected employee and month.
 * Does not recompute or overwrite database values.
 */
public class SalarySlipFrame extends JFrame {

    private JComboBox<Employee> cbEmployee;
    private JComboBox<String> cbMonth;
    private JComboBox<Integer> cbYear;
    private JButton btnViewSlip;

    // Display Labels for Salary Slip
    private JLabel lblEmpId;
    private JLabel lblEmpName;
    private JLabel lblDept;
    private JLabel lblDesignation;
    private JLabel lblPeriod;
    private JLabel lblProcessedDate;

    // Earnings
    private JLabel lblBasic;
    private JLabel lblHra;
    private JLabel lblOtHours;
    private JLabel lblOtPay;
    private JLabel lblGross;

    // Deductions
    private JLabel lblUnpaidDed;
    private JLabel lblOtherDed;
    private JLabel lblTotalDed;

    // Net
    private JLabel lblNetSalary;

    private static final String[] MONTH_NAMES = {
            "1 - January", "2 - February", "3 - March", "4 - April",
            "5 - May", "6 - June", "7 - July", "8 - August",
            "9 - September", "10 - October", "11 - November", "12 - December"
    };

    public SalarySlipFrame() {
        setTitle("Employee Salary Slip Viewer");
        setSize(780, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadEmployees();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Top Filter Controls
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        topPanel.setBorder(BorderFactory.createTitledBorder("Select Employee & Month"));

        cbEmployee = new JComboBox<>();
        cbMonth = new JComboBox<>(MONTH_NAMES);
        cbYear = new JComboBox<>(new Integer[]{2024, 2025, 2026, 2027, 2028});
        cbYear.setSelectedItem(2026);
        btnViewSlip = new JButton("View Salary Slip");
        btnViewSlip.setFont(new Font("SansSerif", Font.BOLD, 12));

        topPanel.add(new JLabel("Employee:"));
        topPanel.add(cbEmployee);
        topPanel.add(new JLabel("Month:"));
        topPanel.add(cbMonth);
        topPanel.add(new JLabel("Year:"));
        topPanel.add(cbYear);
        topPanel.add(btnViewSlip);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center Slip Container (Pay Slip Sheet design)
        JPanel slipCard = new JPanel(new BorderLayout(10, 15));
        slipCard.setBackground(Color.WHITE);
        slipCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 2),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        // Header inside Slip
        JPanel slipHeader = new JPanel(new GridLayout(2, 1, 3, 3));
        slipHeader.setBackground(Color.WHITE);
        JLabel compTitle = new JLabel("XYZ ENTERPRISES PRIVATE LIMITED", SwingConstants.CENTER);
        compTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        compTitle.setForeground(new Color(30, 60, 120));

        JLabel docTitle = new JLabel("CONFIDENTIAL SALARY PAY SLIP", SwingConstants.CENTER);
        docTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        docTitle.setForeground(Color.DARK_GRAY);

        slipHeader.add(compTitle);
        slipHeader.add(docTitle);
        slipCard.add(slipHeader, BorderLayout.NORTH);

        // Middle Slip Info
        JPanel slipBody = new JPanel(new BorderLayout(10, 15));
        slipBody.setBackground(Color.WHITE);

        // Employee Metadata Grid
        JPanel empMetaGrid = new JPanel(new GridLayout(3, 4, 10, 8));
        empMetaGrid.setBackground(new Color(248, 250, 252));
        empMetaGrid.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        lblEmpId = new JLabel("-");
        lblEmpName = new JLabel("-");
        lblDept = new JLabel("-");
        lblDesignation = new JLabel("-");
        lblPeriod = new JLabel("-");
        lblProcessedDate = new JLabel("-");

        empMetaGrid.add(createBoldLabel("Employee ID:"));
        empMetaGrid.add(lblEmpId);
        empMetaGrid.add(createBoldLabel("Employee Name:"));
        empMetaGrid.add(lblEmpName);

        empMetaGrid.add(createBoldLabel("Department:"));
        empMetaGrid.add(lblDept);
        empMetaGrid.add(createBoldLabel("Designation:"));
        empMetaGrid.add(lblDesignation);

        empMetaGrid.add(createBoldLabel("Pay Period:"));
        empMetaGrid.add(lblPeriod);
        empMetaGrid.add(createBoldLabel("Generated On:"));
        empMetaGrid.add(lblProcessedDate);

        slipBody.add(empMetaGrid, BorderLayout.NORTH);

        // Side-by-side Earnings and Deductions tables
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        tablesPanel.setBackground(Color.WHITE);

        // Earnings Column
        JPanel earningsPanel = new JPanel(new GridLayout(6, 2, 8, 8));
        earningsPanel.setBackground(Color.WHITE);
        earningsPanel.setBorder(BorderFactory.createTitledBorder("Earnings"));

        lblBasic = new JLabel("₹ 0.00");
        lblHra = new JLabel("₹ 0.00");
        lblOtHours = new JLabel("0.0 hrs");
        lblOtPay = new JLabel("₹ 0.00");
        lblGross = new JLabel("₹ 0.00");
        lblGross.setFont(new Font("SansSerif", Font.BOLD, 13));

        earningsPanel.add(new JLabel("Basic Salary:"));
        earningsPanel.add(lblBasic);
        earningsPanel.add(new JLabel("HRA (20%):"));
        earningsPanel.add(lblHra);
        earningsPanel.add(new JLabel("Overtime Hours:"));
        earningsPanel.add(lblOtHours);
        earningsPanel.add(new JLabel("Overtime Pay:"));
        earningsPanel.add(lblOtPay);
        earningsPanel.add(new JLabel("----------------"));
        earningsPanel.add(new JLabel("----------------"));
        earningsPanel.add(createBoldLabel("Gross Salary:"));
        earningsPanel.add(lblGross);

        tablesPanel.add(earningsPanel);

        // Deductions Column
        JPanel deductionsPanel = new JPanel(new GridLayout(6, 2, 8, 8));
        deductionsPanel.setBackground(Color.WHITE);
        deductionsPanel.setBorder(BorderFactory.createTitledBorder("Deductions"));

        lblUnpaidDed = new JLabel("₹ 0.00");
        lblOtherDed = new JLabel("₹ 0.00");
        lblTotalDed = new JLabel("₹ 0.00");
        lblTotalDed.setFont(new Font("SansSerif", Font.BOLD, 13));

        deductionsPanel.add(new JLabel("Unpaid Leave Deduction:"));
        deductionsPanel.add(lblUnpaidDed);
        deductionsPanel.add(new JLabel("Other Deductions:"));
        deductionsPanel.add(lblOtherDed);
        deductionsPanel.add(new JLabel(""));
        deductionsPanel.add(new JLabel(""));
        deductionsPanel.add(new JLabel(""));
        deductionsPanel.add(new JLabel(""));
        deductionsPanel.add(new JLabel("----------------"));
        deductionsPanel.add(new JLabel("----------------"));
        deductionsPanel.add(createBoldLabel("Total Deductions:"));
        deductionsPanel.add(lblTotalDed);

        tablesPanel.add(deductionsPanel);
        slipBody.add(tablesPanel, BorderLayout.CENTER);

        // Bottom Net Pay Banner
        JPanel netPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        netPanel.setBackground(new Color(230, 245, 230));
        netPanel.setBorder(BorderFactory.createLineBorder(new Color(40, 167, 69), 1));

        JLabel lblNetTag = new JLabel("NET TAKE-HOME SALARY:");
        lblNetTag.setFont(new Font("SansSerif", Font.BOLD, 15));

        lblNetSalary = new JLabel("₹ 0.00");
        lblNetSalary.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblNetSalary.setForeground(new Color(0, 120, 0));

        netPanel.add(lblNetTag);
        netPanel.add(lblNetSalary);

        slipBody.add(netPanel, BorderLayout.SOUTH);
        slipCard.add(slipBody, BorderLayout.CENTER);

        mainPanel.add(slipCard, BorderLayout.CENTER);
        add(mainPanel);

        // Event listener
        btnViewSlip.addActionListener(e -> loadSalarySlip());
    }

    private JLabel createBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        return label;
    }

    private void loadEmployees() {
        try {
            cbEmployee.removeAllItems();
            List<Employee> list = DatabaseHelper.getAllEmployees();
            for (Employee emp : list) {
                cbEmployee.addItem(emp);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Database error while loading employees: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSalarySlip() {
        Employee emp = (Employee) cbEmployee.getSelectedItem();
        if (emp == null) {
            JOptionPane.showMessageDialog(this, "Please select an employee.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int month = cbMonth.getSelectedIndex() + 1;
        int year = (Integer) cbYear.getSelectedItem();

        try {
            Payroll p = DatabaseHelper.getPayroll(emp.getEmployeeId(), month, year);
            if (p == null) {
                JOptionPane.showMessageDialog(this,
                        "No payroll record found for " + emp.getName() + " for " + month + "/" + year + ".\n" +
                                "Please process payroll first in the Payroll Processing module.",
                        "Payroll Not Found", JOptionPane.INFORMATION_MESSAGE);
                resetSlipFields();
                return;
            }

            // Populate snapshot values directly from DB record
            lblEmpId.setText(String.valueOf(p.getEmployeeId()));
            lblEmpName.setText(p.getEmployeeName());
            lblDept.setText(p.getDepartmentName());
            lblDesignation.setText(p.getDesignation());
            lblPeriod.setText(p.getMonth() + " / " + p.getYear());
            lblProcessedDate.setText(p.getProcessedDate() != null ? p.getProcessedDate().toString().substring(0, 16) : "-");

            lblBasic.setText("₹ " + p.getBasicSalary());
            lblHra.setText("₹ " + p.getHra());
            lblOtHours.setText(p.getOvertimeHours() + " hrs");
            lblOtPay.setText("₹ " + p.getOvertimePay());
            lblGross.setText("₹ " + p.getGrossSalary());

            lblUnpaidDed.setText("₹ " + p.getUnpaidLeaveDeduction());
            lblOtherDed.setText("₹ " + p.getOtherDeductions());
            lblTotalDed.setText("₹ " + p.getTotalDeductions());

            lblNetSalary.setText("₹ " + p.getNetSalary());

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetSlipFields() {
        lblEmpId.setText("-");
        lblEmpName.setText("-");
        lblDept.setText("-");
        lblDesignation.setText("-");
        lblPeriod.setText("-");
        lblProcessedDate.setText("-");

        lblBasic.setText("₹ 0.00");
        lblHra.setText("₹ 0.00");
        lblOtHours.setText("0.0 hrs");
        lblOtPay.setText("₹ 0.00");
        lblGross.setText("₹ 0.00");

        lblUnpaidDed.setText("₹ 0.00");
        lblOtherDed.setText("₹ 0.00");
        lblTotalDed.setText("₹ 0.00");

        lblNetSalary.setText("₹ 0.00");
    }
}
