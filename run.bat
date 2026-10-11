@echo off
echo ====================================================================
echo  Disaster Management Resource Allocation System
echo  Course: 24GEP31 - Mini Project (Second Review)
echo  Aruvi District Emergency Command Operations
echo ====================================================================

if not exist bin (
    echo Compiling application...
    call compile.bat
)

echo Starting Server on http://localhost:8080 ...
java -cp "bin;lib\mysql-connector-j-26.7.0.jar" com.aruvi.dms.Main
