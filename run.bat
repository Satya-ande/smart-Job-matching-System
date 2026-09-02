@echo off
if not exist "bin\com\smartjob\Main.class" (
    echo Compiling before running...
    call compile.bat
)
java -cp bin com.smartjob.Main
