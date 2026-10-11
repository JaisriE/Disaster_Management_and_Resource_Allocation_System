package com.aruvi.dms.dao;

import com.aruvi.dms.config.DBConfig;
import com.aruvi.dms.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RequestDAO {

    public List<ReliefRequest> getAllRequests() throws SQLException {
        List<ReliefRequest> list = new ArrayList<>();
        String sql = "SELECT er.*, r.name AS region_name " +
                     "FROM emergency_requests er " +
                     "JOIN regions r ON er.region_id = r.id " +
                     "ORDER BY er.id ASC";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToRequest(rs));
            }
        }
        return list;
    }

    public ReliefRequest getRequestById(int id) throws SQLException {
        String sql = "SELECT er.*, r.name AS region_name " +
                     "FROM emergency_requests er " +
                     "JOIN regions r ON er.region_id = r.id " +
                     "WHERE er.id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRequest(rs);
                }
            }
        }
        return null;
    }

    public int getNextArrivalOrder() throws SQLException {
        String sql = "SELECT COALESCE(MAX(arrival_order), 0) + 1 AS next_order FROM emergency_requests";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("next_order");
            }
        }
        return 1;
    }

    public int insertRequest(ReliefRequest request) throws SQLException {
        String sql = "INSERT INTO emergency_requests (" +
                     "region_id, severity, affected_population, arrival_order, request_type, " +
                     "requested_food, allocated_food, outstanding_food, " +
                     "requested_medicine, allocated_medicine, outstanding_medicine, " +
                     "requested_teams, allocated_teams, " +
                     "requested_shelter_places, allocated_shelter_places, outstanding_shelter_places, " +
                     "status, reported_by, rejection_reason" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, request.getRegionId());
            ps.setInt(2, request.getSeverity());
            ps.setInt(3, request.getAffectedPopulation());
            ps.setInt(4, request.getArrivalOrder());
            ps.setString(5, request.getRequestType());

            if (request instanceof SupplyRequest) {
                SupplyRequest sr = (SupplyRequest) request;
                ps.setInt(6, sr.getRequestedFood());
                ps.setInt(7, sr.getAllocatedFood());
                ps.setInt(8, sr.getOutstandingFood());
                ps.setInt(9, sr.getRequestedMedicine());
                ps.setInt(10, sr.getAllocatedMedicine());
                ps.setInt(11, sr.getOutstandingMedicine());
                ps.setInt(12, 0);
                ps.setInt(13, 0);
                ps.setInt(14, 0);
                ps.setInt(15, 0);
                ps.setInt(16, 0);
            } else if (request instanceof RescueRequest) {
                RescueRequest rr = (RescueRequest) request;
                ps.setInt(6, 0);
                ps.setInt(7, 0);
                ps.setInt(8, 0);
                ps.setInt(9, 0);
                ps.setInt(10, 0);
                ps.setInt(11, 0);
                ps.setInt(12, rr.getRequestedTeams());
                ps.setInt(13, rr.getAllocatedTeams());
                ps.setInt(14, rr.getRequestedShelterPlaces());
                ps.setInt(15, rr.getAllocatedShelterPlaces());
                ps.setInt(16, rr.getOutstandingShelterPlaces());
            }

            ps.setString(17, request.getStatus().name());
            ps.setString(18, request.getReportedBy());
            ps.setString(19, request.getRejectionReason());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    request.setId(id);
                    return id;
                }
            }
        }
        return 0;
    }

    public void updateRequest(Connection conn, ReliefRequest request) throws SQLException {
        String sql = "UPDATE emergency_requests SET " +
                     "region_id = ?, severity = ?, affected_population = ?, arrival_order = ?, " +
                     "requested_food = ?, allocated_food = ?, outstanding_food = ?, " +
                     "requested_medicine = ?, allocated_medicine = ?, outstanding_medicine = ?, " +
                     "requested_teams = ?, allocated_teams = ?, " +
                     "requested_shelter_places = ?, allocated_shelter_places = ?, outstanding_shelter_places = ?, " +
                     "status = ?, rejection_reason = ? " +
                     "WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, request.getRegionId());
            ps.setInt(2, request.getSeverity());
            ps.setInt(3, request.getAffectedPopulation());
            ps.setInt(4, request.getArrivalOrder());

            if (request instanceof SupplyRequest) {
                SupplyRequest sr = (SupplyRequest) request;
                ps.setInt(5, sr.getRequestedFood());
                ps.setInt(6, sr.getAllocatedFood());
                ps.setInt(7, sr.getOutstandingFood());
                ps.setInt(8, sr.getRequestedMedicine());
                ps.setInt(9, sr.getAllocatedMedicine());
                ps.setInt(10, sr.getOutstandingMedicine());
                ps.setInt(11, 0);
                ps.setInt(12, 0);
                ps.setInt(13, 0);
                ps.setInt(14, 0);
                ps.setInt(15, 0);
            } else if (request instanceof RescueRequest) {
                RescueRequest rr = (RescueRequest) request;
                ps.setInt(5, 0);
                ps.setInt(6, 0);
                ps.setInt(7, 0);
                ps.setInt(8, 0);
                ps.setInt(9, 0);
                ps.setInt(10, 0);
                ps.setInt(11, rr.getRequestedTeams());
                ps.setInt(12, rr.getAllocatedTeams());
                ps.setInt(13, rr.getRequestedShelterPlaces());
                ps.setInt(14, rr.getAllocatedShelterPlaces());
                ps.setInt(15, rr.getOutstandingShelterPlaces());
            }

            ps.setString(16, request.getStatus().name());
            ps.setString(17, request.getRejectionReason());
            ps.setInt(18, request.getId());

            ps.executeUpdate();
        }
    }

    public void updateRequest(ReliefRequest request) throws SQLException {
        try (Connection conn = DBConfig.getConnection()) {
            updateRequest(conn, request);
        }
    }

    public void cancelRequest(int id) throws SQLException {
        String sql = "UPDATE emergency_requests SET status = 'CANCELLED' WHERE id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void rejectRequest(int id, String reason) throws SQLException {
        String sql = "UPDATE emergency_requests SET status = 'REJECTED', rejection_reason = ? WHERE id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private ReliefRequest mapResultSetToRequest(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int regionId = rs.getInt("region_id");
        String regionName = rs.getString("region_name");
        int severity = rs.getInt("severity");
        int affectedPop = rs.getInt("affected_population");
        int arrivalOrder = rs.getInt("arrival_order");
        String type = rs.getString("request_type");
        String statusStr = rs.getString("status");
        String reportedBy = rs.getString("reported_by");
        String rejectionReason = rs.getString("rejection_reason");
        Timestamp createdAt = rs.getTimestamp("created_at");

        ReliefRequest req;
        if ("SUPPLY".equalsIgnoreCase(type)) {
            SupplyRequest sr = new SupplyRequest(
                    id, regionId, severity, affectedPop, arrivalOrder,
                    rs.getInt("requested_food"),
                    rs.getInt("requested_medicine")
            );
            sr.setAllocatedFood(rs.getInt("allocated_food"));
            sr.setOutstandingFood(rs.getInt("outstanding_food"));
            sr.setAllocatedMedicine(rs.getInt("allocated_medicine"));
            sr.setOutstandingMedicine(rs.getInt("outstanding_medicine"));
            req = sr;
        } else {
            RescueRequest rr = new RescueRequest(
                    id, regionId, severity, affectedPop, arrivalOrder,
                    rs.getInt("requested_teams"),
                    rs.getInt("requested_shelter_places")
            );
            rr.setAllocatedTeams(rs.getInt("allocated_teams"));
            rr.setAllocatedShelterPlaces(rs.getInt("allocated_shelter_places"));
            rr.setOutstandingShelterPlaces(rs.getInt("outstanding_shelter_places"));
            req = rr;
        }

        req.setRegionName(regionName);
        req.setStatus(RequestStatus.valueOf(statusStr));
        req.setReportedBy(reportedBy);
        req.setRejectionReason(rejectionReason);
        if (createdAt != null) {
            req.setCreatedAt(new java.util.Date(createdAt.getTime()));
        }
        return req;
    }
}
