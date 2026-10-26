package payroll;

import java.math.BigDecimal;

/**
 * Concrete subclass representing a Contractual Employee.
 * Demonstrates inheritance and dynamic method dispatch when accessed via an Employee reference.
 */
public class ContractEmployee extends Employee {

    public ContractEmployee() {
        super();
    }

    public ContractEmployee(int employeeId, String name, int departmentId, String designation,
                            String contactInfo, String joiningDate, BigDecimal basicSalary) {
        super(employeeId, name, departmentId, designation, contactInfo, joiningDate, basicSalary);
    }

    @Override
    public String getEmployeeType() {
        return "Contract";
    }

    @Override
    public String getEmploymentDetails() {
        return "Contract Staff: Fixed-tenure contract, entitled to hourly overtime compensation.";
    }
}
