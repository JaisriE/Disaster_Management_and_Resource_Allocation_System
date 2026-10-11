# Step-by-Step Installation & Setup Guide
**Project:** Disaster Management Resource Allocation System (Aruvi District)  
**Course:** 24GEP31 - Mini Project (Second Review), Department of CSE  

---

This guide provides complete, step-by-step instructions to set up, configure, compile, test, and run the system from scratch on any standard machine.

---

## 1. System Prerequisites

1. **Java Development Kit (JDK):**
   - JDK 17, 21, or 26 installed.
   - Verify in terminal:
     ```cmd
     java -version
     javac -version
     ```
2. **MySQL Server 8.0:**
   - MySQL Server 8.0.x running on `localhost:3306`.
3. **Operating System:**
   - Windows 10/11, Linux, or macOS.

---

## 2. MySQL Database Setup

Open the MySQL command line client or MySQL Workbench as `root`:

```sql
-- 1. Create the database
CREATE DATABASE IF NOT EXISTS disaster_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. Create the dedicated user
CREATE USER IF NOT EXISTS 'disaster_user'@'localhost' IDENTIFIED BY 'YOUR_PASSWORD';

-- 3. Grant full privileges on the disaster database
GRANT ALL PRIVILEGES ON disaster_db.* TO 'disaster_user'@'localhost';
FLUSH PRIVILEGES;
```

---

## 3. Database Credentials Configuration

1. In the project root directory, copy `db.properties.example` to `db.properties`:
   ```cmd
   copy db.properties.example db.properties
   ```
2. Open `db.properties` and configure your database parameters:
   ```properties
   db.url=jdbc:mysql://localhost:3306/disaster_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.user=disaster_user
   db.password=YOUR_PASSWORD
   db.driver=com.mysql.cj.jdbc.Driver
   server.port=8080
   ```
   *(Note: `db.properties` is protected by `.gitignore` and must never be committed to public repositories).*

---

## 4. Initializing Schema & Seed Data

You can initialize the database using the MySQL CLI:

```cmd
mysql -u disaster_user -p disaster_db < sql\schema.sql
mysql -u disaster_user -p disaster_db < sql\seed.sql
```

*(Alternatively, the system's automated test runner and web app include a built-in Reset Demo Data feature that initializes tables automatically).*

---

## 5. Compiling the Code

Compile all Java source files using the provided batch script:

### Windows:
```cmd
compile.bat
```

### Linux / macOS:
```bash
chmod +x run.sh test.sh
javac -cp ".:lib/mysql-connector-j-26.7.0.jar" -d bin src/com/aruvi/dms/config/*.java src/com/aruvi/dms/model/*.java src/com/aruvi/dms/graph/*.java src/com/aruvi/dms/dao/*.java src/com/aruvi/dms/util/*.java src/com/aruvi/dms/service/*.java src/com/aruvi/dms/web/*.java src/com/aruvi/dms/test/*.java src/com/aruvi/dms/*.java
```

Class files will be generated in the `bin/` directory.

---

## 6. Running the Automated Test Suite

Before running the server, execute the automated test suite to verify data structures, reachability algorithms, and database transactions:

### Windows:
```cmd
test.bat
```

### Linux / macOS:
```bash
./test.sh
```

**Expected Result:** All 83 unit and integration tests must pass with `0 FAILED`.

---

## 7. Starting the Web Server & Command Center

Launch the server:

### Windows:
```cmd
run.bat
```

### Linux / macOS:
```bash
./run.sh
```

**Expected Terminal Output:**
```
*******************************************************************************
*            DISASTER MANAGEMENT RESOURCE ALLOCATION SYSTEM                   *
*  Course: 24GEP31 - Mini Project (Review 2) | CSE Department                 *
*  Case Study: Aruvi District Severe Storm Emergency Operations               *
*  Team: Dinaker G (25CSR066), Jaisri E (25CSR104), Kabila N (25CSR114)       *
*  Mentor: Dr. K. Dinesh, Associate Professor                                 *
*******************************************************************************
[Bootstrap] Connecting to MySQL database at jdbc:mysql://localhost:3306/disaster_db...
[DisasterManagementService] Synchronized state from MySQL successfully. Total requests: 16
[Bootstrap] In-memory graph and priority queues initialized.
====================================================================
 Aruvi Disaster Management Resource Allocation System
 Web Server running at: http://localhost:8080
 API Endpoints at:    http://localhost:8080/api/...
 Control Room UI:     http://localhost:8080/index.html
====================================================================
```

---

## 8. Accessing the Web Application

Open any modern browser (Chrome, Edge, Firefox) and navigate to:
```
http://localhost:8080/index.html
```

The application works 100% offline and requires no internet connection.

---

## 9. Troubleshooting Guide

| Issue | Likely Cause | Solution |
| :--- | :--- | :--- |
| **`Port 8080 already in use`** | A previous instance of the server or another service is listening on port 8080. | Change `server.port=8085` in `db.properties` or kill the existing process via Task Manager or `netstat -ano \| findstr :8080`. |
| **`Access denied for user 'disaster_user'`** | Incorrect password in `db.properties` or user not granted permissions. | Verify credentials in MySQL: `mysql -u disaster_user -p` and check permissions. |
| **`Communications link failure`** | MySQL Server service is stopped. | Start MySQL via Windows Services (`services.msc` -> Start `MySQL80`) or `net start MySQL80`. |
| **`ClassNotFoundException: com.mysql.cj.jdbc.Driver`** | MySQL connector jar missing from classpath. | Ensure `lib/mysql-connector-j-26.7.0.jar` is present in the `lib/` directory and classpath is formatted with `;` on Windows or `:` on Linux. |
