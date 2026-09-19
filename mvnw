#!/bin/sh
# ==========================================
# SmartJob Maven Wrapper for Unix / macOS
# ==========================================
if command -v mvn >/dev/null 2>&1; then
    exec mvn "$@"
else
    echo "[ERROR] Maven ('mvn') is not found in PATH."
    echo "Please install Maven: https://maven.apache.org/install.html"
    exit 1
fi
