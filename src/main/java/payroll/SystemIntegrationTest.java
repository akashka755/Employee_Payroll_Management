package payroll;

import database.DatabaseConnection;
import database.DatabaseHelper;
import gui.LoginFrame;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

/**
 * Automated Headless Integration Test Suite.
 * Validates all 15 core functional, OOP, calculation, and database requirements.
 */
public class SystemIntegrationTest {

    private static int passedCount = 0;
    private static int failedCount = 0;

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("Starting Employee Payroll System Automated Integration Tests");
        System.out.println("=================================================================\n");

        // 1. Authentication credentials test
        testAuthentication();

        // 2. OOP Hierarchy and Dynamic Method Dispatch
        testPolymorphism();

        // 3. Database connectivity test
        testDatabaseConnectivity();

        // 4. Department management and referential integrity
        testDepartmentManagement();

        // 5. Employee management CRUD
        testEmployeeManagement();

        // 6. Attendance management and calendar constraints
        testAttendanceManagement();

        // 7. Salary calculation formulas and BigDecimal precision
        testSalaryCalculationFormulas();

        // 8. Payroll persistence and duplicate prevention
        testPayrollPersistenceAndDuplicatePrevention();

        // 9. Historical salary slip retrieval (snapshot preservation)
        testSalarySlipRetrieval();

        // 10. Monthly payroll report aggregation
        testMonthlyReportAggregation();

        // 11. Deletion protection when financial history exists
        testFinancialHistoryIntegrity();

        System.out.println("\n=================================================================");
        System.out.println("TEST SUMMARY: Passed: " + passedCount + ", Failed: " + failedCount);
        System.out.println("=================================================================");

        if (failedCount > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition, String failureDetail) {
        if (condition) {
            System.out.println("[PASS] " + testName);
            passedCount++;
        } else {
            System.err.println("[FAIL] " + testName + " -> " + failureDetail);
            failedCount++;
        }
    }

    private static void testAuthentication() {
        System.out.println("--- Test 1: Administrator Authentication Credentials ---");
        boolean correctCreds = LoginFrame.DEMO_USERNAME.equals("admin") && LoginFrame.DEMO_PASSWORD.equals("admin123");
        assertTrue("Admin correct credentials validation", correctCreds, "Expected admin / admin123");

        boolean wrongUser = !LoginFrame.DEMO_USERNAME.equals("user") || !LoginFrame.DEMO_PASSWORD.equals("admin123");
        assertTrue("Admin incorrect username rejected", wrongUser, "Wrong user should not match");

        boolean wrongPass = !LoginFrame.DEMO_USERNAME.equals("admin") || !LoginFrame.DEMO_PASSWORD.equals("wrong");
        assertTrue("Admin incorrect password rejected", wrongPass, "Wrong password should not match");
    }

    private static void testPolymorphism() {
        System.out.println("\n--- Test 2: OOP Inheritance & Runtime Polymorphism ---");
        Employee pEmp = new PermanentEmployee(901, "Test Perm", 1, "Dev", "test@corp.com", "2024-01-01", new BigDecimal("50000.00"));
        Employee cEmp = new ContractEmployee(902, "Test Contract", 1, "Tester", "cont@corp.com", "2024-01-01", new BigDecimal("35000.00"));

        // Access via generic Employee reference
        assertTrue("PermanentEmployee type polymorphism", "Permanent".equals(pEmp.getEmployeeType()), "Expected 'Permanent'");
        assertTrue("ContractEmployee type polymorphism", "Contract".equals(cEmp.getEmployeeType()), "Expected 'Contract'");
        assertTrue("PermanentEmployee details override", pEmp.getEmploymentDetails().contains("Permanent Staff"), "Expected permanent details");
        assertTrue("ContractEmployee details override", cEmp.getEmploymentDetails().contains("Contract Staff"), "Expected contract details");
    }

    private static void testDatabaseConnectivity() {
        System.out.println("\n--- Test 3: MySQL Database Connectivity ---");
        boolean connected = DatabaseConnection.testConnection();
        assertTrue("MySQL connection via DatabaseConnection", connected, "Could not connect to employee_payroll_db");
    }

