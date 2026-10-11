#!/bin/bash
echo "===================================================================="
echo " Running Automated Test Suite"
echo " Course: 24GEP31 - Mini Project (Second Review)"
echo "===================================================================="

mkdir -p bin
javac -cp ".:lib/mysql-connector-j-26.7.0.jar" -d bin src/com/aruvi/dms/config/*.java src/com/aruvi/dms/model/*.java src/com/aruvi/dms/graph/*.java src/com/aruvi/dms/dao/*.java src/com/aruvi/dms/util/*.java src/com/aruvi/dms/service/*.java src/com/aruvi/dms/web/*.java src/com/aruvi/dms/test/*.java src/com/aruvi/dms/*.java

java -cp "bin:lib/mysql-connector-j-26.7.0.jar" com.aruvi.dms.test.TestRunner
