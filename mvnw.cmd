@REM ==========================================
@REM SmartJob Maven Wrapper for Windows
@REM ==========================================
@REM This script routes to the locally installed Maven.
@REM Usage: mvnw.cmd clean test
@REM        mvnw.cmd spring-boot:run

@echo off
setlocal

set "MAVEN_CMD=C:\Users\hp\AppData\Local\Programs\apache-maven-3.9.6\bin\mvn.cmd"

if not exist "%MAVEN_CMD%" (
    echo [ERROR] Maven not found at %MAVEN_CMD%
    echo Please install Maven or update the path in this script.
    exit /b 1
)

call "%MAVEN_CMD%" %*
