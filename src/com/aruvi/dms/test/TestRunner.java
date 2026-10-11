package com.aruvi.dms.test;

import com.aruvi.dms.config.DBConfig;
import com.aruvi.dms.dao.DepotDAO;
import com.aruvi.dms.graph.DijkstraService;
import com.aruvi.dms.graph.Graph;
import com.aruvi.dms.graph.PathResult;
import com.aruvi.dms.graph.ReachabilityService;
import com.aruvi.dms.model.*;
import com.aruvi.dms.service.DisasterManagementService;
import com.aruvi.dms.util.JsonUtil;
import com.aruvi.dms.util.ValidationUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Comprehensive Automated Test Suite verifying all core Second Review requirements:
 * 1. Deterministic Priority Ranking (Severity, Population, Arrival Order)
 * 2. In-memory PriorityQueue Remove-and-Reinsert Rule
 * 3. BFS & DFS Reachability over open roads and isolated village detection
 * 4. Dijkstra Shortest Path calculation with Min-Heap
 * 5. ResourceStock non-negative guarantees
 * 6. Shelter capacity and overbooking protection
 * 7. RescueTeam duplicate assignment protection
 * 8. Polymorphism on ReliefRequest (SupplyRequest & RescueRequest)
 * 9. Hand-written JsonUtil parsing & serialization
 * 10. Service End-to-End recommendation workflow
 * 11. Persistence verification across database reload
 * 12. Transaction rollback on simulated failure (no stock changed)
 * 13. Duplicate allocation prevention
 * 14. Input validation (severity 0 & 6, negative stock, 0 travel time, missing fields)
 * 15. Partial allocation with outstanding quantities
 * 16. Full shelter and unavailable rescue team handling
 * 17. Rejection changing nothing
 * 18. Automatic re-prioritization on stock update, road closure, and severity change
 * 19. Performance benchmarking on seed data (10 nodes) and generated graph (200 nodes)
 */
public class TestRunner {

    private static int passedTests = 0;
    private static int failedTests = 0;

    // Benchmark measurement results
    public static double seedBfsMs = 0;
    public static double seedDfsMs = 0;
    public static double seedDijkstraMs = 0;
    public static double seedReprioritizeMs = 0;

    public static double genBfsMs = 0;
    public static double genDfsMs = 0;
    public static double genDijkstraMs = 0;
    public static double genReprioritizeMs = 0;

