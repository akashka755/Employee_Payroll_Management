package payroll;

import java.math.BigDecimal;

/**
 * Abstract class representing a generic Employee.
 * Demonstrates:
 * - Abstraction: Cannot be instantiated directly; provides common template.
 * - Encapsulation: Private instance variables accessible only through getters/setters.
 * - Inheritance & Polymorphism: Extended by PermanentEmployee and ContractEmployee.
 */
public abstract class Employee {
    private int employeeId;
    private String name;
    private int departmentId;
    private String departmentName; // Auxiliary field for GUI table display
    private String designation;
    private String contactInfo;
    private String joiningDate; // Format: YYYY-MM-DD
    private BigDecimal basicSalary;

    // Default constructor
    public Employee() {
    }

    // Parameterized constructor
    public Employee(int employeeId, String name, int departmentId, String designation,
                    String contactInfo, String joiningDate, BigDecimal basicSalary) {
        this.employeeId = employeeId;
        this.name = name;
        this.departmentId = departmentId;
        this.designation = designation;
        this.contactInfo = contactInfo;
        this.joiningDate = joiningDate;
        this.basicSalary = basicSalary;
    }

    // Abstract methods to be overridden by subclasses (Runtime Polymorphism)
    public abstract String getEmployeeType();
    public abstract String getEmploymentDetails();

    // Getters and Setters
    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
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

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(String joiningDate) {
        this.joiningDate = joiningDate;
    }

    public BigDecimal getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(BigDecimal basicSalary) {
        this.basicSalary = basicSalary;
    }

    @Override
    public String toString() {
        return employeeId + " - " + name + " (" + designation + ")";
    }
}
