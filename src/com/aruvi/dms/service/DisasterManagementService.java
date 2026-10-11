package com.aruvi.dms.service;

import com.aruvi.dms.config.DBConfig;
import com.aruvi.dms.dao.*;
import com.aruvi.dms.graph.DijkstraService;
import com.aruvi.dms.graph.Graph;
import com.aruvi.dms.graph.PathResult;
import com.aruvi.dms.graph.ReachabilityService;
import com.aruvi.dms.model.*;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DisasterManagementService is the central coordinator for business logic.
 * Manages in-memory state synchronized with MySQL persistence,
 * maintains deterministic PriorityQueue for pending requests,
 * and executes algorithmic recommendations (BFS/Dijkstra).
 */
public class DisasterManagementService {

    private final RegionDAO regionDAO = new RegionDAO();
    private final DepotDAO depotDAO = new DepotDAO();
    private final ShelterDAO shelterDAO = new ShelterDAO();
    private final RescueTeamDAO teamDAO = new RescueTeamDAO();
    private final RoadDAO roadDAO = new RoadDAO();
    private final RequestDAO requestDAO = new RequestDAO();
    private final AllocationDAO allocationDAO = new AllocationDAO();
    private final AuditDAO auditDAO = new AuditDAO();

    // In-memory state collections
    private final Map<Integer, AffectedRegion> regionsMap = new ConcurrentHashMap<>();
    private final Map<Integer, Depot> depotsMap = new ConcurrentHashMap<>();
    private final Map<Integer, Shelter> sheltersMap = new ConcurrentHashMap<>();
    private final Map<Integer, RescueTeam> teamsMap = new ConcurrentHashMap<>();
    private final Map<Integer, Road> roadsMap = new ConcurrentHashMap<>();
    private final Map<Integer, ReliefRequest> requestsMap = new ConcurrentHashMap<>();
    private final Graph roadGraph = new Graph();

    // Priority Queue for deterministic request ranking
    private final PriorityQueue<ReliefRequest> priorityQueue = new PriorityQueue<>();

    private static DisasterManagementService instance;

    public static synchronized DisasterManagementService getInstance() {
        if (instance == null) {
            instance = new DisasterManagementService();
        }
        return instance;
    }

    private DisasterManagementService() {
        loadAllFromDatabase();
    }

