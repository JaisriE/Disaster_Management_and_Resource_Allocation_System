package com.aruvi.dms.model;

/**
 * RescueTeam represents an emergency response unit stationed at a home depot.
 * Manages availability status to guarantee no conflicting active assignments.
 */
public class RescueTeam {
    private int id;
    private String name;
    private int homeDepotId;
    private String homeDepotName;
    private boolean available;
    private Integer currentRequestId;

    public RescueTeam(int id, String name, int homeDepotId, boolean available) {
        if (id <= 0) throw new IllegalArgumentException("RescueTeam ID must be positive.");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Team name cannot be empty.");
        if (homeDepotId <= 0) throw new IllegalArgumentException("Home depot ID must be positive.");
        this.id = id;
        this.name = name.trim();
        this.homeDepotId = homeDepotId;
        this.available = available;
        this.currentRequestId = null;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Team name cannot be empty.");
        this.name = name.trim();
    }

    public int getHomeDepotId() {
        return homeDepotId;
    }

    public void setHomeDepotId(int homeDepotId) {
        if (homeDepotId <= 0) throw new IllegalArgumentException("Home depot ID must be positive.");
        this.homeDepotId = homeDepotId;
    }

    public String getHomeDepotName() {
        return homeDepotName != null ? homeDepotName : "";
    }

    public void setHomeDepotName(String homeDepotName) {
        this.homeDepotName = homeDepotName;
    }

    public synchronized boolean isAvailable() {
        return available;
    }

    public synchronized void setAvailable(boolean available) {
        this.available = available;
        if (available) {
            this.currentRequestId = null;
        }
    }

    public synchronized Integer getCurrentRequestId() {
        return currentRequestId;
    }

    public synchronized void setCurrentRequestId(Integer currentRequestId) {
        this.currentRequestId = currentRequestId;
    }

    /**
     * Reserves team for an emergency mission if currently available.
     * @return true if successfully reserved, false if already busy.
     */
    public synchronized boolean reserveTeam(int requestId) {
        if (!this.available) {
            return false;
        }
        this.available = false;
        this.currentRequestId = requestId;
        return true;
    }

    /**
     * Releases team from current assignment back to available status.
     */
    public synchronized void releaseTeam() {
        this.available = true;
        this.currentRequestId = null;
    }

    @Override
    public synchronized String toString() {
        return "RescueTeam{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", homeDepotId=" + homeDepotId +
                ", available=" + available +
                ", currentRequestId=" + currentRequestId +
                '}';
    }
}
