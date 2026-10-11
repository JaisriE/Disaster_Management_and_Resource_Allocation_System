package com.aruvi.dms.model;

import java.util.Date;

/**
 * Allocation represents an authorized, finalized allocation confirmed by an officer.
 * Persisted in the MySQL allocations table.
 */
public class Allocation {
    private int id;
    private int requestId;
    private Integer depotId;
    private String depotName;
    private Integer rescueTeamId;
    private String rescueTeamName;
    private Integer shelterId;
    private String shelterName;
    private int foodAllocated;
    private int medicineAllocated;
    private int shelterPlacesAllocated;
    private String routePath;
    private int travelTime;
    private String officerName;
    private String status; // "CONFIRMED"
    private Date confirmedAt;

    public Allocation() {
        this.status = "CONFIRMED";
        this.confirmedAt = new Date();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public Integer getDepotId() {
        return depotId;
    }

    public void setDepotId(Integer depotId) {
        this.depotId = depotId;
    }

    public String getDepotName() {
        return depotName != null ? depotName : "";
    }

    public void setDepotName(String depotName) {
        this.depotName = depotName;
    }

    public Integer getRescueTeamId() {
        return rescueTeamId;
    }

    public void setRescueTeamId(Integer rescueTeamId) {
        this.rescueTeamId = rescueTeamId;
    }

    public String getRescueTeamName() {
        return rescueTeamName != null ? rescueTeamName : "";
    }

    public void setRescueTeamName(String rescueTeamName) {
        this.rescueTeamName = rescueTeamName;
    }

    public Integer getShelterId() {
        return shelterId;
    }

    public void setShelterId(Integer shelterId) {
        this.shelterId = shelterId;
    }

    public String getShelterName() {
        return shelterName != null ? shelterName : "";
    }

    public void setShelterName(String shelterName) {
        this.shelterName = shelterName;
    }

    public int getFoodAllocated() {
        return foodAllocated;
    }

    public void setFoodAllocated(int foodAllocated) {
        this.foodAllocated = foodAllocated;
    }

    public int getMedicineAllocated() {
        return medicineAllocated;
    }

    public void setMedicineAllocated(int medicineAllocated) {
        this.medicineAllocated = medicineAllocated;
    }

    public int getShelterPlacesAllocated() {
        return shelterPlacesAllocated;
    }

    public void setShelterPlacesAllocated(int shelterPlacesAllocated) {
        this.shelterPlacesAllocated = shelterPlacesAllocated;
    }

    public String getRoutePath() {
        return routePath != null ? routePath : "";
    }

    public void setRoutePath(String routePath) {
        this.routePath = routePath;
    }

    public int getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(int travelTime) {
        this.travelTime = travelTime;
    }

    public String getOfficerName() {
        return officerName != null ? officerName : "Command Officer";
    }

    public void setOfficerName(String officerName) {
        this.officerName = officerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Date confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    @Override
    public String toString() {
        return "Allocation{" +
                "id=" + id +
                ", requestId=" + requestId +
                ", depot=" + depotName +
                ", team=" + rescueTeamName +
                ", shelter=" + shelterName +
                ", food=" + foodAllocated +
                ", med=" + medicineAllocated +
                ", shelterPlaces=" + shelterPlacesAllocated +
                ", officer='" + officerName + '\'' +
                '}';
    }
}
