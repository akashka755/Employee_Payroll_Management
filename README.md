# Employee Payroll Management System (Java Swing + MySQL + JDBC)

**Course:** PBCST304 – Object Oriented Programming (Semester 3)  
**Platform:** Fedora Linux 44 (GNOME) / Any standard Java environment  
**Tech Stack:** Java 25, Java Swing, MySQL 8.4, JDBC (MySQL Connector/J 26.10.0)

---

## 1. Project Overview

The **Employee Payroll Management System** is a complete, desktop-based business application developed in Java using standard Swing GUI components and a relational MySQL database connected via JDBC.

It models real-world corporate payroll workflows—employee records, departmental structure, monthly attendance tracking, rule-based salary calculation (HRA, Overtime, Absent-day unpaid leave deductions), historical salary slip viewing, and aggregate monthly payroll reports.

The project strictly follows the **PBCST304 syllabus**, demonstrating core Object-Oriented Programming (OOP) concepts:
- **Abstraction:** Abstract `Employee` base class.
- **Inheritance & Polymorphism:** `PermanentEmployee` and `ContractEmployee` subclasses overriding common behavior with dynamic method dispatch.
- **Interfaces:** `SalaryCalculable` contract implemented by `Payroll`.
- **Encapsulation:** Private fields, parameterized constructors, standard getters/setters.
- **Exception Handling:** Built-in exceptions and custom `ValidationException`.
- **GUI Event-Driven Programming:** Swing components and the Delegation Event Model (`ActionListener`).
- **Database Persistence:** JDBC `Connection`, `PreparedStatement`, `ResultSet`, and transactional commits.

---

## 2. Key Features

1. **Administrator Authentication:** Secure login screen with demonstration credentials (`admin` / `admin123`).
2. **Central Dashboard:** Clean navigation to all application modules and single-click Logout.
3. **Department Management:** Add, view, update, search, and delete departments; prevents deletion of departments assigned to employees.
4. **Employee Management:** Add, update, delete, search, and view permanent and contractual employees with salary specifications.
5. **Attendance Management:** Monthly tracking of present days, absent days, and overtime hours, strictly validated against the actual number of days in the calendar month.
6. **Payroll Processing:** High-precision monetary computation using `BigDecimal`:
   - Basic Salary
   - House Rent Allowance (HRA) @ 20%
   - Overtime Compensation @ ₹100/hr
   - Gross Salary = Basic + HRA + Overtime Pay
   - Unpaid-Leave Deduction = (Basic / 30) × Absent Days
   - Total Deductions = Unpaid-Leave + Other Deductions
   - Net Salary = Gross Salary − Total Deductions
   - Rejection of invalid negative net salaries and prevention of duplicate monthly payrolls.
7. **Salary Slips:** Clean on-screen pay slip viewing retrieved from persistent historical database snapshots (never recomputes or corrupts historical figures).
8. **Monthly Payroll Reports:** Aggregated organization payroll summaries showing per-employee figures and total gross payroll, total deductions, and net payroll.

---

## 3. Technology Stack & Environment Details

- **Programming Language:** Java (OpenJDK 25)
- **GUI Toolkit:** Java Swing (`JFrame`, `JPanel`, `JTable`, `JComboBox`, `JOptionPane`, `BorderLayout`, `GridLayout`, `FlowLayout`)
- **Database:** MySQL Server 8.4.10 (Service: `mysqld`)
- **Database Driver:** MySQL Connector/J 26.10.0 (`/usr/share/java/mysql-connector-j.jar`)
- **Operating System:** Fedora Linux 44 (x86_64)

> [!IMPORTANT]
> **Environment Package Prerequisite on Fedora:**
> Fedora minimal/headless installations only have `java-25-openjdk-headless`.
> To compile using `javac` and run Swing GUI applications on your Fedora desktop, install the full OpenJDK and development tools:
> ```bash
> sudo dnf install -y java-25-openjdk java-25-openjdk-devel
> ```

---

## 4. Database Setup Instructions

1. Ensure the MySQL service is active:
   ```bash
   sudo systemctl status mysqld
   # If not running: sudo systemctl start mysqld
   ```

