#!/usr/bin/env bash

# =======================================================================
# Execution Script for Employee Payroll Management System
# =======================================================================

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

JDBC_JAR="/usr/share/java/mysql-connector-j.jar"

if [ ! -f "$JDBC_JAR" ]; then
    echo "[ERROR] MySQL Connector/J JAR not found at $JDBC_JAR"
    exit 1
fi

if [ -f "out/payroll/Main.class" ]; then
    echo "[INFO] Running Employee Payroll Management System from compiled classes..."
    java -cp "out:$JDBC_JAR" payroll.Main "$@"
else
    echo "[INFO] Running Employee Payroll Management System directly from source..."
    java -cp "src/main/java:$JDBC_JAR" src/main/java/payroll/Main.java "$@"
fi
