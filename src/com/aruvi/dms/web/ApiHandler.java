package com.aruvi.dms.web;

import com.aruvi.dms.model.*;
import com.aruvi.dms.service.DisasterManagementService;
import com.aruvi.dms.util.JsonUtil;
import com.aruvi.dms.util.ValidationUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * ApiHandler processes all REST API endpoints under /api/...
 * Ensures robust error handling, returning clean HTTP 400 responses on invalid user input.
 */
public class ApiHandler implements HttpHandler {

    private final DisasterManagementService service = DisasterManagementService.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath();

        // Handle CORS Pre-flight requests
        if ("OPTIONS".equals(method)) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            if ("GET".equals(method)) {
                handleGet(exchange, path);
            } else if ("POST".equals(method)) {
                handlePost(exchange, path);
            } else {
                sendError(exchange, 405, "Method Not Allowed");
            }
        } catch (IllegalArgumentException e) {
            sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            System.err.println("[ApiHandler] Error processing " + path + ": " + e.getMessage());
            e.printStackTrace();
            sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws Exception {
        Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());

        if ("/api/status".equals(path)) {
            Map<String, Object> status = new LinkedHashMap<>();
            status.put("title", "Disaster Management Resource Allocation System");
            status.put("district", "Aruvi District Control Room");
            status.put("totalRegions", service.getAllRegions().size());
            status.put("totalDepots", service.getAllDepots().size());
            status.put("totalShelters", service.getAllShelters().size());
            status.put("totalTeams", service.getAllRescueTeams().size());
            status.put("totalRequests", service.getAllRequests().size());
            status.put("pendingRequests", service.getPrioritizedPendingRequests().size());
            sendJsonResponse(exchange, 200, status);

        } else if ("/api/regions".equals(path)) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (AffectedRegion r : service.getAllRegions()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", r.getId());
                m.put("name", r.getName());
                m.put("population", r.getPopulation());
                m.put("description", r.getDescription());
                list.add(m);
            }
            sendJsonResponse(exchange, 200, list);

        } else if ("/api/depots".equals(path)) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Depot d : service.getAllDepots()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", d.getId());
                m.put("name", d.getName());
                m.put("regionId", d.getRegionId());
                m.put("regionName", d.getRegionName());
                m.put("foodKits", d.getFoodKits());
                m.put("medicineKits", d.getMedicineKits());
                list.add(m);
            }
            sendJsonResponse(exchange, 200, list);

        } else if ("/api/shelters".equals(path)) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Shelter s : service.getAllShelters()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", s.getId());
                m.put("name", s.getName());
                m.put("regionId", s.getRegionId());
                m.put("regionName", s.getRegionName());
                m.put("capacity", s.getCapacity());
                m.put("reservedPlaces", s.getReservedPlaces());
                m.put("availablePlaces", s.availablePlaces());
                list.add(m);
            }
            sendJsonResponse(exchange, 200, list);

        } else if ("/api/teams".equals(path)) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (RescueTeam t : service.getAllRescueTeams()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", t.getId());
                m.put("name", t.getName());
                m.put("homeDepotId", t.getHomeDepotId());
                m.put("homeDepotName", t.getHomeDepotName());
                m.put("available", t.isAvailable());
                m.put("currentRequestId", t.getCurrentRequestId());
                list.add(m);
            }
            sendJsonResponse(exchange, 200, list);

        } else if ("/api/roads".equals(path)) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Road r : service.getAllRoads()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", r.getId());
                m.put("fromRegionId", r.getFromRegionId());
                m.put("fromRegionName", r.getFromRegionName());
                m.put("toRegionId", r.getToRegionId());
                m.put("toRegionName", r.getToRegionName());
                m.put("travelTime", r.getTravelTime());
                m.put("open", r.isOpen());
                m.put("description", r.getDescription());
                list.add(m);
            }
            sendJsonResponse(exchange, 200, list);

        } else if ("/api/requests".equals(path)) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (ReliefRequest req : service.getAllRequests()) {
                list.add(mapRequestToMap(req));
            }
            sendJsonResponse(exchange, 200, list);

        } else if ("/api/requests/prioritized".equals(path)) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (ReliefRequest req : service.getPrioritizedPendingRequests()) {
                list.add(mapRequestToMap(req));
            }
            sendJsonResponse(exchange, 200, list);

        } else if ("/api/recommendation".equals(path)) {
            String idStr = query.get("requestId");
            if (idStr == null || idStr.trim().isEmpty()) {
                sendError(exchange, 400, "Missing required query parameter: requestId");
                return;
            }
            int reqId = Integer.parseInt(idStr);
            Recommendation rec = service.generateRecommendation(reqId);
            if (rec == null) {
                sendError(exchange, 404, "Request #" + reqId + " not found.");
                return;
            }

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("requestId", rec.getRequestId());
            m.put("requestType", rec.getRequestType());
            m.put("regionId", rec.getRegionId());
            m.put("regionName", rec.getRegionName());
            m.put("reachable", rec.isReachable());
            m.put("routeSummary", rec.getRouteSummary());
            m.put("pathRegionIds", rec.getPathRegionIds());
            m.put("pathRegionNames", rec.getPathRegionNames());
            m.put("travelTimeMinutes", rec.getTravelTimeMinutes());
            m.put("recommendedDepotId", rec.getRecommendedDepotId());
            m.put("recommendedDepotName", rec.getRecommendedDepotName());
            m.put("foodRecommended", rec.getFoodRecommended());
            m.put("medicineRecommended", rec.getMedicineRecommended());
            m.put("outstandingFood", rec.getOutstandingFood());
            m.put("outstandingMedicine", rec.getOutstandingMedicine());
            m.put("recommendedTeamId", rec.getRecommendedTeamId());
            m.put("recommendedTeamName", rec.getRecommendedTeamName());
            m.put("recommendedShelterId", rec.getRecommendedShelterId());
            m.put("recommendedShelterName", rec.getRecommendedShelterName());
            m.put("shelterPlacesRecommended", rec.getShelterPlacesRecommended());
            m.put("outstandingShelterPlaces", rec.getOutstandingShelterPlaces());
            m.put("canAllocate", rec.isCanAllocate());
            m.put("recommendedStatus", rec.getRecommendedStatus().name());
            m.put("explanation", rec.getExplanation());

            sendJsonResponse(exchange, 200, m);

        } else if ("/api/allocations".equals(path)) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Allocation a : service.getAllAllocations()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", a.getId());
                m.put("requestId", a.getRequestId());
                m.put("depotName", a.getDepotName());
                m.put("rescueTeamName", a.getRescueTeamName());
                m.put("shelterName", a.getShelterName());
                m.put("foodAllocated", a.getFoodAllocated());
                m.put("medicineAllocated", a.getMedicineAllocated());
                m.put("shelterPlacesAllocated", a.getShelterPlacesAllocated());
                m.put("routePath", a.getRoutePath());
                m.put("travelTime", a.getTravelTime());
                m.put("officerName", a.getOfficerName());
                m.put("status", a.getStatus());
                m.put("confirmedAt", a.getConfirmedAt() != null ? a.getConfirmedAt().toString() : "");
                list.add(m);
            }
            sendJsonResponse(exchange, 200, list);

        } else if ("/api/audits".equals(path)) {
            int limit = 50;
            if (query.get("limit") != null) {
                try {
                    limit = Integer.parseInt(query.get("limit"));
                } catch (NumberFormatException ignored) {}
            }
            List<Map<String, Object>> list = new ArrayList<>();
            for (AuditLog l : service.getAuditLogs(limit)) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", l.getId());
                m.put("actionType", l.getActionType());
                m.put("description", l.getDescription());
                m.put("performedBy", l.getPerformedBy());
                m.put("createdAt", l.getCreatedAt() != null ? l.getCreatedAt().toString() : "");
                list.add(m);
            }
            sendJsonResponse(exchange, 200, list);

        } else {
            sendError(exchange, 404, "API endpoint not found: " + path);
        }
    }

    private void handlePost(HttpExchange exchange, String path) throws Exception {
        String body = readBody(exchange);
        Map<String, Object> json = JsonUtil.parseObject(body);

        if ("/api/requests/create".equals(path)) {
            int regionId = JsonUtil.getInt(json, "regionId", -1);
            int severity = JsonUtil.getInt(json, "severity", -1);
            int population = JsonUtil.getInt(json, "population", -1);
            String type = JsonUtil.getString(json, "type", "SUPPLY").toUpperCase();
            String officer = JsonUtil.getString(json, "reportedBy", "Field Officer");

            // Input validations
            if (regionId <= 0) {
                sendError(exchange, 400, "Valid regionId is required.");
                return;
            }
            if (service.getRegion(regionId) == null) {
                sendError(exchange, 400, "Referenced region ID " + regionId + " does not exist.");
                return;
            }
            ValidationUtil.ValidationResult vSev = ValidationUtil.validateSeverity(severity);
            if (!vSev.isValid()) {
                sendError(exchange, 400, vSev.getErrorMessage());
                return;
            }
            ValidationUtil.ValidationResult vPop = ValidationUtil.validatePopulation(population);
            if (!vPop.isValid()) {
                sendError(exchange, 400, vPop.getErrorMessage());
                return;
            }

            ReliefRequest req;
            if ("SUPPLY".equals(type)) {
                int food = JsonUtil.getInt(json, "requestedFood", 0);
                int med = JsonUtil.getInt(json, "requestedMedicine", 0);
                if (food < 0 || med < 0) {
                    sendError(exchange, 400, "Requested quantities cannot be negative.");
                    return;
                }
                if (food == 0 && med == 0) {
                    sendError(exchange, 400, "Supply request must specify at least 1 food kit or medicine kit.");
                    return;
                }
                req = new SupplyRequest(1, regionId, severity, population, 1, food, med);
            } else if ("RESCUE".equals(type)) {
                int teams = JsonUtil.getInt(json, "requestedTeams", 1);
                int shelter = JsonUtil.getInt(json, "requestedShelterPlaces", 0);
                if (teams < 0 || shelter < 0) {
                    sendError(exchange, 400, "Requested quantities cannot be negative.");
                    return;
                }
                if (teams == 0 && shelter == 0) {
                    sendError(exchange, 400, "Rescue request must specify at least 1 rescue team or shelter capacity.");
                    return;
                }
                req = new RescueRequest(1, regionId, severity, population, 1, teams, shelter);
            } else {
                sendError(exchange, 400, "Invalid request type: " + type + ". Must be 'SUPPLY' or 'RESCUE'.");
                return;
            }

            req.setReportedBy(officer);
            int newId = service.addEmergencyReport(req);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("id", newId);
            resp.put("message", "Emergency report #" + newId + " created successfully.");
            sendJsonResponse(exchange, 201, resp);

        } else if ("/api/requests/update".equals(path)) {
            int id = JsonUtil.getInt(json, "id", -1);
            int severity = JsonUtil.getInt(json, "severity", -1);
            int population = JsonUtil.getInt(json, "population", -1);
            int food = JsonUtil.getInt(json, "requestedFood", 0);
            int med = JsonUtil.getInt(json, "requestedMedicine", 0);
            int teams = JsonUtil.getInt(json, "requestedTeams", 0);
            int shelter = JsonUtil.getInt(json, "requestedShelterPlaces", 0);

            if (id <= 0 || service.getRequest(id) == null) {
                sendError(exchange, 400, "Valid request ID is required.");
                return;
            }
            ValidationUtil.ValidationResult vSev = ValidationUtil.validateSeverity(severity);
            if (!vSev.isValid()) {
                sendError(exchange, 400, vSev.getErrorMessage());
                return;
            }
            ValidationUtil.ValidationResult vPop = ValidationUtil.validatePopulation(population);
            if (!vPop.isValid()) {
                sendError(exchange, 400, vPop.getErrorMessage());
                return;
            }

            service.updateEmergencyReport(id, severity, population, food, med, teams, shelter);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("message", "Emergency report #" + id + " updated successfully.");
            sendJsonResponse(exchange, 200, resp);

        } else if ("/api/requests/cancel".equals(path)) {
            int id = JsonUtil.getInt(json, "id", -1);
            String officer = JsonUtil.getString(json, "officer", "Field Officer");
            if (id <= 0 || service.getRequest(id) == null) {
                sendError(exchange, 400, "Valid request ID is required.");
                return;
            }

            service.cancelEmergencyReport(id, officer);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("message", "Request #" + id + " has been cancelled.");
            sendJsonResponse(exchange, 200, resp);

        } else if ("/api/depots/stock".equals(path)) {
            int depotId = JsonUtil.getInt(json, "depotId", -1);
            int foodKits = JsonUtil.getInt(json, "foodKits", 0);
            int medicineKits = JsonUtil.getInt(json, "medicineKits", 0);
            boolean isAdd = JsonUtil.getBoolean(json, "isAdd", false);

            if (depotId <= 0 || service.getDepot(depotId) == null) {
                sendError(exchange, 400, "Valid depot ID is required.");
                return;
            }
            if (foodKits < 0 || medicineKits < 0) {
                sendError(exchange, 400, "Stock numbers cannot be negative.");
                return;
            }

            service.updateDepotStock(depotId, foodKits, medicineKits, isAdd);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("message", "Depot stock updated successfully.");
            sendJsonResponse(exchange, 200, resp);

        } else if ("/api/teams/availability".equals(path)) {
            int teamId = JsonUtil.getInt(json, "teamId", -1);
            boolean available = JsonUtil.getBoolean(json, "available", true);

            if (teamId <= 0 || service.getTeam(teamId) == null) {
                sendError(exchange, 400, "Valid team ID is required.");
                return;
            }

            service.updateTeamAvailability(teamId, available);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("message", "Rescue team availability updated.");
            sendJsonResponse(exchange, 200, resp);

        } else if ("/api/shelters/capacity".equals(path)) {
            int shelterId = JsonUtil.getInt(json, "shelterId", -1);
            int capacity = JsonUtil.getInt(json, "capacity", 0);

            if (shelterId <= 0 || service.getShelter(shelterId) == null) {
                sendError(exchange, 400, "Valid shelter ID is required.");
                return;
            }
            if (capacity <= 0) {
                sendError(exchange, 400, "Shelter capacity must be greater than zero.");
                return;
            }

            service.updateShelterCapacity(shelterId, capacity);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("message", "Shelter capacity updated.");
            sendJsonResponse(exchange, 200, resp);

        } else if ("/api/roads/toggle".equals(path)) {
            int roadId = JsonUtil.getInt(json, "roadId", -1);
            boolean open = JsonUtil.getBoolean(json, "open", true);

            if (roadId <= 0) {
                sendError(exchange, 400, "Valid road ID is required.");
                return;
            }

            service.setRoadOpen(roadId, open);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("message", "Road status toggled to " + (open ? "OPEN" : "CLOSED"));
            sendJsonResponse(exchange, 200, resp);

        } else if ("/api/roads/add".equals(path)) {
            int from = JsonUtil.getInt(json, "fromRegionId", -1);
            int to = JsonUtil.getInt(json, "toRegionId", -1);
            int time = JsonUtil.getInt(json, "travelTime", 0);
            boolean open = JsonUtil.getBoolean(json, "open", true);
            String desc = JsonUtil.getString(json, "description", "Road Link");

            if (from <= 0 || to <= 0 || from == to) {
                sendError(exchange, 400, "Valid and distinct from/to region IDs are required.");
                return;
            }
            if (time <= 0) {
                sendError(exchange, 400, "Travel time must be strictly positive (> 0 minutes).");
                return;
            }

            Road road = new Road(1, from, to, time, open, desc);
            int roadId = service.addRoad(road);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("id", roadId);
            resp.put("message", "Road added successfully.");
            sendJsonResponse(exchange, 201, resp);

        } else if ("/api/allocation/confirm".equals(path)) {
            int requestId = JsonUtil.getInt(json, "requestId", -1);
            String officer = JsonUtil.getString(json, "officerName", "Command Officer");

            if (requestId <= 0) {
                sendError(exchange, 400, "Valid requestId is required.");
                return;
            }

            boolean confirmed = service.confirmAllocation(requestId, officer);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", confirmed);
            resp.put("message", "Allocation successfully confirmed and committed to database.");
            sendJsonResponse(exchange, 200, resp);

        } else if ("/api/allocation/reject".equals(path)) {
            int requestId = JsonUtil.getInt(json, "requestId", -1);
            String reason = JsonUtil.getString(json, "reason", "Dismissed by officer.");
            String officer = JsonUtil.getString(json, "officerName", "Command Officer");

            if (requestId <= 0) {
                sendError(exchange, 400, "Valid requestId is required.");
                return;
            }

            service.rejectAllocation(requestId, reason, officer);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("message", "Request #" + requestId + " rejected.");
            sendJsonResponse(exchange, 200, resp);

        } else if ("/api/demo/reset".equals(path)) {
            service.resetDemoData();
            Map<String, Object> resp = new LinkedHashMap<String, Object>();
            resp.put("success", true);
            resp.put("message", "Original seed data restored successfully. All tables and queues reset.");
            sendJsonResponse(exchange, 200, resp);

        } else {
            sendError(exchange, 404, "API endpoint not found: " + path);
        }
    }

    private Map<String, Object> mapRequestToMap(ReliefRequest req) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", req.getId());
        m.put("regionId", req.getRegionId());
        m.put("regionName", req.getRegionName());
        m.put("severity", req.getSeverity());
        m.put("affectedPopulation", req.getAffectedPopulation());
        m.put("arrivalOrder", req.getArrivalOrder());
        m.put("requestType", req.getRequestType());
        m.put("status", req.getStatus().name());
        m.put("reportedBy", req.getReportedBy());
        m.put("rejectionReason", req.getRejectionReason());
        m.put("createdAt", req.getCreatedAt() != null ? req.getCreatedAt().toString() : "");

        if (req instanceof SupplyRequest) {
            SupplyRequest sr = (SupplyRequest) req;
            m.put("requestedFood", sr.getRequestedFood());
            m.put("allocatedFood", sr.getAllocatedFood());
            m.put("outstandingFood", sr.getOutstandingFood());
            m.put("requestedMedicine", sr.getRequestedMedicine());
            m.put("allocatedMedicine", sr.getAllocatedMedicine());
            m.put("outstandingMedicine", sr.getOutstandingMedicine());
        } else if (req instanceof RescueRequest) {
            RescueRequest rr = (RescueRequest) req;
            m.put("requestedTeams", rr.getRequestedTeams());
            m.put("allocatedTeams", rr.getAllocatedTeams());
            m.put("requestedShelterPlaces", rr.getRequestedShelterPlaces());
            m.put("allocatedShelterPlaces", rr.getAllocatedShelterPlaces());
            m.put("outstandingShelterPlaces", rr.getOutstandingShelterPlaces());
        }
        return m;
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int n;
            while ((n = is.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            return baos.toString(StandardCharsets.UTF_8.name());
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.trim().isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                map.put(pair.substring(0, idx), pair.substring(idx + 1));
            } else {
                map.put(pair, "");
            }
        }
        return map;
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String json = JsonUtil.toJson(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("success", false);
        err.put("error", message);
        sendJsonResponse(exchange, statusCode, err);
    }
}
