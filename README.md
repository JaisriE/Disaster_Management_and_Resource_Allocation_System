# Disaster Management Resource Allocation System
**Aruvi District Emergency Command Operations**  
**Course:** 24GEP31 - Mini Project (Second Review)  
**Department:** Computer Science and Engineering  

---

## 1. Project Identity & Team

- **Academic Project:** 24GEP31 - Mini Project (Second Review)
- **Case Study:** Aruvi District has suffered severe flash storms and flooding. Field officers across 10 villages report critical casualties, displacement, and food/medical shortages. Traditional uncoordinated management leads to stock duplication, neglected high-severity areas, and deliveries sent along flooded, severed roads.
- **Core Research Question:** *How can a Java-based system rank affected regions, allocate response resources, identify reachable routes, and revise unfulfilled allocations when new reports arrive?*
- **Design Principle:** **Automation SUPPORTS human decisions.** The system only RECOMMENDS an allocation. An authorized officer reviews and confirms or rejects it. Nothing is deducted from inventory until an officer confirms.
- **Project Team:**
  - **Dinaker G** (Roll No: 25CSR066)
  - **Jaisri E** (Roll No: 25CSR104)
  - **Kabila N** (Roll No: 25CSR114)
- **Faculty Mentor:** **Dr. K. Dinesh**, Associate Professor, Dept. of CSE
- **UN Sustainable Development Goals (SDG) Alignment:**
  - **SDG 3 (Good Health and Well-being):** Rapid trauma kit delivery and emergency medical evacuations.
  - **SDG 9 (Industry, Innovation and Infrastructure):** Resilient routing over damaged and open road networks.
  - **SDG 11 (Sustainable Cities and Communities):** Safe shelter allocation preventing overbooking and exposure.
  - **SDG 13 (Climate Action):** Preparedness and agile disaster response to extreme storm events.

---

## 2. Technology Stack & Architectural Constraints

In strict accordance with the project guidelines:
- **Programming Language:** Java 17+ (Java 26 verified).
- **Backend Architecture:** Built-in JDK HTTP server (`com.sun.net.httpserver.HttpServer`). Zero third-party web frameworks (No Spring Boot, No Servlets, No Dependency Injection).
- **JSON Processing:** Custom, hand-written `JsonUtil` class utilizing recursive parsing and `StringBuilder`. Zero third-party JSON libraries (No Gson, No Jackson).
- **Database Persistence:** MySQL Server 8 accessed via plain JDBC (`lib/mysql-connector-j-26.7.0.jar`) with parameterized `PreparedStatement`.
- **Frontend:** Pure HTML5, CSS3 (Modern dark-mode glassmorphic theme), and Vanilla JavaScript (Fetch API). Works 100% offline on localhost without any external CDNs.
- **Build System:** Platform batch scripts (`compile.bat`, `run.bat`, `test.bat`) and shell scripts (`run.sh`, `test.sh`).

---

## 3. Object-Oriented Programming (OOP) Mapping

| OOP Concept | Class / Interface | File Path | Practical Implementation in Project |
| :--- | :--- | :--- | :--- |
| **Encapsulation** | `ResourceStock` | `src/com/aruvi/dms/model/ResourceStock.java` | Private fields `foodKits`, `medicineKits`. All changes enforced via synchronized `reserveStock()`, `releaseStock()`, and `addStock()`, preventing negative quantities. |
| **Encapsulation** | `Shelter` | `src/com/aruvi/dms/model/Shelter.java` | Private `capacity` and `reservedPlaces`. Enforces invariant `0 <= reservedPlaces <= capacity`, eliminating overbooking risk. |
| **Encapsulation** | `RescueTeam` | `src/com/aruvi/dms/model/RescueTeam.java` | Private `available` flag and `currentRequestId`. Guarantees no duplicate concurrent assignment. |
| **Abstraction** | `ReliefRequest` | `src/com/aruvi/dms/model/ReliefRequest.java` | Abstract base class defining common incident contract (`severity`, `affectedPopulation`, `arrivalOrder`, `status`) and abstract methods `isFulfillable()` and `getSummary()`. |
| **Inheritance** | `SupplyRequest` | `src/com/aruvi/dms/model/SupplyRequest.java` | Extends `ReliefRequest`, adding food and medicine tracking, requested counts, and outstanding balances. |
| **Inheritance** | `RescueRequest` | `src/com/aruvi/dms/model/RescueRequest.java` | Extends `ReliefRequest`, adding rescue team dispatch and shelter accommodation tracking. |
| **Polymorphism** | `ReliefRequest` Hierarchy | `src/com/aruvi/dms/model/ReliefRequest.java` | Collections of type `List<ReliefRequest>` or `PriorityQueue<ReliefRequest>` uniformly invoke `isFulfillable()`, `getSummary()`, and `compareTo()`. |
| **Composition** | `Depot` | `src/com/aruvi/dms/model/Depot.java` | `Depot` has-a `ResourceStock` instance, delegating inventory actions with strict bounds checking. |

---

## 4. Algorithmic Foundation & Workflow