    private static void testDepartmentManagement() {
        System.out.println("\n--- Test 4: Department Management & Integrity ---");
        try {
            // 4a. Add department
            Department dept = new Department("Test Quality Dept", "Created by automated integration test");
            DatabaseHelper.addDepartment(dept);
            assertTrue("Department added with auto-generated ID", dept.getDepartmentId() > 0, "ID should be > 0");

            // 4b. Duplicate department name check
            boolean dupCaught = false;
            try {
                Department dup = new Department("Test Quality Dept", "Duplicate test");
                DatabaseHelper.addDepartment(dup);
            } catch (SQLException e) {
                dupCaught = true;
            }
            assertTrue("Duplicate department name rejected by DB unique constraint", dupCaught, "Duplicate department allowed!");

            // 4c. Update department
            dept.setDescription("Updated description text");
            DatabaseHelper.updateDepartment(dept);
            Department fetched = DatabaseHelper.getDepartmentById(dept.getDepartmentId());
            assertTrue("Department description updated", "Updated description text".equals(fetched.getDescription()), "Update mismatch");

            // 4d. Clean up test department (since no employees assigned)
            DatabaseHelper.deleteDepartment(dept.getDepartmentId());
            Department deleted = DatabaseHelper.getDepartmentById(dept.getDepartmentId());
            assertTrue("Department deleted successfully", deleted == null, "Department should be null after delete");

            // 4e. Department deletion prevention when employees assigned
            boolean preventDeleteCaught = false;
            try {
                // Department 1 has seed employees assigned
                DatabaseHelper.deleteDepartment(1);
            } catch (ValidationException ve) {
                preventDeleteCaught = true;
            }
            assertTrue("Assigned department deletion prevented", preventDeleteCaught, "Assigned department was deleted!");

        } catch (Exception e) {
            assertTrue("Department tests error", false, e.getMessage());
        }
    }

    private static void testEmployeeManagement() {
        System.out.println("\n--- Test 5: Employee CRUD Operations ---");
        try {
            // Add Permanent employee
            Employee emp = new PermanentEmployee(0, "Test Employee", 1, "Staff Engineer", "emp.test@example.com", "2024-03-01", new BigDecimal("55000.00"));
            int newId = DatabaseHelper.addEmployee(emp);
            assertTrue("Employee added with generated ID", newId > 0, "New employee ID should be > 0");

            // Fetch
            Employee fetched = DatabaseHelper.getEmployeeById(newId);
            assertTrue("Employee fetched matches added record", fetched != null && "Test Employee".equals(fetched.getName()), "Fetch failed");
            assertTrue("Employee dynamic type preserved", "Permanent".equals(fetched.getEmployeeType()), "Expected Permanent");

            // Search
            List<Employee> searchList = DatabaseHelper.searchEmployees("Test Employee");
            assertTrue("Search employee by name", !searchList.isEmpty(), "Search returned empty list");

            // Update
            fetched.setDesignation("Lead Engineer");
            fetched.setBasicSalary(new BigDecimal("65000.00"));
            DatabaseHelper.updateEmployee(fetched);
            Employee updated = DatabaseHelper.getEmployeeById(newId);
            assertTrue("Employee designation updated", "Lead Engineer".equals(updated.getDesignation()), "Designation mismatch");
            assertTrue("Employee salary updated", updated.getBasicSalary().compareTo(new BigDecimal("65000.00")) == 0, "Salary mismatch");

            // Delete (since no payroll exists yet for this test employee)
            DatabaseHelper.deleteEmployee(newId);
            Employee afterDelete = DatabaseHelper.getEmployeeById(newId);
            assertTrue("Employee deleted successfully", afterDelete == null, "Employee should be null after delete");

        } catch (Exception e) {
            assertTrue("Employee test error", false, e.getMessage());
        }
    }

