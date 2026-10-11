package com.aruvi.dms.graph;

import com.aruvi.dms.model.Road;

import java.util.*;

/**
 * DijkstraService implements Dijkstra's Shortest Path algorithm with a Min-Heap (PriorityQueue).
 * Computes minimum travel time in minutes from a source region to a target region
 * through OPEN roads only, reconstructing the exact sequence of hops.
 */
public class DijkstraService {

    /**
     * Inner helper class representing a node entry in the Min-Heap priority queue.
     */
    private static class NodeEntry implements Comparable<NodeEntry> {
        final int regionId;
        final int travelTime;

        NodeEntry(int regionId, int travelTime) {
            this.regionId = regionId;
            this.travelTime = travelTime;
        }

        @Override
        public int compareTo(NodeEntry other) {
            return Integer.compare(this.travelTime, other.travelTime);
        }
    }

    /**
     * Finds the minimum travel time path from sourceRegionId to targetRegionId
     * over open roads using Dijkstra's algorithm with a Min-Heap.
     */
    public static PathResult findShortestPath(Graph graph, int sourceRegionId, int targetRegionId) {
        if (sourceRegionId == targetRegionId) {
            List<Integer> singleHop = new ArrayList<>();
            singleHop.add(sourceRegionId);
            return new PathResult(true, 0, singleHop);
        }

        Map<Integer, Integer> minDistance = new HashMap<>();
        Map<Integer, Integer> predecessor = new HashMap<>();
        PriorityQueue<NodeEntry> minHeap = new PriorityQueue<>();

        // Initialize distances
        minDistance.put(sourceRegionId, 0);
        minHeap.add(new NodeEntry(sourceRegionId, 0));

        while (!minHeap.isEmpty()) {
            NodeEntry current = minHeap.poll();
            int u = current.regionId;
            int distU = current.travelTime;

            // Skip if a shorter path to u has already been processed
            if (distU > minDistance.getOrDefault(u, Integer.MAX_VALUE)) {
                continue;
            }

            // Early exit if target region reached
            if (u == targetRegionId) {
                break;
            }

            for (Road road : graph.getOpenEdges(u)) {
                int v = road.getToRegionId();
                int weight = road.getTravelTime();
                int newDist = distU + weight;

                if (newDist < minDistance.getOrDefault(v, Integer.MAX_VALUE)) {
                    minDistance.put(v, newDist);
                    predecessor.put(v, u);
                    minHeap.add(new NodeEntry(v, newDist));
                }
            }
        }

        if (!minDistance.containsKey(targetRegionId)) {
            return PathResult.unreachable();
        }

        int totalTime = minDistance.get(targetRegionId);

        // Reconstruct path from predecessors
        LinkedList<Integer> path = new LinkedList<>();
        Integer curr = targetRegionId;
        while (curr != null) {
            path.addFirst(curr);
            curr = predecessor.get(curr);
        }

        return new PathResult(true, totalTime, path);
    }
}
