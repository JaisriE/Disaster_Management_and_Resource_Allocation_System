package com.aruvi.dms.dao;

import com.aruvi.dms.config.DBConfig;
import com.aruvi.dms.model.Depot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepotDAO {

    public List<Depot> getAllDepots() throws SQLException {
        List<Depot> list = new ArrayList<>();
        String sql = "SELECT d.id, d.name, d.region_id, r.name AS region_name, d.food_kits, d.medicine_kits " +
                     "FROM depots d " +
                     "JOIN regions r ON d.region_id = r.id " +
                     "ORDER BY d.id ASC";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Depot depot = new Depot(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("region_id"),
                        rs.getInt("food_kits"),
                        rs.getInt("medicine_kits")
                );
                depot.setRegionName(rs.getString("region_name"));
                list.add(depot);
            }
        }
        return list;
    }

    public Depot getDepotById(int id) throws SQLException {
        String sql = "SELECT d.id, d.name, d.region_id, r.name AS region_name, d.food_kits, d.medicine_kits " +
                     "FROM depots d " +
                     "JOIN regions r ON d.region_id = r.id " +
                     "WHERE d.id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Depot depot = new Depot(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("region_id"),
                            rs.getInt("food_kits"),
                            rs.getInt("medicine_kits")
                    );
                    depot.setRegionName(rs.getString("region_name"));
                    return depot;
                }
            }
        }
        return null;
    }

    public void updateStock(Connection conn, int depotId, int foodKits, int medicineKits) throws SQLException {
        String sql = "UPDATE depots SET food_kits = ?, medicine_kits = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, foodKits);
            ps.setInt(2, medicineKits);
            ps.setInt(3, depotId);
            ps.executeUpdate();
        }
    }

    public void updateStock(int depotId, int foodKits, int medicineKits) throws SQLException {
        try (Connection conn = DBConfig.getConnection()) {
            updateStock(conn, depotId, foodKits, medicineKits);
        }
    }
}
