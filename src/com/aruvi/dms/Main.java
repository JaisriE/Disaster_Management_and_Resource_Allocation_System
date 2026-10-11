package com.aruvi.dms;

import com.aruvi.dms.config.DBConfig;
import com.aruvi.dms.service.DisasterManagementService;
import com.aruvi.dms.web.WebServer;

import java.io.IOException;

/**
 * Main application entry point for the Disaster Management Resource Allocation System.
 * Course: 24GEP31 - Mini Project (Second Review), Department of CSE
 * Case Study: Aruvi District Emergency Operations Control Room
 * Team: Dinaker G (25CSR066), Jaisri E (25CSR104), Kabila N (25CSR114)
 * Mentor: Dr. K. Dinesh, Associate Professor
 */
public class Main {

    public static void main(String[] args) {
        printBanner();

        try {
            System.out.println("[Bootstrap] Connecting to MySQL database at " + DBConfig.getUrl() + "...");
            DisasterManagementService service = DisasterManagementService.getInstance();
            System.out.println("[Bootstrap] In-memory graph and priority queues initialized.");

            int port = DBConfig.getServerPort();
            WebServer server = new WebServer(port);
            server.start();

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("\n[Shutdown] Stopping web server...");
                    server.stop();
                }
            }));

        } catch (IOException e) {
            System.err.println("[Bootstrap] Failed to start HTTP server: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[Bootstrap] Unexpected startup error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printBanner() {
        System.out.println("*******************************************************************************");
        System.out.println("*            DISASTER MANAGEMENT RESOURCE ALLOCATION SYSTEM                   *");
        System.out.println("*  Course: 24GEP31 - Mini Project (Review 2) | CSE Department                 *");
        System.out.println("*  Case Study: Aruvi District Severe Storm Emergency Operations               *");
        System.out.println("*  Team: Dinaker G (25CSR066), Jaisri E (25CSR104), Kabila N (25CSR114)       *");
        System.out.println("*  Mentor: Dr. K. Dinesh, Associate Professor                                 *");
        System.out.println("*  SDG Alignment: SDG 3 (Health), SDG 9 (Infra), SDG 11 (Cities), SDG 13 (Climate)*");
        System.out.println("*  Design Rule: Automation SUPPORTS Decisions (Recommends -> Officer Confirms)*");
        System.out.println("*******************************************************************************");
    }
}
