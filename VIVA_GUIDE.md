# Mini Project (Second Review) - Viva Voce & Presentation Guide
**Course:** 24GEP31 - Mini Project | Department of Computer Science and Engineering  
**Project:** Disaster Management Resource Allocation System (Aruvi District)  
**Team Members:** Dinaker G (25CSR066), Jaisri E (25CSR104), Kabila N (25CSR114)  
**Project Mentor:** Dr. K. Dinesh, Associate Professor  

---

## 1. Project Overview & Elevator Pitch

> *"Good morning respected evaluators and panel members. We present the Disaster Management Resource Allocation System developed for the fictional district of Aruvi, which has been struck by severe flash storms and flooding.*
> 
> *In traditional emergency scenarios, stocks, roads, and rescue team assignments are tracked manually on disjoint spreadsheets, causing catastrophic delays: high-severity villages wait while lower-urgency requests get served, resources are promised twice, and vehicles are dispatched along damaged, impassable roads.*
> 
> *Our solution implements a Java-based, algorithmic decision-support architecture. Pending requests are deterministically ranked using a 3-tier Priority Queue. Road reachability is verified via BFS/DFS over open roads, and Dijkstra's algorithm with a Min-Heap computes optimal travel time routes. Crucially, following the core ethical design principle: automation SUPPORTS human decisions—the system recommends an allocation, and an authorized officer reviews and confirms or rejects it before any resources are deducted inside an atomic MySQL database transaction."*

---

## 2. Transition from Review 1 to Review 2

| Feature | Review 1 (Phase 1) | Review 2 (Phase 2 - Current) |
| :--- | :--- | :--- |
| **Persistence** | In-memory only (lost on process restart) | MySQL Server 8 via plain JDBC `PreparedStatement` |
| **Interface** | Command-line terminal simulation | Real-time web control room (HTML5, CSS3, Vanilla JS) |
| **Architecture** | Monolithic console runner | Embedded JDK `HttpServer` on port 8080 serving REST APIs |
| **Routing** | Static distance assumptions | Dynamic graph: roads can be closed/reopened live |
| **Human Decision** | Automated mock allocation | Human-in-the-loop review modal with confirmation/rejection |
| **Transactional Integrity** | Non-transactional | ACID atomic MySQL transactions with rollback protection |

---

## 3. Anticipated Panel Questions & High-Scoring Answers

### Q1: How does the deterministic priority queue rank waiting requests?
**Answer:**  
*"We implement `Comparable<ReliefRequest>` in `ReliefRequest.java` using three deterministic tie-breaker criteria:*
1. *Higher Severity First (`5` down to `1`).*
2. *Higher Affected Population First (larger vulnerable populations take precedence).*
3. *Earlier Arrival Order Sequence Number First (FIFO tie-breaker to ensure fairness).*
*This guarantees that if two villages report equal catastrophe levels, the more populous village gets evaluated first, and if populations are identical, the earlier incoming radio report is served first."*

---

### Q2: Why does the system REMOVE and RE-INSERT requests in the PriorityQueue instead of mutating them in-place?
**Answer:**  
*"This is a crucial data structure property in Java's `java.util.PriorityQueue` (a binary heap). The heap property is maintained only when items are inserted via `add()` or removed via `poll()`. If you modify an object's fields (such as `severity` or `population`) while it sits inside the heap, the heap does not know its key changed and does not re-heapify. This causes silent priority inversion. Therefore, in `DisasterManagementService.updateEmergencyReport()`, we explicitly call `priorityQueue.remove(request)`, mutate the fields, and then call `priorityQueue.add(request)` to trigger the standard `O(log n)` heap sift operation."*

---

### Q3: How do BFS/DFS and Dijkstra interact in your routing algorithm?
**Answer:**  
*"We use a two-phase graph analysis in `ReachabilityService` and `DijkstraService`:*
1. *First, BFS checks connectivity: Can any candidate depot or team reach the destination village over OPEN roads (`road.isOpen() == true`)? If BFS returns `false`, we immediately stop, report status `UNREACHABLE`, and notify traffic control. For example, Region 10 (Solaiyur) has two incoming roads blocked by landslides; BFS immediately flags it as isolated.*
2. *Second, if reachable, Dijkstra's algorithm uses a Min-Heap (`PriorityQueue<NodeEntry>`) to find the path that minimizes total travel time in minutes. It reconstructs the exact sequence of hops from the predecessor map."*

