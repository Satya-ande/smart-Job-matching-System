@echo off
echo ========================================================
echo  Compiling SmartJob Matcher Sources...
echo ========================================================
if not exist "bin" mkdir bin
javac -d bin src\main\java\com\smartjob\model\*.java src\main\java\com\smartjob\util\*.java src\main\java\com\smartjob\repository\*.java src\main\java\com\smartjob\service\*.java src\main\java\com\smartjob\Main.java src\test\java\com\smartjob\*.java
if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation finished successfully.
) else (
    echo [ERROR] Compilation failed.
)
