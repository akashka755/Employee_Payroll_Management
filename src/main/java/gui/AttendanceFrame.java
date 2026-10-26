package gui;

import database.DatabaseHelper;
import payroll.Attendance;
import payroll.Employee;
import payroll.ValidationException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;

/**
 * Attendance Management Window.
 * Records and validates monthly present days, absent days, and overtime hours.
 * Validates days against the calendar month length (e.g., Leap years, 30/31 days).
 */
public class AttendanceFrame extends JFrame {

    private JComboBox<Employee> cbEmployee;
    private JComboBox<String> cbMonth;
    private JComboBox<Integer> cbYear;
    private JTextField txtPresentDays;
    private JTextField txtAbsentDays;
    private JTextField txtOvertimeHours;

    private JButton btnSave;
    private JButton btnDelete;
    private JButton btnClear;

    private JTable tableAttendance;
    private DefaultTableModel tableModel;

    private int selectedAttendanceId = 0;

    private static final String[] MONTH_NAMES = {
            "1 - January", "2 - February", "3 - March", "4 - April",
            "5 - May", "6 - June", "7 - July", "8 - August",
            "9 - September", "10 - October", "11 - November", "12 - December"
    };

    public AttendanceFrame() {
        setTitle("Attendance Management");
        setSize(900, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadEmployees();
        loadAttendanceTable();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JLabel lblTitle = new JLabel("Monthly Attendance Records", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Center Split
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));

        // FORM PANEL (WEST)
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setBorder(BorderFactory.createTitledBorder("Log Monthly Attendance"));
        formWrapper.setPreferredSize(new Dimension(360, 450));

        JPanel formGrid = new JPanel(new GridLayout(6, 2, 8, 12));
        formGrid.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        cbEmployee = new JComboBox<>();
        cbMonth = new JComboBox<>(MONTH_NAMES);
        cbYear = new JComboBox<>(new Integer[]{2024, 2025, 2026, 2027, 2028});
        cbYear.setSelectedItem(2026);

        txtPresentDays = new JTextField();
        txtAbsentDays = new JTextField();
        txtOvertimeHours = new JTextField("0.0");

        formGrid.add(new JLabel("Employee *:"));
        formGrid.add(cbEmployee);

        formGrid.add(new JLabel("Payroll Month *:"));
        formGrid.add(cbMonth);

        formGrid.add(new JLabel("Payroll Year *:"));
        formGrid.add(cbYear);

        formGrid.add(new JLabel("Present Days *:"));
        formGrid.add(txtPresentDays);

        formGrid.add(new JLabel("Absent Days *:"));
        formGrid.add(txtAbsentDays);

        formGrid.add(new JLabel("Overtime Hours *:"));
        formGrid.add(txtOvertimeHours);

        formWrapper.add(formGrid, BorderLayout.CENTER);

        // Buttons
        JPanel formButtonPanel = new JPanel(new GridLayout(1, 3, 8, 8));
        formButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 15, 10));

        btnSave = new JButton("Save Record");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear");

        formButtonPanel.add(btnSave);
        formButtonPanel.add(btnDelete);
        formButtonPanel.add(btnClear);

        formWrapper.add(formButtonPanel, BorderLayout.SOUTH);
        centerPanel.add(formWrapper, BorderLayout.WEST);

        // TABLE PANEL (CENTER)
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Attendance Log"));

        String[] cols = {"ID", "Emp ID", "Employee Name", "Month", "Year", "Present Days", "Absent Days", "OT (Hours)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tableAttendance = new JTable(tableModel);
        tableAttendance.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableAttendance.setRowHeight(22);
        JScrollPane scrollPane = new JScrollPane(tableAttendance);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);

        // Listeners
        btnSave.addActionListener(e -> saveAttendance());
        btnDelete.addActionListener(e -> deleteAttendance());
        btnClear.addActionListener(e -> clearForm());

        tableAttendance.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableAttendance.getSelectedRow() != -1) {
                populateFormFromRow();
            }
        });
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

    private void loadAttendanceTable() {
        try {
            tableModel.setRowCount(0);
            List<Attendance> list = DatabaseHelper.getAllAttendance();
            for (Attendance a : list) {
                tableModel.addRow(new Object[]{
                        a.getAttendanceId(),
                        a.getEmployeeId(),
                        a.getEmployeeName(),
                        a.getMonth(),
                        a.getYear(),
                        a.getPresentDays(),
                        a.getAbsentDays(),
                        a.getOvertimeHours()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Database error while loading attendance: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateFormFromRow() {
        int row = tableAttendance.getSelectedRow();
        if (row < 0) return;

        selectedAttendanceId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        int empId = Integer.parseInt(tableModel.getValueAt(row, 1).toString());
        int month = Integer.parseInt(tableModel.getValueAt(row, 3).toString());
        int year = Integer.parseInt(tableModel.getValueAt(row, 4).toString());

        for (int i = 0; i < cbEmployee.getItemCount(); i++) {
            if (cbEmployee.getItemAt(i).getEmployeeId() == empId) {
                cbEmployee.setSelectedIndex(i);
                break;
            }
        }

        cbMonth.setSelectedIndex(month - 1);
        cbYear.setSelectedItem(year);

        txtPresentDays.setText(tableModel.getValueAt(row, 5).toString());
        txtAbsentDays.setText(tableModel.getValueAt(row, 6).toString());
        txtOvertimeHours.setText(tableModel.getValueAt(row, 7).toString());
    }

    private void saveAttendance() {
        Employee emp = (Employee) cbEmployee.getSelectedItem();
        if (emp == null) {
            JOptionPane.showMessageDialog(this, "Please select an employee.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int month = cbMonth.getSelectedIndex() + 1;
        Integer year = (Integer) cbYear.getSelectedItem();
        if (year == null) year = 2026;

        String presentStr = txtPresentDays.getText().trim();
        String absentStr = txtAbsentDays.getText().trim();
        String otStr = txtOvertimeHours.getText().trim();

        if (presentStr.isEmpty() || absentStr.isEmpty() || otStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all attendance fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int presentDays;
        int absentDays;
        double overtimeHours;

        try {
            presentDays = Integer.parseInt(presentStr);
            absentDays = Integer.parseInt(absentStr);
            if (presentDays < 0 || absentDays < 0) {
                JOptionPane.showMessageDialog(this, "Present and absent days cannot be negative.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Present days and absent days must be whole numbers.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            overtimeHours = Double.parseDouble(otStr);
            if (overtimeHours < 0) {
                JOptionPane.showMessageDialog(this, "Overtime hours cannot be negative.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Overtime hours must be a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate against calendar days in the selected month
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        if (presentDays > daysInMonth) {
            JOptionPane.showMessageDialog(this,
                    "Present days (" + presentDays + ") cannot exceed total days in " +
                            YearMonth.of(year, month).getMonth().name() + " (" + daysInMonth + " days).",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (absentDays > daysInMonth) {
            JOptionPane.showMessageDialog(this,
                    "Absent days (" + absentDays + ") cannot exceed total days in " +
                            YearMonth.of(year, month).getMonth().name() + " (" + daysInMonth + " days).",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (presentDays + absentDays > daysInMonth) {
            JOptionPane.showMessageDialog(this,
                    "Total of present days (" + presentDays + ") and absent days (" + absentDays + ") is " +
                            (presentDays + absentDays) + ", which exceeds the month's total days (" + daysInMonth + ").",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Attendance att = new Attendance(selectedAttendanceId, emp.getEmployeeId(), month, year, presentDays, absentDays, overtimeHours);
            DatabaseHelper.saveOrUpdateAttendance(att);
            JOptionPane.showMessageDialog(this, "Attendance record saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadAttendanceTable();
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, ve.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException se) {
            JOptionPane.showMessageDialog(this, "Database Error: " + se.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAttendance() {
        if (selectedAttendanceId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select an attendance record from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this attendance record?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                DatabaseHelper.deleteAttendance(selectedAttendanceId);
                JOptionPane.showMessageDialog(this, "Attendance record deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadAttendanceTable();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedAttendanceId = 0;
        if (cbEmployee.getItemCount() > 0) cbEmployee.setSelectedIndex(0);
        cbMonth.setSelectedIndex(0);
        cbYear.setSelectedItem(2026);
        txtPresentDays.setText("");
        txtAbsentDays.setText("");
        txtOvertimeHours.setText("0.0");
        tableAttendance.clearSelection();
    }
}
