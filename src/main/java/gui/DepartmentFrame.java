package gui;

import database.DatabaseHelper;
import payroll.Department;
import payroll.ValidationException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Department Management Window.
 * Provides Add, View, Update, Delete, and Search operations for departments.
 * Ensures referential integrity by preventing deletion of assigned departments.
 */
public class DepartmentFrame extends JFrame {

    private JTextField txtDeptId;
    private JTextField txtDeptName;
    private JTextField txtDescription;

    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnResetSearch;

    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    private JTable tableDepartments;
    private DefaultTableModel tableModel;

    public DepartmentFrame() {
        setTitle("Department Management");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadDepartmentTable();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Title
        JLabel lblTitle = new JLabel("Department Records Management", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Center Split
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));

        // LEFT FORM PANEL
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setBorder(BorderFactory.createTitledBorder("Department Info"));
        formWrapper.setPreferredSize(new Dimension(320, 400));

        JPanel formGrid = new JPanel(new GridLayout(3, 2, 8, 12));
        formGrid.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        txtDeptId = new JTextField();
        txtDeptId.setEditable(false);
        txtDeptId.setBackground(new Color(240, 240, 240));

        txtDeptName = new JTextField();
        txtDescription = new JTextField();

        formGrid.add(new JLabel("Department ID:"));
        formGrid.add(txtDeptId);

        formGrid.add(new JLabel("Department Name *:"));
        formGrid.add(txtDeptName);

        formGrid.add(new JLabel("Description:"));
        formGrid.add(txtDescription);

        formWrapper.add(formGrid, BorderLayout.CENTER);

        // Buttons
        JPanel formButtonPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        formButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 15, 10));

        btnAdd = new JButton("Add Dept");
        btnUpdate = new JButton("Update Dept");
        btnDelete = new JButton("Delete Dept");
        btnClear = new JButton("Clear Form");

        formButtonPanel.add(btnAdd);
        formButtonPanel.add(btnUpdate);
        formButtonPanel.add(btnDelete);
        formButtonPanel.add(btnClear);

        formWrapper.add(formButtonPanel, BorderLayout.SOUTH);
        centerPanel.add(formWrapper, BorderLayout.WEST);

        // RIGHT PANEL: Search and Table
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search"));
        txtSearch = new JTextField(15);
        btnSearch = new JButton("Search");
        btnResetSearch = new JButton("Reset");

        searchPanel.add(new JLabel("Keyword:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnResetSearch);

        rightPanel.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"Dept ID", "Department Name", "Description"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableDepartments = new JTable(tableModel);
        tableDepartments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableDepartments.setRowHeight(22);
        JScrollPane scrollPane = new JScrollPane(tableDepartments);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(rightPanel, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);

        // Attach Listeners
        btnAdd.addActionListener(e -> addDepartment());
        btnUpdate.addActionListener(e -> updateDepartment());
        btnDelete.addActionListener(e -> deleteDepartment());
        btnClear.addActionListener(e -> clearForm());

        btnSearch.addActionListener(e -> searchDepartments());
        btnResetSearch.addActionListener(e -> {
            txtSearch.setText("");
            loadDepartmentTable();
        });

        tableDepartments.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableDepartments.getSelectedRow() != -1) {
                populateFormFromSelectedRow();
            }
        });
    }

    private void loadDepartmentTable() {
        try {
            tableModel.setRowCount(0);
            List<Department> list = DatabaseHelper.getAllDepartments();
            for (Department d : list) {
                tableModel.addRow(new Object[]{
                        d.getDepartmentId(),
                        d.getDepartmentName(),
                        d.getDescription()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Database error while loading departments: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchDepartments() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadDepartmentTable();
            return;
        }
        try {
            tableModel.setRowCount(0);
            List<Department> list = DatabaseHelper.searchDepartments(keyword);
            for (Department d : list) {
                tableModel.addRow(new Object[]{
                        d.getDepartmentId(),
                        d.getDepartmentName(),
                        d.getDescription()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Search error: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateFormFromSelectedRow() {
        int row = tableDepartments.getSelectedRow();
        if (row < 0) return;

        txtDeptId.setText(tableModel.getValueAt(row, 0).toString());
        txtDeptName.setText(tableModel.getValueAt(row, 1).toString());
        Object desc = tableModel.getValueAt(row, 2);
        txtDescription.setText(desc != null ? desc.toString() : "");
    }

    private void addDepartment() {
        String name = txtDeptName.getText().trim();
        String desc = txtDescription.getText().trim();

        try {
            Department dept = new Department(name, desc);
            DatabaseHelper.addDepartment(dept);
            JOptionPane.showMessageDialog(this, "Department added successfully with ID: " + dept.getDepartmentId(), "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadDepartmentTable();
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, ve.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException se) {
            if (se.getMessage().contains("Duplicate entry")) {
                JOptionPane.showMessageDialog(this, "Department name '" + name + "' already exists. Please choose a unique name.", "Duplicate Department", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Database Error: " + se.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateDepartment() {
        String idStr = txtDeptId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a department from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            String name = txtDeptName.getText().trim();
            String desc = txtDescription.getText().trim();

            Department dept = new Department(id, name, desc);
            DatabaseHelper.updateDepartment(dept);
            JOptionPane.showMessageDialog(this, "Department updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadDepartmentTable();
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, ve.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException se) {
            if (se.getMessage().contains("Duplicate entry")) {
                JOptionPane.showMessageDialog(this, "A department with this name already exists.", "Duplicate Department", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Database Error: " + se.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteDepartment() {
        String idStr = txtDeptId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a department from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(idStr);
        String name = txtDeptName.getText().trim();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete department: " + name + " (ID: " + id + ")?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                DatabaseHelper.deleteDepartment(id);
                JOptionPane.showMessageDialog(this, "Department deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadDepartmentTable();
            } catch (ValidationException ve) {
                JOptionPane.showMessageDialog(this, ve.getMessage(), "Cannot Delete Department", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException se) {
                JOptionPane.showMessageDialog(this, "Database Error: " + se.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtDeptId.setText("");
        txtDeptName.setText("");
        txtDescription.setText("");
        tableDepartments.clearSelection();
    }
}
