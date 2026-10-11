# Automated Test Cases and Execution Results
**Project:** Disaster Management Resource Allocation System (Aruvi District)  
**Course:** 24GEP31 - Mini Project (Second Review), Department of CSE  
**Executed Command:** `cmd /c test.bat`  
**Execution Environment:** Windows | Java 26.0.2 | MySQL Server 8.0.46  
**Total Executed Tests:** **83 Tests**  
**Passed:** **83** | **Failed:** **0** | **Pass Rate:** **100%**  

---

## 1. Test Suite Results Overview

| Test Suite ID | Category | Tests Executed | Result |
| :---: | :--- | :---: | :---: |
| **TS-01** | Deterministic Priority Queue Ranking | 3 | **PASS** |
| **TS-02** | PriorityQueue Mutation Rule (Remove & Re-insert) | 2 | **PASS** |
| **TS-03** | BFS & DFS Reachability & Isolated Village | 4 | **PASS** |
| **TS-04** | Dijkstra Shortest Path with Min-Heap | 3 | **PASS** |
| **TS-05** | ResourceStock Non-Negative Invariant | 6 | **PASS** |
| **TS-06** | Shelter Capacity & Overbooking Protection | 5 | **PASS** |
| **TS-07** | RescueTeam Duplicate Assignment Protection | 5 | **PASS** |
| **TS-08** | OOP Polymorphism on ReliefRequest Hierarchy | 3 | **PASS** |
| **TS-09** | Hand-written JsonUtil Serialization & Parsing | 4 | **PASS** |
| **TS-10** | Service End-to-End Recommendation Engine | 6 | **PASS** |
| **TS-11** | Restart Persistence across MySQL Reload | 4 | **PASS** |
| **TS-12** | Atomic Database Transaction Rollback on Failure | 3 | **PASS** |
| **TS-13** | Duplicate Allocation Prevention | 3 | **PASS** |
| **TS-14** | Input Validation Rules (Boundary & Error Cases) | 13 | **PASS** |
| **TS-15** | Partial Allocation with Outstanding Quantities | 6 | **PASS** |
| **TS-16** | Full Shelter & Unavailable Rescue Team | 3 | **PASS** |
| **TS-17** | Officer Rejection Changes Nothing | 4 | **PASS** |
| **TS-18** | Automatic Re-prioritization On State Changes | 4 | **PASS** |
| **TS-19** | Performance Benchmarking (Seed & 200 Nodes) | 2 | **PASS** |
| **TOTAL** | **Comprehensive Validation** | **83** | **100% PASS** |

---

## 2. Detailed Test Cases Matrix

### TS-01: Deterministic Priority Queue Ranking
- **TC-01.1:** Higher population wins when severity is tied.
  - *Input:* Request A (Sev 5, Pop 2500) vs Request B (Sev 5, Pop 1200).
  - *Expected:* Request A ordered before Request B (`rA.compareTo(rB) < 0`).
  - *Actual:* `[PASS]` Higher population wins on tie severity.
- **TC-01.2:** Higher severity wins over lower severity.
  - *Input:* Request B (Sev 5, Pop 1200) vs Request C (Sev 4, Pop 1500).
  - *Expected:* Request B ordered before Request C (`rB.compareTo(rC) < 0`).
  - *Actual:* `[PASS]` Higher severity wins over lower severity.
- **TC-01.3:** Earlier arrival order wins when severity and population are tied.
  - *Input:* Request D (Sev 4, Pop 1500, Order 1) vs Request E (Sev 4, Pop 1500, Order 4).
  - *Expected:* Request D ordered before Request E (`rD.compareTo(rE) < 0`).
  - *Actual:* `[PASS]` Earlier arrival order wins on tie severity and pop.

### TS-02: Priority Queue Mutation Rule (Remove & Re-insert)
- **TC-02.1:** Queue head validation.
  - *Input:* Low priority request (Sev 2, Pop 500) and High priority request (Sev 4, Pop 2000).
  - *Actual:* `[PASS]` High priority is head of queue.
- **TC-02.2:** Mutation re-insertion re-heapification.
  - *Action:* Remove low priority request, change its severity to 5 and population to 5000, re-insert.
  - *Actual:* `[PASS]` Mutated request becomes new head after re-insertion.

### TS-03: BFS/DFS Reachability & Isolated Village
- **TC-03.1:** BFS connected traversal.
  - *Input:* Road 1->2 open, Road 2->3 open. Check reachability 1->3.
  - *Actual:* `[PASS]` BFS: Village 1 reaches Village 3.
- **TC-03.2:** DFS connected traversal.
  - *Actual:* `[PASS]` DFS: Village 1 reaches Village 3.
- **TC-03.3:** Isolated village blocked road detection.
  - *Input:* Road 3->10 marked closed (`open = false`). Check reachability 1->10.
  - *Actual:* `[PASS]` BFS: Village 10 is unreachable when road closed.
