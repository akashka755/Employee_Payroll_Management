package payroll;

/**
 * Represents an employee's monthly attendance summary.
 * Tracks present days, absent days, and overtime hours for a given month and year.
 */
public class Attendance {
    private int attendanceId;
    private int employeeId;
    private String employeeName; // Auxiliary field for GUI table display
    private int month;
    private int year;
    private int presentDays;
    private int absentDays;
    private double overtimeHours;

    public Attendance() {
    }

    public Attendance(int attendanceId, int employeeId, int month, int year,
                      int presentDays, int absentDays, double overtimeHours) {
        this.attendanceId = attendanceId;
        this.employeeId = employeeId;
        this.month = month;
        this.year = year;
        this.presentDays = presentDays;
        this.absentDays = absentDays;
        this.overtimeHours = overtimeHours;
    }

    public Attendance(int employeeId, int month, int year,
                      int presentDays, int absentDays, double overtimeHours) {
        this.employeeId = employeeId;
        this.month = month;
        this.year = year;
        this.presentDays = presentDays;
        this.absentDays = absentDays;
        this.overtimeHours = overtimeHours;
    }

    // Getters and Setters
    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
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

    public int getPresentDays() {
        return presentDays;
    }

    public void setPresentDays(int presentDays) {
        this.presentDays = presentDays;
    }

    public int getAbsentDays() {
        return absentDays;
    }

    public void setAbsentDays(int absentDays) {
        this.absentDays = absentDays;
    }

    public double getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(double overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    @Override
    public String toString() {
        return "Attendance[EmpID=" + employeeId + ", " + month + "/" + year +
               ", Present=" + presentDays + ", Absent=" + absentDays + ", OT=" + overtimeHours + "h]";
    }
}