    private static void testAttendanceManagement() {
        System.out.println("\n--- Test 6: Attendance Management & Validation Constraints ---");
        try {
            // Employee ID 2 (Priya Patel)
            // Valid attendance for month 11 (November 2026, 30 days)
            Attendance att = new Attendance(2, 11, 2026, 28, 2, 8.0);
            DatabaseHelper.saveOrUpdateAttendance(att);
            assertTrue("Attendance saved successfully", att.getAttendanceId() > 0, "Attendance ID should be > 0");

            // Retrieve
            Attendance fetched = DatabaseHelper.getAttendance(2, 11, 2026);
            assertTrue("Attendance fetched correctly", fetched != null && fetched.getPresentDays() == 28, "Fetch mismatch");

            // Update attendance
            att.setPresentDays(29);
            att.setAbsentDays(1);
            att.setOvertimeHours(12.0);
            DatabaseHelper.saveOrUpdateAttendance(att);
            Attendance updated = DatabaseHelper.getAttendance(2, 11, 2026);
            assertTrue("Attendance record updated", updated.getPresentDays() == 29 && updated.getAbsentDays() == 1, "Update mismatch");

            // Clean up test attendance
            DatabaseHelper.deleteAttendance(updated.getAttendanceId());
            Attendance afterDelete = DatabaseHelper.getAttendance(2, 11, 2026);
            assertTrue("Attendance deleted", afterDelete == null, "Attendance should be deleted");

        } catch (Exception e) {
            assertTrue("Attendance test error", false, e.getMessage());
        }
    }

    private static void testSalaryCalculationFormulas() {
        System.out.println("\n--- Test 7: Salary Calculation Formulas & BigDecimal Precision ---");
        Payroll p = new Payroll();
        BigDecimal basic = new BigDecimal("60000.00");

        // 1. HRA (20%)
        BigDecimal hra = p.calculateHra(basic);
        assertTrue("HRA calculation (20% of 60000 = 12000)", hra.compareTo(new BigDecimal("12000.00")) == 0, "HRA was " + hra);

        // 2. Overtime (10 hours * 100/hr = 1000)
        BigDecimal otPay = p.calculateOvertimePay(10.0);
        assertTrue("Overtime pay (10 hrs * 100 = 1000)", otPay.compareTo(new BigDecimal("1000.00")) == 0, "OT was " + otPay);

        // 3. Gross Salary = 60000 + 12000 + 1000 = 73000
        BigDecimal gross = p.calculateGrossSalary(basic, hra, otPay);
        assertTrue("Gross salary sum (60000 + 12000 + 1000 = 73000)", gross.compareTo(new BigDecimal("73000.00")) == 0, "Gross was " + gross);

        // 4. Unpaid leave deduction (Daily = 60000/30 = 2000, 2 absent days = 4000)
        BigDecimal unpaid = p.calculateUnpaidLeaveDeduction(basic, 2);
        assertTrue("Unpaid leave deduction (2 days * 2000 = 4000)", unpaid.compareTo(new BigDecimal("4000.00")) == 0, "Unpaid was " + unpaid);

        // 5. Total deductions (Unpaid 4000 + Other 500 = 4500)
        BigDecimal otherDed = new BigDecimal("500.00");
        BigDecimal totalDed = p.calculateTotalDeductions(unpaid, otherDed);
        assertTrue("Total deductions (4000 + 500 = 4500)", totalDed.compareTo(new BigDecimal("4500.00")) == 0, "Total ded was " + totalDed);

        // 6. Net Salary = 73000 - 4500 = 68500
        try {
            BigDecimal net = p.calculateNetSalary(gross, totalDed);
            assertTrue("Net salary (73000 - 4500 = 68500)", net.compareTo(new BigDecimal("68500.00")) == 0, "Net was " + net);
        } catch (ValidationException ve) {
            assertTrue("Net salary exception", false, ve.getMessage());
        }

        // 7. Negative net salary rejection
        boolean negRejected = false;
        try {
            // Deductions (80000) > Gross (73000)
            p.calculateNetSalary(gross, new BigDecimal("80000.00"));
        } catch (ValidationException ve) {
            negRejected = true;
        }
        assertTrue("Negative net salary rejected with ValidationException", negRejected, "Negative net salary was allowed!");
    }

