-- =======================================================================
-- Employee Payroll Management System Database Script
-- Database: employee_payroll_db
-- =======================================================================

DROP DATABASE IF EXISTS employee_payroll_db;
CREATE DATABASE employee_payroll_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE employee_payroll_db;

-- 1. Departments Table
CREATE TABLE departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB;

-- 2. Employees Table
CREATE TABLE employees (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department_id INT NOT NULL,
    designation VARCHAR(100) NOT NULL,
    contact_info VARCHAR(100) NOT NULL,
    joining_date DATE NOT NULL,
    employee_type ENUM('Permanent', 'Contract') NOT NULL,
    basic_salary DECIMAL(10, 2) NOT NULL CHECK (basic_salary >= 0),
    CONSTRAINT fk_employee_department
        FOREIGN KEY (department_id)
        REFERENCES departments(department_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 3. Attendance Table (one monthly record per employee per month)
CREATE TABLE attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    month INT NOT NULL CHECK (month BETWEEN 1 AND 12),
    year INT NOT NULL CHECK (year >= 2000),
    present_days INT NOT NULL CHECK (present_days >= 0),
    absent_days INT NOT NULL CHECK (absent_days >= 0),
    overtime_hours DECIMAL(5, 2) NOT NULL DEFAULT 0.00 CHECK (overtime_hours >= 0),
    CONSTRAINT fk_attendance_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT uk_employee_attendance_month
        UNIQUE (employee_id, month, year)
) ENGINE=InnoDB;

-- 4. Payroll Table (historical snapshot per employee per month)
CREATE TABLE payroll (
    payroll_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    month INT NOT NULL CHECK (month BETWEEN 1 AND 12),
    year INT NOT NULL CHECK (year >= 2000),
    basic_salary DECIMAL(10, 2) NOT NULL,
    hra DECIMAL(10, 2) NOT NULL,
    overtime_hours DECIMAL(5, 2) NOT NULL,
    overtime_pay DECIMAL(10, 2) NOT NULL,
    gross_salary DECIMAL(10, 2) NOT NULL,
    unpaid_leave_deduction DECIMAL(10, 2) NOT NULL,
    other_deductions DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_deductions DECIMAL(10, 2) NOT NULL,
    net_salary DECIMAL(10, 2) NOT NULL CHECK (net_salary >= 0),
    processed_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payroll_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT uk_employee_payroll_month
        UNIQUE (employee_id, month, year)
) ENGINE=InnoDB;

-- =======================================================================
-- Sample Seed Data for Demonstration
-- =======================================================================

INSERT INTO departments (department_name, description) VALUES
('Information Technology', 'Software development, infrastructure, and technical support'),
('Human Resources', 'Recruitment, employee relations, and policy compliance'),
('Finance & Accounts', 'Financial planning, accounting, and payroll administration'),
('Marketing', 'Brand management, sales, and client outreach');

INSERT INTO employees (name, department_id, designation, contact_info, joining_date, employee_type, basic_salary) VALUES
('Rahul Sharma', 1, 'Senior Software Engineer', 'rahul.sharma@example.com', '2023-01-15', 'Permanent', 60000.00),
('Priya Patel', 2, 'HR Executive', 'priya.patel@example.com', '2023-06-01', 'Permanent', 42000.00),
('Amit Kumar', 1, 'QA Tester', 'amit.kumar@example.com', '2024-02-10', 'Contract', 35000.00),
('Sneha Rao', 3, 'Accountant', 'sneha.rao@example.com', '2022-11-20', 'Permanent', 48000.00);

-- Sample attendance for October 2026 (31 days)
INSERT INTO attendance (employee_id, month, year, present_days, absent_days, overtime_hours) VALUES
(1, 10, 2026, 29, 2, 10.00),
(2, 10, 2026, 30, 1, 4.00),
(3, 10, 2026, 28, 3, 15.00),
(4, 10, 2026, 31, 0, 0.00);

-- Sample processed payroll for Rahul Sharma for October 2026
-- Basic: 60000, HRA (20%): 12000, Overtime (10 hrs * 100): 1000, Gross: 73000
-- Daily Salary (60000 / 30) = 2000, Unpaid (2 days * 2000) = 4000, Other: 500, Total Ded: 4500
-- Net Salary = 73000 - 4500 = 68500.00
INSERT INTO payroll (employee_id, month, year, basic_salary, hra, overtime_hours, overtime_pay, gross_salary, unpaid_leave_deduction, other_deductions, total_deductions, net_salary) VALUES
(1, 10, 2026, 60000.00, 12000.00, 10.00, 1000.00, 73000.00, 4000.00, 500.00, 4500.00, 68500.00);
