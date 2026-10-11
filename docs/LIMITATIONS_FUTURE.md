# System Limitations & Future Scope
**Project:** Disaster Management Resource Allocation System (Aruvi District)  
**Course:** 24GEP31 - Mini Project (Second Review), Department of CSE  
**Team:** Dinaker G (25CSR066), Jaisri E (25CSR104), Kabila N (25CSR114)  
**Mentor:** Dr. K. Dinesh, Associate Professor  

---

## 1. Current System Limitations

While the current system fulfills all Review 2 requirements with high performance and transactional integrity, several technical and operational boundaries exist:

### A. Technical Limitations
1. **Single-Node Embedded Server:**  
   The application runs on JDK's built-in `com.sun.net.httpserver.HttpServer` bound to a single local machine. While sufficient for local district control rooms (supporting 10–50 concurrent operators), it is not clustered for multi-district failover.
2. **Simplified Network Congestion:**  
   Road travel times ($t > 0$) are assumed static for a given open/closed state. Real disaster conditions experience dynamic bottlenecks where relief truck convoys cause temporary congestion on narrow rural roads.
3. **Synthetic Topology vs. Real Geospatial GIS:**  
   The interactive map utilizes a clean, normalized SVG coordinate system rather than real-time GeoJSON latitude/longitude coordinates layered on GIS shapefiles.
4. **Single-Depot Fulfillment Assumption:**  
   For supply requests, the recommendation engine currently selects the single best reachable depot. If one depot has 60 food kits and another has 40, it allocates what the closest depot has, leaving the rest outstanding rather than automatically splitting the delivery across two depots.

### B. Operational Limitations
1. **Manual Field Input Dependency:**  
   Severity ratings (1–5) and population counts depend on human field officer reporting. Discrepancies in manual assessments could skew priority ranking until adjusted by the command officer.
2. **Offline Localhost Boundary:**  
   Field officers in remote hamlets without local network access must communicate via radio back to the central control room operator who enters reports into the web interface.

---

## 2. Future Enhancement Roadmap

### Phase 1: Near-Term Enhancements (Review 3 / Final Capstone)
1. **Multi-Depot Split Allocations (Minimum-Cost Flow):**  
   Implement a Minimum-Cost Maximum-Flow algorithm to fulfill large requests by aggregating supplies from multiple depots when no single depot has sufficient stock.
2. **Offline LoRa / Mesh Radio Gateway:**  
   Create an interface for inexpensive LoRa (Long Range) 433/868 MHz radio transceivers. Field officers could transmit structured text packets (e.g., `REQ,R10,SEV5,POP800,FOOD50`) that are parsed directly into the Priority Queue without requiring internet access.
3. **Role-Based Access Control (RBAC) & Cryptographic Signatures:**  
   Add distinct roles (Field Reporter, Logistics Dispatcher, Command Officer) with cryptographic password hashing (BCrypt) and digital audit signatures for authorizations.

### Phase 2: Long-Term Enterprise Upgrades
1. **Offline GIS Mapping with Leaflet & OpenStreetMap Tiles:**  
   Replace the custom SVG topology with an offline tile-server rendering real topographic terrain, flood basin contours, and elevation profiles.
2. **Drone Transit Corridors for Isolated Hamlets:**  
   Model secondary 3D graph edges representing aerial drone corridors. When ground roads are blocked (such as Region 10 Solaiyur), the system could recommend automated lightweight medical drone drops while ground crews clear roads.
3. **Predictive Weather & Inundation ML Models:**  
   Integrate rainfall radar predictions to automatically increase edge travel times or close roads proactively *before* flash flooding breaches riverbanks.
