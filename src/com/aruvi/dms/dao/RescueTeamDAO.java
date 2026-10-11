package com.aruvi.dms.dao;

import com.aruvi.dms.config.DBConfig;
import com.aruvi.dms.model.RescueTeam;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RescueTeamDAO {

    public List<RescueTeam> getAllRescueTeams() throws SQLException {
        List<RescueTeam> list = new ArrayList<>();
        String sql = "SELECT t.id, t.name, t.home_depot_id, d.name AS home_depot_name, t.is_available, t.current_request_id " +
                     "FROM rescue_teams t " +
                     "JOIN depots d ON t.home_depot_id = d.id " +
                     "ORDER BY t.id ASC";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RescueTeam team = new RescueTeam(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("home_depot_id"),
                        rs.getBoolean("is_available")
                );
                team.setHomeDepotName(rs.getString("home_depot_name"));
                int reqId = rs.getInt("current_request_id");
                if (!rs.wasNull()) {
                    team.setCurrentRequestId(reqId);
                }
                list.add(team);
            }
        }
        return list;
    }

    public RescueTeam getTeamById(int id) throws SQLException {
        String sql = "SELECT t.id, t.name, t.home_depot_id, d.name AS home_depot_name, t.is_available, t.current_request_id " +
                     "FROM rescue_teams t " +
                     "JOIN depots d ON t.home_depot_id = d.id " +
                     "WHERE t.id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RescueTeam team = new RescueTeam(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("home_depot_id"),
                            rs.getBoolean("is_available")
                    );
                    team.setHomeDepotName(rs.getString("home_depot_name"));
                    int reqId = rs.getInt("current_request_id");
                    if (!rs.wasNull()) {
                        team.setCurrentRequestId(reqId);
                    }
                    return team;
                }
            }
        }
        return null;
    }

    public void updateAvailability(Connection conn, int teamId, boolean isAvailable, Integer currentRequestId) throws SQLException {
        String sql = "UPDATE rescue_teams SET is_available = ?, current_request_id = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isAvailable);
            if (currentRequestId != null) {
                ps.setInt(2, currentRequestId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, teamId);
            ps.executeUpdate();
        }
    }

    public void setAvailability(int teamId, boolean isAvailable) throws SQLException {
        try (Connection conn = DBConfig.getConnection()) {
            updateAvailability(conn, teamId, isAvailable, isAvailable ? null : 0);
        }
    }
}
