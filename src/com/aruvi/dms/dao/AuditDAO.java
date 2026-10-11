package com.aruvi.dms.dao;

import com.aruvi.dms.config.DBConfig;
import com.aruvi.dms.model.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditDAO {

    public void insertAuditLog(Connection conn, AuditLog log) throws SQLException {
        String sql = "INSERT INTO audit_logs (action_type, description, performed_by) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, log.getActionType());
            ps.setString(2, log.getDescription());
            ps.setString(3, log.getPerformedBy());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    log.setId(rs.getInt(1));
                }
            }
        }
    }

    public void insertAuditLog(AuditLog log) throws SQLException {
        try (Connection conn = DBConfig.getConnection()) {
            insertAuditLog(conn, log);
        }
    }

    public List<AuditLog> getAllLogs(int limit) throws SQLException {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT id, action_type, description, performed_by, created_at " +
                     "FROM audit_logs ORDER BY id DESC LIMIT ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 50);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AuditLog(
                            rs.getInt("id"),
                            rs.getString("action_type"),
                            rs.getString("description"),
                            rs.getString("performed_by"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        }
        return list;
    }
}
