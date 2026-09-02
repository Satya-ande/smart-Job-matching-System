@echo off
if not exist "bin\com\smartjob\TestRunner.class" (
    echo Compiling before running tests...
    call compile.bat
)
java -ea -cp bin com.smartjob.TestRunner
