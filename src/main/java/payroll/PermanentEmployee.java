package payroll;

import java.math.BigDecimal;

/**
 * Concrete subclass representing a Permanent Employee.
 * Demonstrates single inheritance and method overriding.
 */
public class PermanentEmployee extends Employee {

    public PermanentEmployee() {
        super();
    }

    public PermanentEmployee(int employeeId, String name, int departmentId, String designation,
                             String contactInfo, String joiningDate, BigDecimal basicSalary) {
        super(employeeId, name, departmentId, designation, contactInfo, joiningDate, basicSalary);
    }

    @Override
    public String getEmployeeType() {
        return "Permanent";
    }

    @Override
    public String getEmploymentDetails() {
        return "Permanent Staff: Eligible for regular company allowances, HRA, and long-term benefits.";
    }
}
