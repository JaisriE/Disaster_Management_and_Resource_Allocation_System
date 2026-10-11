/**
 * app.js - Application Controller for Aruvi District Emergency Operations Console
 * Handles REST API synchronization, deterministic priority queues,
 * algorithmic routing recommendations, and transactional officer decisions.
 * Designed for flat, utilitarian dispatch software without emojis.
 */

const App = (function () {
    let regions = [];
    let depots = [];
    let shelters = [];
    let teams = [];
    let roads = [];
    let requests = [];
    let prioritizedRequests = [];
    let currentRecommendation = null;

    // Toast Alert Helper (Utilitarian plain text, no emojis)
    function showToast(message, type = "info") {
        const container = document.getElementById("toast-container");
        if (!container) return;

        const tag = (type === 'success') ? '[OK]' : (type === 'error') ? '[ERROR]' : '[INFO]';
        const toast = document.createElement("div");
        toast.className = `toast ${type}`;
        toast.innerHTML = `
            <span style="font-weight: 700;">${tag}</span>
            <div>${message}</div>
        `;
        container.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            setTimeout(() => toast.remove(), 250);
        }, 4000);
    }

    // Tab Switching
    function initTabs() {
        document.querySelectorAll(".tab-btn").forEach(btn => {
            btn.addEventListener("click", () => {
                if (!btn.dataset.tab) return;

                document.querySelectorAll(".tab-btn").forEach(b => b.classList.remove("active"));
                document.querySelectorAll(".tab-pane").forEach(p => p.classList.remove("active"));

                btn.classList.add("active");
                const targetPane = document.getElementById(btn.dataset.tab);
                if (targetPane) targetPane.classList.add("active");

                // Scroll to target element if specified (e.g. Queue)
                if (btn.dataset.scroll) {
                    const scrollTarget = document.getElementById(btn.dataset.scroll);
                    if (scrollTarget) {
                        scrollTarget.scrollIntoView({ behavior: "smooth", block: "start" });
                    }
                }

                // Redraw map schematic if switching to overview
                if (btn.dataset.tab === "pane-overview") {
                    MapTopology.setRoads(roads);
                }
            });
        });
    }

    // Data Loaders
    async function refreshAll() {
        await Promise.all([
            loadRegions(),
            loadDepots(),
            loadShelters(),
            loadTeams(),
            loadRoads(),
            loadRequests(),
            loadAllocations(),
            loadAudits()
        ]);
        updateKpiMetrics();
    }

    async function loadRegions() {
        try {
            const res = await fetch("/api/regions");
            regions = await res.json();
            populateRegionSelects();
        } catch (e) {
            console.error("Error loading regions", e);
        }
    }

    async function loadDepots() {
        try {
            const res = await fetch("/api/depots");
            depots = await res.json();
            renderDepotsTable();
        } catch (e) {
            console.error("Error loading depots", e);
        }
    }

    async function loadShelters() {
        try {
            const res = await fetch("/api/shelters");
            shelters = await res.json();
            renderSheltersTable();
        } catch (e) {
            console.error("Error loading shelters", e);
        }
    }

    async function loadTeams() {
        try {
            const res = await fetch("/api/teams");
            teams = await res.json();
            renderTeamsTable();
        } catch (e) {
            console.error("Error loading teams", e);
        }
    }

    async function loadRoads() {
        try {
            const res = await fetch("/api/roads");
            roads = await res.json();
            MapTopology.setRoads(roads);
            renderRoadsTable();
        } catch (e) {
            console.error("Error loading roads", e);
        }
    }

    async function loadRequests() {
        try {
            const [allRes, prioRes] = await Promise.all([
                fetch("/api/requests"),
                fetch("/api/requests/prioritized")
            ]);
            requests = await allRes.json();
            prioritizedRequests = await prioRes.json();

            renderPriorityQueueTable();
            renderRequestsDesk();
        } catch (e) {
            console.error("Error loading requests", e);
        }
    }

    async function loadAllocations() {
        try {
            const res = await fetch("/api/allocations");
            const data = await res.json();
            renderAllocationsTable(data);
        } catch (e) {
            console.error("Error loading allocations", e);
        }
    }

    async function loadAudits() {
        try {
            const res = await fetch("/api/audits");
            const data = await res.json();
            renderAuditTable(data);
        } catch (e) {
            console.error("Error loading audits", e);
        }
    }

    function updateKpiMetrics() {
        // Pending queue count
        const pendingBadge = document.getElementById("kpi-pending");
        if (pendingBadge) pendingBadge.textContent = prioritizedRequests.length;

        const tabPendingBadge = document.getElementById("badge-queue-count");
        if (tabPendingBadge) tabPendingBadge.textContent = prioritizedRequests.length;

        // Stock totals across depots
        let totalFood = 0;
        let totalMed = 0;
        depots.forEach(d => {
            totalFood += d.foodKits;
            totalMed += d.medicineKits;
        });
        const foodEl = document.getElementById("kpi-food");
        if (foodEl) foodEl.textContent = totalFood.toLocaleString();
        const medEl = document.getElementById("kpi-med");
        if (medEl) medEl.textContent = totalMed.toLocaleString();

        // Standby rescue teams count
        const availTeams = teams.filter(t => t.available).length;
        const teamsEl = document.getElementById("kpi-teams");
        if (teamsEl) teamsEl.textContent = `${availTeams} / ${teams.length}`;

        // Shelter occupancy
        let totalCap = 0;
        let totalRes = 0;
        shelters.forEach(s => {
            totalCap += s.capacity;
            totalRes += s.reservedPlaces;
        });
        const shelterEl = document.getElementById("kpi-shelter");
        if (shelterEl) {
            const pct = totalCap > 0 ? Math.round((totalRes / totalCap) * 100) : 0;
            shelterEl.textContent = `${totalRes} / ${totalCap} [${pct}%]`;
        }
    }

    function populateRegionSelects() {
        const selects = document.querySelectorAll(".region-select");
        selects.forEach(select => {
            select.innerHTML = '<option value="">-- SELECT AFFECTED REGION --</option>';
            regions.forEach(r => {
                const opt = document.createElement("option");
                opt.value = r.id;
                opt.textContent = `R${r.id}: ${r.name.toUpperCase()} (POP: ${r.population.toLocaleString()})`;
                select.appendChild(opt);
            });
        });
    }

    // Render Compact Priority Queue Table
    function renderPriorityQueueTable() {
        const tbody = document.getElementById("priority-queue-table-body");
        if (!tbody) return;

        if (prioritizedRequests.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" style="text-align: center; padding: 24px; color: var(--text-dim); font-family: var(--font-mono);">
                        [QUEUE EMPTY - ALL INCIDENTS RESOLVED OR CONFIRMED]
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = '';
        prioritizedRequests.forEach((req, idx) => {
            const isRank1 = (idx === 0);
            const tr = document.createElement("tr");
            if (isRank1) tr.className = "row-rank-1";

            const sevMarker = (req.severity >= 4) ? 'marker-red' : (req.severity === 3) ? 'marker-amber' : 'marker-slate';
            const statusMarker = (req.status === 'ALLOCATED') ? 'marker-green' : (req.status === 'UNREACHABLE') ? 'marker-red' : (req.status === 'PARTIALLY_ALLOCATED') ? 'marker-amber' : 'marker-slate';

            tr.innerHTML = `
                <td class="id-cell" style="font-weight: 700; ${isRank1 ? 'color: var(--accent-red);' : ''}">#${idx + 1}</td>
                <td class="id-cell">#${req.id}</td>
                <td><strong>${req.regionName}</strong></td>
                <td>
                    <span class="sev-cell">
                        <span class="marker ${sevMarker}"></span>
                        <span>SEV ${req.severity}</span>
                    </span>
                </td>
                <td class="num-cell">${req.affectedPopulation.toLocaleString()}</td>
                <td>
                    <span class="status-indicator">
                        <span class="marker ${statusMarker}"></span>
                        <span>${req.status}</span>
                    </span>
                </td>
                <td style="text-align: right;">
                    <button class="btn btn-primary btn-sm" onclick="App.openRecommendationModal(${req.id})">
                        REVIEW
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    }

    // Render Incident Reports Register Table
    function renderRequestsDesk() {
        const tbody = document.getElementById("requests-table-body");
        if (!tbody) return;

        tbody.innerHTML = '';
        requests.forEach(req => {
            const tr = document.createElement("tr");
            const needs = req.requestType === 'SUPPLY'
                ? `Food: ${req.requestedFood} (Out: ${req.outstandingFood}) | Med: ${req.requestedMedicine} (Out: ${req.outstandingMedicine})`
                : `Teams: ${req.requestedTeams} | Shelter: ${req.requestedShelterPlaces} (Out: ${req.outstandingShelterPlaces})`;

            const sevMarker = (req.severity >= 4) ? 'marker-red' : (req.severity === 3) ? 'marker-amber' : 'marker-slate';
            const statusMarker = (req.status === 'ALLOCATED') ? 'marker-green' : (req.status === 'UNREACHABLE' || req.status === 'REJECTED') ? 'marker-red' : (req.status === 'PARTIALLY_ALLOCATED') ? 'marker-amber' : 'marker-slate';

            tr.innerHTML = `
                <td class="id-cell">#${req.id}</td>
                <td><strong>${req.regionName}</strong></td>
                <td>
                    <span class="sev-cell">
                        <span class="marker ${sevMarker}"></span>
                        <span>${req.severity}</span>
                    </span>
                </td>
                <td class="num-cell">${req.affectedPopulation.toLocaleString()}</td>
                <td style="font-family: var(--font-mono); font-weight: 700; color: ${req.requestType === 'SUPPLY' ? 'var(--accent-slate)' : 'var(--accent-amber)'}">${req.requestType}</td>
                <td style="font-family: var(--font-mono); font-size: 11px;">${needs}</td>
                <td class="num-cell">#${req.arrivalOrder}</td>
                <td>
                    <span class="status-indicator">
                        <span class="marker ${statusMarker}"></span>
                        <span>${req.status}</span>
                    </span>
                </td>
                <td style="text-align: right;">
                    <div style="display: inline-flex; gap: 4px;">
                        ${req.status === 'PENDING' || req.status === 'PARTIALLY_ALLOCATED' || req.status === 'UNREACHABLE' ? `
                            <button class="btn btn-primary btn-sm" onclick="App.openRecommendationModal(${req.id})">REVIEW</button>
                        ` : ''}
                        <button class="btn btn-outline btn-sm" onclick="App.openEditRequestModal(${req.id})">EDIT</button>
                        ${req.status !== 'CANCELLED' && req.status !== 'ALLOCATED' ? `
                            <button class="btn btn-outline-red btn-sm" onclick="App.cancelRequest(${req.id})">CANCEL</button>
                        ` : ''}
                    </div>
                </td>
            `;
            tbody.appendChild(tr);
        });
    }

    // Render Depots Table
    function renderDepotsTable() {
        const tbody = document.getElementById("depots-table-body");
        if (!tbody) return;

        tbody.innerHTML = '';
        depots.forEach(d => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td class="id-cell">#DEPOT-${d.id}</td>
                <td><strong>${d.name}</strong></td>
                <td>Region R${d.regionId}</td>
                <td class="num-cell" style="font-weight: 700;">${d.foodKits.toLocaleString()}</td>
                <td class="num-cell" style="font-weight: 700;">${d.medicineKits.toLocaleString()}</td>
                <td style="text-align: right;">
                    <button class="btn btn-outline btn-sm" onclick="App.openStockModal(${d.id}, '${d.name}', ${d.foodKits}, ${d.medicineKits})">
                        [UPDATE STOCK]
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    }

    // Render Teams Table
    function renderTeamsTable() {
        const tbody = document.getElementById("teams-table-body");
        if (!tbody) return;

        tbody.innerHTML = '';
        teams.forEach(t => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td class="id-cell">#TEAM-${t.id}</td>
                <td><strong>${t.name}</strong></td>
                <td>${t.homeDepotName}</td>
                <td style="font-family: var(--font-mono);">${t.currentRequestId ? 'REQUEST #' + t.currentRequestId : 'STANDBY'}</td>
                <td>
                    <span class="status-indicator">
                        <span class="marker ${t.available ? 'marker-green' : 'marker-red'}"></span>
                        <span>${t.available ? 'AVAILABLE' : 'DEPLOYED / BUSY'}</span>
                    </span>
                </td>
                <td style="text-align: right;">
                    <button class="btn ${t.available ? 'btn-outline-red' : 'btn-outline-green'} btn-sm" onclick="App.toggleTeamAvailability(${t.id}, ${!t.available})">
                        ${t.available ? '[MARK AS DEPLOYED]' : '[MARK AS STANDBY]'}
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    }

    // Render Shelters Table
    function renderSheltersTable() {
        const tbody = document.getElementById("shelters-table-body");
        if (!tbody) return;

        tbody.innerHTML = '';
        shelters.forEach(s => {
            const pct = Math.round((s.reservedPlaces / s.capacity) * 100);
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td class="id-cell">#SHELTER-${s.id}</td>
                <td><strong>${s.name}</strong></td>
                <td>Region R${s.regionId}</td>
                <td class="num-cell">${s.capacity.toLocaleString()}</td>
                <td class="num-cell">${s.reservedPlaces.toLocaleString()}</td>
                <td class="num-cell" style="font-weight: 700;">${s.availablePlaces.toLocaleString()}</td>
                <td class="num-cell" style="font-weight: 700; ${pct > 80 ? 'color: var(--accent-red);' : ''}">${pct}%</td>
                <td style="text-align: right;">
                    <button class="btn btn-outline btn-sm" onclick="App.openShelterModal(${s.id}, '${s.name}', ${s.capacity})">
                        [MODIFY CAPACITY]
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    }

    // Render Roads Table
    function renderRoadsTable() {
        const tbody = document.getElementById("roads-table-body");
        if (!tbody) return;

        tbody.innerHTML = '';
        roads.forEach(r => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td class="id-cell">#ROAD-${r.id}</td>
                <td><strong>${r.fromRegionName}</strong> (R${r.fromRegionId})</td>
                <td><strong>${r.toRegionName}</strong> (R${r.toRegionId})</td>
                <td class="num-cell">${r.travelTime} min</td>
                <td>
                    <span class="status-indicator">
                        <span class="marker ${r.open ? 'marker-green' : 'marker-red'}"></span>
                        <span>${r.open ? 'OPEN' : 'BLOCKED / CLOSED'}</span>
                    </span>
                </td>
                <td style="font-size: 11px; color: var(--text-muted);">${r.description || '-'}</td>
                <td style="text-align: right;">
                    <button class="btn ${r.open ? 'btn-outline-red' : 'btn-outline-green'} btn-sm" onclick="App.toggleRoadStatus(${r.id}, ${!r.open})">
                        ${r.open ? '[CLOSE ROAD]' : '[REOPEN ROAD]'}
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    }

    // Render Historical Allocations Table
    function renderAllocationsTable(allocations) {
        const tbody = document.getElementById("allocations-table-body");
        if (!tbody) return;

        tbody.innerHTML = '';
        if (!allocations || allocations.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: var(--text-dim); padding: 16px; font-family: var(--font-mono);">[NO CONFIRMED ALLOCATIONS COMMITTED]</td></tr>';
            return;
        }

        allocations.forEach(a => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td class="id-cell">#AL-${a.id}</td>
                <td class="id-cell">#REQ-${a.requestId}</td>
                <td>${a.depotName || a.rescueTeamName || a.shelterName || '-'}</td>
                <td style="font-family: var(--font-mono);">Food: ${a.foodAllocated} | Med: ${a.medicineAllocated} | Shelter: ${a.shelterPlacesAllocated}</td>
                <td class="num-cell">${a.travelTime} min</td>
                <td style="font-family: var(--font-mono); font-size: 11px;">${a.routePath}</td>
                <td style="font-family: var(--font-mono); font-weight: 700;">${a.officerName}</td>
                <td style="font-family: var(--font-mono); font-size: 11px; color: var(--text-dim);">${a.confirmedAt}</td>
            `;
            tbody.appendChild(tr);
        });
    }

    // Render Operational Audit Table
    function renderAuditTable(audits) {
        const tbody = document.getElementById("audit-table-body");
        if (!tbody) return;

        tbody.innerHTML = '';
        if (!audits || audits.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--text-dim); padding: 16px; font-family: var(--font-mono);">[NO AUDIT ENTRIES FOUND]</td></tr>';
            return;
        }

        audits.forEach(l => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td class="id-cell">#LOG-${l.id}</td>
                <td style="font-family: var(--font-mono); font-weight: 700;">${l.actionType}</td>
                <td>${l.description}</td>
                <td style="font-family: var(--font-mono);">${l.performedBy}</td>
                <td style="font-family: var(--font-mono); font-size: 11px; color: var(--text-dim);">${l.createdAt}</td>
            `;
            tbody.appendChild(tr);
        });
    }

    // ==========================================
    // RECOMMENDATION & OFFICER REVIEW MODAL
    // ==========================================

    function openTopRecommendation() {
        if (prioritizedRequests && prioritizedRequests.length > 0) {
            openRecommendationModal(prioritizedRequests[0].id);
        } else {
            showToast("No pending emergency reports in priority queue to review.", "info");
        }
    }

    async function openRecommendationModal(requestId) {
        try {
            const res = await fetch(`/api/recommendation?requestId=${requestId}`);
            if (!res.ok) {
                const err = await res.json();
                showToast(err.error || "Failed to generate recommendation", "error");
                return;
            }
            const rec = await res.json();
            currentRecommendation = rec;

            // Highlight path in topology schematic
            if (rec.pathRegionIds && rec.pathRegionIds.length > 0) {
                MapTopology.highlightRoute(rec.pathRegionIds);
            } else {
                MapTopology.clearHighlight();
            }

            document.getElementById("rec-modal-title").textContent =
                `Algorithmic Allocation Advice for Request #${rec.requestId}`;
            document.getElementById("rec-region-name").textContent =
                `TARGET: R${rec.regionId} - ${rec.regionName.toUpperCase()} [${rec.requestType}]`;

            // Plain route line
            const routeBanner = document.getElementById("rec-route-banner");
            if (rec.reachable) {
                routeBanner.className = "rec-route-line";
                routeBanner.innerHTML = `
                    <div><strong>DIJKSTRA OPTIMAL ROUTE:</strong> ${rec.routeSummary}</div>
                    <div style="font-size: 11px; color: var(--text-muted); margin-top: 2px;">ESTIMATED TRAVEL TIME: ${rec.travelTimeMinutes} MINUTES</div>
                `;
            } else {
                routeBanner.className = "rec-route-line route-severed";
                routeBanner.innerHTML = `
                    <div><strong>ROUTE SEVERED:</strong> NO PASSABLE ROADS CONNECT TO ${rec.regionName.toUpperCase()}</div>
                `;
            }

            // Side-by-side Request and Recommended Allocation Layout
            const statsContainer = document.getElementById("rec-stats-container");
            if (rec.requestType === 'SUPPLY') {
                statsContainer.innerHTML = `
                    <div class="rec-block">
                        <div class="rec-block-title">Incident Demand</div>
                        <table class="rec-quantities-table">
                            <thead>
                                <tr>
                                    <th>Resource</th>
                                    <th style="text-align: right;">Requested</th>
                                    <th style="text-align: right;">Outstanding</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>Food Survival Kits</td>
                                    <td style="text-align: right;">${rec.requestedFood || (rec.foodRecommended + rec.outstandingFood)}</td>
                                    <td style="text-align: right; color: var(--accent-red); font-weight: 700;">${rec.outstandingFood}</td>
                                </tr>
                                <tr>
                                    <td>Medicine Trauma Kits</td>
                                    <td style="text-align: right;">${rec.requestedMedicine || (rec.medicineRecommended + rec.outstandingMedicine)}</td>
                                    <td style="text-align: right; color: var(--accent-red); font-weight: 700;">${rec.outstandingMedicine}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <div class="rec-block">
                        <div class="rec-block-title">Recommended Dispatch</div>
                        <div style="font-family: var(--font-mono); font-size: 11px; margin-bottom: 8px;">
                            SOURCE: <strong>${rec.recommendedDepotName || 'NONE AVAILABLE'}</strong>
                        </div>
                        <table class="rec-quantities-table">
                            <thead>
                                <tr>
                                    <th>Item</th>
                                    <th style="text-align: right;">Allocation</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>Food Kits Recommended</td>
                                    <td style="text-align: right; font-weight: 700; color: var(--accent-green);">${rec.foodRecommended}</td>
                                </tr>
                                <tr>
                                    <td>Medicine Kits Recommended</td>
                                    <td style="text-align: right; font-weight: 700; color: var(--accent-green);">${rec.medicineRecommended}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                `;
            } else {
                statsContainer.innerHTML = `
                    <div class="rec-block">
                        <div class="rec-block-title">Rescue Incident Demand</div>
                        <table class="rec-quantities-table">
                            <thead>
                                <tr>
                                    <th>Requirement</th>
                                    <th style="text-align: right;">Requested</th>
                                    <th style="text-align: right;">Outstanding</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>Rescue Teams</td>
                                    <td style="text-align: right;">${rec.requestedTeams || (rec.recommendedTeamId ? 1 : 0)}</td>
                                    <td style="text-align: right;">${rec.outstandingTeams || 0}</td>
                                </tr>
                                <tr>
                                    <td>Shelter Places</td>
                                    <td style="text-align: right;">${(rec.shelterPlacesRecommended + rec.outstandingShelterPlaces)}</td>
                                    <td style="text-align: right; color: var(--accent-red); font-weight: 700;">${rec.outstandingShelterPlaces}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <div class="rec-block">
                        <div class="rec-block-title">Recommended Dispatch</div>
                        <div style="font-family: var(--font-mono); font-size: 11px; margin-bottom: 4px;">
                            TEAM: <strong>${rec.recommendedTeamName || 'NONE AVAILABLE'}</strong>
                        </div>
                        <div style="font-family: var(--font-mono); font-size: 11px; margin-bottom: 8px;">
                            SHELTER: <strong>${rec.recommendedShelterName || 'NONE AVAILABLE'}</strong>
                        </div>
                        <table class="rec-quantities-table">
                            <thead>
                                <tr>
                                    <th>Item</th>
                                    <th style="text-align: right;">Allocation</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>Shelter Places Reserved</td>
                                    <td style="text-align: right; font-weight: 700; color: var(--accent-green);">${rec.shelterPlacesRecommended}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                `;
            }

            document.getElementById("rec-explanation").textContent = rec.explanation;

            const confirmBtn = document.getElementById("btn-confirm-allocation");
            if (rec.canAllocate) {
                confirmBtn.disabled = false;
            } else {
                confirmBtn.disabled = true;
            }

            openModal("modal-recommendation");
        } catch (e) {
            console.error("Error opening recommendation modal", e);
            showToast("Network error getting recommendation", "error");
        }
    }

    async function confirmAllocation() {
        if (!currentRecommendation) return;
        const officerInput = document.getElementById("officer-name-input");
        const officerName = officerInput.value.trim() || "Command Officer";

        try {
            const res = await fetch("/api/allocation/confirm", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    requestId: currentRecommendation.requestId,
                    officerName: officerName
                })
            });
            const data = await res.json();
            if (data.success) {
                showToast(`Allocation confirmed and committed by ${officerName}.`, "success");
                closeModal("modal-recommendation");
                MapTopology.clearHighlight();
                await refreshAll();
            } else {
                showToast(data.error || "Failed to confirm allocation", "error");
            }
        } catch (e) {
            showToast("Network error confirming allocation", "error");
        }
    }

    async function rejectAllocation() {
        if (!currentRecommendation) return;
        const reason = prompt("Enter mandatory rejection reason:", "Insufficient priority / Alternative routing");
        if (!reason || reason.trim() === "") {
            showToast("Rejection cancelled: Reason is mandatory.", "info");
            return;
        }

        const officerInput = document.getElementById("officer-name-input");
        const officerName = officerInput.value.trim() || "Command Officer";

        try {
            const res = await fetch("/api/allocation/reject", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    requestId: currentRecommendation.requestId,
                    reason: reason.trim(),
                    officerName: officerName
                })
            });
            const data = await res.json();
            if (data.success) {
                showToast(`Request #${currentRecommendation.requestId} rejected by ${officerName}.`, "info");
                closeModal("modal-recommendation");
                MapTopology.clearHighlight();
                await refreshAll();
            } else {
                showToast(data.error || "Failed to reject", "error");
            }
        } catch (e) {
            showToast("Network error rejecting request", "error");
        }
    }

    // ==========================================
    // MANUAL OPERATIONS
    // ==========================================

    async function submitNewRequest(event) {
        event.preventDefault();
        const regionId = parseInt(document.getElementById("new-req-region").value);
        const severity = parseInt(document.getElementById("new-req-severity").value);
        const population = parseInt(document.getElementById("new-req-population").value);
        const type = document.getElementById("new-req-type").value;
        const officer = document.getElementById("new-req-officer").value;

        const food = parseInt(document.getElementById("new-req-food").value || 0);
        const med = parseInt(document.getElementById("new-req-med").value || 0);
        const teamsVal = parseInt(document.getElementById("new-req-teams").value || 0);
        const shelter = parseInt(document.getElementById("new-req-shelter").value || 0);

        const payload = {
            regionId, severity, population, type, reportedBy: officer,
            requestedFood: food, requestedMedicine: med,
            requestedTeams: teamsVal, requestedShelterPlaces: shelter
        };

        try {
            const res = await fetch("/api/requests/create", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            const data = await res.json();
            if (data.success) {
                showToast(`Incident report #${data.id} logged and prioritized.`, "success");
                closeModal("modal-new-request");
                document.getElementById("form-new-request").reset();
                await refreshAll();
            } else {
                showToast(data.error || "Validation error", "error");
            }
        } catch (e) {
            showToast("Network error creating report", "error");
        }
    }

    function openEditRequestModal(reqId) {
        const req = requests.find(r => r.id === reqId);
        if (!req) return;

        document.getElementById("edit-req-id").value = req.id;
        document.getElementById("edit-req-severity").value = req.severity;
        document.getElementById("edit-req-population").value = req.affectedPopulation;

        const isSupply = req.requestType === 'SUPPLY';
        document.getElementById("edit-supply-fields").style.display = isSupply ? 'grid' : 'none';
        document.getElementById("edit-rescue-fields").style.display = isSupply ? 'none' : 'grid';

        if (isSupply) {
            document.getElementById("edit-req-food").value = req.requestedFood;
            document.getElementById("edit-req-med").value = req.requestedMedicine;
        } else {
            document.getElementById("edit-req-teams").value = req.requestedTeams;
            document.getElementById("edit-req-shelter").value = req.requestedShelterPlaces;
        }

        openModal("modal-edit-request");
    }

    async function submitEditRequest(event) {
        event.preventDefault();
        const id = parseInt(document.getElementById("edit-req-id").value);
        const severity = parseInt(document.getElementById("edit-req-severity").value);
        const population = parseInt(document.getElementById("edit-req-population").value);
        const food = parseInt(document.getElementById("edit-req-food").value || 0);
        const med = parseInt(document.getElementById("edit-req-med").value || 0);
        const teamsVal = parseInt(document.getElementById("edit-req-teams").value || 0);
        const shelter = parseInt(document.getElementById("edit-req-shelter").value || 0);

        try {
            const res = await fetch("/api/requests/update", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ id, severity, population, requestedFood: food, requestedMedicine: med, requestedTeams: teamsVal, requestedShelterPlaces: shelter })
            });
            const data = await res.json();
            if (data.success) {
                showToast(`Request #${id} updated and re-prioritized.`, "success");
                closeModal("modal-edit-request");
                await refreshAll();
            } else {
                showToast(data.error || "Update error", "error");
            }
        } catch (e) {
            showToast("Network error updating request", "error");
        }
    }

    async function cancelRequest(id) {
        if (!confirm(`Confirm cancellation of emergency request #${id}?`)) return;
        try {
            const res = await fetch("/api/requests/cancel", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ id, officer: "Command Officer" })
            });
            const data = await res.json();
            if (data.success) {
                showToast(`Request #${id} marked as cancelled.`, "info");
                await refreshAll();
            } else {
                showToast(data.error || "Cancellation error", "error");
            }
        } catch (e) {
            showToast("Network error cancelling request", "error");
        }
    }

    function openStockModal(depotId, name, food, med) {
        document.getElementById("stock-depot-id").value = depotId;
        document.getElementById("stock-depot-title").textContent = `Stock Management: ${name.toUpperCase()}`;
        document.getElementById("stock-food").value = food;
        document.getElementById("stock-med").value = med;
        openModal("modal-stock");
    }

    async function submitStockUpdate(event) {
        event.preventDefault();
        const depotId = parseInt(document.getElementById("stock-depot-id").value);
        const food = parseInt(document.getElementById("stock-food").value);
        const med = parseInt(document.getElementById("stock-med").value);
        const isAdd = document.getElementById("stock-is-add").checked;

        try {
            const res = await fetch("/api/depots/stock", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ depotId, foodKits: food, medicineKits: med, isAdd })
            });
            const data = await res.json();
            if (data.success) {
                showToast("Depot stock updated successfully.", "success");
                closeModal("modal-stock");
                await refreshAll();
            } else {
                showToast(data.error || "Stock update error", "error");
            }
        } catch (e) {
            showToast("Network error updating stock", "error");
        }
    }

    async function toggleTeamAvailability(teamId, newAvailable) {
        try {
            const res = await fetch("/api/teams/availability", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ teamId, available: newAvailable })
            });
            const data = await res.json();
            if (data.success) {
                showToast("Team availability status updated.", "success");
                await refreshAll();
            } else {
                showToast(data.error || "Team update error", "error");
            }
        } catch (e) {
            showToast("Network error updating team", "error");
        }
    }

    function openShelterModal(shelterId, name, capacity) {
        document.getElementById("shelter-id").value = shelterId;
        document.getElementById("shelter-title").textContent = `Modify Shelter Capacity: ${name.toUpperCase()}`;
        document.getElementById("shelter-capacity").value = capacity;
        openModal("modal-shelter");
    }

    async function submitShelterUpdate(event) {
        event.preventDefault();
        const shelterId = parseInt(document.getElementById("shelter-id").value);
        const capacity = parseInt(document.getElementById("shelter-capacity").value);

        try {
            const res = await fetch("/api/shelters/capacity", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ shelterId, capacity })
            });
            const data = await res.json();
            if (data.success) {
                showToast("Shelter accommodation capacity updated.", "success");
                closeModal("modal-shelter");
                await refreshAll();
            } else {
                showToast(data.error || "Capacity error", "error");
            }
        } catch (e) {
            showToast("Network error updating shelter", "error");
        }
    }

    async function toggleRoadStatus(roadId, newOpen) {
        try {
            const res = await fetch("/api/roads/toggle", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ roadId, open: newOpen })
            });
            const data = await res.json();
            if (data.success) {
                showToast(`Road status changed to ${newOpen ? 'OPEN' : 'BLOCKED'}. Network recalculated.`, "info");
                await refreshAll();
            } else {
                showToast(data.error || "Road toggle error", "error");
            }
        } catch (e) {
            showToast("Network error toggling road", "error");
        }
    }

    async function submitAddRoad(event) {
        event.preventDefault();
        const from = parseInt(document.getElementById("new-road-from").value);
        const to = parseInt(document.getElementById("new-road-to").value);
        const travelTime = parseInt(document.getElementById("new-road-time").value);
        const open = document.getElementById("new-road-open").checked;
        const description = document.getElementById("new-road-desc").value;

        try {
            const res = await fetch("/api/roads/add", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ fromRegionId: from, toRegionId: to, travelTime, open, description })
            });
            const data = await res.json();
            if (data.success) {
                showToast("New road edge added to transport network.", "success");
                closeModal("modal-add-road");
                document.getElementById("form-add-road").reset();
                await refreshAll();
            } else {
                showToast(data.error || "Road creation error", "error");
            }
        } catch (e) {
            showToast("Network error adding road", "error");
        }
    }

    // Modal Helpers
    function openModal(id) {
        const el = document.getElementById(id);
        if (el) el.classList.add("open");
    }

    function closeModal(id) {
        const el = document.getElementById(id);
        if (el) el.classList.remove("open");
    }

    function initModals() {
        document.querySelectorAll(".modal-close").forEach(btn => {
            btn.addEventListener("click", () => {
                const overlay = btn.closest(".modal-overlay");
                if (overlay) overlay.classList.remove("open");
            });
        });

        document.querySelectorAll(".modal-overlay").forEach(overlay => {
            overlay.addEventListener("click", (e) => {
                if (e.target === overlay) overlay.classList.remove("open");
            });
        });
    }

    // Request Type Toggle
    function initRequestTypeToggle() {
        const typeSelect = document.getElementById("new-req-type");
        if (!typeSelect) return;

        typeSelect.addEventListener("change", () => {
            const val = typeSelect.value;
            const supplyRow = document.getElementById("new-supply-fields");
            const rescueRow = document.getElementById("new-rescue-fields");
            if (val === 'SUPPLY') {
                supplyRow.style.display = 'grid';
                rescueRow.style.display = 'none';
            } else {
                supplyRow.style.display = 'none';
                rescueRow.style.display = 'grid';
            }
        });
    }

    // Interactive Map Click Callbacks
    window.onRegionClicked = function (regionId, regionNode) {
        showToast(`Selected Region ${regionId}: ${regionNode.name}`, "info");
    };

    window.onRoadClicked = function (road) {
        toggleRoadStatus(road.id, !road.open);
    };

    // Public Initializer
    function init() {
        initTabs();
        initModals();
        initRequestTypeToggle();
        MapTopology.init("topology-map-svg");

        // Form listeners
        const formNew = document.getElementById("form-new-request");
        if (formNew) formNew.addEventListener("submit", submitNewRequest);

        const formEdit = document.getElementById("form-edit-request");
        if (formEdit) formEdit.addEventListener("submit", submitEditRequest);

        const formStock = document.getElementById("form-stock");
        if (formStock) formStock.addEventListener("submit", submitStockUpdate);

        const formShelter = document.getElementById("form-shelter");
        if (formShelter) formShelter.addEventListener("submit", submitShelterUpdate);

        const formAddRoad = document.getElementById("form-add-road");
        if (formAddRoad) formAddRoad.addEventListener("submit", submitAddRoad);

        const btnConfirm = document.getElementById("btn-confirm-allocation");
        if (btnConfirm) btnConfirm.addEventListener("click", confirmAllocation);

        const btnReject = document.getElementById("btn-reject-allocation");
        if (btnReject) btnReject.addEventListener("click", rejectAllocation);

        refreshAll();

        // Polling interval (10 seconds)
        setInterval(refreshAll, 10000);
    }

    async function resetDemoData() {
        if (!confirm("Confirm reset of all database tables and priority queues to initial demo seed state?")) return;
        try {
            showToast("Resetting demo database...", "info");
            const res = await fetch("/api/demo/reset", {
                method: "POST"
            });
            const data = await res.json();
            if (data.success) {
                showToast("Seed data restored successfully. All tables and queues reset.", "success");
                MapTopology.clearHighlight();
                await refreshAll();
            } else {
                showToast(data.error || "Reset failed", "error");
            }
        } catch (e) {
            showToast("Network error resetting data", "error");
        }
    }

    return {
        init,
        resetDemoData,
        openTopRecommendation,
        openRecommendationModal,
        openEditRequestModal,
        cancelRequest,
        openStockModal,
        openShelterModal,
        toggleTeamAvailability,
        toggleRoadStatus,
        openModal,
        closeModal
    };
})();

document.addEventListener("DOMContentLoaded", () => {
    App.init();
});
