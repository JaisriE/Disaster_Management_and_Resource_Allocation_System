package com.aruvi.dms.model;

/**
 * Depot represents a physical storage depot housing emergency supplies.
 * Composes a ResourceStock instance. Changes to stock are delegated and strictly checked.
 */
public class Depot {
    private int id;
    private String name;
    private int regionId;
    private String regionName;
    private final ResourceStock stock;

    public Depot(int id, String name, int regionId, int foodKits, int medicineKits) {
        if (id <= 0) throw new IllegalArgumentException("Depot ID must be positive.");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Depot name cannot be empty.");
        if (regionId <= 0) throw new IllegalArgumentException("Region ID must be positive.");
        this.id = id;
        this.name = name.trim();
        this.regionId = regionId;
        this.stock = new ResourceStock(foodKits, medicineKits);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Depot name cannot be empty.");
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

    public ResourceStock getStock() {
        return stock;
    }

    public int getFoodKits() {
        return stock.getFoodKits();
    }

    public int getMedicineKits() {
        return stock.getMedicineKits();
    }

    public boolean reserveStock(int food, int medicine) {
        return stock.reserveStock(food, medicine);
    }

    public void addStock(int food, int medicine) {
        stock.addStock(food, medicine);
    }

    public void setStock(int food, int medicine) {
        stock.setStock(food, medicine);
    }

    public void releaseStock(int food, int medicine) {
        stock.releaseStock(food, medicine);
    }

    @Override
    public String toString() {
        return "Depot{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", regionId=" + regionId +
                ", stock=" + stock +
                '}';
    }
}
