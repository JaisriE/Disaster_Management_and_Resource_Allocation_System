package com.aruvi.dms.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.file.Files;

/**
 * StaticFileHandler serves static HTML, CSS, JavaScript, and asset files
 * from the 'web/' directory using JDK's built-in HttpServer.
 */
public class StaticFileHandler implements HttpHandler {
    private final File webDir;

    public StaticFileHandler(String webPath) {
        this.webDir = new File(webPath);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path == null || path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        }

        // Prevent path traversal attacks
        if (path.contains("..")) {
            sendResponse(exchange, 403, "Forbidden", "text/plain");
            return;
        }

        File targetFile = new File(webDir, path.substring(1));
        if (!targetFile.exists() || targetFile.isDirectory()) {
            sendResponse(exchange, 404, "404 Not Found", "text/plain");
            return;
        }

        String mimeType = getMimeType(targetFile.getName());
        byte[] bytes = Files.readAllBytes(targetFile.toPath());

        exchange.getResponseHeaders().set("Content-Type", mimeType);
        exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String getMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".html")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".svg")) return "image/svg+xml; charset=UTF-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String message, String contentType) throws IOException {
        byte[] bytes = message.getBytes();
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