2. Execute the database script `database.sql` to create `employee_payroll_db`, its tables, foreign key constraints, and sample seed data:
   ```bash
   mysql -u root < database.sql
   ```
   *(If your root account requires a password, use `mysql -u root -p < database.sql` and type your password).*

3. Verify database creation:
   ```bash
   mysql -u root -e "USE employee_payroll_db; SHOW TABLES;"
   ```

---

## 5. JDBC Configuration

The database connection settings are stored in `src/main/java/database/DatabaseConnection.java`:

```java
public static final String DB_URL = "jdbc:mysql://localhost:3306/employee_payroll_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
public static final String DB_USER = "root";       // Change if using a custom user
public static final String DB_PASSWORD = "";       // Set your MySQL password here (or leave empty)
```

If your MySQL configuration uses a different user or password, edit those constants in `DatabaseConnection.java`.

---

## 6. Build and Run Instructions

All operations can be performed directly from the terminal without needing Maven or third-party build tools.

### Option A: Using Helper Scripts (Fastest)

```bash
cd /home/akashka/EmployeePayrollManagement

# 1. Run Automated Integration Tests (Headless verification)
./test.sh

# 2. Compile Java classes (requires javac / java-25-openjdk-devel)
./compile.sh

# 3. Launch GUI Application on your Fedora GNOME Desktop
./run.sh
```

### Option B: Manual Command-Line Compilation and Execution

1. **Compile all Java source files:**
   ```bash
   cd /home/akashka/EmployeePayrollManagement
   mkdir -p out
   find src/main/java -name "*.java" > sources.txt
   javac -cp "/usr/share/java/mysql-connector-j.jar" -d out @sources.txt
   rm sources.txt
   ```

2. **Run the Application:**
   ```bash
   java -cp "out:/usr/share/java/mysql-connector-j.jar" payroll.Main
   ```

3. **Run from source (Modern Java 22+ feature):**
   ```bash
   java -cp "src/main/java:/usr/share/java/mysql-connector-j.jar" src/main/java/payroll/Main.java
   ```

---

## 7. Demonstration Login Credentials

When the application starts, log in using the pre-configured administrator credentials:

| Field | Value |
|---|---|
| **Username** | `admin` |
| **Password** | `admin123` |

*(These credentials are defined as constants in `gui/LoginFrame.java` for academic evaluation).*

---

## 8. Automated Integration Tests

A comprehensive integration test suite is included in `src/main/java/payroll/SystemIntegrationTest.java`. It verifies 42 assertions covering:
- Authentication validation
- Runtime polymorphism & dynamic dispatch
- MySQL database connectivity
- Department CRUD & referential integrity
- Employee CRUD & type instantiation
- Attendance calendar constraints
- Salary calculation formulas with exact `BigDecimal` rounding
- Duplicate monthly payroll prevention
- Historical salary slip retrieval
- Monthly payroll summary aggregation
- Financial audit trail protection (preventing deletion of employees with active payroll records)

To run the test suite:
```bash
./test.sh
```

---

## 9. Common Errors and Troubleshooting

1. **`java.awt.HeadlessException: No X11 DISPLAY variable was set`**
   - **Cause:** Attempting to launch the Swing GUI from a remote SSH shell, background cron, or headless terminal without an active graphical session.
   - **Solution:** Open a terminal inside your GNOME desktop session (or verify `echo $DISPLAY` outputs `:0` or `$WAYLAND_DISPLAY` is set). Ensure `java-25-openjdk` (non-headless) is installed.

2. **`ClassNotFoundException: com.mysql.cj.jdbc.Driver`**
   - **Cause:** The MySQL Connector/J JAR is missing from the classpath.
   - **Solution:** Verify `/usr/share/java/mysql-connector-j.jar` exists. If not, install it with `sudo dnf install mysql-connector-j`. Always include it in the `-cp` argument.

3. **`Access denied for user 'root'@'localhost'`**
   - **Cause:** MySQL requires a password that does not match `DB_PASSWORD` in `DatabaseConnection.java`.
   - **Solution:** Open `src/main/java/database/DatabaseConnection.java` and update `DB_PASSWORD` to your MySQL root password.

4. **`Table 'employee_payroll_db.employees' doesn't exist`**
   - **Cause:** `database.sql` has not been executed yet.
   - **Solution:** Run `mysql -u root < database.sql`.
