package payroll;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;

/**
 * Represents a processed payroll record and implements salary calculation logic.
 * Demonstrates:
 * - Implementation of SalaryCalculable interface.
 * - Accurate financial computation using BigDecimal.
 * - Encapsulation of historical payroll snapshots.
 */
public class Payroll implements SalaryCalculable {

    // Configurable constants for calculation
    public static final BigDecimal DEFAULT_HRA_RATE = new BigDecimal("0.20");      // 20% HRA
    public static final BigDecimal DEFAULT_OVERTIME_RATE = new BigDecimal("100.00"); // ₹100 per hour
    public static final BigDecimal SALARY_DAYS_DIVISOR = new BigDecimal("30");     // 30 days divisor for daily salary

    // Instance attributes (stored snapshot in DB)
    private int payrollId;
    private int employeeId;
    private String employeeName;     // Auxiliary field for display
    private String departmentName;   // Auxiliary field for display
    private String designation;      // Auxiliary field for display
    private int month;
    private int year;
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private double overtimeHours;
    private BigDecimal overtimePay;
    private BigDecimal grossSalary;
    private BigDecimal unpaidLeaveDeduction;
    private BigDecimal otherDeductions;
    private BigDecimal totalDeductions;
    private BigDecimal netSalary;
    private Timestamp processedDate;

    // Default constructor
    public Payroll() {
    }

    // Full constructor
    public Payroll(int payrollId, int employeeId, int month, int year,
                   BigDecimal basicSalary, BigDecimal hra, double overtimeHours,
                   BigDecimal overtimePay, BigDecimal grossSalary,
                   BigDecimal unpaidLeaveDeduction, BigDecimal otherDeductions,
                   BigDecimal totalDeductions, BigDecimal netSalary,
                   Timestamp processedDate) {
        this.payrollId = payrollId;
        this.employeeId = employeeId;
        this.month = month;
        this.year = year;
        this.basicSalary = basicSalary.setScale(2, RoundingMode.HALF_UP);
        this.hra = hra.setScale(2, RoundingMode.HALF_UP);
        this.overtimeHours = overtimeHours;
        this.overtimePay = overtimePay.setScale(2, RoundingMode.HALF_UP);
        this.grossSalary = grossSalary.setScale(2, RoundingMode.HALF_UP);
        this.unpaidLeaveDeduction = unpaidLeaveDeduction.setScale(2, RoundingMode.HALF_UP);
        this.otherDeductions = otherDeductions.setScale(2, RoundingMode.HALF_UP);
        this.totalDeductions = totalDeductions.setScale(2, RoundingMode.HALF_UP);
        this.netSalary = netSalary.setScale(2, RoundingMode.HALF_UP);
        this.processedDate = processedDate;
    }

    // =========================================================================
    // SalaryCalculable Interface Implementation
    // =========================================================================

