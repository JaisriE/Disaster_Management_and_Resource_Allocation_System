# System Architecture Diagram
**Project:** Disaster Management Resource Allocation System (Aruvi District)  
**Course:** 24GEP31 - Mini Project (Second Review), Department of CSE  

---

```mermaid
graph TB
    subgraph ClientLayer ["1. Presentation Layer (Localhost Browser)"]
        UI["Aruvi Command Operations UI<br/>(Single-Page HTML5 / Vanilla CSS3)"]
        JSApp["app.js<br/>(State Controller & Fetch API)"]
        JSMap["map.js<br/>(Interactive SVG Topology Map)"]
        UI --- JSApp
        UI --- JSMap
    end

    subgraph TransportLayer ["2. Web Server Layer (JDK Built-in)"]
        HttpServer["com.sun.net.httpserver.HttpServer<br/>Port 8080"]
        StaticRouter["StaticFileHandler<br/>(/, /css/*, /js/*)"]
        ApiRouter["ApiHandler<br/>(/api/*)"]
        JsonUtil["JsonUtil<br/>(Hand-written JSON Parser / Serializer)"]
        
        HttpServer --> StaticRouter
        HttpServer --> ApiRouter
        ApiRouter --- JsonUtil
    end

    subgraph ServiceLayer ["3. Business Logic & In-Memory State Layer"]
        DMS["DisasterManagementService<br/>(Central Singleton Coordinator)"]
        
        subgraph InMemData ["In-Memory Data Structures"]
            PQ["Deterministic PriorityQueue<br/>(Severity DESC -> Pop DESC -> Order ASC)"]
            GraphMem["Graph Adjacency List<br/>(10 Regions, 28 Road Edges)"]
            StateMaps["Concurrent State Maps<br/>(Regions, Depots, Shelters, Teams)"]
        end

        subgraph GraphAlgos ["Routing & Graph Engines"]
            BFS["ReachabilityService<br/>(BFS / DFS over OPEN roads only)"]
            Dijkstra["DijkstraService<br/>(Shortest Travel Time via Min-Heap)"]
        end

        DMS --- PQ
        DMS --- GraphMem
        DMS --- StateMaps
        DMS --- BFS
        DMS --- Dijkstra
    end

    subgraph PersistenceLayer ["4. Persistence & Data Access Layer"]
        DAOs["Data Access Objects (DAOs)<br/>RegionDAO, DepotDAO, ShelterDAO, TeamDAO,<br/>RoadDAO, RequestDAO, AllocationDAO, AuditDAO"]
        DBConfig["DBConfig & Connection Pool<br/>(Plain JDBC PreparedStatement)"]
        
        DAOs --- DBConfig
    end

    subgraph DatabaseLayer ["5. Relational Database (MySQL 8)"]
        MySQL[("MySQL Server 8.0.46<br/>Database: disaster_db<br/>Port: 3306")]
        Tables["Tables:<br/>• regions<br/>• depots<br/>• shelters<br/>• rescue_teams<br/>• roads<br/>• emergency_requests<br/>• allocations<br/>• audit_logs"]
        
        MySQL --- Tables
    end

    %% Flow connections
    JSApp -->|"HTTP GET/POST (JSON)"| ApiRouter
    StaticRouter -->|"Serves Assets"| UI
    ApiRouter -->|"Invokes Operations"| DMS
    DMS -->|"Synchronizes & Persists"| DAOs
    DBConfig -->|"JDBC TCP Connection"| MySQL

    classDef client fill:#1e293b,stroke:#00f0ff,stroke-width:2px,color:#fff;
    classDef server fill:#0f172a,stroke:#3b82f6,stroke-width:2px,color:#fff;
    classDef service fill:#064e3b,stroke:#10b981,stroke-width:2px,color:#fff;
    classDef dao fill:#312e81,stroke:#818cf8,stroke-width:2px,color:#fff;
    classDef db fill:#451a03,stroke:#f59e0b,stroke-width:2px,color:#fff;

    class UI,JSApp,JSMap client;
    class HttpServer,StaticRouter,ApiRouter,JsonUtil server;
    class DMS,PQ,GraphMem,StateMaps,BFS,Dijkstra service;
    class DAOs,DBConfig dao;
    class MySQL,Tables db;
```
