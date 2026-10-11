package com.aruvi.dms.graph;

import com.aruvi.dms.model.Road;

import java.util.*;

/**
 * Graph represents the weighted transportation network of Aruvi district.
 * Uses adjacency list representation: Map of regionId -> List of outgoing Road edges.
 */
public class Graph {
    private final Map<Integer, List<Road>> adjList = new HashMap<>();
    private final Set<Integer> vertexIds = new HashSet<>();

    public Graph() {
    }

    public synchronized void addVertex(int regionId) {
        vertexIds.add(regionId);
        List<Road> list = adjList.get(regionId);
        if (list == null) {
            list = new ArrayList<Road>();
            adjList.put(regionId, list);
        }
    }

    public synchronized void addEdge(Road road) {
        addVertex(road.getFromRegionId());
        addVertex(road.getToRegionId());
        adjList.get(road.getFromRegionId()).add(road);
    }

    public synchronized List<Road> getEdges(int regionId) {
        List<Road> edges = adjList.get(regionId);
        return edges != null ? new ArrayList<>(edges) : Collections.emptyList();
    }

    public synchronized List<Road> getOpenEdges(int regionId) {
        List<Road> edges = adjList.get(regionId);
        if (edges == null) return Collections.emptyList();
        List<Road> open = new ArrayList<>();
        for (Road r : edges) {
            if (r.isOpen()) {
                open.add(r);
            }
        }
        return open;
    }

    public synchronized Set<Integer> getVertices() {
        return new HashSet<>(vertexIds);
    }

    public synchronized void clear() {
        adjList.clear();
        vertexIds.clear();
    }

    public synchronized void setRoadOpen(int roadId, boolean open) {
        for (List<Road> list : adjList.values()) {
            for (Road r : list) {
                if (r.getId() == roadId) {
                    r.setOpen(open);
                }
            }
        }
    }
}
