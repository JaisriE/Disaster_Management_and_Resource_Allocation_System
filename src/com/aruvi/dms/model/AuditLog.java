package com.aruvi.dms.model;

import java.util.Date;

/**
 * AuditLog records administrative actions, status transitions, and emergency operations.
 */
public class AuditLog {
    private int id;
    private String actionType;
    private String description;
    private String performedBy;
    private Date createdAt;

    public AuditLog() {
        this.createdAt = new Date();
    }

    public AuditLog(int id, String actionType, String description, String performedBy, Date createdAt) {
        this.id = id;
        this.actionType = actionType;
        this.description = description;
        this.performedBy = performedBy;
        this.createdAt = createdAt;
    }

    public AuditLog(String actionType, String description, String performedBy) {
        this.actionType = actionType;
        this.description = description;
        this.performedBy = performedBy;
        this.createdAt = new Date();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
