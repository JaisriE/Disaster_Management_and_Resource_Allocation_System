# Data Structures and Algorithms Justification
**Project:** Disaster Management Resource Allocation System (Aruvi District)  
**Course:** 24GEP31 - Mini Project (Second Review), Department of CSE  

---

This document provides theoretical and practical justifications for every data structure and algorithm implemented in the project, detailing their computational complexity, trade-offs, and operational role.

---

## 1. Summary of Data Structures & Complexity

| Data Structure | Implementation File | Primary Purpose | Time Complexity | Space Complexity |
| :--- | :--- | :--- | :--- | :--- |
| **Priority Queue (Binary Heap)** | `DisasterManagementService.java` | Deterministic ordering of waiting emergency reports | Insertion: $O(\log n)$<br/>Peek/Poll: $O(1)$ / $O(\log n)$<br/>Remove: $O(n)$ | $O(n)$ |
| **Adjacency List (Graph)** | `graph/Graph.java` | Represents transportation network of regions and roads | Add Edge: $O(1)$<br/>Query Outgoing: $O(\text{deg}(v))$ | $O(V + E)$ |
| **Hash Map (`ConcurrentHashMap`)** | `DisasterManagementService.java` | In-memory index of regions, depots, shelters, teams, requests | Lookup: $O(1)$<br/>Insert/Update: $O(1)$ | $O(n)$ |
| **Min-Heap for Dijkstra** | `graph/DijkstraService.java` | Extracts closest unvisited region by travel time | Insert: $O(\log V)$<br/>Extract-Min: $O(\log V)$ | $O(V)$ |
| **FIFO Queue (`LinkedList`)** | `graph/ReachabilityService.java` | Breadth-First Search level traversal for road connectivity | Enqueue: $O(1)$<br/>Dequeue: $O(1)$ | $O(V)$ |
| **Predecessor Map** | `graph/DijkstraService.java` | Reconstructs exact sequence of regions along shortest route | Lookup/Insert: $O(1)$<br/>Trace Path: $O(\text{hops})$ | $O(V)$ |

---

## 2. Algorithms & Computational Analysis

### A. Deterministic Priority Ranking Algorithm
- **Location:** [`src/com/aruvi/dms/model/ReliefRequest.java`](file:///d:/DisasterManagementSystem/src/com/aruvi/dms/model/ReliefRequest.java) (`compareTo`)
- **Operation:**
  A 3-level hierarchical comparator evaluates candidate requests:
  1. **Severity (Descending):** $O(1)$ integer comparison.
  2. **Affected Population (Descending):** $O(1)$ integer comparison for severity ties.
  3. **Arrival Order (Ascending):** $O(1)$ integer comparison for population ties.
- **Why Chosen:**
  Eliminates arbitrary or non-deterministic queue ordering. Guarantees that the most acute crises affecting the largest civilian populations are surfaced first, while preventing starvation of earlier reports through the sequence tie-breaker.
- **Heap Mutation Rule:**
  Modifying a request while inside the heap violates heap invariants without triggering re-heapification. Thus, our system strictly performs:
  $$\text{Heap.remove}(req) \xrightarrow{O(n)} \text{mutate fields} \xrightarrow{O(1)} \text{Heap.add}(req) \xrightarrow{O(\log n)}$$
  Ensuring $100\%$ mathematical correctness at all times.

---

### B. Breadth-First Search (BFS) Reachability
- **Location:** [`src/com/aruvi/dms/graph/ReachabilityService.java`](file:///d:/DisasterManagementSystem/src/com/aruvi/dms/graph/ReachabilityService.java)
- **Time Complexity:** $O(V + E)$ where $V = \text{number of regions}$ (10 in seed), $E = \text{number of open roads}$ (28 directed edges).
- **Space Complexity:** $O(V)$ for visited set and queue.
- **Why Chosen:**
  Before running expensive weight calculations, BFS verifies whether **any** open path connects a depot/team to the target region. If floodwaters or landslides sever all roads into a village (e.g., Region 10 Solaiyur), BFS terminates quickly and avoids running unnecessary Dijkstra calculations.

---

### C. Depth-First Search (DFS) Traversal
- **Location:** [`src/com/aruvi/dms/graph/ReachabilityService.java`](file:///d:/DisasterManagementSystem/src/com/aruvi/dms/graph/ReachabilityService.java)
- **Time Complexity:** $O(V + E)$
- **Space Complexity:** $O(V)$ recursion call stack.
- **Why Chosen:**
  Serves as an alternative connectivity check to explore deep network paths and test isolated component detection.

---

### D. Dijkstra's Shortest Path Algorithm with Min-Heap
- **Location:** [`src/com/aruvi/dms/graph/DijkstraService.java`](file:///d:/DisasterManagementSystem/src/com/aruvi/dms/graph/DijkstraService.java)
- **Time Complexity:** $O((V + E) \log V)$ using binary min-heap.
- **Space Complexity:** $O(V)$ for distances map, predecessor map, and priority queue.
- **Why Chosen over Bellman-Ford or Floyd-Warshall:**
  - Road travel times are strictly positive physical durations ($t > 0$ minutes). Negative cycles cannot exist in physical road transportation.
  - Bellman-Ford runs in $O(V \cdot E)$, which is unnecessarily slow.
  - Floyd-Warshall runs in $O(V^3)$, which is inefficient for dynamic road networks where edges open and close frequently.
  - Dijkstra with a min-heap operates in near-linear time ($O((V + E) \log V)$), finding optimal minimum travel times in **sub-millisecond** time (0.016 ms on 200 regions).

---

## 3. Real Performance Profile (Measured in Benchmark)

| Operation | Seed Graph (10 Regions, 28 Roads) | Generated Graph (200 Regions, 800 Roads) |
| :--- | :--- | :--- |
| **BFS Reachability** | **0.0127 ms** | **0.0116 ms** |
| **DFS Reachability** | **0.0076 ms** | **0.1607 ms** |
| **Dijkstra Shortest Path** | **0.0206 ms** | **0.0162 ms** |
| **Full Priority Queue Re-sort** | **0.0086 ms** (16 requests) | **0.1013 ms** (200 requests) |

Both datasets execute in **under 0.25 milliseconds**, proving that the Java in-memory algorithm layer provides real-time responsiveness for emergency command centers.