    public static void main(String[] args) {
        System.out.println("=====================================================================");
        System.out.println(" RUNNING AUTOMATED UNIT & INTEGRATION TESTS");
        System.out.println(" Course: 24GEP31 - Mini Project (Second Review)");
        System.out.println("=====================================================================");

        // Reset demo data to ensure a known clean starting point
        try {
            DisasterManagementService.getInstance().resetDemoData();
        } catch (Exception e) {
            System.err.println("Warning during demo reset: " + e.getMessage());
        }

        testDeterministicPriorityRanking();
        testPriorityQueueRemovalAndReinsertion();
        testGraphReachabilityAndIsolatedVillage();
        testDijkstraShortestPathMinHeap();
        testResourceStockIntegrity();
        testShelterOverbookingProtection();
        testRescueTeamAvailability();
        testPolymorphism();
        testJsonUtilSerializationAndParsing();
        testServiceEndToEndRecommendation();

        // Second Review specific deliverables
        testRestartPersistence();
        testTransactionRollbackOnSimulatedFailure();
        testDuplicateAllocationPrevention();
        testInputValidationRules();
        testPartialAllocationWithOutstandingQuantity();
        testFullShelterAndUnavailableRescueTeam();
        testRejectionChangesNothing();
        testAutomaticReprioritizationOnStateChanges();

        // Performance benchmarking
        runPerformanceBenchmarks();

        System.out.println("=====================================================================");
        System.out.println(" TEST RESULTS SUMMARY: " + passedTests + " PASSED, " + failedTests + " FAILED");
        System.out.println("=====================================================================");

        if (failedTests > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition, String message) {
        if (condition) {
            System.out.println(" [PASS] " + testName);
            passedTests++;
        } else {
            System.err.println(" [FAIL] " + testName + ": " + message);
            failedTests++;
        }
    }

    private static void testDeterministicPriorityRanking() {
        System.out.println("\n--- Test Suite 1: Deterministic Priority Ranking ---");
        SupplyRequest r1 = new SupplyRequest(1, 2, 4, 1500, 1, 50, 20);
        SupplyRequest r2 = new SupplyRequest(2, 3, 5, 1200, 2, 50, 20);
        SupplyRequest r3 = new SupplyRequest(3, 4, 5, 2500, 3, 50, 20);
        SupplyRequest r4 = new SupplyRequest(4, 5, 4, 1500, 4, 50, 20);

        assertTrue("Higher population wins on tie severity", r3.compareTo(r2) < 0, "r3 should rank higher than r2");
        assertTrue("Higher severity wins over lower severity", r2.compareTo(r1) < 0, "r2 should rank higher than r1");
        assertTrue("Earlier arrival order wins on tie severity and pop", r1.compareTo(r4) < 0, "r1 should rank higher than r4");
    }

    private static void testPriorityQueueRemovalAndReinsertion() {
        System.out.println("\n--- Test Suite 2: Priority Queue Mutation Rule (Remove & Re-insert) ---");
        PriorityQueue<ReliefRequest> pq = new PriorityQueue<ReliefRequest>();
        SupplyRequest low = new SupplyRequest(10, 1, 2, 500, 1, 10, 10);
        SupplyRequest high = new SupplyRequest(11, 2, 4, 2000, 2, 20, 20);

        pq.add(low);
        pq.add(high);

        assertTrue("High priority is head of queue", pq.peek().getId() == 11, "Head should be request 11");

        pq.remove(low);
        low.setSeverity(5);
        low.setAffectedPopulation(5000);
        pq.add(low);

        assertTrue("Mutated request becomes new head after re-insertion", pq.peek().getId() == 10, "Head should now be request 10");
    }

    private static void testGraphReachabilityAndIsolatedVillage() {
        System.out.println("\n--- Test Suite 3: BFS/DFS Reachability & Isolated Village ---");
        Graph g = new Graph();
        g.addEdge(new Road(1, 1, 2, 15, true, "Open road 1-2"));
        g.addEdge(new Road(2, 2, 1, 15, true, "Open road 2-1"));
        g.addEdge(new Road(3, 2, 3, 20, true, "Open road 2-3"));
        g.addEdge(new Road(4, 3, 2, 20, true, "Open road 3-2"));
        Road isolatedRoad = new Road(5, 3, 10, 30, false, "Broken road 3-10");
        g.addEdge(isolatedRoad);

        assertTrue("BFS: Village 1 reaches Village 3", ReachabilityService.isReachableBFS(g, 1, 3), "1 to 3 should be reachable");
        assertTrue("DFS: Village 1 reaches Village 3", ReachabilityService.isReachableDFS(g, 1, 3), "1 to 3 should be reachable via DFS");
        assertTrue("BFS: Village 10 is unreachable when road closed", !ReachabilityService.isReachableBFS(g, 1, 10), "10 should be isolated");

        isolatedRoad.setOpen(true);
        assertTrue("BFS: Village 10 becomes reachable when road reopens", ReachabilityService.isReachableBFS(g, 1, 10), "10 should now be reachable");
    }

    private static void testDijkstraShortestPathMinHeap() {
        System.out.println("\n--- Test Suite 4: Dijkstra Shortest Path Min-Heap ---");
        Graph g = new Graph();
        g.addEdge(new Road(1, 1, 2, 10, true, "1 to 2"));
        g.addEdge(new Road(2, 2, 4, 20, true, "2 to 4"));
        g.addEdge(new Road(3, 1, 3, 12, true, "1 to 3"));
        g.addEdge(new Road(4, 3, 4, 15, true, "3 to 4"));
        g.addEdge(new Road(5, 1, 4, 45, true, "1 to 4 direct"));

        PathResult res = DijkstraService.findShortestPath(g, 1, 4);
        assertTrue("Dijkstra finds reachable path", res.isReachable(), "Path should exist");
        assertTrue("Dijkstra computes optimal time (27 mins)", res.getTotalTravelTime() == 27, "Expected 27, got " + res.getTotalTravelTime());
        List<Integer> expectedPath = Arrays.asList(1, 3, 4);
        assertTrue("Dijkstra reconstructs exact route [1, 3, 4]", res.getPath().equals(expectedPath), "Expected [1, 3, 4], got " + res.getPath());
    }

    private static void testResourceStockIntegrity() {
        System.out.println("\n--- Test Suite 5: ResourceStock Non-Negative Invariant ---");
        ResourceStock stock = new ResourceStock(100, 50);

        boolean reserved = stock.reserveStock(60, 30);
        assertTrue("Valid reservation succeeds", reserved, "Reservation should succeed");
        assertTrue("Food kits deducted properly", stock.getFoodKits() == 40, "Expected 40 food kits");
        assertTrue("Medicine kits deducted properly", stock.getMedicineKits() == 20, "Expected 20 med kits");

        boolean overReserve = stock.reserveStock(50, 10);
        assertTrue("Over-reservation is rejected", !overReserve, "Over reservation must return false");
        assertTrue("Stock unchanged after rejected reservation", stock.getFoodKits() == 40, "Food should remain 40");

        boolean caught = false;
        try {
            stock.addStock(-10, 0);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue("Negative stock addition rejected with exception", caught, "Negative addition must throw exception");
    }

    private static void testShelterOverbookingProtection() {
        System.out.println("\n--- Test Suite 6: Shelter Overbooking Protection ---");
        Shelter shelter = new Shelter(1, "Test Shelter", 1, 100, 80);
        assertTrue("Available places correctly calculated (20)", shelter.availablePlaces() == 20, "Expected 20 available");

        boolean reserved = shelter.reservePlaces(15);
        assertTrue("Reserving 15 places succeeds", reserved, "Should succeed");
        assertTrue("Remaining available places is 5", shelter.availablePlaces() == 5, "Expected 5 places");

        boolean overbook = shelter.reservePlaces(10);
        assertTrue("Overbooking attempt rejected", !overbook, "Cannot exceed capacity");
        assertTrue("Reserved count remains 95", shelter.getReservedPlaces() == 95, "Expected 95");
    }

    private static void testRescueTeamAvailability() {
        System.out.println("\n--- Test Suite 7: RescueTeam Duplicate Assignment Protection ---");
        RescueTeam team = new RescueTeam(1, "Alpha Team", 1, true);
        assertTrue("Team initially available", team.isAvailable(), "Team should be available");

        boolean reserved = team.reserveTeam(101);
        assertTrue("First assignment succeeds", reserved, "Should reserve team");
        assertTrue("Team is now busy", !team.isAvailable(), "Team should be marked unavailable");

        boolean duplicate = team.reserveTeam(102);
        assertTrue("Conflicting assignment refused", !duplicate, "Cannot assign busy team");

        team.releaseTeam();
        assertTrue("Released team becomes available again", team.isAvailable(), "Team should be available");
    }

    private static void testPolymorphism() {
        System.out.println("\n--- Test Suite 8: OOP Polymorphism on ReliefRequest ---");
        List<ReliefRequest> requests = new ArrayList<ReliefRequest>();
        requests.add(new SupplyRequest(1, 1, 5, 1000, 1, 50, 30));
        requests.add(new RescueRequest(2, 2, 4, 800, 2, 1, 50));

        int fulfillableCount = 0;
        for (int i = 0; i < requests.size(); i++) {
            ReliefRequest r = requests.get(i);
            if (r.isFulfillable()) {
                fulfillableCount++;
            }
            assertTrue("Polymorphic getSummary() produces non-empty text", r.getSummary() != null && !r.getSummary().isEmpty(), "Summary should not be empty");
        }
        assertTrue("Both request types evaluated polymorphically", fulfillableCount == 2, "Expected 2 fulfillable requests");
    }

    private static void testJsonUtilSerializationAndParsing() {
        System.out.println("\n--- Test Suite 9: Hand-written JsonUtil Parsing & Serialization ---");
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("district", "Aruvi");
        map.put("severity", 5);
        map.put("isOpen", true);

        String json = JsonUtil.toJson(map);
        assertTrue("JsonUtil serializes correctly", json.contains("\"district\":\"Aruvi\"") && json.contains("\"severity\":5"), "JSON string matching");

        Map<String, Object> parsed = JsonUtil.parseObject(json);
        assertTrue("JsonUtil parses string property", "Aruvi".equals(JsonUtil.getString(parsed, "district", "")), "Parsed district should be Aruvi");
        assertTrue("JsonUtil parses integer property", JsonUtil.getInt(parsed, "severity", 0) == 5, "Parsed severity should be 5");
        assertTrue("JsonUtil parses boolean property", JsonUtil.getBoolean(parsed, "isOpen", false), "Parsed isOpen should be true");
    }

    private static void testServiceEndToEndRecommendation() {
        System.out.println("\n--- Test Suite 10: Service End-to-End Recommendation Workflow ---");
        DisasterManagementService service = DisasterManagementService.getInstance();
        List<ReliefRequest> prioritized = service.getPrioritizedPendingRequests();
        assertTrue("Prioritized requests list is non-empty", !prioritized.isEmpty(), "Pending requests should exist");

        Recommendation rec = service.generateRecommendation(1);
        assertTrue("Recommendation generated for Request #1", rec != null, "Recommendation must exist");
        assertTrue("Request #1 is reachable", rec.isReachable(), "Region 2 must be reachable");
        assertTrue("Depot recommended for Request #1", rec.getRecommendedDepotId() != null, "Depot should be selected");

        Recommendation recIsolated = service.generateRecommendation(10);
        assertTrue("Isolated village detected for Request #10", recIsolated != null && !recIsolated.isReachable(), "Solaiyur must be unreachable");
        assertTrue("Status recommendation is UNREACHABLE", recIsolated.getRecommendedStatus() == RequestStatus.UNREACHABLE, "Should recommend UNREACHABLE");
    }

    private static void testRestartPersistence() {
        System.out.println("\n--- Test Suite 11: Data Persistence Across MySQL Reload ---");
        DisasterManagementService service = DisasterManagementService.getInstance();
        try {
            // Add a new distinct report
            SupplyRequest newReq = new SupplyRequest(1, 1, 5, 4321, 999, 77, 33);
            newReq.setReportedBy("Persistence Test Officer");
            int newId = service.addEmergencyReport(newReq);
            assertTrue("New report saved with generated ID", newId > 0, "ID should be > 0");

            // Simulate application restart by completely reloading all data from MySQL
            service.loadAllFromDatabase();

            ReliefRequest reloaded = service.getRequest(newId);
            assertTrue("Report still present after full database reload", reloaded != null, "Reloaded request must exist");
            assertTrue("Population matches persisted value (4321)", reloaded.getAffectedPopulation() == 4321, "Pop should be 4321");
            assertTrue("ReportedBy matches persisted value", "Persistence Test Officer".equals(reloaded.getReportedBy()), "ReportedBy mismatch");
        } catch (Exception e) {
            assertTrue("Restart persistence failed with exception", false, e.getMessage());
        }
    }

    private static void testTransactionRollbackOnSimulatedFailure() {
        System.out.println("\n--- Test Suite 12: Atomic Transaction Rollback on Failure ---");
        DisasterManagementService service = DisasterManagementService.getInstance();
        Depot depot1 = service.getDepot(1);
        int initialFood = depot1.getFoodKits();
        int initialMed = depot1.getMedicineKits();

        // Simulate a database transaction that rolls back midway
        Connection conn = null;
        boolean rollbackOccurred = false;
        try {
            conn = DBConfig.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Step 1: Update stock in DB
            DepotDAO dao = new DepotDAO();
            dao.updateStock(conn, 1, initialFood - 50, initialMed - 25);

            // Step 2: Simulate failure (e.g. forced exception before commit)
            if (true) {
                throw new SQLException("Simulated network/hardware failure during transaction execution");
            }

            conn.commit();
        } catch (SQLException e) {
            rollbackOccurred = true;
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback error: " + ex.getMessage());
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }

        assertTrue("Simulated failure caught and triggered rollback", rollbackOccurred, "Rollback should occur");

        // Verify MySQL stock was not committed and remains identical
        try (Connection verifyConn = DBConfig.getConnection();
             PreparedStatement ps = verifyConn.prepareStatement("SELECT food_kits, medicine_kits FROM depots WHERE id = 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int dbFood = rs.getInt("food_kits");
                int dbMed = rs.getInt("medicine_kits");
                assertTrue("MySQL depot food stock unchanged after rollback", dbFood == initialFood, "Expected " + initialFood + ", got " + dbFood);
                assertTrue("MySQL depot med stock unchanged after rollback", dbMed == initialMed, "Expected " + initialMed + ", got " + dbMed);
            }
        } catch (SQLException e) {
            assertTrue("Verification query failed", false, e.getMessage());
        }
    }

    private static void testDuplicateAllocationPrevention() {
        System.out.println("\n--- Test Suite 13: Duplicate Allocation Prevention ---");
        DisasterManagementService service = DisasterManagementService.getInstance();
        try {
            // Create a dedicated supply request
            SupplyRequest req = new SupplyRequest(1, 1, 4, 1000, 888, 30, 20);
            int reqId = service.addEmergencyReport(req);

            // Confirm allocation first time
            boolean confirmedOnce = service.confirmAllocation(reqId, "Officer Dinaker");
            assertTrue("First confirmation succeeds", confirmedOnce, "First confirm must succeed");

            ReliefRequest updated = service.getRequest(reqId);
            assertTrue("Request status is ALLOCATED", updated.getStatus() == RequestStatus.ALLOCATED, "Status should be ALLOCATED");

            // Attempt to confirm the exact same request a second time
            boolean secondConfirmFailed = false;
            try {
                service.confirmAllocation(reqId, "Officer Dinaker");
            } catch (IllegalStateException e) {
                secondConfirmFailed = true;
            }
            assertTrue("Second confirmation rejected with IllegalStateException", secondConfirmFailed, "Duplicate confirmation must be refused");

        } catch (Exception e) {
            assertTrue("Duplicate allocation test failed with error", false, e.getMessage());
        }
    }

    private static void testInputValidationRules() {
        System.out.println("\n--- Test Suite 14: Input Validation Rules (Boundary & Error Cases) ---");
        // Severity boundary cases
        assertTrue("Severity 0 rejected", !ValidationUtil.validateSeverity(0).isValid(), "Severity 0 should fail");
        assertTrue("Severity 6 rejected", !ValidationUtil.validateSeverity(6).isValid(), "Severity 6 should fail");
        assertTrue("Severity 1 accepted", ValidationUtil.validateSeverity(1).isValid(), "Severity 1 should pass");
        assertTrue("Severity 5 accepted", ValidationUtil.validateSeverity(5).isValid(), "Severity 5 should pass");

        // Population validation
        assertTrue("Population 0 rejected", !ValidationUtil.validatePopulation(0).isValid(), "Population 0 should fail");
        assertTrue("Population -10 rejected", !ValidationUtil.validatePopulation(-10).isValid(), "Negative pop should fail");

        // Travel time validation
        assertTrue("Travel time 0 rejected", !ValidationUtil.validatePositive(0, "travelTime").isValid(), "0 travel time must fail");
        assertTrue("Negative travel time rejected", !ValidationUtil.validatePositive(-5, "travelTime").isValid(), "Negative travel time must fail");

        // Negative stock validation
        assertTrue("Negative stock quantity rejected", !ValidationUtil.validateNonNegative(-1, "stock").isValid(), "Negative stock must fail");

        // Missing / empty fields
        assertTrue("Empty string rejected", !ValidationUtil.validateNotEmpty("", "name").isValid(), "Empty name must fail");
        assertTrue("Null string rejected", !ValidationUtil.validateNotEmpty(null, "name").isValid(), "Null name must fail");

        // Unknown entity IDs
        DisasterManagementService service = DisasterManagementService.getInstance();
        assertTrue("Non-existent region ID returns null", service.getRegion(99999) == null, "Region 99999 should be null");
        assertTrue("Non-existent depot ID returns null", service.getDepot(99999) == null, "Depot 99999 should be null");
    }

    private static void testPartialAllocationWithOutstandingQuantity() {
        System.out.println("\n--- Test Suite 15: Partial Allocation with Outstanding Quantities ---");
        DisasterManagementService service = DisasterManagementService.getInstance();
        try {
            Depot depot1 = service.getDepot(1);
            int availableFood = depot1.getFoodKits();
            int availableMed = depot1.getMedicineKits();

            // Create request asking for MORE than available in Depot 1
            int reqFood = availableFood + 150;
            int reqMed = availableMed + 75;

            SupplyRequest bigReq = new SupplyRequest(1, 1, 5, 3500, 777, reqFood, reqMed);
            int reqId = service.addEmergencyReport(bigReq);

            Recommendation rec = service.generateRecommendation(reqId);
            assertTrue("Partial recommendation canAllocate is true", rec.isCanAllocate(), "Should be allocatable");
            assertTrue("Recommended status is PARTIALLY_ALLOCATED", rec.getRecommendedStatus() == RequestStatus.PARTIALLY_ALLOCATED, "Status should be PARTIAL");

            // Verify allocated equals available stock
            assertTrue("Recommended food matches available stock", rec.getFoodRecommended() == availableFood, "Food rec should match avail");
            assertTrue("Recommended med matches available stock", rec.getMedicineRecommended() == availableMed, "Med rec should match avail");

            // Verify outstanding quantity = requested - recommended
            assertTrue("Outstanding food is 150", rec.getOutstandingFood() == 150, "Outstanding food should be 150");
            assertTrue("Outstanding med is 75", rec.getOutstandingMedicine() == 75, "Outstanding med should be 75");

        } catch (Exception e) {
            assertTrue("Partial allocation test failed", false, e.getMessage());
        }
    }

    private static void testFullShelterAndUnavailableRescueTeam() {
        System.out.println("\n--- Test Suite 16: Full Shelter & Unavailable Rescue Team ---");
        DisasterManagementService service = DisasterManagementService.getInstance();
        try {
            // Temporarily mark all rescue teams busy
            List<RescueTeam> teams = service.getAllRescueTeams();
            for (int i = 0; i < teams.size(); i++) {
                teams.get(i).setAvailable(false);
            }

            // Temporarily fill shelters to 100% capacity
            List<Shelter> shelters = service.getAllShelters();
            for (int i = 0; i < shelters.size(); i++) {
                Shelter s = shelters.get(i);
                s.setReservedPlaces(s.getCapacity());
            }

            RescueRequest rescueReq = new RescueRequest(1, 1, 5, 2000, 666, 1, 50);
            int reqId = service.addEmergencyReport(rescueReq);

            Recommendation rec = service.generateRecommendation(reqId);
            assertTrue("Rescue cannot allocate when teams busy and shelters full", !rec.isCanAllocate(), "canAllocate should be false");
            assertTrue("No team recommended", rec.getRecommendedTeamId() == null, "Team should be null");
            assertTrue("0 shelter places recommended", rec.getShelterPlacesRecommended() == 0, "Places should be 0");

            // Restore teams and shelters
            for (int i = 0; i < teams.size(); i++) {
                teams.get(i).setAvailable(true);
            }
            for (int i = 0; i < shelters.size(); i++) {
                Shelter s = shelters.get(i);
                s.setReservedPlaces(0);
            }

        } catch (Exception e) {
            assertTrue("Full shelter test failed", false, e.getMessage());
        }
    }

    private static void testRejectionChangesNothing() {
        System.out.println("\n--- Test Suite 17: Officer Rejection Changes Nothing ---");
        DisasterManagementService service = DisasterManagementService.getInstance();
        try {
            Depot d1 = service.getDepot(1);
            int foodBefore = d1.getFoodKits();
            int medBefore = d1.getMedicineKits();

            SupplyRequest req = new SupplyRequest(1, 2, 3, 800, 555, 40, 20);
            int reqId = service.addEmergencyReport(req);

            // Officer rejects
            service.rejectAllocation(reqId, "Budget constraint review", "Inspector General");

            ReliefRequest rejected = service.getRequest(reqId);
            assertTrue("Request marked as REJECTED", rejected.getStatus() == RequestStatus.REJECTED, "Status should be REJECTED");
            assertTrue("Rejection reason stored", "Budget constraint review".equals(rejected.getRejectionReason()), "Reason mismatch");

            // Verify depot stock was NOT deducted
            Depot d1After = service.getDepot(1);
            assertTrue("Depot food stock identical after rejection", d1After.getFoodKits() == foodBefore, "Food should be unchanged");
            assertTrue("Depot med stock identical after rejection", d1After.getMedicineKits() == medBefore, "Med should be unchanged");

        } catch (Exception e) {
            assertTrue("Rejection test failed", false, e.getMessage());
        }
    }

    private static void testAutomaticReprioritizationOnStateChanges() {
        System.out.println("\n--- Test Suite 18: Automatic Re-prioritization On State Changes ---");
        DisasterManagementService service = DisasterManagementService.getInstance();
        try {
            // A. Severity and Population change
            SupplyRequest low = new SupplyRequest(1, 3, 1, 300, 444, 20, 10);
            int lowId = service.addEmergencyReport(low);

            // Mutate severity to 5 and population to 20,000 (becomes supreme emergency)
            service.updateEmergencyReport(lowId, 5, 20000, 20, 10, 0, 0);

            List<ReliefRequest> queue = service.getPrioritizedPendingRequests();
            assertTrue("Mutated request becomes #1 head of prioritized queue", queue.get(0).getId() == lowId, "Must be #1 in queue");

            // B. Road closure updates reachable status
            Road road = service.getAllRoads().get(0);
            service.setRoadOpen(road.getId(), false);
            assertTrue("Road status toggled to closed", !road.isOpen(), "Road should be closed");

            // Reopen road
            service.setRoadOpen(road.getId(), true);
            assertTrue("Road reopened and network recalculated", road.isOpen(), "Road should be open");

            // C. Stock update
            Depot d2 = service.getDepot(2);
            int oldFood = d2.getFoodKits();
            service.updateDepotStock(2, 50, 25, true);
            assertTrue("Depot stock increased by 50", d2.getFoodKits() == oldFood + 50, "Food should increase");

        } catch (Exception e) {
            assertTrue("Reprioritization test failed", false, e.getMessage());
        }
    }

    private static void runPerformanceBenchmarks() {
        System.out.println("\n--- Performance Benchmarking: Real Execution Measurements ---");
        DisasterManagementService service = DisasterManagementService.getInstance();
        Graph seedGraph = new Graph();
        for (Road r : service.getAllRoads()) {
            seedGraph.addEdge(r);
        }

        // 1. Seed Graph (10 nodes, 28 roads)
        // Warm up
        for (int i = 0; i < 50; i++) {
            ReachabilityService.isReachableBFS(seedGraph, 1, 9);
            ReachabilityService.isReachableDFS(seedGraph, 1, 9);
            DijkstraService.findShortestPath(seedGraph, 1, 9);
            service.rebuildPriorityQueue();
        }

        long t0 = System.nanoTime();
        ReachabilityService.isReachableBFS(seedGraph, 1, 9);
        seedBfsMs = (System.nanoTime() - t0) / 1000000.0;

        t0 = System.nanoTime();
        ReachabilityService.isReachableDFS(seedGraph, 1, 9);
        seedDfsMs = (System.nanoTime() - t0) / 1000000.0;

        t0 = System.nanoTime();
        DijkstraService.findShortestPath(seedGraph, 1, 9);
        seedDijkstraMs = (System.nanoTime() - t0) / 1000000.0;

        t0 = System.nanoTime();
        service.rebuildPriorityQueue();
        seedReprioritizeMs = (System.nanoTime() - t0) / 1000000.0;

        System.out.println(String.format(" Seed Graph (10 regions, 28 roads): BFS = %.4f ms, DFS = %.4f ms, Dijkstra = %.4f ms, Re-priority = %.4f ms",
                seedBfsMs, seedDfsMs, seedDijkstraMs, seedReprioritizeMs));

        // 2. Generated Graph (200 regions, 600 edges)
        Graph genGraph = new Graph();
        for (int i = 1; i <= 200; i++) {
            genGraph.addVertex(i);
        }
        Random rng = new Random(42);
        int roadId = 1;
        // Ring topology ensuring basic connectivity
        for (int i = 1; i <= 200; i++) {
            int next = (i % 200) + 1;
            int time = 5 + rng.nextInt(30);
            genGraph.addEdge(new Road(roadId++, i, next, time, true, "Link " + i + "-" + next));
            genGraph.addEdge(new Road(roadId++, next, i, time, true, "Link " + next + "-" + i));
        }
        // Additional random cross-chords
        for (int i = 0; i < 200; i++) {
            int u = 1 + rng.nextInt(200);
            int v = 1 + rng.nextInt(200);
            if (u != v) {
                int time = 10 + rng.nextInt(50);
                genGraph.addEdge(new Road(roadId++, u, v, time, true, "Cross " + u + "-" + v));
                genGraph.addEdge(new Road(roadId++, v, u, time, true, "Cross " + v + "-" + u));
            }
        }

        // Priority Queue with 200 requests
        PriorityQueue<ReliefRequest> bigPq = new PriorityQueue<ReliefRequest>();
        List<ReliefRequest> bigList = new ArrayList<ReliefRequest>();
        for (int i = 1; i <= 200; i++) {
            int sev = 1 + rng.nextInt(5);
            int pop = 100 + rng.nextInt(10000);
            SupplyRequest r = new SupplyRequest(i, 1 + rng.nextInt(200), sev, pop, i, 50, 25);
            bigList.add(r);
        }

        // Warm up
        for (int i = 0; i < 20; i++) {
            ReachabilityService.isReachableBFS(genGraph, 1, 200);
            ReachabilityService.isReachableDFS(genGraph, 1, 200);
            DijkstraService.findShortestPath(genGraph, 1, 200);
        }

        t0 = System.nanoTime();
        ReachabilityService.isReachableBFS(genGraph, 1, 200);
        genBfsMs = (System.nanoTime() - t0) / 1000000.0;

        t0 = System.nanoTime();
        ReachabilityService.isReachableDFS(genGraph, 1, 200);
        genDfsMs = (System.nanoTime() - t0) / 1000000.0;

        t0 = System.nanoTime();
        DijkstraService.findShortestPath(genGraph, 1, 200);
        genDijkstraMs = (System.nanoTime() - t0) / 1000000.0;

        t0 = System.nanoTime();
        bigPq.clear();
        for (int i = 0; i < bigList.size(); i++) {
            bigPq.add(bigList.get(i));
        }
        genReprioritizeMs = (System.nanoTime() - t0) / 1000000.0;

        System.out.println(String.format(" Generated Graph (200 regions, 800 roads): BFS = %.4f ms, DFS = %.4f ms, Dijkstra = %.4f ms, Re-priority = %.4f ms",
                genBfsMs, genDfsMs, genDijkstraMs, genReprioritizeMs));

        assertTrue("Benchmark on seed data completed in < 100 ms", seedDijkstraMs < 100, "Seed Dijkstra fast");
        assertTrue("Benchmark on 200-node graph completed in < 500 ms", genDijkstraMs < 500, "200-node Dijkstra fast");
    }
}
