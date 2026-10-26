package database;

import payroll.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Centralized JDBC Data Access Helper.
 * Handles all CRUD operations, PreparedStatement queries, ResultSets,
 * and transactions with clean resource management.
 */
public class DatabaseHelper {

    // =========================================================================
    // DEPARTMENT CRUD OPERATIONS
    // =========================================================================

    public static List<Department> getAllDepartments() throws SQLException {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT department_id, department_name, description FROM departments ORDER BY department_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Department(
                        rs.getInt("department_id"),
                        rs.getString("department_name"),
                        rs.getString("description")
                ));
            }
        }
        return list;
    }

    public static Department getDepartmentById(int id) throws SQLException {
        String sql = "SELECT department_id, department_name, description FROM departments WHERE department_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Department(
                            rs.getInt("department_id"),
                            rs.getString("department_name"),
                            rs.getString("description")
                    );
                }
            }
        }
        return null;
    }

    public static void addDepartment(Department dept) throws SQLException, ValidationException {
        if (dept.getDepartmentName() == null || dept.getDepartmentName().trim().isEmpty()) {
            throw new ValidationException("Department name cannot be empty.");
        }
        String sql = "INSERT INTO departments (department_name, description) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dept.getDepartmentName().trim());
            ps.setString(2, dept.getDescription() != null ? dept.getDescription().trim() : "");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    dept.setDepartmentId(keys.getInt(1));
                }
            }
        }
    }

    public static void updateDepartment(Department dept) throws SQLException, ValidationException {
        if (dept.getDepartmentName() == null || dept.getDepartmentName().trim().isEmpty()) {
            throw new ValidationException("Department name cannot be empty.");
        }
        String sql = "UPDATE departments SET department_name = ?, description = ? WHERE department_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dept.getDepartmentName().trim());
            ps.setString(2, dept.getDescription() != null ? dept.getDescription().trim() : "");
            ps.setInt(3, dept.getDepartmentId());
            ps.executeUpdate();
        }
    }

    public static boolean isDepartmentAssigned(int departmentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM employees WHERE department_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public static void deleteDepartment(int departmentId) throws SQLException, ValidationException {
        if (isDepartmentAssigned(departmentId)) {
            throw new ValidationException("Cannot delete department (ID: " + departmentId +
                    ") because employees are still assigned to it. Reassign or remove employees first.");
        }
        String sql = "DELETE FROM departments WHERE department_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            ps.executeUpdate();
        }
    }

    public static List<Department> searchDepartments(String query) throws SQLException {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT department_id, department_name, description FROM departments " +
                     "WHERE department_name LIKE ? OR description LIKE ? ORDER BY department_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String wildcard = "%" + query.trim() + "%";
            ps.setString(1, wildcard);
            ps.setString(2, wildcard);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Department(
                            rs.getInt("department_id"),
                            rs.getString("department_name"),
                            rs.getString("description")
                    ));
                }
            }
        }
        return list;
    }

    // =========================================================================
    // EMPLOYEE CRUD OPERATIONS
    // =========================================================================

    public static List<Employee> getAllEmployees() throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT e.employee_id, e.name, e.department_id, d.department_name, e.designation, " +
                     "e.contact_info, e.joining_date, e.employee_type, e.basic_salary " +
                     "FROM employees e " +
                     "JOIN departments d ON e.department_id = d.department_id " +
                     "ORDER BY e.employee_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapEmployeeRow(rs));
            }
        }
        return list;
    }

    public static Employee getEmployeeById(int id) throws SQLException {
        String sql = "SELECT e.employee_id, e.name, e.department_id, d.department_name, e.designation, " +
                     "e.contact_info, e.joining_date, e.employee_type, e.basic_salary " +
                     "FROM employees e " +
                     "JOIN departments d ON e.department_id = d.department_id " +
                     "WHERE e.employee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEmployeeRow(rs);
                }
            }
        }
        return null;
    }

    public static int addEmployee(Employee emp) throws SQLException, ValidationException {
        validateEmployeeInput(emp);
        String sql = "INSERT INTO employees (name, department_id, designation, contact_info, joining_date, employee_type, basic_salary) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, emp.getName().trim());
            ps.setInt(2, emp.getDepartmentId());
            ps.setString(3, emp.getDesignation().trim());
            ps.setString(4, emp.getContactInfo().trim());
            ps.setString(5, emp.getJoiningDate().trim());
            ps.setString(6, emp.getEmployeeType());
            ps.setBigDecimal(7, emp.getBasicSalary());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    emp.setEmployeeId(id);
                    return id;
                }
            }
        }
        return 0;
    }

    public static void updateEmployee(Employee emp) throws SQLException, ValidationException {
        validateEmployeeInput(emp);
        String sql = "UPDATE employees SET name = ?, department_id = ?, designation = ?, contact_info = ?, " +
                     "joining_date = ?, employee_type = ?, basic_salary = ? WHERE employee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emp.getName().trim());
            ps.setInt(2, emp.getDepartmentId());
            ps.setString(3, emp.getDesignation().trim());
            ps.setString(4, emp.getContactInfo().trim());
            ps.setString(5, emp.getJoiningDate().trim());
            ps.setString(6, emp.getEmployeeType());
            ps.setBigDecimal(7, emp.getBasicSalary());
            ps.setInt(8, emp.getEmployeeId());
            ps.executeUpdate();
        }
    }

    public static void deleteEmployee(int employeeId) throws SQLException, ValidationException {
        // Attendance cascades on delete; check if payroll records exist to protect historical accounting
        String checkSql = "SELECT COUNT(*) FROM payroll WHERE employee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new ValidationException("Cannot delete Employee (ID: " + employeeId +
                            ") because historical payroll records exist. Deleting this employee would corrupt financial history.");
                }
            }
        }

        String sql = "DELETE FROM employees WHERE employee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.executeUpdate();
        }
    }

    public static List<Employee> searchEmployees(String keyword) throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT e.employee_id, e.name, e.department_id, d.department_name, e.designation, " +
                     "e.contact_info, e.joining_date, e.employee_type, e.basic_salary " +
                     "FROM employees e " +
                     "JOIN departments d ON e.department_id = d.department_id " +
                     "WHERE e.name LIKE ? OR e.designation LIKE ? OR d.department_name LIKE ? " +
                     "ORDER BY e.employee_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String wildcard = "%" + keyword.trim() + "%";
            ps.setString(1, wildcard);
            ps.setString(2, wildcard);
            ps.setString(3, wildcard);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEmployeeRow(rs));
                }
            }
        }
        return list;
    }

    private static Employee mapEmployeeRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("employee_id");
        String name = rs.getString("name");
        int deptId = rs.getInt("department_id");
        String deptName = rs.getString("department_name");
        String designation = rs.getString("designation");
        String contact = rs.getString("contact_info");
        String joinDate = rs.getString("joining_date");
        String type = rs.getString("employee_type");
        BigDecimal basic = rs.getBigDecimal("basic_salary");

        Employee emp;
        // Demonstrating dynamic instantiation and polymorphism
        if ("Contract".equalsIgnoreCase(type)) {
            emp = new ContractEmployee(id, name, deptId, designation, contact, joinDate, basic);
        } else {
            emp = new PermanentEmployee(id, name, deptId, designation, contact, joinDate, basic);
        }
        emp.setDepartmentName(deptName);
        return emp;
    }

    private static void validateEmployeeInput(Employee emp) throws ValidationException {
        if (emp.getName() == null || emp.getName().trim().isEmpty()) {
            throw new ValidationException("Employee name is required.");
        }
        if (emp.getDepartmentId() <= 0) {
            throw new ValidationException("Please select a valid department.");
        }
        if (emp.getDesignation() == null || emp.getDesignation().trim().isEmpty()) {
            throw new ValidationException("Designation is required.");
        }
        if (emp.getContactInfo() == null || emp.getContactInfo().trim().isEmpty()) {
            throw new ValidationException("Contact information is required.");
        }
        if (emp.getJoiningDate() == null || !emp.getJoiningDate().matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new ValidationException("Joining date must be in YYYY-MM-DD format.");
        }
        if (emp.getBasicSalary() == null || emp.getBasicSalary().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Basic salary must be non-negative.");
        }
    }

    // =========================================================================
    // ATTENDANCE OPERATIONS
    // =========================================================================

    public static List<Attendance> getAllAttendance() throws SQLException {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.attendance_id, a.employee_id, e.name AS employee_name, a.month, a.year, " +
                     "a.present_days, a.absent_days, a.overtime_hours " +
                     "FROM attendance a " +
                     "JOIN employees e ON a.employee_id = e.employee_id " +
                     "ORDER BY a.year DESC, a.month DESC, a.employee_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Attendance att = new Attendance(
                        rs.getInt("attendance_id"),
                        rs.getInt("employee_id"),
                        rs.getInt("month"),
                        rs.getInt("year"),
                        rs.getInt("present_days"),
                        rs.getInt("absent_days"),
                        rs.getDouble("overtime_hours")
                );
                att.setEmployeeName(rs.getString("employee_name"));
                list.add(att);
            }
        }
        return list;
    }

    public static Attendance getAttendance(int employeeId, int month, int year) throws SQLException {
        String sql = "SELECT a.attendance_id, a.employee_id, e.name AS employee_name, a.month, a.year, " +
                     "a.present_days, a.absent_days, a.overtime_hours " +
                     "FROM attendance a " +
                     "JOIN employees e ON a.employee_id = e.employee_id " +
                     "WHERE a.employee_id = ? AND a.month = ? AND a.year = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Attendance att = new Attendance(
                            rs.getInt("attendance_id"),
                            rs.getInt("employee_id"),
                            rs.getInt("month"),
                            rs.getInt("year"),
                            rs.getInt("present_days"),
                            rs.getInt("absent_days"),
                            rs.getDouble("overtime_hours")
                    );
                    att.setEmployeeName(rs.getString("employee_name"));
                    return att;
                }
            }
        }
        return null;
    }

    public static void saveOrUpdateAttendance(Attendance att) throws SQLException, ValidationException {
        // Duplicate check or update
        Attendance existing = getAttendance(att.getEmployeeId(), att.getMonth(), att.getYear());
        if (existing != null && existing.getAttendanceId() != att.getAttendanceId()) {
            // Update existing
            String updateSql = "UPDATE attendance SET present_days = ?, absent_days = ?, overtime_hours = ? WHERE attendance_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, att.getPresentDays());
                ps.setInt(2, att.getAbsentDays());
                ps.setDouble(3, att.getOvertimeHours());
                ps.setInt(4, existing.getAttendanceId());
                ps.executeUpdate();
                att.setAttendanceId(existing.getAttendanceId());
            }
        } else if (att.getAttendanceId() > 0) {
            String updateSql = "UPDATE attendance SET present_days = ?, absent_days = ?, overtime_hours = ? WHERE attendance_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, att.getPresentDays());
                ps.setInt(2, att.getAbsentDays());
                ps.setDouble(3, att.getOvertimeHours());
                ps.setInt(4, att.getAttendanceId());
                ps.executeUpdate();
            }
        } else {
            String insertSql = "INSERT INTO attendance (employee_id, month, year, present_days, absent_days, overtime_hours) " +
                               "VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, att.getEmployeeId());
                ps.setInt(2, att.getMonth());
                ps.setInt(3, att.getYear());
                ps.setInt(4, att.getPresentDays());
                ps.setInt(5, att.getAbsentDays());
                ps.setDouble(6, att.getOvertimeHours());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        att.setAttendanceId(keys.getInt(1));
                    }
                }
            }
        }
    }

    public static void deleteAttendance(int attendanceId) throws SQLException {
        String sql = "DELETE FROM attendance WHERE attendance_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attendanceId);
            ps.executeUpdate();
        }
    }

    // =========================================================================
    // PAYROLL OPERATIONS
    // =========================================================================

    public static boolean isPayrollProcessed(int employeeId, int month, int year) throws SQLException {
        String sql = "SELECT COUNT(*) FROM payroll WHERE employee_id = ? AND month = ? AND year = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public static Payroll getPayroll(int employeeId, int month, int year) throws SQLException {
        String sql = "SELECT p.*, e.name AS employee_name, d.department_name, e.designation " +
                     "FROM payroll p " +
                     "JOIN employees e ON p.employee_id = e.employee_id " +
                     "JOIN departments d ON e.department_id = d.department_id " +
                     "WHERE p.employee_id = ? AND p.month = ? AND p.year = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Payroll p = new Payroll(
                            rs.getInt("payroll_id"),
                            rs.getInt("employee_id"),
                            rs.getInt("month"),
                            rs.getInt("year"),
                            rs.getBigDecimal("basic_salary"),
                            rs.getBigDecimal("hra"),
                            rs.getDouble("overtime_hours"),
                            rs.getBigDecimal("overtime_pay"),
                            rs.getBigDecimal("gross_salary"),
                            rs.getBigDecimal("unpaid_leave_deduction"),
                            rs.getBigDecimal("other_deductions"),
                            rs.getBigDecimal("total_deductions"),
                            rs.getBigDecimal("net_salary"),
                            rs.getTimestamp("processed_date")
                    );
                    p.setEmployeeName(rs.getString("employee_name"));
                    p.setDepartmentName(rs.getString("department_name"));
                    p.setDesignation(rs.getString("designation"));
                    return p;
                }
            }
        }
        return null;
    }

    public static void savePayroll(Payroll p) throws SQLException, ValidationException {
        if (isPayrollProcessed(p.getEmployeeId(), p.getMonth(), p.getYear())) {
            throw new ValidationException("Payroll for Employee ID " + p.getEmployeeId() +
                    " for " + p.getMonth() + "/" + p.getYear() + " has already been processed and saved.");
        }

        String sql = "INSERT INTO payroll (employee_id, month, year, basic_salary, hra, overtime_hours, " +
                     "overtime_pay, gross_salary, unpaid_leave_deduction, other_deductions, total_deductions, net_salary) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Use transaction to ensure safe persistence
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, p.getEmployeeId());
                ps.setInt(2, p.getMonth());
                ps.setInt(3, p.getYear());
                ps.setBigDecimal(4, p.getBasicSalary());
                ps.setBigDecimal(5, p.getHra());
                ps.setDouble(6, p.getOvertimeHours());
                ps.setBigDecimal(7, p.getOvertimePay());
                ps.setBigDecimal(8, p.getGrossSalary());
                ps.setBigDecimal(9, p.getUnpaidLeaveDeduction());
                ps.setBigDecimal(10, p.getOtherDeductions());
                ps.setBigDecimal(11, p.getTotalDeductions());
                ps.setBigDecimal(12, p.getNetSalary());

                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        p.setPayrollId(keys.getInt(1));
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static List<Payroll> getMonthlyReport(int month, int year) throws SQLException {
        List<Payroll> list = new ArrayList<>();
        String sql = "SELECT p.*, e.name AS employee_name, d.department_name, e.designation " +
                     "FROM payroll p " +
                     "JOIN employees e ON p.employee_id = e.employee_id " +
                     "JOIN departments d ON e.department_id = d.department_id " +
                     "WHERE p.month = ? AND p.year = ? " +
                     "ORDER BY p.employee_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Payroll p = new Payroll(
                            rs.getInt("payroll_id"),
                            rs.getInt("employee_id"),
                            rs.getInt("month"),
                            rs.getInt("year"),
                            rs.getBigDecimal("basic_salary"),
                            rs.getBigDecimal("hra"),
                            rs.getDouble("overtime_hours"),
                            rs.getBigDecimal("overtime_pay"),
                            rs.getBigDecimal("gross_salary"),
                            rs.getBigDecimal("unpaid_leave_deduction"),
                            rs.getBigDecimal("other_deductions"),
                            rs.getBigDecimal("total_deductions"),
                            rs.getBigDecimal("net_salary"),
                            rs.getTimestamp("processed_date")
                    );
                    p.setEmployeeName(rs.getString("employee_name"));
                    p.setDepartmentName(rs.getString("department_name"));
                    p.setDesignation(rs.getString("designation"));
                    list.add(p);
                }
            }
        }
        return list;
    }
}
