package gui;

import database.DatabaseHelper;
import payroll.*;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Payroll Processing Window.
 * Coordinates salary calculations, attendance lookups, and payroll persistence.
 * Implements strict business rule validations:
 * - Requires existing attendance record.
 * - Prevents duplicate monthly payroll processing.
 * - Enforces non-negative net salary.
 */
public class PayrollFrame extends JFrame {

    private JComboBox<Employee> cbEmployee;
    private JComboBox<String> cbMonth;
    private JComboBox<Integer> cbYear;

    // Attendance display fields
    private JTextField txtPresentDays;
    private JTextField txtAbsentDays;
    private JTextField txtOvertimeHours;

    // Calculation inputs & outputs
    private JTextField txtBasicSalary;
    private JTextField txtHra;
    private JTextField txtOvertimePay;
    private JTextField txtGrossSalary;
    private JTextField txtUnpaidDeduction;
    private JTextField txtOtherDeductions;
    private JTextField txtTotalDeductions;
    private JTextField txtNetSalary;

    private JButton btnFetchAttendance;
    private JButton btnCalculate;
    private JButton btnSavePayroll;
    private JButton btnClear;

    private Attendance currentAttendance;
    private Payroll calculatedPayroll;

    private static final String[] MONTH_NAMES = {
            "1 - January", "2 - February", "3 - March", "4 - April",
            "5 - May", "6 - June", "7 - July", "8 - August",
            "9 - September", "10 - October", "11 - November", "12 - December"
    };

    public PayrollFrame() {
        setTitle("Payroll Processing");
        setSize(850, 680);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadEmployees();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Header
        JLabel lblTitle = new JLabel("Monthly Salary & Payroll Processing", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Center Panel: Divided into Selection, Attendance, and Breakdown
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));

        // TOP SELECTION PANEL
        JPanel selectionPanel = new JPanel(new GridLayout(2, 4, 10, 8));
        selectionPanel.setBorder(BorderFactory.createTitledBorder("Step 1: Select Employee & Payroll Period"));

        cbEmployee = new JComboBox<>();
        cbMonth = new JComboBox<>(MONTH_NAMES);
        cbYear = new JComboBox<>(new Integer[]{2024, 2025, 2026, 2027, 2028});
        cbYear.setSelectedItem(2026);
        btnFetchAttendance = new JButton("Load Attendance");

        selectionPanel.add(new JLabel("Employee:"));
        selectionPanel.add(new JLabel("Month:"));
        selectionPanel.add(new JLabel("Year:"));
        selectionPanel.add(new JLabel("Action:"));

        selectionPanel.add(cbEmployee);
        selectionPanel.add(cbMonth);
        selectionPanel.add(cbYear);
        selectionPanel.add(btnFetchAttendance);

        contentPanel.add(selectionPanel, BorderLayout.NORTH);

        // MIDDLE PANEL: Two columns (Attendance Info & Salary Calculation Breakdown)
        JPanel middlePanel = new JPanel(new GridLayout(1, 2, 15, 10));

        // LEFT: Attendance Summary
        JPanel attPanel = new JPanel(new GridLayout(4, 2, 8, 12));
        attPanel.setBorder(BorderFactory.createTitledBorder("Step 2: Attendance Summary"));

        txtPresentDays = createReadOnlyField();
        txtAbsentDays = createReadOnlyField();
        txtOvertimeHours = createReadOnlyField();
        txtOtherDeductions = new JTextField("0.00");

        attPanel.add(new JLabel("Present Days:"));
        attPanel.add(txtPresentDays);
        attPanel.add(new JLabel("Absent (Unpaid) Days:"));
        attPanel.add(txtAbsentDays);
        attPanel.add(new JLabel("Overtime Hours:"));
        attPanel.add(txtOvertimeHours);
        attPanel.add(new JLabel("Other Deductions (₹):"));
        attPanel.add(txtOtherDeductions);

