package com.aruvi.dms.dao;

import com.aruvi.dms.config.DBConfig;
import com.aruvi.dms.model.Shelter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShelterDAO {

    public List<Shelter> getAllShelters() throws SQLException {
        List<Shelter> list = new ArrayList<>();
        String sql = "SELECT s.id, s.name, s.region_id, r.name AS region_name, s.capacity, s.reserved_places " +
                     "FROM shelters s " +
                     "JOIN regions r ON s.region_id = r.id " +
                     "ORDER BY s.id ASC";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Shelter shelter = new Shelter(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("region_id"),
                        rs.getInt("capacity"),
                        rs.getInt("reserved_places")
                );
                shelter.setRegionName(rs.getString("region_name"));
                list.add(shelter);
            }
        }
        return list;
    }

    public Shelter getShelterById(int id) throws SQLException {
        String sql = "SELECT s.id, s.name, s.region_id, r.name AS region_name, s.capacity, s.reserved_places " +
                     "FROM shelters s " +
                     "JOIN regions r ON s.region_id = r.id " +
                     "WHERE s.id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Shelter shelter = new Shelter(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("region_id"),
                            rs.getInt("capacity"),
                            rs.getInt("reserved_places")
                    );
                    shelter.setRegionName(rs.getString("region_name"));
                    return shelter;
                }
            }
        }
        return null;
    }

    public void updateReservedPlaces(Connection conn, int shelterId, int reservedPlaces) throws SQLException {
        String sql = "UPDATE shelters SET reserved_places = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservedPlaces);
            ps.setInt(2, shelterId);
            ps.executeUpdate();
        }
    }

    public void updateCapacity(int shelterId, int capacity) throws SQLException {
        String sql = "UPDATE shelters SET capacity = ? WHERE id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, capacity);
            ps.setInt(2, shelterId);
            ps.executeUpdate();
        }
    }
}
