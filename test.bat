@echo off
echo ====================================================================
echo  Running Automated Test Suite
echo  Course: 24GEP31 - Mini Project (Second Review)
echo ====================================================================

if not exist bin (
    call compile.bat
)

java -cp "bin;lib\mysql-connector-j-26.7.0.jar" com.aruvi.dms.test.TestRunner
