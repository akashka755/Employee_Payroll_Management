package gui;

import database.DatabaseHelper;
import payroll.Payroll;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Monthly Payroll Summary Report Window.
 * Aggregates all employee payrolls processed for a specific calendar month.
 * Displays individual entries and calculates payroll totals.
 */
public class ReportFrame extends JFrame {

    private JComboBox<String> cbMonth;
    private JComboBox<Integer> cbYear;
    private JButton btnGenerate;

    private JTable tableReport;
    private DefaultTableModel tableModel;

    // Totals labels
    private JLabel lblCount;
    private JLabel lblTotalGross;
    private JLabel lblTotalDeductions;
    private JLabel lblTotalNet;

    private static final String[] MONTH_NAMES = {
            "1 - January", "2 - February", "3 - March", "4 - April",
            "5 - May", "6 - June", "7 - July", "8 - August",
            "9 - September", "10 - October", "11 - November", "12 - December"
    };

    public ReportFrame() {
        setTitle("Monthly Payroll Summary Report");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        generateReport(); // Auto-generate for current default selection (October 2026)
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Header Title
        JLabel lblTitle = new JLabel("Monthly Organization Payroll Report", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Center Content Container
        JPanel centerContainer = new JPanel(new BorderLayout(10, 10));

        // Top Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        filterBar.setBorder(BorderFactory.createTitledBorder("Select Reporting Period"));

        cbMonth = new JComboBox<>(MONTH_NAMES);
        cbMonth.setSelectedIndex(9); // Default to October (index 9)
        cbYear = new JComboBox<>(new Integer[]{2024, 2025, 2026, 2027, 2028});
        cbYear.setSelectedItem(2026);

        btnGenerate = new JButton("Generate Report");
        btnGenerate.setFont(new Font("SansSerif", Font.BOLD, 12));

        filterBar.add(new JLabel("Month:"));
        filterBar.add(cbMonth);
        filterBar.add(new JLabel("Year:"));
        filterBar.add(cbYear);
        filterBar.add(btnGenerate);

        centerContainer.add(filterBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Emp ID", "Employee Name", "Department", "Designation", "Gross Salary (₹)", "Deductions (₹)", "Net Salary (₹)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tableReport = new JTable(tableModel);
        tableReport.setRowHeight(24);
        tableReport.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(tableReport);
        centerContainer.add(scrollPane, BorderLayout.CENTER);

        // Bottom Totals Card
        JPanel totalsCard = new JPanel(new GridLayout(2, 4, 10, 8));
        totalsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Payroll Summary Totals"),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        totalsCard.setBackground(new Color(245, 247, 250));

        lblCount = new JLabel("0");
        lblTotalGross = new JLabel("₹ 0.00");
        lblTotalDeductions = new JLabel("₹ 0.00");
        lblTotalNet = new JLabel("₹ 0.00");

        lblTotalNet.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTotalNet.setForeground(new Color(0, 120, 0));

        totalsCard.add(createHeaderLabel("Employees Processed:"));
        totalsCard.add(createHeaderLabel("Total Gross Payroll:"));
        totalsCard.add(createHeaderLabel("Total Deductions:"));
        totalsCard.add(createHeaderLabel("Total Net Payroll:"));

        totalsCard.add(lblCount);
        totalsCard.add(lblTotalGross);
        totalsCard.add(lblTotalDeductions);
        totalsCard.add(lblTotalNet);

        centerContainer.add(totalsCard, BorderLayout.SOUTH);
        mainPanel.add(centerContainer, BorderLayout.CENTER);

        add(mainPanel);

        // Action Listener
        btnGenerate.addActionListener(e -> generateReport());
    }

    private JLabel createHeaderLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        return l;
    }

    private void generateReport() {
        int month = cbMonth.getSelectedIndex() + 1;
        int year = (Integer) cbYear.getSelectedItem();

        try {
            tableModel.setRowCount(0);
            List<Payroll> list = DatabaseHelper.getMonthlyReport(month, year);

            BigDecimal sumGross = BigDecimal.ZERO;
            BigDecimal sumDeductions = BigDecimal.ZERO;
            BigDecimal sumNet = BigDecimal.ZERO;

            for (Payroll p : list) {
                tableModel.addRow(new Object[]{
                        p.getEmployeeId(),
                        p.getEmployeeName(),
                        p.getDepartmentName(),
                        p.getDesignation(),
                        "₹ " + p.getGrossSalary(),
                        "₹ " + p.getTotalDeductions(),
                        "₹ " + p.getNetSalary()
                });

                sumGross = sumGross.add(p.getGrossSalary());
                sumDeductions = sumDeductions.add(p.getTotalDeductions());
                sumNet = sumNet.add(p.getNetSalary());
            }

            lblCount.setText(String.valueOf(list.size()));
            lblTotalGross.setText("₹ " + sumGross);
            lblTotalDeductions.setText("₹ " + sumDeductions);
            lblTotalNet.setText("₹ " + sumNet);

            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No processed payroll records found for " + month + "/" + year + ".",
                        "Report Empty", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
