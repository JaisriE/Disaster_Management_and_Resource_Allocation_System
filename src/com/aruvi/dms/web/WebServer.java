package com.aruvi.dms.web;

import com.aruvi.dms.config.DBConfig;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * WebServer starts the embedded JDK HttpServer on localhost:8080,
 * routing static website requests and REST API calls.
 */
public class WebServer {
    private final int port;
    private HttpServer server;

    public WebServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Register REST API handler
        server.createContext("/api", new ApiHandler());

        // Register static frontend handler
        server.createContext("/", new StaticFileHandler("web"));

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("====================================================================");
        System.out.println(" Aruvi Disaster Management Resource Allocation System");
        System.out.println(" Web Server running at: http://localhost:" + port);
        System.out.println(" API Endpoints at:    http://localhost:" + port + "/api/...");
        System.out.println(" Control Room UI:     http://localhost:" + port + "/index.html");
        System.out.println("====================================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("[WebServer] Stopped.");
        }
    }

    public static void main(String[] args) throws IOException {
        int port = DBConfig.getServerPort();
        WebServer webServer = new WebServer(port);
        webServer.start();
    }
}
