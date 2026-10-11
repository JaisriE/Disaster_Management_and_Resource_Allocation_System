package com.aruvi.dms.model;

/**
 * RescueRequest represents an emergency call for rescue personnel teams and shelter accommodations.
 * Demonstrates Inheritance and Polymorphism by extending ReliefRequest.
 */
public class RescueRequest extends ReliefRequest {
    private int requestedTeams;
    private int allocatedTeams;

    private int requestedShelterPlaces;
    private int allocatedShelterPlaces;
    private int outstandingShelterPlaces;

    public RescueRequest(int id, int regionId, int severity, int affectedPopulation, int arrivalOrder,
                         int requestedTeams, int requestedShelterPlaces) {
        super(id, regionId, severity, affectedPopulation, arrivalOrder, "RESCUE");
        setRequestedTeams(requestedTeams);
        setRequestedShelterPlaces(requestedShelterPlaces);
        this.allocatedTeams = 0;
        this.allocatedShelterPlaces = 0;
        this.outstandingShelterPlaces = requestedShelterPlaces;
    }

    public int getRequestedTeams() {
        return requestedTeams;
    }

    public void setRequestedTeams(int requestedTeams) {
        if (requestedTeams < 0) throw new IllegalArgumentException("Requested rescue teams cannot be negative.");
        this.requestedTeams = requestedTeams;
    }

    public int getAllocatedTeams() {
        return allocatedTeams;
    }

    public void setAllocatedTeams(int allocatedTeams) {
        if (allocatedTeams < 0) throw new IllegalArgumentException("Allocated rescue teams cannot be negative.");
        this.allocatedTeams = allocatedTeams;
    }

    public int getRequestedShelterPlaces() {
        return requestedShelterPlaces;
    }

    public void setRequestedShelterPlaces(int requestedShelterPlaces) {
        if (requestedShelterPlaces < 0) throw new IllegalArgumentException("Requested shelter places cannot be negative.");
        this.requestedShelterPlaces = requestedShelterPlaces;
        this.outstandingShelterPlaces = Math.max(0, this.requestedShelterPlaces - this.allocatedShelterPlaces);
    }

    public int getAllocatedShelterPlaces() {
        return allocatedShelterPlaces;
    }

    public void setAllocatedShelterPlaces(int allocatedShelterPlaces) {
        if (allocatedShelterPlaces < 0) throw new IllegalArgumentException("Allocated shelter places cannot be negative.");
        this.allocatedShelterPlaces = allocatedShelterPlaces;
        this.outstandingShelterPlaces = Math.max(0, this.requestedShelterPlaces - this.allocatedShelterPlaces);
    }

    public int getOutstandingShelterPlaces() {
        return outstandingShelterPlaces;
    }

    public void setOutstandingShelterPlaces(int outstandingShelterPlaces) {
        this.outstandingShelterPlaces = Math.max(0, outstandingShelterPlaces);
    }

    /**
     * Polymorphic implementation: A rescue request is fulfillable if it needs an unassigned team or shelter capacity.
     */
    @Override
    public boolean isFulfillable() {
        return ((requestedTeams > allocatedTeams) || outstandingShelterPlaces > 0) &&
               status != RequestStatus.ALLOCATED &&
               status != RequestStatus.REJECTED &&
               status != RequestStatus.CANCELLED;
    }

    @Override
    public String getSummary() {
        return String.format("Rescue Request #%d [Region: %s, Sev: %d] Teams: (Req: %d, Alloc: %d) | Shelter: (Req: %d, Alloc: %d, Out: %d) [%s]",
                id, (regionName != null ? regionName : String.valueOf(regionId)), severity,
                requestedTeams, allocatedTeams,
                requestedShelterPlaces, allocatedShelterPlaces, outstandingShelterPlaces, status);
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
