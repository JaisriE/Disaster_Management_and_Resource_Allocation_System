package com.aruvi.dms.dao;

import com.aruvi.dms.config.DBConfig;
import com.aruvi.dms.model.AffectedRegion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegionDAO {

    public List<AffectedRegion> getAllRegions() throws SQLException {
        List<AffectedRegion> list = new ArrayList<>();
        String sql = "SELECT id, name, population, description FROM regions ORDER BY id ASC";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new AffectedRegion(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("population"),
                        rs.getString("description")
                ));
            }
        }
        return list;
    }

    public AffectedRegion getRegionById(int id) throws SQLException {
        String sql = "SELECT id, name, population, description FROM regions WHERE id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AffectedRegion(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("population"),
                            rs.getString("description")
                    );
                }
            }
        }
        return null;
    }

    public int addRegion(AffectedRegion region) throws SQLException {
        String sql = "INSERT INTO regions (name, population, description) VALUES (?, ?, ?)";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, region.getName());
            ps.setInt(2, region.getPopulation());
            ps.setString(3, region.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    region.setId(id);
                    return id;
                }
            }
        }
        return 0;
    }
}