- **TC-03.4:** Dynamic network healing.
  - *Action:* Road 3->10 reopened (`open = true`).
  - *Actual:* `[PASS]` BFS: Village 10 becomes reachable when road reopens.

### TS-04: Dijkstra Shortest Path Min-Heap
- **TC-04.1:** Multi-path shortest route selection.
  - *Path A:* 1->2->4 (30 mins). *Path B:* 1->3->4 (27 mins). *Path C:* 1->4 (45 mins).
  - *Actual:* `[PASS]` Dijkstra finds reachable path.
- **TC-04.2:** Optimal travel time.
  - *Expected:* 27 mins. *Actual:* `[PASS]` Dijkstra computes optimal time (27 mins).
- **TC-04.3:** Hop-by-hop path reconstruction.
  - *Expected:* `[1, 3, 4]`. *Actual:* `[PASS]` Dijkstra reconstructs exact route [1, 3, 4].

### TS-05: ResourceStock Non-Negative Invariant
- **TC-05.1:** Valid inventory reservation.
  - *Input:* Stock 100 food, 50 med. Reserve 60 food, 30 med.
  - *Actual:* `[PASS]` Valid reservation succeeds.
- **TC-05.2:** Deduction verification.
  - *Actual:* `[PASS]` Food kits deducted properly (40 remaining).
  - *Actual:* `[PASS]` Medicine kits deducted properly (20 remaining).
- **TC-05.3:** Over-reservation rejection.
  - *Input:* Attempt to reserve 50 food when only 40 remain.
  - *Actual:* `[PASS]` Over-reservation is rejected.
- **TC-05.4:** State unchanged after rejected reservation.
  - *Actual:* `[PASS]` Stock unchanged after rejected reservation (remains 40).
- **TC-05.5:** Negative addition rejection.
  - *Input:* `addStock(-10, 0)`.
  - *Actual:* `[PASS]` Negative stock addition rejected with exception.

### TS-06: Shelter Capacity & Overbooking Protection
- **TC-06.1:** Available capacity calculation.
  - *Input:* Capacity 100, Reserved 80.
  - *Actual:* `[PASS]` Available places correctly calculated (20).
- **TC-06.2:** Partial reservation.
  - *Input:* Reserve 15 places.
  - *Actual:* `[PASS]` Reserving 15 places succeeds.
  - *Actual:* `[PASS]` Remaining available places is 5.
- **TC-06.3:** Overbooking prevention.
  - *Input:* Attempt to reserve 10 places when only 5 are free.
  - *Actual:* `[PASS]` Overbooking attempt rejected.
  - *Actual:* `[PASS]` Reserved count remains 95.

### TS-07: RescueTeam Duplicate Assignment Protection
- **TC-07.1:** Standby availability.
  - *Actual:* `[PASS]` Team initially available.
- **TC-07.2:** Assignment lock.
  - *Action:* Reserve team for Request #101.
  - *Actual:* `[PASS]` First assignment succeeds.
  - *Actual:* `[PASS]` Team is now busy (`available == false`).
- **TC-07.3:** Conflicting second assignment refusal.
  - *Action:* Attempt to reserve busy team for Request #102.
  - *Actual:* `[PASS]` Conflicting assignment refused.
- **TC-07.4:** Safe release.
  - *Actual:* `[PASS]` Released team becomes available again.

### TS-08: OOP Polymorphism on ReliefRequest
- **TC-08.1:** Heterogeneous collection execution.
  - *Input:* `List<ReliefRequest>` holding both `SupplyRequest` and `RescueRequest`.
  - *Actual:* `[PASS]` Polymorphic getSummary() produces non-empty text.
  - *Actual:* `[PASS]` Polymorphic getSummary() produces non-empty text.
  - *Actual:* `[PASS]` Both request types evaluated polymorphically via `isFulfillable()`.

### TS-09: Hand-written JsonUtil Serialization & Parsing
- **TC-09.1:** Serialization.
  - *Input:* Map with string, integer, boolean.
  - *Actual:* `[PASS]` JsonUtil serializes correctly.
- **TC-09.2:** Recursive string parsing.
  - *Actual:* `[PASS]` JsonUtil parses string property.
  - *Actual:* `[PASS]` JsonUtil parses integer property.
  - *Actual:* `[PASS]` JsonUtil parses boolean property.

### TS-10: Service End-to-End Recommendation Engine
- **TC-10.1:** Queue initialization from MySQL seed data.
  - *Actual:* `[PASS]` Prioritized requests list is non-empty.
