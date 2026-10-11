@echo off
echo ====================================================================
echo  Compiling Disaster Management Resource Allocation System
echo ====================================================================

if not exist bin mkdir bin

javac -cp ".;lib\mysql-connector-j-26.7.0.jar" -d bin src\com\aruvi\dms\config\*.java src\com\aruvi\dms\model\*.java src\com\aruvi\dms\graph\*.java src\com\aruvi\dms\dao\*.java src\com\aruvi\dms\util\*.java src\com\aruvi\dms\service\*.java src\com\aruvi\dms\web\*.java src\com\aruvi\dms\test\*.java src\com\aruvi\dms\*.java

if %ERRORLEVEL% equ 0 (
    echo.
    echo Compilation successful! Class files generated in bin/
) else (
    echo.
    echo Compilation FAILED with error code %ERRORLEVEL%
)
