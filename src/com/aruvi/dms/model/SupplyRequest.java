package com.aruvi.dms.model;

/**
 * SupplyRequest represents an emergency request for tangible survival kits:
 * Food Kits and Medicine Kits.
 * Demonstrates Inheritance and Polymorphism by extending ReliefRequest.
 */
public class SupplyRequest extends ReliefRequest {
    private int requestedFood;
    private int allocatedFood;
    private int outstandingFood;

    private int requestedMedicine;
    private int allocatedMedicine;
    private int outstandingMedicine;

    public SupplyRequest(int id, int regionId, int severity, int affectedPopulation, int arrivalOrder,
                         int requestedFood, int requestedMedicine) {
        super(id, regionId, severity, affectedPopulation, arrivalOrder, "SUPPLY");
        setRequestedFood(requestedFood);
        setRequestedMedicine(requestedMedicine);
        this.allocatedFood = 0;
        this.outstandingFood = requestedFood;
        this.allocatedMedicine = 0;
        this.outstandingMedicine = requestedMedicine;
    }

    public int getRequestedFood() {
        return requestedFood;
    }

    public void setRequestedFood(int requestedFood) {
        if (requestedFood < 0) throw new IllegalArgumentException("Requested food kits cannot be negative.");
        this.requestedFood = requestedFood;
        this.outstandingFood = Math.max(0, this.requestedFood - this.allocatedFood);
    }

    public int getAllocatedFood() {
        return allocatedFood;
    }

    public void setAllocatedFood(int allocatedFood) {
        if (allocatedFood < 0) throw new IllegalArgumentException("Allocated food kits cannot be negative.");
        this.allocatedFood = allocatedFood;
        this.outstandingFood = Math.max(0, this.requestedFood - this.allocatedFood);
    }

    public int getOutstandingFood() {
        return outstandingFood;
    }

    public void setOutstandingFood(int outstandingFood) {
        this.outstandingFood = Math.max(0, outstandingFood);
    }

    public int getRequestedMedicine() {
        return requestedMedicine;
    }

    public void setRequestedMedicine(int requestedMedicine) {
        if (requestedMedicine < 0) throw new IllegalArgumentException("Requested medicine kits cannot be negative.");
        this.requestedMedicine = requestedMedicine;
        this.outstandingMedicine = Math.max(0, this.requestedMedicine - this.allocatedMedicine);
    }

    public int getAllocatedMedicine() {
        return allocatedMedicine;
    }

    public void setAllocatedMedicine(int allocatedMedicine) {
        if (allocatedMedicine < 0) throw new IllegalArgumentException("Allocated medicine kits cannot be negative.");
        this.allocatedMedicine = allocatedMedicine;
        this.outstandingMedicine = Math.max(0, this.requestedMedicine - this.allocatedMedicine);
    }

    public int getOutstandingMedicine() {
        return outstandingMedicine;
    }

    public void setOutstandingMedicine(int outstandingMedicine) {
        this.outstandingMedicine = Math.max(0, outstandingMedicine);
    }

    /**
     * Polymorphic implementation: A supply request is fulfillable if it has outstanding food or medicine to supply.
     */
    @Override
    public boolean isFulfillable() {
        return (outstandingFood > 0 || outstandingMedicine > 0) &&
               status != RequestStatus.ALLOCATED &&
               status != RequestStatus.REJECTED &&
               status != RequestStatus.CANCELLED;
    }

    @Override
    public String getSummary() {
        return String.format("Supply Request #%d [Region: %s, Sev: %d] Food: (Req: %d, Alloc: %d, Out: %d) | Med: (Req: %d, Alloc: %d, Out: %d) [%s]",
                id, (regionName != null ? regionName : String.valueOf(regionId)), severity,
                requestedFood, allocatedFood, outstandingFood,
                requestedMedicine, allocatedMedicine, outstandingMedicine, status);
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
