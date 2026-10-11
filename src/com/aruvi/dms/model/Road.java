package com.aruvi.dms.model;

/**
 * Road represents a directed edge in the transportation graph network between regions.
 * Each road has a strictly positive travel time (in minutes) and can be opened/closed.
 */
public class Road {
    private int id;
    private int fromRegionId;
    private int toRegionId;
    private String fromRegionName;
    private String toRegionName;
    private int travelTime; // in minutes > 0
    private boolean open;
    private String description;

    public Road() {
    }

    public Road(int id, int fromRegionId, int toRegionId, int travelTime, boolean open, String description) {
        setId(id);
        setFromRegionId(fromRegionId);
        setToRegionId(toRegionId);
        setTravelTime(travelTime);
        this.open = open;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id <= 0) throw new IllegalArgumentException("Road ID must be positive.");
        this.id = id;
    }

    public int getFromRegionId() {
        return fromRegionId;
    }

    public void setFromRegionId(int fromRegionId) {
        if (fromRegionId <= 0) throw new IllegalArgumentException("From region ID must be positive.");
        this.fromRegionId = fromRegionId;
    }

    public int getToRegionId() {
        return toRegionId;
    }

    public void setToRegionId(int toRegionId) {
        if (toRegionId <= 0) throw new IllegalArgumentException("To region ID must be positive.");
        this.toRegionId = toRegionId;
    }

    public String getFromRegionName() {
        return fromRegionName != null ? fromRegionName : "";
    }

    public void setFromRegionName(String fromRegionName) {
        this.fromRegionName = fromRegionName;
    }

    public String getToRegionName() {
        return toRegionName != null ? toRegionName : "";
    }

    public void setToRegionName(String toRegionName) {
        this.toRegionName = toRegionName;
    }

    public int getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(int travelTime) {
        if (travelTime <= 0) {
            throw new IllegalArgumentException("Travel time must be strictly positive (> 0 minutes). Got: " + travelTime);
        }
        this.travelTime = travelTime;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Road{" +
                "id=" + id +
                ", from=" + fromRegionId +
                ", to=" + toRegionId +
                ", travelTime=" + travelTime +
                ", open=" + open +
                ", desc='" + description + '\'' +
                '}';
    }
}
