package com.aruvi.dms.model;

/**
 * AffectedRegion represents a geographic administrative unit/village in Aruvi
 * district. Demonstrates OOP Encapsulation with private state and explicit
 * validation.
 */
public class AffectedRegion {

    private int id;
    private String name;
    private int population;
    private String description;

    public AffectedRegion() {
    }

    public AffectedRegion(int id, String name, int population, String description) {
        setId(id);
        setName(name);
        setPopulation(population);
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Region ID must be positive.");
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Region name cannot be empty.");
        }
        this.name = name.trim();
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        if (population <= 0) {
            throw new IllegalArgumentException("Region population must be greater than zero.");
        }
        this.population = population;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "AffectedRegion{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", population=" + population
                + '}';
    }
}
