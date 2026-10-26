package gui;

import database.DatabaseHelper;
import payroll.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Employee Management Window.
 * Provides Add, View, Update, Delete, and Search operations for employees.
 * Demonstrates:
 * - JTable with DefaultTableModel.
 * - JComboBox for Department selection.
 * - Form event handling with selection synchronization.
 * - Exception handling and validation feedback via JOptionPane.
 */
public class EmployeeFrame extends JFrame {

    private JTextField txtId;
    private JTextField txtName;
    private JComboBox<Department> cbDepartment;
    private JTextField txtDesignation;
    private JTextField txtContact;
    private JTextField txtJoiningDate;
    private JComboBox<String> cbType;
    private JTextField txtBasicSalary;

    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnResetSearch;

    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    private JTable tableEmployees;
    private DefaultTableModel tableModel;

    public EmployeeFrame() {
        setTitle("Employee Management");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadDepartments();
        loadEmployeeTable();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Title
        JLabel lblTitle = new JLabel("Employee Records Management", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Center Split: Left = Input Form, Right = Table & Search
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));

        // LEFT FORM PANEL
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setBorder(BorderFactory.createTitledBorder("Employee Details"));
        formWrapper.setPreferredSize(new Dimension(380, 500));

        JPanel formGrid = new JPanel(new GridLayout(8, 2, 8, 12));
        formGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtId = new JTextField();
        txtId.setEditable(false);
        txtId.setBackground(new Color(240, 240, 240));

        txtName = new JTextField();
        cbDepartment = new JComboBox<>();
        txtDesignation = new JTextField();
        txtContact = new JTextField();
        txtJoiningDate = new JTextField(); // YYYY-MM-DD
        cbType = new JComboBox<>(new String[]{"Permanent", "Contract"});
        txtBasicSalary = new JTextField();

        formGrid.add(new JLabel("Employee ID:"));
        formGrid.add(txtId);

        formGrid.add(new JLabel("Full Name *:"));
        formGrid.add(txtName);

        formGrid.add(new JLabel("Department *:"));
        formGrid.add(cbDepartment);

        formGrid.add(new JLabel("Designation *:"));
        formGrid.add(txtDesignation);

        formGrid.add(new JLabel("Contact Info *:"));
        formGrid.add(txtContact);

        formGrid.add(new JLabel("Joining Date (YYYY-MM-DD) *:"));
        formGrid.add(txtJoiningDate);

        formGrid.add(new JLabel("Employee Type *:"));
        formGrid.add(cbType);

        formGrid.add(new JLabel("Basic Salary (₹) *:"));
        formGrid.add(txtBasicSalary);

        formWrapper.add(formGrid, BorderLayout.CENTER);

        // Buttons under Form
        JPanel formButtonPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        formButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        btnAdd = new JButton("Add Employee");
        btnUpdate = new JButton("Update Employee");
        btnDelete = new JButton("Delete Employee");
        btnClear = new JButton("Clear Form");

        formButtonPanel.add(btnAdd);
        formButtonPanel.add(btnUpdate);
        formButtonPanel.add(btnDelete);
        formButtonPanel.add(btnClear);

        formWrapper.add(formButtonPanel, BorderLayout.SOUTH);
        centerPanel.add(formWrapper, BorderLayout.WEST);