    @Override
    public BigDecimal calculateHra(BigDecimal basicSalary) {
        if (basicSalary == null || basicSalary.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return basicSalary.multiply(DEFAULT_HRA_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateOvertimePay(double overtimeHours) {
        if (overtimeHours < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(overtimeHours)
                .multiply(DEFAULT_OVERTIME_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateGrossSalary(BigDecimal basicSalary, BigDecimal hra, BigDecimal overtimePay) {
        BigDecimal safeBasic = basicSalary != null ? basicSalary : BigDecimal.ZERO;
        BigDecimal safeHra = hra != null ? hra : BigDecimal.ZERO;
        BigDecimal safeOt = overtimePay != null ? overtimePay : BigDecimal.ZERO;
        return safeBasic.add(safeHra).add(safeOt).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateUnpaidLeaveDeduction(BigDecimal basicSalary, int absentDays) {
        if (basicSalary == null || basicSalary.compareTo(BigDecimal.ZERO) <= 0 || absentDays <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        // Daily salary = Basic / 30
        BigDecimal dailySalary = basicSalary.divide(SALARY_DAYS_DIVISOR, 4, RoundingMode.HALF_UP);
        return dailySalary.multiply(BigDecimal.valueOf(absentDays)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateTotalDeductions(BigDecimal unpaidLeaveDeduction, BigDecimal otherDeductions) {
        BigDecimal safeUnpaid = unpaidLeaveDeduction != null ? unpaidLeaveDeduction : BigDecimal.ZERO;
        BigDecimal safeOther = otherDeductions != null ? otherDeductions : BigDecimal.ZERO;
        return safeUnpaid.add(safeOther).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateNetSalary(BigDecimal grossSalary, BigDecimal totalDeductions) throws ValidationException {
        BigDecimal safeGross = grossSalary != null ? grossSalary : BigDecimal.ZERO;
        BigDecimal safeDeductions = totalDeductions != null ? totalDeductions : BigDecimal.ZERO;
        BigDecimal net = safeGross.subtract(safeDeductions).setScale(2, RoundingMode.HALF_UP);

        if (net.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Net salary cannot be negative (Calculated Net: ₹" + net + "). " +
                    "Total deductions (₹" + safeDeductions + ") exceed gross earnings (₹" + safeGross + ").");
        }
        return net;
    }

    /**
     * Factory/processor helper method to compute a complete Payroll instance from Employee and Attendance records.
     */
    public static Payroll compute(Employee employee, Attendance attendance, BigDecimal otherDeductions) throws ValidationException {
        if (employee == null) {
            throw new ValidationException("Cannot process payroll without valid employee data.");
        }
        if (attendance == null) {
            throw new ValidationException("No attendance record found for Employee ID: " + employee.getEmployeeId() + " in this period.");
        }
        if (otherDeductions == null || otherDeductions.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Other deductions cannot be negative.");
        }

        Payroll p = new Payroll();
        p.setEmployeeId(employee.getEmployeeId());
        p.setEmployeeName(employee.getName());
        p.setDepartmentName(employee.getDepartmentName());
        p.setDesignation(employee.getDesignation());
        p.setMonth(attendance.getMonth());
        p.setYear(attendance.getYear());

        p.setBasicSalary(employee.getBasicSalary());
        p.setHra(p.calculateHra(employee.getBasicSalary()));
        p.setOvertimeHours(attendance.getOvertimeHours());
        p.setOvertimePay(p.calculateOvertimePay(attendance.getOvertimeHours()));
        p.setGrossSalary(p.calculateGrossSalary(p.getBasicSalary(), p.getHra(), p.getOvertimePay()));

        p.setUnpaidLeaveDeduction(p.calculateUnpaidLeaveDeduction(p.getBasicSalary(), attendance.getAbsentDays()));
        p.setOtherDeductions(otherDeductions);
        p.setTotalDeductions(p.calculateTotalDeductions(p.getUnpaidLeaveDeduction(), p.getOtherDeductions()));
        p.setNetSalary(p.calculateNetSalary(p.getGrossSalary(), p.getTotalDeductions()));

        return p;
    }

    // Getters and Setters
    public int getPayrollId() {
        return payrollId;
    }

    public void setPayrollId(int payrollId) {
        this.payrollId = payrollId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public BigDecimal getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(BigDecimal basicSalary) {
        this.basicSalary = basicSalary != null ? basicSalary.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getHra() {
        return hra;
    }

    public void setHra(BigDecimal hra) {
        this.hra = hra != null ? hra.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public double getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(double overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public BigDecimal getOvertimePay() {
        return overtimePay;
    }

    public void setOvertimePay(BigDecimal overtimePay) {
        this.overtimePay = overtimePay != null ? overtimePay.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getGrossSalary() {
        return grossSalary;
    }

    public void setGrossSalary(BigDecimal grossSalary) {
        this.grossSalary = grossSalary != null ? grossSalary.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getUnpaidLeaveDeduction() {
        return unpaidLeaveDeduction;
    }

    public void setUnpaidLeaveDeduction(BigDecimal unpaidLeaveDeduction) {
        this.unpaidLeaveDeduction = unpaidLeaveDeduction != null ? unpaidLeaveDeduction.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getOtherDeductions() {
        return otherDeductions;
    }

    public void setOtherDeductions(BigDecimal otherDeductions) {
        this.otherDeductions = otherDeductions != null ? otherDeductions.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(BigDecimal totalDeductions) {
        this.totalDeductions = totalDeductions != null ? totalDeductions.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(BigDecimal netSalary) {
        this.netSalary = netSalary != null ? netSalary.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public Timestamp getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(Timestamp processedDate) {
        this.processedDate = processedDate;
    }
}
