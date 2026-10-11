package com.aruvi.dms.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PathResult encapsulates the shortest path calculation outcome:
 * whether the destination was reachable, total travel time in minutes,
 * and the sequence of region IDs along the path.
 */
public class PathResult {
    private final boolean reachable;
    private final int totalTravelTime;
    private final List<Integer> path;

    public PathResult(boolean reachable, int totalTravelTime, List<Integer> path) {
        this.reachable = reachable;
        this.totalTravelTime = totalTravelTime;
        this.path = path != null ? path : Collections.emptyList();
    }

    public static PathResult unreachable() {
        return new PathResult(false, Integer.MAX_VALUE, Collections.emptyList());
    }

    public boolean isReachable() {
        return reachable;
    }

    public int getTotalTravelTime() {
        return totalTravelTime;
    }

    public List<Integer> getPath() {
        return new ArrayList<>(path);
    }

    @Override
    public String toString() {
        return "PathResult{" +
                "reachable=" + reachable +
                ", travelTime=" + totalTravelTime + "m" +
                ", path=" + path +
                '}';
    }
}
