package com.aruvi.dms.model;

import java.util.Date;

/**
 * ReliefRequest is the abstract base class for all emergency requests in Aruvi district.
 * Demonstrates OOP Abstraction and Polymorphism.
 * 
 * Implements Comparable for deterministic priority ranking:
 * 1. Higher severity first (5 down to 1)
 * 2. Higher affected population first (descending)
 * 3. Earlier arrival order sequence first (ascending, FIFO tie-breaker)
 */
public abstract class ReliefRequest implements Comparable<ReliefRequest> {
    protected int id;
    protected int regionId;
    protected String regionName;
    protected int severity; // 1 to 5
    protected int affectedPopulation; // > 0
    protected int arrivalOrder; // sequence number
    protected String requestType; // "SUPPLY" or "RESCUE"
    protected RequestStatus status;
    protected String reportedBy;
    protected String rejectionReason;
    protected Date createdAt;

    public ReliefRequest(int id, int regionId, int severity, int affectedPopulation, int arrivalOrder, String requestType) {
        setId(id);
        setRegionId(regionId);
        setSeverity(severity);
        setAffectedPopulation(affectedPopulation);
        setArrivalOrder(arrivalOrder);
        this.requestType = requestType;
        this.status = RequestStatus.PENDING;
        this.reportedBy = "Field Officer";
        this.createdAt = new Date();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id <= 0) throw new IllegalArgumentException("Request ID must be positive.");
        this.id = id;
    }

    public int getRegionId() {
        return regionId;
    }

    public void setRegionId(int regionId) {
        if (regionId <= 0) throw new IllegalArgumentException("Region ID must be positive.");
        this.regionId = regionId;
    }

    public String getRegionName() {
        return regionName != null ? regionName : "";
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        if (severity < 1 || severity > 5) {
            throw new IllegalArgumentException("Severity must be an integer between 1 and 5. Got: " + severity);
        }
        this.severity = severity;
    }

    public int getAffectedPopulation() {
        return affectedPopulation;
    }

    public void setAffectedPopulation(int affectedPopulation) {
        if (affectedPopulation <= 0) {
            throw new IllegalArgumentException("Affected population must be greater than zero. Got: " + affectedPopulation);
        }
        this.affectedPopulation = affectedPopulation;
    }

    public int getArrivalOrder() {
        return arrivalOrder;
    }

    public void setArrivalOrder(int arrivalOrder) {
        if (arrivalOrder <= 0) {
            throw new IllegalArgumentException("Arrival order must be positive. Got: " + arrivalOrder);
        }
        this.arrivalOrder = arrivalOrder;
    }

    public String getRequestType() {
        return requestType;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        if (status == null) throw new IllegalArgumentException("Request status cannot be null.");
        this.status = status;
    }

    public String getReportedBy() {
        return reportedBy != null ? reportedBy : "Field Officer";
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    public String getRejectionReason() {
        return rejectionReason != null ? rejectionReason : "";
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Deterministic comparison for priority queue ordering:
     * 1. Higher severity first (5 down to 1)
     * 2. Higher affected population first (descending)
     * 3. Earlier arrival order first (ascending - FIFO tie break)
     */
    @Override
    public int compareTo(ReliefRequest other) {
        if (other == null) return -1;
        
        // 1. Severity descending
        int sevComp = Integer.compare(other.severity, this.severity);
        if (sevComp != 0) return sevComp;

        // 2. Affected population descending
        int popComp = Integer.compare(other.affectedPopulation, this.affectedPopulation);
        if (popComp != 0) return popComp;

        // 3. Arrival order ascending (earlier arrival first)
        return Integer.compare(this.arrivalOrder, other.arrivalOrder);
    }

    /**
     * Abstract method demonstrating Polymorphism:
     * Checks if the given request can be fulfilled with currently available resources.
     */
    public abstract boolean isFulfillable();

    /**
     * Returns a human-readable summary of requested and outstanding supplies/services.
     */
    public abstract String getSummary();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReliefRequest that = (ReliefRequest) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
