# UML Class Diagram
**Project:** Disaster Management Resource Allocation System (Aruvi District)  
**Course:** 24GEP31 - Mini Project (Second Review), Department of CSE  

---

```mermaid
classDiagram
    direction TB

    class AffectedRegion {
        -int id
        -String name
        -int population
        -String description
        +getId() int
        +setId(int id) void
        +getName() String
        +setName(String name) void
        +getPopulation() int
        +setPopulation(int pop) void
        +getDescription() String
    }

    class ResourceStock {
        -int foodKits
        -int medicineKits
        +getFoodKits() int
        +getMedicineKits() int
        +addStock(int food, int med) void
        +setStock(int food, int med) void
        +reserveStock(int food, int med) boolean
        +releaseStock(int food, int med) void
        +hasStock(int food, int med) boolean
    }

    class Depot {
        -int id
        -String name
        -int regionId
        -String regionName
        -ResourceStock stock
        +getId() int
        +getName() String
        +getRegionId() int
        +getStock() ResourceStock
        +reserveStock(int food, int med) boolean
        +addStock(int food, int med) void
        +setStock(int food, int med) void
    }

    class Shelter {
        -int id
        -String name
        -int regionId
        -String regionName
        -int capacity
        -int reservedPlaces
        +getId() int
        +getName() String
        +getCapacity() int
        +getReservedPlaces() int
        +availablePlaces() int
        +reservePlaces(int places) boolean
        +releasePlaces(int places) void
    }

    class RescueTeam {
        -int id
        -String name
        -int homeDepotId
        -String homeDepotName
        -boolean available
        -Integer currentRequestId
        +getId() int
        +getName() String
        +isAvailable() boolean
        +reserveTeam(int reqId) boolean
        +releaseTeam() void
    }

    class Road {
        -int id
        -int fromRegionId
        -int toRegionId
        -int travelTime
        -boolean open
        -String description
        +getId() int
        +getTravelTime() int
        +isOpen() boolean
        +setOpen(boolean open) void
    }

    class Graph {
        -Map~int, List~Road~~ adjList
        -Set~int~ vertexIds
        +addVertex(int id) void
        +addEdge(Road road) void
        +getEdges(int id) List~Road~
        +getOpenEdges(int id) List~Road~
        +setRoadOpen(int id, boolean open) void
    }

    class RequestStatus {
        <<enumeration>>
        PENDING
        PARTIALLY_ALLOCATED
        ALLOCATED
        REJECTED
        CANCELLED
        UNREACHABLE
    }

    class ReliefRequest {
        <<abstract>>
        #int id
        #int regionId
        #String regionName
        #int severity
        #int affectedPopulation
        #int arrivalOrder
        #String requestType
        #RequestStatus status
        #String reportedBy
        #String rejectionReason
        +compareTo(ReliefRequest other) int
        +isFulfillable()* boolean
        +getSummary()* String
    }

    class SupplyRequest {
        -int requestedFood
        -int allocatedFood
        -int outstandingFood
        -int requestedMedicine
        -int allocatedMedicine
        -int outstandingMedicine
        +isFulfillable() boolean
        +getSummary() String
    }

    class RescueRequest {
        -int requestedTeams
        -int allocatedTeams
        -int requestedShelterPlaces
        -int allocatedShelterPlaces
        -int outstandingShelterPlaces
        +isFulfillable() boolean
        +getSummary() String
    }

    class Recommendation {
        -int requestId
        -String requestType
        -int regionId
        -boolean reachable
        -List~int~ pathRegionIds
        -int travelTimeMinutes
        -Integer recommendedDepotId
        -int foodRecommended
        -int medicineRecommended
        -int outstandingFood
        -int outstandingMedicine
        -Integer recommendedTeamId
        -Integer recommendedShelterId
        -int shelterPlacesRecommended
        -boolean canAllocate
        -RequestStatus recommendedStatus
        -String explanation
    }

    class Allocation {
        -int id
        -int requestId
        -Integer depotId
        -Integer rescueTeamId
        -Integer shelterId
        -int foodAllocated
        -int medicineAllocated
        -int shelterPlacesAllocated
        -String routePath
        -int travelTime
        -String officerName
        -String status
    }

    class DisasterManagementService {
        -Map~int, AffectedRegion~ regionsMap
        -Map~int, Depot~ depotsMap
        -Map~int, Shelter~ sheltersMap
        -Map~int, RescueTeam~ teamsMap
        -Map~int, Road~ roadsMap
        -Map~int, ReliefRequest~ requestsMap
        -PriorityQueue~ReliefRequest~ priorityQueue
        -Graph roadGraph
        +getInstance() DisasterManagementService
        +loadAllFromDatabase() void
        +rebuildPriorityQueue() void
        +generateRecommendation(int id) Recommendation
        +confirmAllocation(int id, String officer) boolean
        +rejectAllocation(int id, String reason, String officer) void
        +addEmergencyReport(ReliefRequest req) int
        +updateEmergencyReport(...) void
        +setRoadOpen(int id, boolean open) void
        +resetDemoData() void
    }

    %% Relationships
    Depot *-- ResourceStock : composes
    Depot --> AffectedRegion : references location
    Shelter --> AffectedRegion : references location
    RescueTeam --> Depot : stationed at
    Graph o-- Road : contains
    Road --> AffectedRegion : links endpoints
    ReliefRequest <|-- SupplyRequest : extends
    ReliefRequest <|-- RescueRequest : extends
    ReliefRequest --> RequestStatus : has lifecycle state
    DisasterManagementService --> Graph : uses
    DisasterManagementService --> ReliefRequest : manages PriorityQueue of
    DisasterManagementService ..> Recommendation : generates
    DisasterManagementService ..> Allocation : persists
```
