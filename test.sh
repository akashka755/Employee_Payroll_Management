#!/usr/bin/env bash

# =======================================================================
# Automated Test Runner for Employee Payroll Management System
# =======================================================================

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

JDBC_JAR="/usr/share/java/mysql-connector-j.jar"

if [ -f "out/payroll/SystemIntegrationTest.class" ]; then
    java -cp "out:$JDBC_JAR" payroll.SystemIntegrationTest
else
    java -cp "src/main/java:$JDBC_JAR" src/main/java/payroll/SystemIntegrationTest.java
fi
