package com.aruvi.dms.dao;

import com.aruvi.dms.config.DBConfig;
import com.aruvi.dms.model.Road;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoadDAO {

    public List<Road> getAllRoads() throws SQLException {
        List<Road> list = new ArrayList<>();
        String sql = "SELECT rd.id, rd.from_region_id, r1.name AS from_region_name, " +
                     "rd.to_region_id, r2.name AS to_region_name, rd.travel_time, rd.is_open, rd.description " +
                     "FROM roads rd " +
                     "JOIN regions r1 ON rd.from_region_id = r1.id " +
                     "JOIN regions r2 ON rd.to_region_id = r2.id " +
                     "ORDER BY rd.id ASC";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Road road = new Road(
                        rs.getInt("id"),
                        rs.getInt("from_region_id"),
                        rs.getInt("to_region_id"),
                        rs.getInt("travel_time"),
                        rs.getBoolean("is_open"),
                        rs.getString("description")
                );
                road.setFromRegionName(rs.getString("from_region_name"));
                road.setToRegionName(rs.getString("to_region_name"));
                list.add(road);
            }
        }
        return list;
    }

    public Road getRoadById(int id) throws SQLException {
        String sql = "SELECT rd.id, rd.from_region_id, r1.name AS from_region_name, " +
                     "rd.to_region_id, r2.name AS to_region_name, rd.travel_time, rd.is_open, rd.description " +
                     "FROM roads rd " +
                     "JOIN regions r1 ON rd.from_region_id = r1.id " +
                     "JOIN regions r2 ON rd.to_region_id = r2.id " +
                     "WHERE rd.id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Road road = new Road(
                            rs.getInt("id"),
                            rs.getInt("from_region_id"),
                            rs.getInt("to_region_id"),
                            rs.getInt("travel_time"),
                            rs.getBoolean("is_open"),
                            rs.getString("description")
                    );
                    road.setFromRegionName(rs.getString("from_region_name"));
                    road.setToRegionName(rs.getString("to_region_name"));
                    return road;
                }
            }
        }
        return null;
    }

    public void setRoadOpen(int roadId, boolean isOpen) throws SQLException {
        String sql = "UPDATE roads SET is_open = ? WHERE id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isOpen);
            ps.setInt(2, roadId);
            ps.executeUpdate();
        }
    }

    public int addRoad(Road road) throws SQLException {
        String sql = "INSERT INTO roads (from_region_id, to_region_id, travel_time, is_open, description) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, road.getFromRegionId());
            ps.setInt(2, road.getToRegionId());
            ps.setInt(3, road.getTravelTime());
            ps.setBoolean(4, road.isOpen());
            ps.setString(5, road.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    road.setId(id);
                    return id;
                }
            }
        }
        return 0;
    }

    public void updateRoad(Road road) throws SQLException {
        String sql = "UPDATE roads SET travel_time = ?, is_open = ?, description = ? WHERE id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, road.getTravelTime());
            ps.setBoolean(2, road.isOpen());
            ps.setString(3, road.getDescription());
            ps.setInt(4, road.getId());
            ps.executeUpdate();
        }
    }
}
