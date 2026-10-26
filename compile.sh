#!/usr/bin/env bash
set -e

# =======================================================================
# Compilation Script for Employee Payroll Management System
# =======================================================================

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

JDBC_JAR="/usr/share/java/mysql-connector-j.jar"

if [ ! -f "$JDBC_JAR" ]; then
    echo "[ERROR] MySQL Connector/J JAR not found at $JDBC_JAR"
    echo "Please ensure the connector is installed (e.g. sudo dnf install mysql-connector-j)"
    exit 1
fi

mkdir -p out

if command -v javac &> /dev/null; then
    echo "[INFO] Compiling Java source files using javac..."
    find src/main/java -name "*.java" > sources.txt
    javac -cp "$JDBC_JAR" -d out @sources.txt
    rm -f sources.txt
    echo "[SUCCESS] All source files compiled successfully into out/"
else
    echo "[NOTICE] javac is not currently installed in this environment."
    echo "To install the Java development kit on Fedora, run:"
    echo "    sudo dnf install -y java-25-openjdk java-25-openjdk-devel"
    echo ""
    echo "[INFO] Modern Java (Java 22+) allows running directly from source via ./run.sh"
fi
