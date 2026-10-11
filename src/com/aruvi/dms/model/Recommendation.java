package com.aruvi.dms.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Recommendation encapsulates the automated algorithmic advice calculated by BFS/Dijkstra
 * and stock inventory checks.
 * In accordance with the core design principle:
 * "automation SUPPORTS human decisions. The system only RECOMMENDS an allocation.
 * An authorized officer reviews and confirms or rejects it."
 */
public class Recommendation {
    private int requestId;
    private String requestType;
    private int regionId;
    private String regionName;
    private boolean reachable;
    private List<Integer> pathRegionIds = new ArrayList<>();
    private List<String> pathRegionNames = new ArrayList<>();
    private int travelTimeMinutes;

    // Supplies recommended
    private Integer recommendedDepotId;
    private String recommendedDepotName;
    private int foodRecommended;
    private int medicineRecommended;
    private int outstandingFood;
    private int outstandingMedicine;

    // Rescue recommended
    private Integer recommendedTeamId;
    private String recommendedTeamName;
    private Integer recommendedShelterId;
    private String recommendedShelterName;
    private int shelterPlacesRecommended;
    private int outstandingShelterPlaces;

    private boolean canAllocate;
    private RequestStatus recommendedStatus;
    private String explanation;

    public Recommendation() {
        this.reachable = false;
        this.canAllocate = false;
        this.recommendedStatus = RequestStatus.PENDING;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public int getRegionId() {
        return regionId;
    }

    public void setRegionId(int regionId) {
        this.regionId = regionId;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public boolean isReachable() {
        return reachable;
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    public List<Integer> getPathRegionIds() {
        return pathRegionIds;
    }

    public void setPathRegionIds(List<Integer> pathRegionIds) {
        this.pathRegionIds = pathRegionIds;
    }

    public List<String> getPathRegionNames() {
        return pathRegionNames;
    }

    public void setPathRegionNames(List<String> pathRegionNames) {
        this.pathRegionNames = pathRegionNames;
    }

    public int getTravelTimeMinutes() {
        return travelTimeMinutes;
    }

    public void setTravelTimeMinutes(int travelTimeMinutes) {
        this.travelTimeMinutes = travelTimeMinutes;
    }

    public Integer getRecommendedDepotId() {
        return recommendedDepotId;
    }

    public void setRecommendedDepotId(Integer recommendedDepotId) {
        this.recommendedDepotId = recommendedDepotId;
    }

    public String getRecommendedDepotName() {
        return recommendedDepotName;
    }

    public void setRecommendedDepotName(String recommendedDepotName) {
        this.recommendedDepotName = recommendedDepotName;
    }

    public int getFoodRecommended() {
        return foodRecommended;
    }

    public void setFoodRecommended(int foodRecommended) {
        this.foodRecommended = foodRecommended;
    }

    public int getMedicineRecommended() {
        return medicineRecommended;
    }

    public void setMedicineRecommended(int medicineRecommended) {
        this.medicineRecommended = medicineRecommended;
    }

    public int getOutstandingFood() {
        return outstandingFood;
    }

    public void setOutstandingFood(int outstandingFood) {
        this.outstandingFood = outstandingFood;
    }

    public int getOutstandingMedicine() {
        return outstandingMedicine;
    }

    public void setOutstandingMedicine(int outstandingMedicine) {
        this.outstandingMedicine = outstandingMedicine;
    }

    public Integer getRecommendedTeamId() {
        return recommendedTeamId;
    }

    public void setRecommendedTeamId(Integer recommendedTeamId) {
        this.recommendedTeamId = recommendedTeamId;
    }

    public String getRecommendedTeamName() {
        return recommendedTeamName;
    }

    public void setRecommendedTeamName(String recommendedTeamName) {
        this.recommendedTeamName = recommendedTeamName;
    }

    public Integer getRecommendedShelterId() {
        return recommendedShelterId;
    }

    public void setRecommendedShelterId(Integer recommendedShelterId) {
        this.recommendedShelterId = recommendedShelterId;
    }

    public String getRecommendedShelterName() {
        return recommendedShelterName;
    }

    public void setRecommendedShelterName(String recommendedShelterName) {
        this.recommendedShelterName = recommendedShelterName;
    }

    public int getShelterPlacesRecommended() {
        return shelterPlacesRecommended;
    }

    public void setShelterPlacesRecommended(int shelterPlacesRecommended) {
        this.shelterPlacesRecommended = shelterPlacesRecommended;
    }

    public int getOutstandingShelterPlaces() {
        return outstandingShelterPlaces;
    }

    public void setOutstandingShelterPlaces(int outstandingShelterPlaces) {
        this.outstandingShelterPlaces = outstandingShelterPlaces;
    }

    public boolean isCanAllocate() {
        return canAllocate;
    }

    public void setCanAllocate(boolean canAllocate) {
        this.canAllocate = canAllocate;
    }

    public RequestStatus getRecommendedStatus() {
        return recommendedStatus;
    }

    public void setRecommendedStatus(RequestStatus recommendedStatus) {
        this.recommendedStatus = recommendedStatus;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getRouteSummary() {
        if (pathRegionNames == null || pathRegionNames.isEmpty()) {
            return "No Route Available";
        }
        return String.join(" -> ", pathRegionNames) + " (" + travelTimeMinutes + " mins)";
    }
}
