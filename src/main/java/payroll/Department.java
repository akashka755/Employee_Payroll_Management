package payroll;

/**
 * Represents a Department entity.
 * Demonstrates encapsulation with private fields, constructors, and getters/setters.
 */
public class Department {
    private int departmentId;
    private String departmentName;
    private String description;

    // Default constructor
    public Department() {
    }

    // Constructor with all fields
    public Department(int departmentId, String departmentName, String description) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.description = description;
    }

    // Constructor without ID (for insertion where ID is auto-generated)
    public Department(String departmentName, String description) {
        this.departmentName = departmentName;
        this.description = description;
    }

    // Getters and Setters
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Override toString() for readable display in Swing JComboBox
    @Override
    public String toString() {
        return departmentId > 0 ? (departmentId + " - " + departmentName) : departmentName;
    }
}