- **TC-10.2:** Reachable village recommendation (Request #1).
  - *Actual:* `[PASS]` Recommendation generated for Request #1.
  - *Actual:* `[PASS]` Request #1 is reachable.
  - *Actual:* `[PASS]` Depot recommended for Request #1.
- **TC-10.3:** Isolated village recommendation (Request #10).
  - *Actual:* `[PASS]` Isolated village detected for Request #10.
  - *Actual:* `[PASS]` Status recommendation is UNREACHABLE.

### TS-11: Data Persistence Across MySQL Reload
- **TC-11.1:** Save and reload.
  - *Action:* Add report #17 (pop 4321), call `loadAllFromDatabase()`.
  - *Actual:* `[PASS]` New report saved with generated ID.
  - *Actual:* `[PASS]` Report still present after full database reload.
  - *Actual:* `[PASS]` Population matches persisted value (4321).
  - *Actual:* `[PASS]` ReportedBy matches persisted value.

### TS-12: Atomic Database Transaction Rollback on Failure
- **TC-12.1:** Failure midway through transaction.
  - *Action:* Update depot stock inside uncommitted transaction, simulate failure, invoke `conn.rollback()`.
  - *Actual:* `[PASS]` Simulated failure caught and triggered rollback.
  - *Actual:* `[PASS]` MySQL depot food stock unchanged after rollback.
  - *Actual:* `[PASS]` MySQL depot med stock unchanged after rollback.

### TS-13: Duplicate Allocation Prevention
- **TC-13.1:** Multiple confirmation refusal.
  - *Action:* Confirm request first time -> Status `ALLOCATED`. Confirm same request second time.
  - *Actual:* `[PASS]` First confirmation succeeds.
  - *Actual:* `[PASS]` Request status is ALLOCATED.
  - *Actual:* `[PASS]` Second confirmation rejected with IllegalStateException.

### TS-14: Input Validation Rules (Boundary & Error Cases)
- **TC-14.1:** Severity boundaries.
  - *Actual:* `[PASS]` Severity 0 rejected.
  - *Actual:* `[PASS]` Severity 6 rejected.
  - *Actual:* `[PASS]` Severity 1 accepted.
  - *Actual:* `[PASS]` Severity 5 accepted.
- **TC-14.2:** Population boundaries.
  - *Actual:* `[PASS]` Population 0 rejected.
  - *Actual:* `[PASS]` Population -10 rejected.
- **TC-14.3:** Travel time boundaries.
  - *Actual:* `[PASS]` Travel time 0 rejected.
  - *Actual:* `[PASS]` Negative travel time rejected.
- **TC-14.4:** Quantities and strings.
  - *Actual:* `[PASS]` Negative stock quantity rejected.
  - *Actual:* `[PASS]` Empty string rejected.
  - *Actual:* `[PASS]` Null string rejected.
- **TC-14.5:** Unknown entity IDs.
  - *Actual:* `[PASS]` Non-existent region ID returns null.
  - *Actual:* `[PASS]` Non-existent depot ID returns null.

### TS-15: Partial Allocation with Outstanding Quantities
- **TC-15.1:** Excess demand handling.
  - *Input:* Request for 750 food, 475 med. Depot has 600 food, 400 med.
  - *Actual:* `[PASS]` Partial recommendation canAllocate is true.
  - *Actual:* `[PASS]` Recommended status is PARTIALLY_ALLOCATED.
  - *Actual:* `[PASS]` Recommended food matches available stock (600).
  - *Actual:* `[PASS]` Recommended med matches available stock (400).
  - *Actual:* `[PASS]` Outstanding food is 150.
  - *Actual:* `[PASS]` Outstanding med is 75.

### TS-16: Full Shelter & Unavailable Rescue Team
- **TC-16.1:** Resource exhaustion detection.
  - *Input:* Rescue request when all teams deployed and all shelters at 100% capacity.
  - *Actual:* `[PASS]` Rescue cannot allocate when teams busy and shelters full.
  - *Actual:* `[PASS]` No team recommended.
  - *Actual:* `[PASS]` 0 shelter places recommended.

### TS-17: Officer Rejection Changes Nothing
- **TC-17.1:** Non-destructive rejection.
  - *Action:* Officer rejects request with reason "Budget constraint review".
  - *Actual:* `[PASS]` Request marked as REJECTED.
  - *Actual:* `[PASS]` Rejection reason stored.
  - *Actual:* `[PASS]` Depot food stock identical after rejection.
  - *Actual:* `[PASS]` Depot med stock identical after rejection.

### TS-18: Automatic Re-prioritization On State Changes
- **TC-18.1:** Severity update re-prioritization.
  - *Action:* Low priority report upgraded to severity 5, pop 20000.
  - *Actual:* `[PASS]` Mutated request becomes #1 head of prioritized queue.
- **TC-18.2:** Road closure reachability update.
  - *Actual:* `[PASS]` Road status toggled to closed.
  - *Actual:* `[PASS]` Road reopened and network recalculated.
- **TC-18.3:** Inventory restock update.
  - *Actual:* `[PASS]` Depot stock increased by 50.

### TS-19: Performance Benchmarking
- **TC-19.1:** Seed data speed threshold.
  - *Actual:* `[PASS]` Benchmark on seed data completed in < 100 ms.
- **TC-19.2:** 200-node graph speed threshold.
  - *Actual:* `[PASS]` Benchmark on 200-node graph completed in < 500 ms.