        middlePanel.add(attPanel);

        // RIGHT: Salary Breakdown
        JPanel salaryPanel = new JPanel(new GridLayout(8, 2, 8, 8));
        salaryPanel.setBorder(BorderFactory.createTitledBorder("Step 3: Computed Salary Breakdown"));

        txtBasicSalary = createReadOnlyField();
        txtHra = createReadOnlyField();
        txtOvertimePay = createReadOnlyField();
        txtGrossSalary = createReadOnlyField();
        txtUnpaidDeduction = createReadOnlyField();
        txtTotalDeductions = createReadOnlyField();
        txtNetSalary = createReadOnlyField();
        txtNetSalary.setFont(new Font("SansSerif", Font.BOLD, 14));
        txtNetSalary.setForeground(new Color(0, 120, 0));

        salaryPanel.add(new JLabel("Basic Salary:"));
        salaryPanel.add(txtBasicSalary);
        salaryPanel.add(new JLabel("HRA (20%):"));
        salaryPanel.add(txtHra);
        salaryPanel.add(new JLabel("Overtime Pay (₹100/h):"));
        salaryPanel.add(txtOvertimePay);
        salaryPanel.add(new JLabel("Gross Salary:"));
        salaryPanel.add(txtGrossSalary);
        salaryPanel.add(new JLabel("Unpaid Leave Ded:"));
        salaryPanel.add(txtUnpaidDeduction);
        salaryPanel.add(new JLabel("Total Deductions:"));
        salaryPanel.add(txtTotalDeductions);
        salaryPanel.add(new JLabel("Final Net Salary:"));
        salaryPanel.add(txtNetSalary);

        middlePanel.add(salaryPanel);
        contentPanel.add(middlePanel, BorderLayout.CENTER);

        // BOTTOM BUTTON PANEL
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnCalculate = new JButton("Calculate & Preview");
        btnSavePayroll = new JButton("Confirm & Save Payroll");
        btnClear = new JButton("Reset Form");

        btnCalculate.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnSavePayroll.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnSavePayroll.setBackground(new Color(40, 167, 69));
        btnSavePayroll.setForeground(Color.WHITE);

        actionPanel.add(btnCalculate);
        actionPanel.add(btnSavePayroll);
        actionPanel.add(btnClear);

        contentPanel.add(actionPanel, BorderLayout.SOUTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);

