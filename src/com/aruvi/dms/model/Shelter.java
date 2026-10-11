package com.aruvi.dms.model;

/**
 * Shelter represents a relief center with fixed accommodation capacity for displaced residents.
 * Guarantees no overbooking: reserved places cannot exceed capacity, nor fall below zero.
 */
public class Shelter {
    private int id;
    private String name;
    private int regionId;
    private String regionName;
    private int capacity;
    private int reservedPlaces;

    public Shelter(int id, String name, int regionId, int capacity, int reservedPlaces) {
        if (id <= 0) throw new IllegalArgumentException("Shelter ID must be positive.");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Shelter name cannot be empty.");
        if (regionId <= 0) throw new IllegalArgumentException("Region ID must be positive.");
        if (capacity <= 0) throw new IllegalArgumentException("Shelter capacity must be greater than zero.");
        if (reservedPlaces < 0 || reservedPlaces > capacity) {
            throw new IllegalArgumentException("Reserved places must be between 0 and capacity (" + capacity + ").");
        }
        this.id = id;
        this.name = name.trim();
        this.regionId = regionId;
        this.capacity = capacity;
        this.reservedPlaces = reservedPlaces;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Shelter name cannot be empty.");
        this.name = name.trim();
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

    public synchronized int getCapacity() {
        return capacity;
    }

    public synchronized void setCapacity(int capacity) {
        if (capacity < this.reservedPlaces) {
            throw new IllegalArgumentException("New capacity (" + capacity + ") cannot be less than currently reserved places (" + this.reservedPlaces + ").");
        }
        this.capacity = capacity;
    }

    public synchronized int getReservedPlaces() {
        return reservedPlaces;
    }

    public synchronized void setReservedPlaces(int reservedPlaces) {
        if (reservedPlaces < 0 || reservedPlaces > this.capacity) {
            throw new IllegalArgumentException("Reserved places must be between 0 and capacity (" + this.capacity + ").");
        }
        this.reservedPlaces = reservedPlaces;
    }

    public synchronized int availablePlaces() {
        return capacity - reservedPlaces;
    }

    /**
     * Atomically reserves shelter accommodation places.
     * @return true if places were available and successfully reserved, false otherwise.
     */
    public synchronized boolean reservePlaces(int places) {
        if (places <= 0) {
            throw new IllegalArgumentException("Places to reserve must be greater than zero.");
        }
        if (availablePlaces() < places) {
            return false;
        }
        this.reservedPlaces += places;
        return true;
    }

    /**
     * Releases accommodation places back to available capacity.
     */
    public synchronized void releasePlaces(int places) {
        if (places <= 0) {
            throw new IllegalArgumentException("Places to release must be greater than zero.");
        }
        if (this.reservedPlaces < places) {
            throw new IllegalArgumentException("Cannot release more places (" + places + ") than currently reserved (" + this.reservedPlaces + ").");
        }
        this.reservedPlaces -= places;
    }

    @Override
    public synchronized String toString() {
        return "Shelter{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", regionId=" + regionId +
                ", capacity=" + capacity +
                ", reservedPlaces=" + reservedPlaces +
                ", available=" + availablePlaces() +
                '}';
    }
}