---

### Q4: How is Object-Oriented Programming (OOP) demonstrated?
**Answer:**  
- **Encapsulation:** In `ResourceStock.java`, `foodKits` and `medicineKits` are private with synchronized methods `reserveStock()` and `releaseStock()`. Stock counts can never fall below zero. Similarly, `Shelter.java` enforces `0 <= reservedPlaces <= capacity`, making overbooking mathematically impossible.
- **Abstraction:** `ReliefRequest.java` is an abstract class declaring `abstract boolean isFulfillable()` and `abstract String getSummary()`.
- **Inheritance:** `SupplyRequest` (tangible kits) and `RescueRequest` (personnel and shelter) extend `ReliefRequest`.
- **Polymorphism:** The priority queue and service methods operate on `ReliefRequest` references. When calling `request.isFulfillable()`, the JVM dynamically dispatches to `SupplyRequest`'s kit check or `RescueRequest`'s team and shelter capacity check.
- **Composition:** `Depot` has a `ResourceStock` instance.

---

### Q5: How do you guarantee database consistency during confirmation?
**Answer:**  
*"In `DisasterManagementService.confirmAllocation()`, all writes occur within a single database transaction:*
```java
conn.setAutoCommit(false); // Begin transaction
// 1. Verify and update depot stock
// 2. Mark rescue team unavailable
// 3. Reserve shelter places
// 4. Update request status to ALLOCATED / PARTIALLY_ALLOCATED
// 5. Insert record into allocations table
// 6. Record audit log
conn.commit(); // Atomic commit
```
*If any constraint fails (e.g. stock was reserved concurrently), a `SQLException` triggers `conn.rollback()`, ensuring the database and in-memory state remain 100% consistent."*

---

### Q6: Why did you choose NOT to use frameworks like Spring Boot or libraries like Gson?
**Answer:**  
*"To demonstrate fundamental computer science mastery: core Java data structures, network socket programming with JDK's built-in `HttpServer`, manual recursive-descent JSON string parsing in `JsonUtil`, and raw JDBC with `PreparedStatement`. This eliminates heavy dependencies, ensures zero external vulnerability surface, and runs instantly on any standard JDK installation."*

---

### Q7: How does this project contribute to the United Nations Sustainable Development Goals (SDGs)?
**Answer:**  
- **SDG 3 (Good Health and Well-being):** Priority delivery of medical kits and urgent dispatch of flood rescue teams.
- **SDG 9 (Industry, Innovation and Infrastructure):** Resilient routing across disrupted road infrastructure.
- **SDG 11 (Sustainable Cities and Communities):** Systematic management of evacuation shelters preventing overcrowding.
- **SDG 13 (Climate Action):** Agile institutional preparedness against climate-induced extreme weather and flooding.

---

## 4. Live Demonstration Script for Review

1. **Step 1: System Boot & State Synchronization**
   - Run `run.bat`. Show terminal logs confirming MySQL connection and loading of 10 regions, 2 depots, 2 shelters, 3 rescue teams, and 16 emergency reports.
2. **Step 2: Command Center Dashboard**
   - Open `http://localhost:8080/index.html`.
   - Point out the KPI counters, dark glassmorphism styling, and the live SVG network map showing all 10 regions and travel times.
3. **Step 3: Isolated Village Demonstration**
   - Point out **Region 10 (Solaiyur)**. Show that its connecting roads are red and dashed (closed).
   - In the Priority Queue, highlight Request #10 from Solaiyur. Click **Recommend & Review**.
   - Show the algorithmic advice: **ROUTE SEVERED - UNREACHABLE**. The confirmation button is disabled.
4. **Step 4: Dynamic Route Healing**
   - Switch to **Road Network & Closures** tab.
   - Click **Reopen Road** on Road #27 (Solaiyur River Bridge).
   - Return to the map: Road #27 turns cyan and active!
   - Open Request #10 again: The system immediately calculates the 92-minute Dijkstra path via Depot 2 and enables allocation!
5. **Step 5: Human Officer Confirmation**
   - On Request #3 (Bhavani Bank), click **Recommend & Review**.
   - Show Dijkstra route from Eastern Staging Depot Beta (28 mins).
   - Enter officer name and click **Confirm & Deduct Resources**.
   - Show success toast. Verify Depot 2 inventory is deducted, the request moves out of pending, and the record appears in **Confirmed Allocations** and **System Audit Trail**.
