package com.aruvi.dms.dao;

import com.aruvi.dms.config.DBConfig;
import com.aruvi.dms.model.Allocation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AllocationDAO {

    public List<Allocation> getAllAllocations() throws SQLException {
        List<Allocation> list = new ArrayList<>();
        String sql = "SELECT a.*, d.name AS depot_name, t.name AS team_name, s.name AS shelter_name " +
                     "FROM allocations a " +
                     "LEFT JOIN depots d ON a.depot_id = d.id " +
                     "LEFT JOIN rescue_teams t ON a.rescue_team_id = t.id " +
                     "LEFT JOIN shelters s ON a.shelter_id = s.id " +
                     "ORDER BY a.id DESC";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Allocation alloc = new Allocation();
                alloc.setId(rs.getInt("id"));
                alloc.setRequestId(rs.getInt("request_id"));

                int depotId = rs.getInt("depot_id");
                if (!rs.wasNull()) alloc.setDepotId(depotId);

                int teamId = rs.getInt("rescue_team_id");
                if (!rs.wasNull()) alloc.setRescueTeamId(teamId);

                int shelterId = rs.getInt("shelter_id");
                if (!rs.wasNull()) alloc.setShelterId(shelterId);

                alloc.setDepotName(rs.getString("depot_name"));
                alloc.setRescueTeamName(rs.getString("team_name"));
                alloc.setShelterName(rs.getString("shelter_name"));

                alloc.setFoodAllocated(rs.getInt("food_allocated"));
                alloc.setMedicineAllocated(rs.getInt("medicine_allocated"));
                alloc.setShelterPlacesAllocated(rs.getInt("shelter_places_allocated"));
                alloc.setRoutePath(rs.getString("route_path"));
                alloc.setTravelTime(rs.getInt("travel_time"));
                alloc.setOfficerName(rs.getString("officer_name"));
                alloc.setStatus(rs.getString("status"));
                alloc.setConfirmedAt(rs.getTimestamp("confirmed_at"));

                list.add(alloc);
            }
        }
        return list;
    }

    public int insertAllocation(Connection conn, Allocation alloc) throws SQLException {
        String sql = "INSERT INTO allocations (" +
                     "request_id, depot_id, rescue_team_id, shelter_id, " +
                     "food_allocated, medicine_allocated, shelter_places_allocated, " +
                     "route_path, travel_time, officer_name, status" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, alloc.getRequestId());

            if (alloc.getDepotId() != null) ps.setInt(2, alloc.getDepotId());
            else ps.setNull(2, Types.INTEGER);

            if (alloc.getRescueTeamId() != null) ps.setInt(3, alloc.getRescueTeamId());
            else ps.setNull(3, Types.INTEGER);

            if (alloc.getShelterId() != null) ps.setInt(4, alloc.getShelterId());
            else ps.setNull(4, Types.INTEGER);

            ps.setInt(5, alloc.getFoodAllocated());
            ps.setInt(6, alloc.getMedicineAllocated());
            ps.setInt(7, alloc.getShelterPlacesAllocated());
            ps.setString(8, alloc.getRoutePath());
            ps.setInt(9, alloc.getTravelTime());
            ps.setString(10, alloc.getOfficerName());
            ps.setString(11, alloc.getStatus());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    alloc.setId(id);
                    return id;
                }
            }
        }
        return 0;
    }
}
