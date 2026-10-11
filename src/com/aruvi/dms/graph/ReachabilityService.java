package com.aruvi.dms.graph;

import com.aruvi.dms.model.Road;

import java.util.*;

/**
 * ReachabilityService uses Breadth-First Search (BFS) and Depth-First Search (DFS)
 * over open roads only to verify network connectivity.
 */
public class ReachabilityService {

    /**
     * Checks if a destination region can be reached from a source region via open roads using BFS.
     */
    public static boolean isReachableBFS(Graph graph, int sourceRegionId, int targetRegionId) {
        if (sourceRegionId == targetRegionId) {
            return true;
        }

        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();

        queue.add(sourceRegionId);
        visited.add(sourceRegionId);

        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (current == targetRegionId) {
                return true;
            }

            for (Road road : graph.getOpenEdges(current)) {
                int neighbor = road.getToRegionId();
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return false;
    }

    /**
     * Checks if a destination region can be reached using DFS traversal.
     */
    public static boolean isReachableDFS(Graph graph, int sourceRegionId, int targetRegionId) {
        Set<Integer> visited = new HashSet<>();
        return dfsHelper(graph, sourceRegionId, targetRegionId, visited);
    }

    private static boolean dfsHelper(Graph graph, int current, int target, Set<Integer> visited) {
        if (current == target) {
            return true;
        }
        visited.add(current);

        for (Road road : graph.getOpenEdges(current)) {
            int neighbor = road.getToRegionId();
            if (!visited.contains(neighbor)) {
                if (dfsHelper(graph, neighbor, target, visited)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Finds all reachable regions from a starting region over open roads.
     */
    public static Set<Integer> getReachableRegions(Graph graph, int sourceRegionId) {
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();

        queue.add(sourceRegionId);
        visited.add(sourceRegionId);

        while (!queue.isEmpty()) {
            int current = queue.poll();
            for (Road road : graph.getOpenEdges(current)) {
                int neighbor = road.getToRegionId();
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return visited;
    }
}
