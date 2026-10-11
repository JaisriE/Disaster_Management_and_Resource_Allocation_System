# Object-Oriented Programming (OOP) Concepts Specification
**Project:** Disaster Management Resource Allocation System (Aruvi District)  
**Course:** 24GEP31 - Mini Project (Second Review), Department of CSE  
**Team:** Dinaker G (25CSR066), Jaisri E (25CSR104), Kabila N (25CSR114)  
**Mentor:** Dr. K. Dinesh, Associate Professor  

---

This document explicitly defines every core Object-Oriented Programming concept applied in the codebase, detailing the exact class, file location, and software architectural purpose.

---

## 1. Encapsulation

Encapsulation packages data (fields) and the methods operating on that data within a unified class, hiding internal state from arbitrary external tampering and strictly enforcing business rules.

### A. `ResourceStock`
- **File:** [`src/com/aruvi/dms/model/ResourceStock.java`](file:///d:/DisasterManagementSystem/src/com/aruvi/dms/model/ResourceStock.java)
- **Private Fields:**
  - `private int foodKits;`
  - `private int medicineKits;`
- **Encapsulated Business Rule:**
  Food kits and medicine kits represent critical survival inventory. Counts can **never** become negative under any circumstance.
- **Methods:**
  - `public synchronized boolean reserveStock(int food, int medicine)`: Checks if sufficient inventory exists. If yes, decrements atomically; if not, returns `false` without modifying counts.
  - `public synchronized void addStock(int food, int medicine)`: Rejects negative addition arguments with an `IllegalArgumentException`.
  - `public synchronized void releaseStock(int food, int medicine)`: Replenishes unconsumed supplies back into stock safely.

### B. `Shelter`
- **File:** [`src/com/aruvi/dms/model/Shelter.java`](file:///d:/DisasterManagementSystem/src/com/aruvi/dms/model/Shelter.java)
- **Private Fields:**
  - `private int capacity;`
  - `private int reservedPlaces;`
- **Encapsulated Business Rule:**
  A shelter cannot exceed physical accommodation capacity (`reservedPlaces <= capacity`) and reserved places cannot fall below zero (`reservedPlaces >= 0`).
- **Methods:**
  - `public int availablePlaces()`: Computes `capacity - reservedPlaces`.
  - `public synchronized boolean reservePlaces(int places)`: Rejects reservations if `availablePlaces() < places`, making shelter overbooking mathematically impossible.

### C. `RescueTeam`
- **File:** [`src/com/aruvi/dms/model/RescueTeam.java`](file:///d:/DisasterManagementSystem/src/com/aruvi/dms/model/RescueTeam.java)
- **Private Fields:**
  - `private boolean available;`
  - `private Integer currentRequestId;`
- **Encapsulated Business Rule:**
  A rescue team can only be deployed on one incident at a time. It cannot be promised to another village while active.
- **Methods:**
  - `public synchronized boolean reserveTeam(int requestId)`: Only allows assignment if `this.available == true`. Once reserved, sets `available = false` and stores `currentRequestId`.

### D. `AffectedRegion`
- **File:** [`src/com/aruvi/dms/model/AffectedRegion.java`](file:///d:/DisasterManagementSystem/src/com/aruvi/dms/model/AffectedRegion.java)
- **Encapsulated Rule:** Validates that region ID is positive, region name is non-empty, and census population is greater than zero.

---

## 2. Abstraction

Abstraction hides complex operational details and exposes only essential interfaces to callers.

### `ReliefRequest` (Abstract Base Class)
- **File:** [`src/com/aruvi/dms/model/ReliefRequest.java`](file:///d:/DisasterManagementSystem/src/com/aruvi/dms/model/ReliefRequest.java)
- **Role:** Defines the general concept of an emergency incident report without binding itself to the specific nature of the demanded resource.
- **Abstract Methods:**
  - `public abstract boolean isFulfillable();`  
    Subclasses determine fulfillment feasibility according to their specific resource types (survival kits vs. rescue units).
  - `public abstract String getSummary();`  
    Subclasses format their human-readable demand summaries polymorphically.
- **Contract Enforcement:**
  Implements `Comparable<ReliefRequest>` to enforce deterministic ranking across all relief requests regardless of subclass.

---

## 3. Inheritance

Inheritance facilitates code reuse and models "is-a" relationships, establishing hierarchical domain structures.

```
                  ReliefRequest (abstract)
                       ▲           ▲
                       │           │
           ┌───────────┴┐         ┌┴───────────┐
     SupplyRequest             RescueRequest
(Food & Medicine Kits)      (Teams & Shelter)
```

### A. `SupplyRequest` (extends `ReliefRequest`)
- **File:** [`src/com/aruvi/dms/model/SupplyRequest.java`](file:///d:/DisasterManagementSystem/src/com/aruvi/dms/model/SupplyRequest.java)
- **Specialized State:**
  - `requestedFood`, `allocatedFood`, `outstandingFood`
  - `requestedMedicine`, `allocatedMedicine`, `outstandingMedicine`
- **Relationship:** A supply request *is-a* relief request that tracks physical rations.

### B. `RescueRequest` (extends `ReliefRequest`)
- **File:** [`src/com/aruvi/dms/model/RescueRequest.java`](file:///d:/DisasterManagementSystem/src/com/aruvi/dms/model/RescueRequest.java)
- **Specialized State:**
  - `requestedTeams`, `allocatedTeams`
  - `requestedShelterPlaces`, `allocatedShelterPlaces`, `outstandingShelterPlaces`
- **Relationship:** A rescue request *is-a* relief request that tracks human emergency workers and evacuation beds.

---

## 4. Polymorphism

Polymorphism allows objects of different concrete subclasses to be treated uniformly through their common parent reference, executing subclass-specific logic dynamically at runtime.

### Dynamic Method Dispatch in Action
- **Method Overriding:**
  Both `SupplyRequest` and `RescueRequest` override `isFulfillable()`:
  - `SupplyRequest.isFulfillable()` checks if `outstandingFood > 0 || outstandingMedicine > 0`.
  - `RescueRequest.isFulfillable()` checks if `requestedTeams > allocatedTeams || outstandingShelterPlaces > 0`.
- **Collection Processing:**
  In [`DisasterManagementService.java`](file:///d:/DisasterManagementSystem/src/com/aruvi/dms/service/DisasterManagementService.java) and [`TestRunner.java`](file:///d:/DisasterManagementSystem/src/com/aruvi/dms/test/TestRunner.java):
  ```java
  List<ReliefRequest> requests = new ArrayList<ReliefRequest>();
  requests.add(new SupplyRequest(...));
  requests.add(new RescueRequest(...));

  for (int i = 0; i < requests.size(); i++) {
      ReliefRequest r = requests.get(i);
      // Polymorphic invocation: JVM dynamically executes the correct subclass method
      boolean eligible = r.isFulfillable();
      String summary = r.getSummary();
  }
  ```
- **Heap Priority Queue Polymorphism:**
  Java's `PriorityQueue<ReliefRequest>` transparently ranks mixed lists of `SupplyRequest` and `RescueRequest` using the base class `compareTo()` method without needing casting or `instanceof` checks.

---

## 5. Composition

Composition models "has-a" relationships where complex components own and coordinate smaller functional objects.

### A. `Depot` has-a `ResourceStock`
- **File:** [`src/com/aruvi/dms/model/Depot.java`](file:///d:/DisasterManagementSystem/src/com/aruvi/dms/model/Depot.java)
- **Code:**
  ```java
  public class Depot {
      private int id;
      private String name;
      private int regionId;
      private final ResourceStock stock; // Composition
      ...
  }
  ```
- **Rationale:** Instead of duplicating integer variables `foodKits` and `medicineKits` inside `Depot`, `Depot` delegates inventory tracking directly to `ResourceStock`.

### B. `Graph` has-a Map of `Road` lists
- **File:** [`src/com/aruvi/dms/graph/Graph.java`](file:///d:/DisasterManagementSystem/src/com/aruvi/dms/graph/Graph.java)
- **Code:**
  ```java
  public class Graph {
      private final Map<Integer, List<Road>> adjList; // Composition of Road edges
      private final Set<Integer> vertexIds;
      ...
  }
  ```
- **Rationale:** The transportation network delegates edge weight and closure properties to individual `Road` objects.