        // Listeners
        btnFetchAttendance.addActionListener(e -> fetchAttendance());
        btnCalculate.addActionListener(e -> calculatePayroll());
        btnSavePayroll.addActionListener(e -> savePayroll());
        btnClear.addActionListener(e -> clearForm());
    }

    private JTextField createReadOnlyField() {
        JTextField tf = new JTextField();
        tf.setEditable(false);
        tf.setBackground(new Color(245, 245, 245));
        return tf;
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

    private void fetchAttendance() {
        Employee emp = (Employee) cbEmployee.getSelectedItem();
        if (emp == null) {
            JOptionPane.showMessageDialog(this, "Please select an employee.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int month = cbMonth.getSelectedIndex() + 1;
        int year = (Integer) cbYear.getSelectedItem();

        try {
            // Check if already processed
            if (DatabaseHelper.isPayrollProcessed(emp.getEmployeeId(), month, year)) {
                JOptionPane.showMessageDialog(this,
                        "Notice: Payroll for " + emp.getName() + " for " + month + "/" + year +
                                " has ALREADY been processed!\nYou can view the processed salary slip in the Salary Slips module.",
                        "Payroll Already Processed", JOptionPane.INFORMATION_MESSAGE);
            }

            currentAttendance = DatabaseHelper.getAttendance(emp.getEmployeeId(), month, year);
            if (currentAttendance == null) {
                JOptionPane.showMessageDialog(this,
                        "No attendance record found for " + emp.getName() + " in " + month + "/" + year + ".\n" +
                                "Please log attendance first in Attendance Management.",
                        "Attendance Missing", JOptionPane.WARNING_MESSAGE);
                txtPresentDays.setText("");
                txtAbsentDays.setText("");
                txtOvertimeHours.setText("");
                return;
            }

            txtPresentDays.setText(String.valueOf(currentAttendance.getPresentDays()));
            txtAbsentDays.setText(String.valueOf(currentAttendance.getAbsentDays()));
            txtOvertimeHours.setText(String.valueOf(currentAttendance.getOvertimeHours()));
            txtBasicSalary.setText(emp.getBasicSalary().toString());

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calculatePayroll() {
        Employee emp = (Employee) cbEmployee.getSelectedItem();
        if (emp == null || currentAttendance == null) {
            JOptionPane.showMessageDialog(this,
                    "Please load a valid attendance record first using 'Load Attendance'.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String otherDeductionsStr = txtOtherDeductions.getText().trim();
        BigDecimal otherDeductions;
        try {
            otherDeductions = new BigDecimal(otherDeductionsStr.isEmpty() ? "0.00" : otherDeductionsStr);
            if (otherDeductions.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "Other deductions cannot be negative.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Other deductions must be a valid numeric amount.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            calculatedPayroll = Payroll.compute(emp, currentAttendance, otherDeductions);

            txtBasicSalary.setText("₹ " + calculatedPayroll.getBasicSalary());
            txtHra.setText("₹ " + calculatedPayroll.getHra());
            txtOvertimePay.setText("₹ " + calculatedPayroll.getOvertimePay());
            txtGrossSalary.setText("₹ " + calculatedPayroll.getGrossSalary());
            txtUnpaidDeduction.setText("₹ " + calculatedPayroll.getUnpaidLeaveDeduction());
            txtTotalDeductions.setText("₹ " + calculatedPayroll.getTotalDeductions());
            txtNetSalary.setText("₹ " + calculatedPayroll.getNetSalary());

            JOptionPane.showMessageDialog(this,
                    "Salary calculation completed successfully.\nNet Salary: ₹" + calculatedPayroll.getNetSalary() +
                            "\nClick 'Confirm & Save Payroll' to store this record permanently.",
                    "Calculation Successful", JOptionPane.INFORMATION_MESSAGE);

        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, ve.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void savePayroll() {
        if (calculatedPayroll == null) {
            JOptionPane.showMessageDialog(this,
                    "Please calculate the payroll before saving.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm saving payroll for " + calculatedPayroll.getEmployeeName() + " (" +
                        calculatedPayroll.getMonth() + "/" + calculatedPayroll.getYear() + ")?\n" +
                        "Gross Salary: ₹" + calculatedPayroll.getGrossSalary() + "\n" +
                        "Total Deductions: ₹" + calculatedPayroll.getTotalDeductions() + "\n" +
                        "Net Salary: ₹" + calculatedPayroll.getNetSalary(),
                "Confirm Save", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                DatabaseHelper.savePayroll(calculatedPayroll);
                JOptionPane.showMessageDialog(this,
                        "Payroll processed and saved permanently to MySQL with Record ID: " + calculatedPayroll.getPayrollId(),
                        "Payroll Saved", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
            } catch (ValidationException ve) {
                JOptionPane.showMessageDialog(this, ve.getMessage(), "Duplicate Processing", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException se) {
                JOptionPane.showMessageDialog(this, "Database Error: " + se.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        currentAttendance = null;
        calculatedPayroll = null;
        txtPresentDays.setText("");
        txtAbsentDays.setText("");
        txtOvertimeHours.setText("");
        txtOtherDeductions.setText("0.00");
        txtBasicSalary.setText("");
        txtHra.setText("");
        txtOvertimePay.setText("");
        txtGrossSalary.setText("");
        txtUnpaidDeduction.setText("");
        txtTotalDeductions.setText("");
        txtNetSalary.setText("");
    }
}
