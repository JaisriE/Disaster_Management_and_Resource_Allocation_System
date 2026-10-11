package com.aruvi.dms.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConfig handles database connection properties and provides JDBC connections.
 * Reads db.properties from the application root or classpath.
 */
public class DBConfig {
    private static String url;
    private static String user;
    private static String password;
    private static String driver;
    private static int serverPort = 8080;
    private static boolean initialized = false;

    static {
        loadProperties();
    }

    public static synchronized void loadProperties() {
        Properties props = new Properties();
        File propFile = new File("db.properties");
        
        if (propFile.exists()) {
            try (InputStream in = new FileInputStream(propFile)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("[DBConfig] Warning: Could not read db.properties: " + e.getMessage());
            }
        } else {
            try (InputStream in = DBConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) {
                    props.load(in);
                }
            } catch (IOException e) {
                System.err.println("[DBConfig] Warning: Could not load db.properties from classpath: " + e.getMessage());
            }
        }

        url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/disaster_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        user = props.getProperty("db.user", "disaster_user");
        password = props.getProperty("db.password", "");
        driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        
        String portStr = props.getProperty("server.port", "8080");
        try {
            serverPort = Integer.parseInt(portStr.trim());
        } catch (NumberFormatException e) {
            serverPort = 8080;
        }

        try {
            Class.forName(driver);
            initialized = true;
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConfig] Error: JDBC Driver not found: " + driver);
            initialized = false;
        }
    }

    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            loadProperties();
        }
        return DriverManager.getConnection(url, user, password);
    }

    public static int getServerPort() {
        return serverPort;
    }

    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }
}