    /**
     * Initializes in-memory data structures from MySQL database.
     */
    public synchronized void loadAllFromDatabase() {
        try {
            regionsMap.clear();
            for (AffectedRegion r : regionDAO.getAllRegions()) {
                regionsMap.put(r.getId(), r);
            }

            depotsMap.clear();
            for (Depot d : depotDAO.getAllDepots()) {
                AffectedRegion reg = regionsMap.get(d.getRegionId());
                if (reg != null) d.setRegionName(reg.getName());
                depotsMap.put(d.getId(), d);
            }

            sheltersMap.clear();
            for (Shelter s : shelterDAO.getAllShelters()) {
                AffectedRegion reg = regionsMap.get(s.getRegionId());
                if (reg != null) s.setRegionName(reg.getName());
                sheltersMap.put(s.getId(), s);
            }

            teamsMap.clear();
            for (RescueTeam t : teamDAO.getAllRescueTeams()) {
                Depot d = depotsMap.get(t.getHomeDepotId());
                if (d != null) t.setHomeDepotName(d.getName());
                teamsMap.put(t.getId(), t);
            }

            roadsMap.clear();
            roadGraph.clear();
            for (Road r : roadDAO.getAllRoads()) {
                AffectedRegion f = regionsMap.get(r.getFromRegionId());
                AffectedRegion t = regionsMap.get(r.getToRegionId());
                if (f != null) r.setFromRegionName(f.getName());
                if (t != null) r.setToRegionName(t.getName());
                roadsMap.put(r.getId(), r);
                roadGraph.addEdge(r);
            }

            requestsMap.clear();
            for (ReliefRequest req : requestDAO.getAllRequests()) {
                AffectedRegion reg = regionsMap.get(req.getRegionId());
                if (reg != null) req.setRegionName(reg.getName());
                requestsMap.put(req.getId(), req);
            }

            rebuildPriorityQueue();
            System.out.println("[DisasterManagementService] Synchronized state from MySQL successfully. Total requests: " + requestsMap.size());
        } catch (SQLException e) {
            System.err.println("[DisasterManagementService] Error loading data from MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Rebuilds the deterministic priority queue of pending/actionable requests.
     */
    public synchronized void rebuildPriorityQueue() {
        priorityQueue.clear();
        for (ReliefRequest req : requestsMap.values()) {
            if (req.getStatus() == RequestStatus.PENDING ||
                req.getStatus() == RequestStatus.PARTIALLY_ALLOCATED ||
                req.getStatus() == RequestStatus.UNREACHABLE) {
                priorityQueue.add(req);
            }
        }
    }

    // ==========================================
    // GETTERS FOR IN-MEMORY OBJECTS
    // ==========================================

    public synchronized List<AffectedRegion> getAllRegions() {
        List<AffectedRegion> list = new ArrayList<AffectedRegion>(regionsMap.values());
        Collections.sort(list, new Comparator<AffectedRegion>() {
            @Override
            public int compare(AffectedRegion a, AffectedRegion b) {
                return Integer.compare(a.getId(), b.getId());
            }
        });
        return list;
    }

    public synchronized List<Depot> getAllDepots() {
        List<Depot> list = new ArrayList<Depot>(depotsMap.values());
        Collections.sort(list, new Comparator<Depot>() {
            @Override
            public int compare(Depot a, Depot b) {
                return Integer.compare(a.getId(), b.getId());
            }
        });
        return list;
    }

    public synchronized List<Shelter> getAllShelters() {
        List<Shelter> list = new ArrayList<Shelter>(sheltersMap.values());
        Collections.sort(list, new Comparator<Shelter>() {
            @Override
            public int compare(Shelter a, Shelter b) {
                return Integer.compare(a.getId(), b.getId());
            }
        });
        return list;
    }

    public synchronized List<RescueTeam> getAllRescueTeams() {
        List<RescueTeam> list = new ArrayList<RescueTeam>(teamsMap.values());
        Collections.sort(list, new Comparator<RescueTeam>() {
            @Override
            public int compare(RescueTeam a, RescueTeam b) {
                return Integer.compare(a.getId(), b.getId());
            }
        });
        return list;
    }

    public synchronized List<Road> getAllRoads() {
        List<Road> list = new ArrayList<Road>(roadsMap.values());
        Collections.sort(list, new Comparator<Road>() {
            @Override
            public int compare(Road a, Road b) {
                return Integer.compare(a.getId(), b.getId());
            }
        });
        return list;
    }

    public synchronized List<ReliefRequest> getAllRequests() {
        List<ReliefRequest> list = new ArrayList<ReliefRequest>(requestsMap.values());
        Collections.sort(list, new Comparator<ReliefRequest>() {
            @Override
            public int compare(ReliefRequest a, ReliefRequest b) {
                return Integer.compare(a.getId(), b.getId());
            }
        });
        return list;
    }

    /**
     * Returns pending requests ordered strictly according to the deterministic priority comparator:
     * 1. Severity DESC, 2. Population DESC, 3. Arrival order ASC
     */
    public synchronized List<ReliefRequest> getPrioritizedPendingRequests() {
        List<ReliefRequest> sorted = new ArrayList<>(priorityQueue);
        Collections.sort(sorted);
        return sorted;
    }

    public synchronized ReliefRequest getRequest(int id) {
        return requestsMap.get(id);
    }

    public synchronized AffectedRegion getRegion(int id) {
        return regionsMap.get(id);
    }

    public synchronized Depot getDepot(int id) {
        return depotsMap.get(id);
    }

    public synchronized Shelter getShelter(int id) {
        return sheltersMap.get(id);
    }

    public synchronized RescueTeam getTeam(int id) {
        return teamsMap.get(id);
    }

    // ==========================================
    // ALGORITHMIC RECOMMENDATION ENGINE
    // ==========================================

    /**
     * Generates an algorithmic recommendation for the specified request.
     * Checks road reachability using BFS/DFS and calculates minimum travel time using Dijkstra.
     */
    public synchronized Recommendation generateRecommendation(int requestId) {
        ReliefRequest request = requestsMap.get(requestId);
        if (request == null) {
            return null;
        }

        Recommendation rec = new Recommendation();
        rec.setRequestId(request.getId());
        rec.setRequestType(request.getRequestType());
        rec.setRegionId(request.getRegionId());
        rec.setRegionName(request.getRegionName());

        if (request.getStatus() == RequestStatus.ALLOCATED) {
            rec.setReachable(true);
            rec.setCanAllocate(false);
            rec.setRecommendedStatus(RequestStatus.ALLOCATED);
            rec.setExplanation("Request #" + requestId + " has already been fully allocated. Duplicate allocation prevented.");
            return rec;
        }

        if (request instanceof SupplyRequest) {
            generateSupplyRecommendation((SupplyRequest) request, rec);
        } else if (request instanceof RescueRequest) {
            generateRescueRecommendation((RescueRequest) request, rec);
        }

        return rec;
    }

    private void generateSupplyRecommendation(SupplyRequest req, Recommendation rec) {
        int targetRegion = req.getRegionId();
        int neededFood = req.getOutstandingFood();
        int neededMed = req.getOutstandingMedicine();

        // 1. Check all candidate depots for road reachability and find closest with stock
        Depot bestDepot = null;
        PathResult bestPath = null;

        for (Depot depot : depotsMap.values()) {
            int depotRegion = depot.getRegionId();

            // BFS Reachability check over open roads
            boolean reachable = ReachabilityService.isReachableBFS(roadGraph, depotRegion, targetRegion);
            if (!reachable) {
                continue;
            }

            // Dijkstra shortest path with Min-Heap
            PathResult path = DijkstraService.findShortestPath(roadGraph, depotRegion, targetRegion);
            if (path.isReachable()) {
                if (bestPath == null || path.getTotalTravelTime() < bestPath.getTotalTravelTime()) {
                    bestPath = path;
                    bestDepot = depot;
                }
            }
        }

        if (bestDepot == null || bestPath == null) {
            // Completely isolated / unreachable
            rec.setReachable(false);
            rec.setCanAllocate(false);
            rec.setRecommendedStatus(RequestStatus.UNREACHABLE);
            rec.setExplanation("CRITICAL: Target region '" + req.getRegionName() +
                    "' is isolated. No open road connects candidate depots to this village.");
            return;
        }

        rec.setReachable(true);
        rec.setRecommendedDepotId(bestDepot.getId());
        rec.setRecommendedDepotName(bestDepot.getName());
        rec.setPathRegionIds(bestPath.getPath());
        rec.setTravelTimeMinutes(bestPath.getTotalTravelTime());

        List<String> names = new ArrayList<>();
        for (int rid : bestPath.getPath()) {
            AffectedRegion r = regionsMap.get(rid);
            names.add(r != null ? r.getName() : "Region " + rid);
        }
        rec.setPathRegionNames(names);

        // Calculate available stock to recommend
        int availFood = bestDepot.getFoodKits();
        int availMed = bestDepot.getMedicineKits();

        int recFood = Math.min(neededFood, availFood);
        int recMed = Math.min(neededMed, availMed);

        int outFood = neededFood - recFood;
        int outMed = neededMed - recMed;

        rec.setFoodRecommended(recFood);
        rec.setMedicineRecommended(recMed);
        rec.setOutstandingFood(outFood);
        rec.setOutstandingMedicine(outMed);

        if (recFood > 0 || recMed > 0) {
            rec.setCanAllocate(true);
            if (outFood == 0 && outMed == 0) {
                rec.setRecommendedStatus(RequestStatus.ALLOCATED);
                rec.setExplanation("Full allocation possible from " + bestDepot.getName() +
                        " via shortest open route (" + bestPath.getTotalTravelTime() + " mins).");
            } else {
                rec.setRecommendedStatus(RequestStatus.PARTIALLY_ALLOCATED);
                rec.setExplanation("Partial allocation recommended. " + bestDepot.getName() +
                        " supplies " + recFood + " food kits & " + recMed + " med kits (" +
                        bestPath.getTotalTravelTime() + " mins). Outstanding kits remain in queue.");
            }
        } else {
            rec.setCanAllocate(false);
            rec.setRecommendedStatus(RequestStatus.PENDING);
            rec.setExplanation("Reachable depot " + bestDepot.getName() +
                    " currently has 0 available kits for this request. Stock replenishment required.");
        }
    }

    private void generateRescueRecommendation(RescueRequest req, Recommendation rec) {
        int targetRegion = req.getRegionId();
        boolean needsTeam = req.getRequestedTeams() > req.getAllocatedTeams();
        int neededShelter = req.getOutstandingShelterPlaces();

        RescueTeam bestTeam = null;
        PathResult bestTeamPath = null;

        if (needsTeam) {
            for (RescueTeam team : teamsMap.values()) {
                if (!team.isAvailable()) continue;
                Depot home = depotsMap.get(team.getHomeDepotId());
                if (home == null) continue;

                if (!ReachabilityService.isReachableBFS(roadGraph, home.getRegionId(), targetRegion)) {
                    continue;
                }

                PathResult path = DijkstraService.findShortestPath(roadGraph, home.getRegionId(), targetRegion);
                if (path.isReachable()) {
                    if (bestTeamPath == null || path.getTotalTravelTime() < bestTeamPath.getTotalTravelTime()) {
                        bestTeamPath = path;
                        bestTeam = team;
                    }
                }
            }
        }

        Shelter bestShelter = null;
        PathResult bestShelterPath = null;

        if (neededShelter > 0) {
            for (Shelter shelter : sheltersMap.values()) {
                if (shelter.availablePlaces() <= 0) continue;

                if (!ReachabilityService.isReachableBFS(roadGraph, targetRegion, shelter.getRegionId())) {
                    continue;
                }

                PathResult path = DijkstraService.findShortestPath(roadGraph, targetRegion, shelter.getRegionId());
                if (path.isReachable()) {
                    if (bestShelterPath == null || path.getTotalTravelTime() < bestShelterPath.getTotalTravelTime()) {
                        bestShelterPath = path;
                        bestShelter = shelter;
                    }
                }
            }
        }

        // Check overall reachability
        if (bestTeamPath == null && bestShelterPath == null) {
            // Check if region itself is blocked by closed roads from central town (Region 1)
            boolean reachableFromHQ = ReachabilityService.isReachableBFS(roadGraph, 1, targetRegion);
            if (!reachableFromHQ) {
                rec.setReachable(false);
                rec.setCanAllocate(false);
                rec.setRecommendedStatus(RequestStatus.UNREACHABLE);
                rec.setExplanation("CRITICAL: Target region '" + req.getRegionName() +
                        "' is isolated. No open road routes reach this village.");
                return;
            }
        }

        rec.setReachable(true);

        PathResult chosenPath = bestTeamPath != null ? bestTeamPath : bestShelterPath;
        if (chosenPath != null) {
            rec.setPathRegionIds(chosenPath.getPath());
            rec.setTravelTimeMinutes(chosenPath.getTotalTravelTime());
            List<String> names = new ArrayList<>();
            for (int rid : chosenPath.getPath()) {
                AffectedRegion r = regionsMap.get(rid);
                names.add(r != null ? r.getName() : "Region " + rid);
            }
            rec.setPathRegionNames(names);
        }

        if (bestTeam != null) {
            rec.setRecommendedTeamId(bestTeam.getId());
            rec.setRecommendedTeamName(bestTeam.getName());
        }

        int recShelter = 0;
        int outShelter = neededShelter;
        if (bestShelter != null) {
            rec.setRecommendedShelterId(bestShelter.getId());
            rec.setRecommendedShelterName(bestShelter.getName());
            recShelter = Math.min(neededShelter, bestShelter.availablePlaces());
            outShelter = neededShelter - recShelter;
        }

        rec.setShelterPlacesRecommended(recShelter);
        rec.setOutstandingShelterPlaces(outShelter);

        boolean hasTeamAllocation = (bestTeam != null);
        boolean hasShelterAllocation = (recShelter > 0);

        if (hasTeamAllocation || hasShelterAllocation) {
            rec.setCanAllocate(true);
            if ((!needsTeam || bestTeam != null) && outShelter == 0) {
                rec.setRecommendedStatus(RequestStatus.ALLOCATED);
                rec.setExplanation("Full rescue allocation available: " +
                        (bestTeam != null ? bestTeam.getName() + " assigned; " : "") +
                        (bestShelter != null ? recShelter + " places reserved at " + bestShelter.getName() : ""));
            } else {
                rec.setRecommendedStatus(RequestStatus.PARTIALLY_ALLOCATED);
                rec.setExplanation("Partial rescue allocation available. " +
                        (bestTeam != null ? bestTeam.getName() + " ready; " : "No rescue team ready; ") +
                        (bestShelter != null ? recShelter + " shelter places available (" + outShelter + " waiting)." : ""));
            }
        } else {
            rec.setCanAllocate(false);
            rec.setRecommendedStatus(RequestStatus.PENDING);
            rec.setExplanation("No rescue teams or shelter capacities currently available for allocation.");
        }
    }

    // ==========================================
    // HUMAN IN THE LOOP: CONFIRMATION & REJECTION
    // ==========================================

    /**
     * Confirms an allocation recommendation inside ONE single atomic database transaction.
     * Guarantees: no negative stock, no duplicate allocations, no shelter overbooking,
     * and no conflicting active rescue team assignments.
     */
    public synchronized boolean confirmAllocation(int requestId, String officerName) throws SQLException {
        ReliefRequest request = requestsMap.get(requestId);
        if (request == null) {
            throw new IllegalArgumentException("Request ID " + requestId + " not found.");
        }

        if (request.getStatus() == RequestStatus.ALLOCATED) {
            throw new IllegalStateException("Duplicate allocation refused: Request #" + requestId + " has already been fully allocated.");
        }

        Recommendation rec = generateRecommendation(requestId);
        if (rec == null || !rec.isCanAllocate()) {
            throw new IllegalStateException("Cannot confirm allocation: No valid resources or path available.");
        }

        if (officerName == null || officerName.trim().isEmpty()) {
            officerName = "Command Officer";
        }

        Connection conn = null;
        try {
            conn = DBConfig.getConnection();
            conn.setAutoCommit(false); // ATOMIC TRANSACTION START

            Depot depot = null;
            if (rec.getRecommendedDepotId() != null) {
                depot = depotsMap.get(rec.getRecommendedDepotId());
                if (depot == null) throw new IllegalStateException("Depot not found.");
                
                int newFood = depot.getFoodKits() - rec.getFoodRecommended();
                int newMed = depot.getMedicineKits() - rec.getMedicineRecommended();
                if (newFood < 0 || newMed < 0) {
                    throw new IllegalStateException("Insufficient stock in depot. Operation aborted.");
                }
                depotDAO.updateStock(conn, depot.getId(), newFood, newMed);
            }

            RescueTeam team = null;
            if (rec.getRecommendedTeamId() != null) {
                team = teamsMap.get(rec.getRecommendedTeamId());
                if (team == null || !team.isAvailable()) {
                    throw new IllegalStateException("Rescue team is no longer available.");
                }
                teamDAO.updateAvailability(conn, team.getId(), false, requestId);
            }

            Shelter shelter = null;
            if (rec.getRecommendedShelterId() != null && rec.getShelterPlacesRecommended() > 0) {
                shelter = sheltersMap.get(rec.getRecommendedShelterId());
                if (shelter == null || shelter.availablePlaces() < rec.getShelterPlacesRecommended()) {
                    throw new IllegalStateException("Shelter places no longer available.");
                }
                int newReserved = shelter.getReservedPlaces() + rec.getShelterPlacesRecommended();
                shelterDAO.updateReservedPlaces(conn, shelter.getId(), newReserved);
            }

            // Update request status and numbers
            if (request instanceof SupplyRequest) {
                SupplyRequest sr = (SupplyRequest) request;
                sr.setAllocatedFood(sr.getAllocatedFood() + rec.getFoodRecommended());
                sr.setAllocatedMedicine(sr.getAllocatedMedicine() + rec.getMedicineRecommended());
            } else if (request instanceof RescueRequest) {
                RescueRequest rr = (RescueRequest) request;
                if (team != null) {
                    rr.setAllocatedTeams(rr.getAllocatedTeams() + 1);
                }
                rr.setAllocatedShelterPlaces(rr.getAllocatedShelterPlaces() + rec.getShelterPlacesRecommended());
            }

            request.setStatus(rec.getRecommendedStatus());
            requestDAO.updateRequest(conn, request);

            // Record confirmed allocation
            Allocation alloc = new Allocation();
            alloc.setRequestId(requestId);
            alloc.setDepotId(rec.getRecommendedDepotId());
            alloc.setRescueTeamId(rec.getRecommendedTeamId());
            alloc.setShelterId(rec.getRecommendedShelterId());
            alloc.setFoodAllocated(rec.getFoodRecommended());
            alloc.setMedicineAllocated(rec.getMedicineRecommended());
            alloc.setShelterPlacesAllocated(rec.getShelterPlacesRecommended());
            alloc.setRoutePath(rec.getRouteSummary());
            alloc.setTravelTime(rec.getTravelTimeMinutes());
            alloc.setOfficerName(officerName);
            alloc.setStatus("CONFIRMED");
            allocationDAO.insertAllocation(conn, alloc);

            // Record audit log
            AuditLog audit = new AuditLog(
                    "ALLOCATION_CONFIRMED",
                    "Officer " + officerName + " confirmed allocation for Request #" + requestId +
                            " [" + rec.getExplanation() + "]",
                    officerName
            );
            auditDAO.insertAuditLog(conn, audit);

            conn.commit(); // COMMIT TRANSACTION

            // Synchronize in-memory objects upon successful commit
            if (depot != null) {
                depot.reserveStock(rec.getFoodRecommended(), rec.getMedicineRecommended());
            }
            if (team != null) {
                team.reserveTeam(requestId);
            }
            if (shelter != null && rec.getShelterPlacesRecommended() > 0) {
                shelter.reservePlaces(rec.getShelterPlacesRecommended());
            }

            // Re-prioritize remaining pending requests
            rebuildPriorityQueue();
            return true;

        } catch (SQLException | RuntimeException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Officer rejects an allocation recommendation with a mandatory reason.
     */
    public synchronized void rejectAllocation(int requestId, String reason, String officerName) throws SQLException {
        ReliefRequest req = requestsMap.get(requestId);
        if (req == null) throw new IllegalArgumentException("Request ID " + requestId + " not found.");
        if (reason == null || reason.trim().isEmpty()) {
            reason = "Dismissed by reviewing officer.";
        }
        if (officerName == null || officerName.trim().isEmpty()) {
            officerName = "Command Officer";
        }

        req.setStatus(RequestStatus.REJECTED);
        req.setRejectionReason(reason);
        requestDAO.rejectRequest(requestId, reason);

        AuditLog audit = new AuditLog(
                "ALLOCATION_REJECTED",
                "Officer " + officerName + " rejected Request #" + requestId + ": " + reason,
                officerName
        );
        auditDAO.insertAuditLog(audit);

        rebuildPriorityQueue();
    }

    // ==========================================
    // MANUAL OPERATIONS
    // ==========================================

    /**
     * Adds a new emergency report, records to DB and memory, then re-prioritizes.
     */
    public synchronized int addEmergencyReport(ReliefRequest request) throws SQLException {
        int order = requestDAO.getNextArrivalOrder();
        request.setArrivalOrder(order);

        int id = requestDAO.insertRequest(request);
        request.setId(id);
        AffectedRegion r = regionsMap.get(request.getRegionId());
        if (r != null) request.setRegionName(r.getName());
        requestsMap.put(id, request);

        // Check initial reachability
        boolean reachableFromHQ = ReachabilityService.isReachableBFS(roadGraph, 1, request.getRegionId());
        if (!reachableFromHQ) {
            request.setStatus(RequestStatus.UNREACHABLE);
            requestDAO.updateRequest(request);
        }

        AuditLog audit = new AuditLog(
                "REQUEST_CREATED",
                "New " + request.getRequestType() + " request #" + id + " in " +
                        request.getRegionName() + " (Sev: " + request.getSeverity() + ", Pop: " +
                        request.getAffectedPopulation() + ")",
                request.getReportedBy()
        );
        auditDAO.insertAuditLog(audit);

        rebuildPriorityQueue();
        return id;
    }

    /**
     * Edits an emergency request.
     * Note: Rule 9 states that if ranking fields change, request must be REMOVED and RE-INSERTED into PriorityQueue.
     */
    public synchronized void updateEmergencyReport(int id, int severity, int population,
                                                   int food, int med, int teams, int shelterPlaces) throws SQLException {
        ReliefRequest req = requestsMap.get(id);
        if (req == null) throw new IllegalArgumentException("Request #" + id + " not found.");

        // Rule 9: Remove from PriorityQueue before mutation
        priorityQueue.remove(req);

        req.setSeverity(severity);
        req.setAffectedPopulation(population);

        if (req instanceof SupplyRequest) {
            SupplyRequest sr = (SupplyRequest) req;
            sr.setRequestedFood(food);
            sr.setRequestedMedicine(med);
        } else if (req instanceof RescueRequest) {
            RescueRequest rr = (RescueRequest) req;
            rr.setRequestedTeams(teams);
            rr.setRequestedShelterPlaces(shelterPlaces);
        }

        requestDAO.updateRequest(req);

        // Re-insert into PriorityQueue
        if (req.getStatus() == RequestStatus.PENDING ||
            req.getStatus() == RequestStatus.PARTIALLY_ALLOCATED ||
            req.getStatus() == RequestStatus.UNREACHABLE) {
            priorityQueue.add(req);
        }

        AuditLog audit = new AuditLog(
                "REQUEST_UPDATED",
                "Updated details for Request #" + id + " (Severity: " + severity + ", Pop: " + population + ")",
                "Command Officer"
        );
        auditDAO.insertAuditLog(audit);
    }

    /**
     * Cancels an emergency request.
     */
    public synchronized void cancelEmergencyReport(int id, String officer) throws SQLException {
        ReliefRequest req = requestsMap.get(id);
        if (req == null) throw new IllegalArgumentException("Request #" + id + " not found.");

        priorityQueue.remove(req);
        req.setStatus(RequestStatus.CANCELLED);
        requestDAO.cancelRequest(id);

        AuditLog audit = new AuditLog(
                "REQUEST_CANCELLED",
                "Request #" + id + " was cancelled by " + officer,
                officer
        );
        auditDAO.insertAuditLog(audit);

        rebuildPriorityQueue();
    }

    /**
     * Updates depot stock inventory.
     */
    public synchronized void updateDepotStock(int depotId, int foodKits, int medicineKits, boolean isAdd) throws SQLException {
        Depot depot = depotsMap.get(depotId);
        if (depot == null) throw new IllegalArgumentException("Depot #" + depotId + " not found.");

        if (isAdd) {
            depot.addStock(foodKits, medicineKits);
        } else {
            depot.setStock(foodKits, medicineKits);
        }

        depotDAO.updateStock(depotId, depot.getFoodKits(), depot.getMedicineKits());

        AuditLog audit = new AuditLog(
                "STOCK_UPDATED",
                "Depot '" + depot.getName() + "' stock updated: Food=" + depot.getFoodKits() + ", Med=" + depot.getMedicineKits(),
                "Inventory Manager"
        );
        auditDAO.insertAuditLog(audit);

        rebuildPriorityQueue();
    }

    /**
     * Toggles or sets rescue team availability.
     */
    public synchronized void updateTeamAvailability(int teamId, boolean available) throws SQLException {
        RescueTeam team = teamsMap.get(teamId);
        if (team == null) throw new IllegalArgumentException("Rescue team #" + teamId + " not found.");

        team.setAvailable(available);
        teamDAO.setAvailability(teamId, available);

        AuditLog audit = new AuditLog(
                "TEAM_STATUS_CHANGED",
                "Rescue Team '" + team.getName() + "' availability set to " + (available ? "AVAILABLE" : "DEPLOYED/OFFLINE"),
                "Dispatcher"
        );
        auditDAO.insertAuditLog(audit);

        rebuildPriorityQueue();
    }

    /**
     * Updates shelter capacity.
     */
    public synchronized void updateShelterCapacity(int shelterId, int capacity) throws SQLException {
        Shelter shelter = sheltersMap.get(shelterId);
        if (shelter == null) throw new IllegalArgumentException("Shelter #" + shelterId + " not found.");

        shelter.setCapacity(capacity);
        shelterDAO.updateCapacity(shelterId, capacity);

        AuditLog audit = new AuditLog(
                "SHELTER_CAPACITY_UPDATED",
                "Shelter '" + shelter.getName() + "' capacity updated to " + capacity,
                "Camp Coordinator"
        );
        auditDAO.insertAuditLog(audit);

        rebuildPriorityQueue();
    }

    /**
     * Opens or closes a road and automatically triggers re-prioritization and route recalculation.
     */
    public synchronized void setRoadOpen(int roadId, boolean isOpen) throws SQLException {
        Road road = roadsMap.get(roadId);
        if (road == null) throw new IllegalArgumentException("Road #" + roadId + " not found.");

        road.setOpen(isOpen);
        roadGraph.setRoadOpen(roadId, isOpen);
        roadDAO.setRoadOpen(roadId, isOpen);

        // Also update matching reverse direction road if one exists between the same regions
        for (Road other : roadsMap.values()) {
            if (other.getFromRegionId() == road.getToRegionId() &&
                other.getToRegionId() == road.getFromRegionId() &&
                other.isOpen() != isOpen) {
                other.setOpen(isOpen);
                roadGraph.setRoadOpen(other.getId(), isOpen);
                roadDAO.setRoadOpen(other.getId(), isOpen);
            }
        }

        AuditLog audit = new AuditLog(
                isOpen ? "ROAD_OPENED" : "ROAD_CLOSED",
                "Road '" + road.getFromRegionName() + " <-> " + road.getToRegionName() + "' was " +
                        (isOpen ? "OPENED" : "CLOSED") + ". Network routes recalculated.",
                "Traffic Control"
        );
        auditDAO.insertAuditLog(audit);

        // Check if unreachable requests now have open paths or vice versa
        for (ReliefRequest req : requestsMap.values()) {
            if (req.getStatus() == RequestStatus.UNREACHABLE || req.getStatus() == RequestStatus.PENDING) {
                boolean reachable = ReachabilityService.isReachableBFS(roadGraph, 1, req.getRegionId());
                if (reachable && req.getStatus() == RequestStatus.UNREACHABLE) {
                    req.setStatus(RequestStatus.PENDING);
                    requestDAO.updateRequest(req);
                } else if (!reachable && req.getStatus() == RequestStatus.PENDING) {
                    req.setStatus(RequestStatus.UNREACHABLE);
                    requestDAO.updateRequest(req);
                }
            }
        }

        rebuildPriorityQueue();
    }

    /**
     * Adds a new road edge.
     */
    public synchronized int addRoad(Road road) throws SQLException {
        int id = roadDAO.addRoad(road);
        road.setId(id);
        AffectedRegion f = regionsMap.get(road.getFromRegionId());
        AffectedRegion t = regionsMap.get(road.getToRegionId());
        if (f != null) road.setFromRegionName(f.getName());
        if (t != null) road.setToRegionName(t.getName());

        roadsMap.put(id, road);
        roadGraph.addEdge(road);

        rebuildPriorityQueue();
        return id;
    }

    public synchronized List<Allocation> getAllAllocations() throws SQLException {
        return allocationDAO.getAllAllocations();
    }

    public synchronized List<AuditLog> getAuditLogs(int limit) throws SQLException {
        return auditDAO.getAllLogs(limit);
    }

    /**
     * Resets the entire MySQL database and in-memory structures back to the original demo seed state.
     */
    public synchronized void resetDemoData() throws SQLException, IOException {
        executeSqlScript("sql/schema.sql");
        executeSqlScript("sql/seed.sql");
        loadAllFromDatabase();
    }

    private void executeSqlScript(String filePath) throws SQLException, IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("SQL script not found: " + filePath);
        }
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("--") || line.isEmpty()) {
                    continue;
                }
                sb.append(line).append("\n");
            }
        } finally {
            reader.close();
        }

        String[] statements = sb.toString().split(";");
        Connection conn = DBConfig.getConnection();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            for (int i = 0; i < statements.length; i++) {
                String trimmed = statements[i].trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }
}
