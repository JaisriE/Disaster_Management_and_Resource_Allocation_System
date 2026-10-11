#!/bin/bash
echo "===================================================================="
echo " Disaster Management Resource Allocation System"
echo " Course: 24GEP31 - Mini Project (Second Review)"
echo " Aruvi District Emergency Command Operations"
echo "===================================================================="

mkdir -p bin

echo "Compiling application..."
javac -cp ".:lib/mysql-connector-j-26.7.0.jar" -d bin src/com/aruvi/dms/config/*.java src/com/aruvi/dms/model/*.java src/com/aruvi/dms/graph/*.java src/com/aruvi/dms/dao/*.java src/com/aruvi/dms/util/*.java src/com/aruvi/dms/service/*.java src/com/aruvi/dms/web/*.java src/com/aruvi/dms/test/*.java src/com/aruvi/dms/*.java

if [ $? -eq 0 ]; then
    echo "Starting Server on http://localhost:8080 ..."
    java -cp "bin:lib/mysql-connector-j-26.7.0.jar" com.aruvi.dms.Main
else
    echo "Compilation failed."
    exit 1
fi
