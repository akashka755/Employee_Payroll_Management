package payroll;

import java.math.BigDecimal;

/**
 * Interface defining salary calculation rules.
 * Demonstrates Module 3 syllabus concept: Interfaces.
 */
public interface SalaryCalculable {

    /**
     * Calculates House Rent Allowance based on basic salary.
     */
    BigDecimal calculateHra(BigDecimal basicSalary);

    /**
     * Calculates overtime payment for total hours worked.
     */
    BigDecimal calculateOvertimePay(double overtimeHours);

    /**
     * Calculates gross salary by summing basic salary, HRA, and overtime pay.
     */
    BigDecimal calculateGrossSalary(BigDecimal basicSalary, BigDecimal hra, BigDecimal overtimePay);

    /**
     * Calculates unpaid-leave deduction based on absent days:
     * Daily Salary = Basic Salary / 30
     * Deduction = Daily Salary * absentDays
     */
    BigDecimal calculateUnpaidLeaveDeduction(BigDecimal basicSalary, int absentDays);

    /**
     * Calculates total deductions (unpaid leave + other voluntary/manual deductions).
     */
    BigDecimal calculateTotalDeductions(BigDecimal unpaidLeaveDeduction, BigDecimal otherDeductions);

    /**
     * Calculates net salary (Gross Salary - Total Deductions).
     * @throws ValidationException if net salary is negative.
     */
    BigDecimal calculateNetSalary(BigDecimal grossSalary, BigDecimal totalDeductions) throws ValidationException;
}