        // RIGHT PANEL: Search bar and Table
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));

        // Search Bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search"));
        txtSearch = new JTextField(18);
        btnSearch = new JButton("Search");
        btnResetSearch = new JButton("Reset");

        searchPanel.add(new JLabel("Search (Name/Designation/Dept):"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnResetSearch);

        rightPanel.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Name", "Department", "Designation", "Contact", "Joining Date", "Type", "Basic Salary"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only cells
            }
        };

        tableEmployees = new JTable(tableModel);
        tableEmployees.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableEmployees.setRowHeight(22);
        JScrollPane scrollPane = new JScrollPane(tableEmployees);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(rightPanel, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);

        // Attach listeners
        btnAdd.addActionListener(e -> addEmployee());
        btnUpdate.addActionListener(e -> updateEmployee());
        btnDelete.addActionListener(e -> deleteEmployee());
        btnClear.addActionListener(e -> clearForm());

        btnSearch.addActionListener(e -> searchEmployees());
        btnResetSearch.addActionListener(e -> {
            txtSearch.setText("");
            loadEmployeeTable();
        });

        tableEmployees.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableEmployees.getSelectedRow() != -1) {
                populateFormFromSelectedRow();
            }
        });
    }

    private void loadDepartments() {
        try {
            cbDepartment.removeAllItems();
            List<Department> list = DatabaseHelper.getAllDepartments();
            for (Department d : list) {
                cbDepartment.addItem(d);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Database error while loading departments: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadEmployeeTable() {
        try {
            tableModel.setRowCount(0);
            List<Employee> list = DatabaseHelper.getAllEmployees();
            for (Employee emp : list) {
                tableModel.addRow(new Object[]{
                        emp.getEmployeeId(),
                        emp.getName(),
                        emp.getDepartmentName(),
                        emp.getDesignation(),
                        emp.getContactInfo(),
                        emp.getJoiningDate(),
                        emp.getEmployeeType(),
                        emp.getBasicSalary()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Database error while loading employees: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchEmployees() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadEmployeeTable();
            return;
        }
        try {
            tableModel.setRowCount(0);
            List<Employee> list = DatabaseHelper.searchEmployees(keyword);
            for (Employee emp : list) {
                tableModel.addRow(new Object[]{
                        emp.getEmployeeId(),
                        emp.getName(),
                        emp.getDepartmentName(),
                        emp.getDesignation(),
                        emp.getContactInfo(),
                        emp.getJoiningDate(),
                        emp.getEmployeeType(),
                        emp.getBasicSalary()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Search error: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateFormFromSelectedRow() {
        int row = tableEmployees.getSelectedRow();
        if (row < 0) return;

        txtId.setText(tableModel.getValueAt(row, 0).toString());
        txtName.setText(tableModel.getValueAt(row, 1).toString());

        String deptName = tableModel.getValueAt(row, 2).toString();
        for (int i = 0; i < cbDepartment.getItemCount(); i++) {
            if (cbDepartment.getItemAt(i).getDepartmentName().equalsIgnoreCase(deptName)) {
                cbDepartment.setSelectedIndex(i);
                break;
            }
        }

        txtDesignation.setText(tableModel.getValueAt(row, 3).toString());
        txtContact.setText(tableModel.getValueAt(row, 4).toString());
        txtJoiningDate.setText(tableModel.getValueAt(row, 5).toString());
        cbType.setSelectedItem(tableModel.getValueAt(row, 6).toString());
        txtBasicSalary.setText(tableModel.getValueAt(row, 7).toString());
    }

    private void addEmployee() {
        try {
            Employee emp = buildEmployeeFromForm(0);
            int generatedId = DatabaseHelper.addEmployee(emp);
            JOptionPane.showMessageDialog(this,
                    "Employee added successfully with ID: " + generatedId,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadEmployeeTable();
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, ve.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException se) {
            JOptionPane.showMessageDialog(this, "Database Error: " + se.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateEmployee() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an employee from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int empId = Integer.parseInt(idStr);
            Employee emp = buildEmployeeFromForm(empId);
            DatabaseHelper.updateEmployee(emp);
            JOptionPane.showMessageDialog(this, "Employee updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadEmployeeTable();
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, ve.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException se) {
            JOptionPane.showMessageDialog(this, "Database Error: " + se.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteEmployee() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an employee from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int empId = Integer.parseInt(idStr);
        String name = txtName.getText().trim();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete employee: " + name + " (ID: " + empId + ")?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                DatabaseHelper.deleteEmployee(empId);
                JOptionPane.showMessageDialog(this, "Employee deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadEmployeeTable();
            } catch (ValidationException ve) {
                JOptionPane.showMessageDialog(this, ve.getMessage(), "Cannot Delete", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException se) {
                JOptionPane.showMessageDialog(this, "Database Error: " + se.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Employee buildEmployeeFromForm(int id) throws ValidationException {
        String name = txtName.getText().trim();
        Department dept = (Department) cbDepartment.getSelectedItem();
        if (dept == null) {
            throw new ValidationException("Please select a department. If none exist, add one in Department Management first.");
        }
        String designation = txtDesignation.getText().trim();
        String contact = txtContact.getText().trim();
        String joiningDate = txtJoiningDate.getText().trim();
        String type = (String) cbType.getSelectedItem();
        String salaryStr = txtBasicSalary.getText().trim();

        if (salaryStr.isEmpty()) {
            throw new ValidationException("Basic salary is required.");
        }

        BigDecimal basicSalary;
        try {
            basicSalary = new BigDecimal(salaryStr);
        } catch (NumberFormatException e) {
            throw new ValidationException("Basic salary must be a valid numeric amount (e.g., 50000.00).");
        }

        if ("Contract".equalsIgnoreCase(type)) {
            return new ContractEmployee(id, name, dept.getDepartmentId(), designation, contact, joiningDate, basicSalary);
        } else {
            return new PermanentEmployee(id, name, dept.getDepartmentId(), designation, contact, joiningDate, basicSalary);
        }
    }

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        if (cbDepartment.getItemCount() > 0) cbDepartment.setSelectedIndex(0);
        txtDesignation.setText("");
        txtContact.setText("");
        txtJoiningDate.setText("");
        cbType.setSelectedIndex(0);
        txtBasicSalary.setText("");
        tableEmployees.clearSelection();
    }
}
