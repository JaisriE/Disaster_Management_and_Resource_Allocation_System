package com.aruvi.dms.model;

/**
 * ResourceStock encapsulates counts of food kits and medicine kits.
 * Ensures strict integrity rules: counts can never fall below zero.
 * Stock mutations happen solely via reserveStock(), releaseStock(), or addStock().
 */
public class ResourceStock {
    private int foodKits;
    private int medicineKits;

    public ResourceStock() {
        this.foodKits = 0;
        this.medicineKits = 0;
    }

    public ResourceStock(int foodKits, int medicineKits) {
        if (foodKits < 0 || medicineKits < 0) {
            throw new IllegalArgumentException("Stock quantities cannot be negative.");
        }
        this.foodKits = foodKits;
        this.medicineKits = medicineKits;
    }

    public synchronized int getFoodKits() {
        return foodKits;
    }

    public synchronized int getMedicineKits() {
        return medicineKits;
    }

    /**
     * Adds stock to the depot. Both values must be non-negative.
     */
    public synchronized void addStock(int food, int medicine) {
        if (food < 0 || medicine < 0) {
            throw new IllegalArgumentException("Cannot add negative stock. Food: " + food + ", Med: " + medicine);
        }
        this.foodKits += food;
        this.medicineKits += medicine;
    }

    /**
     * Directly sets stock count, rejecting negative numbers.
     */
    public synchronized void setStock(int food, int medicine) {
        if (food < 0 || medicine < 0) {
            throw new IllegalArgumentException("Stock counts cannot be negative. Food: " + food + ", Med: " + medicine);
        }
        this.foodKits = food;
        this.medicineKits = medicine;
    }

    /**
     * Atomically reserves stock. Refuses the operation if insufficient stock is available.
     * @return true if reservation succeeded, false otherwise.
     */
    public synchronized boolean reserveStock(int food, int medicine) {
        if (food < 0 || medicine < 0) {
            throw new IllegalArgumentException("Reservation quantities must be non-negative.");
        }
        if (this.foodKits < food || this.medicineKits < medicine) {
            return false;
        }
        this.foodKits -= food;
        this.medicineKits -= medicine;
        return true;
    }

    /**
     * Releases previously reserved or returned stock back into inventory.
     */
    public synchronized void releaseStock(int food, int medicine) {
        if (food < 0 || medicine < 0) {
            throw new IllegalArgumentException("Release quantities must be non-negative.");
        }
        this.foodKits += food;
        this.medicineKits += medicine;
    }

    public synchronized boolean hasStock(int food, int medicine) {
        return this.foodKits >= food && this.medicineKits >= medicine;
    }

    @Override
    public synchronized String toString() {
        return "ResourceStock{" +
                "foodKits=" + foodKits +
                ", medicineKits=" + medicineKits +
                '}';
    }
}