```
[Field Officer Files Report]
            |
            v
[Deterministic Priority Queue] <----- Re-prioritizes on any state change
(1. Severity DESC, 2. Pop DESC, 3. Arrival Order ASC)
            |
            v
[BFS / DFS Connectivity Check] ----> [Road Blocked?] ----> Mark UNREACHABLE
            | (Reachable over open roads)
            v
[Dijkstra Shortest Path (Min-Heap)]
(Calculates minimum travel time in minutes & exact path)
            |
            v
[Algorithmic Recommendation Engine]
(Computes recommended vs requested vs outstanding kits/shelter)
            |
            v
[Human Officer Decision Point]
            |-----------------------|
            v                       v
    [Officer Rejects]       [Officer Confirms]
            |                       |
(Logs reason; queue updated)  (Atomic MySQL Transaction:
                               1. Deduct Depot Stock
                               2. Assign Rescue Team
                               3. Reserve Shelter Places
                               4. Update Request Status & Outstanding
                               5. Record Allocation & Audit Log)
                                    |
                                    v
                           [Synchronize Memory]
                                    |
                                    v
                       [Rebuild Priority Queue]
```

### Deterministic Priority Comparison Formula
```java
// Higher severity first (5 down to 1)
int sevComp = Integer.compare(other.severity, this.severity);
if (sevComp != 0) return sevComp;

// Higher affected population first (descending)
int popComp = Integer.compare(other.affectedPopulation, this.affectedPopulation);
if (popComp != 0) return popComp;

// Earlier arrival order sequence first (ascending - FIFO tie breaker)
return Integer.compare(this.arrivalOrder, other.arrivalOrder);
```

### Road Network and Isolated Village
- The graph models **10 regions** in Aruvi district.
- **Region 10 (Solaiyur)** is deliberately isolated with damaged roads (`Road #25` Mountain Pass blocked by landslide, `Road #27` River Bridge collapsed).
- Any request originating in Solaiyur is automatically flagged as **UNREACHABLE**.
- When an officer opens `Road #27`, the network immediately heals: BFS verifies connectivity, Dijkstra determines the 92-minute route (`Ponnaiyanur -> Karamadai -> Marutham Village -> Solaiyur`), and allocations can proceed!

---

## 5. Database Architecture

MySQL Schema (`sql/schema.sql`):
1. `regions` (id, name, population, description)
2. `depots` (id, name, region_id, food_kits, medicine_kits)
3. `shelters` (id, name, region_id, capacity, reserved_places)
4. `rescue_teams` (id, name, home_depot_id, is_available, current_request_id)
5. `roads` (id, from_region_id, to_region_id, travel_time, is_open, description)
6. `emergency_requests` (id, region_id, severity, affected_population, arrival_order, request_type, requested/allocated/outstanding counts, status, reported_by, rejection_reason)
7. `allocations` (id, request_id, depot_id, rescue_team_id, shelter_id, food_allocated, medicine_allocated, shelter_places_allocated, route_path, travel_time, officer_name, status)
8. `audit_logs` (id, action_type, description, performed_by, created_at)

---

## 6. How to Build, Test, and Run

### Prerequisites
- JDK 17 or higher (Java 26 supported).
- MySQL Server 8 running locally on port 3306 with database `disaster_db`.
- Database credentials configured in `db.properties`.

### 1. Compile Application
```cmd
compile.bat
```
*(On Linux/macOS: `chmod +x run.sh test.sh` and compile via `run.sh`)*

### 2. Run Automated Test Suite
```cmd
test.bat
```
Executes 41 unit and integration tests across:
- Deterministic priority queue ranking
- Priority queue remove-and-reinsert mutation rule
- BFS / DFS connectivity and isolated village detection
- Dijkstra shortest path with min-heap
- Resource stock and shelter overbooking invariants
- Polymorphism and hand-written JSON serialization

### 3. Launch the Server & Control Room
```cmd
run.bat
```
Open your browser and navigate to:
```
http://localhost:8080/index.html
```

---

## 7. REST API Documentation

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/status` | System metrics, counts, and active connections. |
| `GET` | `/api/regions` | All 10 affected regions. |
| `GET` | `/api/depots` | Depot inventory counts (food and medicine). |
| `POST` | `/api/depots/stock` | Add or set depot food and medicine counts. |
| `GET` | `/api/shelters` | Shelter capacities, reserved, and available places. |
| `POST` | `/api/shelters/capacity` | Update shelter maximum accommodation capacity. |
| `GET` | `/api/teams` | Rescue team units and deployment status. |
| `POST` | `/api/teams/availability` | Toggle team ready / deployed state. |
| `GET` | `/api/roads` | All road links with travel time and open/closed status. |
| `POST` | `/api/roads/toggle` | Close or reopen road (triggers network recalculation). |
| `POST` | `/api/roads/add` | Insert a new road edge between regions. |
| `GET` | `/api/requests` | All emergency incident reports. |
| `GET` | `/api/requests/prioritized` | Pending requests ordered by deterministic priority queue. |
| `POST` | `/api/requests/create` | File a new emergency report (SUPPLY or RESCUE). |
| `POST` | `/api/requests/update` | Update severity/population/quantities (removes & re-inserts in PQ). |
| `POST` | `/api/requests/cancel` | Cancel an emergency request. |
| `GET` | `/api/recommendation?requestId={id}` | Algorithmic BFS reachability and Dijkstra routing advice. |
| `POST` | `/api/allocation/confirm` | Officer authorization committing allocation to MySQL transaction. |
| `POST` | `/api/allocation/reject` | Officer rejection with mandatory justification. |
| `GET` | `/api/allocations` | Complete log of confirmed allocations. |
| `GET` | `/api/audits` | Operational audit trail. |