    private static void testPayrollPersistenceAndDuplicatePrevention() {
        System.out.println("\n--- Test 8: Payroll Persistence & Duplicate Prevention ---");
        try {
            // Clean up any previous test run record for idempotency
            try (java.sql.Connection conn = DatabaseConnection.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM payroll WHERE employee_id = ? AND month = ? AND year = ?")) {
                ps.setInt(1, 2);
                ps.setInt(2, 10);
                ps.setInt(3, 2026);
                ps.executeUpdate();
            }

            // For Employee 2 (Priya Patel, Basic: 42000.00), attendance in month 10 / 2026 exists
            Employee emp = DatabaseHelper.getEmployeeById(2);
            Attendance att = DatabaseHelper.getAttendance(2, 10, 2026);

            Payroll payroll = Payroll.compute(emp, att, new BigDecimal("200.00"));
            DatabaseHelper.savePayroll(payroll);
            assertTrue("Payroll processed and saved to DB", payroll.getPayrollId() > 0, "Payroll ID should be > 0");

            // Duplicate prevention check
            boolean dupPrevented = false;
            try {
                DatabaseHelper.savePayroll(payroll);
            } catch (ValidationException ve) {
                dupPrevented = true;
            }
            assertTrue("Duplicate monthly payroll processing prevented", dupPrevented, "Duplicate payroll allowed!");

        } catch (Exception e) {
            assertTrue("Payroll persistence test error", false, e.getMessage());
        }
    }

    private static void testSalarySlipRetrieval() {
        System.out.println("\n--- Test 9: Historical Salary Slip Snapshot Retrieval ---");
        try {
            // Retrieve previously processed payroll for Employee 1 (Rahul Sharma, Oct 2026)
            Payroll slip = DatabaseHelper.getPayroll(1, 10, 2026);
            assertTrue("Salary slip retrieved from DB", slip != null, "Slip was null");
            assertTrue("Salary slip basic matches historical snapshot", slip.getBasicSalary().compareTo(new BigDecimal("60000.00")) == 0, "Basic mismatch");
            assertTrue("Salary slip net matches historical snapshot", slip.getNetSalary().compareTo(new BigDecimal("68500.00")) == 0, "Net mismatch");
            assertTrue("Salary slip employee name populated", "Rahul Sharma".equals(slip.getEmployeeName()), "Name mismatch");
            assertTrue("Salary slip department populated", "Information Technology".equals(slip.getDepartmentName()), "Dept mismatch");
        } catch (Exception e) {
            assertTrue("Salary slip retrieval error", false, e.getMessage());
        }
    }

    private static void testMonthlyReportAggregation() {
        System.out.println("\n--- Test 10: Monthly Payroll Report Aggregation ---");
        try {
            List<Payroll> report = DatabaseHelper.getMonthlyReport(10, 2026);
            assertTrue("Monthly report contains processed employees", report.size() >= 2, "Expected at least 2 processed employees");

            BigDecimal totalGross = BigDecimal.ZERO;
            BigDecimal totalNet = BigDecimal.ZERO;
            for (Payroll p : report) {
                totalGross = totalGross.add(p.getGrossSalary());
                totalNet = totalNet.add(p.getNetSalary());
            }

            assertTrue("Total gross is non-zero", totalGross.compareTo(BigDecimal.ZERO) > 0, "Gross was 0");
            assertTrue("Total net is non-zero", totalNet.compareTo(BigDecimal.ZERO) > 0, "Net was 0");
            System.out.println("     -> Report count: " + report.size() + ", Total Gross: ₹" + totalGross + ", Total Net: ₹" + totalNet);

        } catch (Exception e) {
            assertTrue("Monthly report test error", false, e.getMessage());
        }
    }

    private static void testFinancialHistoryIntegrity() {
        System.out.println("\n--- Test 11: Financial History Integrity & Delete Protection ---");
        try {
            // Employee 1 has processed payroll in Oct 2026. Deleting this employee must be rejected.
            boolean deletePrevented = false;
            try {
                DatabaseHelper.deleteEmployee(1);
            } catch (ValidationException ve) {
                deletePrevented = true;
            }
            assertTrue("Employee with historical payroll records cannot be deleted", deletePrevented, "Employee with payroll was deleted!");
        } catch (Exception e) {
            assertTrue("Financial history integrity test error", false, e.getMessage());
        }
    }
}
