/**
 * map.js - Schematic Transportation Network Map
 * Renders Aruvi District as a clean, utilitarian emergency dispatch schematic:
 * - Square nodes for depots, circles for villages
 * - Thin dark lines for open roads, dashed red lines with [X CLOSED] tags for blocked roads
 * - Thick red lines for algorithmic Dijkstra recommendations
 * - Clean monospace labels and neutral legend
 */

const MapTopology = (function () {
    const REGION_COORDS = {
        1: { x: 120, y: 180, name: "ARUVI TOWN (HQ)", isDepot: true, code: "D1" },
        2: { x: 260, y: 90,  name: "VELLIANGADU", isDepot: false, code: "R2" },
        3: { x: 340, y: 220, name: "THAMARAI KULAM", isDepot: false, code: "R3" },
        4: { x: 440, y: 140, name: "VELLALUR", isDepot: false, code: "R4" },
        5: { x: 530, y: 280, name: "SIRUVANI HILLS", isDepot: false, code: "R5" },
        6: { x: 280, y: 350, name: "PONNAIYANUR (STAGING)", isDepot: true, code: "D2" },
        7: { x: 620, y: 170, name: "KARAMADAI", isDepot: false, code: "R7" },
        8: { x: 480, y: 400, name: "BHAVANI BANK", isDepot: false, code: "R8" },
        9: { x: 740, y: 260, name: "MARUTHAM", isDepot: false, code: "R9" },
        10: { x: 760, y: 400, name: "SOLAIYUR (ISOLATED)", isDepot: false, code: "R10" }
    };

    let svgElement = null;
    let roadsData = [];
    let highlightedPath = [];

    function init(svgId) {
        svgElement = document.getElementById(svgId);
        if (!svgElement) return;
        renderMap();
    }

    function setRoads(roads) {
        roadsData = roads || [];
        renderMap();
    }

    function highlightRoute(pathRegionIds) {
        highlightedPath = pathRegionIds || [];
        renderMap();
    }

    function clearHighlight() {
        highlightedPath = [];
        renderMap();
    }

    function renderMap() {
        if (!svgElement) return;
        svgElement.innerHTML = '';

        // Roads Layer
        const roadsGroup = document.createElementNS("http://www.w3.org/2000/svg", "g");
        roadsGroup.setAttribute("class", "roads-layer");

        const renderedPairs = new Set();

        roadsData.forEach(road => {
            const fromId = road.fromRegionId;
            const toId = road.toRegionId;
            const pairKey = Math.min(fromId, toId) + "-" + Math.max(fromId, toId);

            if (renderedPairs.has(pairKey)) return;
            renderedPairs.add(pairKey);

            const c1 = REGION_COORDS[fromId];
            const c2 = REGION_COORDS[toId];
            if (!c1 || !c2) return;

            let isPathSegment = false;
            for (let i = 0; i < highlightedPath.length - 1; i++) {
                if ((highlightedPath[i] === fromId && highlightedPath[i+1] === toId) ||
                    (highlightedPath[i] === toId && highlightedPath[i+1] === fromId)) {
                    isPathSegment = true;
                    break;
                }
            }

            const line = document.createElementNS("http://www.w3.org/2000/svg", "line");
            line.setAttribute("x1", c1.x);
            line.setAttribute("y1", c1.y);
            line.setAttribute("x2", c2.x);
            line.setAttribute("y2", c2.y);

            if (isPathSegment) {
                line.setAttribute("stroke", "#b3261e"); // Deep urgency red for recommended path
                line.setAttribute("stroke-width", "4.5");
            } else if (road.open) {
                line.setAttribute("stroke", "#374151"); // Thin dark slate for passable road
                line.setAttribute("stroke-width", "1.75");
            } else {
                line.setAttribute("stroke", "#b3261e"); // Red dashed for closed road
                line.setAttribute("stroke-width", "2");
                line.setAttribute("stroke-dasharray", "5,4");
            }

            line.style.cursor = "pointer";
            line.addEventListener("click", () => {
                if (window.onRoadClicked) window.onRoadClicked(road);
            });

            roadsGroup.appendChild(line);

            // Travel Time Tag / X Tag in middle of line
            const midX = (c1.x + c2.x) / 2;
            const midY = (c1.y + c2.y) / 2;

            const badgeBg = document.createElementNS("http://www.w3.org/2000/svg", "rect");
            const tagWidth = road.open ? 30 : 48;
            badgeBg.setAttribute("x", midX - (tagWidth / 2));
            badgeBg.setAttribute("y", midY - 8);
            badgeBg.setAttribute("width", tagWidth);
            badgeBg.setAttribute("height", 16);
            badgeBg.setAttribute("rx", "0");

            if (isPathSegment) {
                badgeBg.setAttribute("fill", "#b3261e");
                badgeBg.setAttribute("stroke", "#1c1c1c");
            } else if (road.open) {
                badgeBg.setAttribute("fill", "#ffffff");
                badgeBg.setAttribute("stroke", "#1c1c1c");
            } else {
                badgeBg.setAttribute("fill", "#ffffff");
                badgeBg.setAttribute("stroke", "#b3261e");
            }
            badgeBg.setAttribute("stroke-width", "1");
            roadsGroup.appendChild(badgeBg);

            const text = document.createElementNS("http://www.w3.org/2000/svg", "text");
            text.setAttribute("x", midX);
            text.setAttribute("y", midY + 4);
            text.setAttribute("font-family", "Consolas, monospace");
            text.setAttribute("font-size", "9px");
            text.setAttribute("font-weight", "700");
            text.setAttribute("text-anchor", "middle");

            if (isPathSegment) {
                text.setAttribute("fill", "#ffffff");
                text.textContent = `${road.travelTime}m`;
            } else if (road.open) {
                text.setAttribute("fill", "#1c1c1c");
                text.textContent = `${road.travelTime}m`;
            } else {
                text.setAttribute("fill", "#b3261e");
                text.textContent = "X CLOSED";
            }
            roadsGroup.appendChild(text);
        });

        svgElement.appendChild(roadsGroup);

        // Nodes Layer
        const nodesGroup = document.createElementNS("http://www.w3.org/2000/svg", "g");
        nodesGroup.setAttribute("class", "nodes-layer");

        Object.keys(REGION_COORDS).forEach(idStr => {
            const id = parseInt(idStr);
            const node = REGION_COORDS[id];

            const isHighlighted = highlightedPath.includes(id);
            const isIsolated = (id === 10);

            if (node.isDepot) {
                // Square node for depots
                const rect = document.createElementNS("http://www.w3.org/2000/svg", "rect");
                rect.setAttribute("x", node.x - 11);
                rect.setAttribute("y", node.y - 11);
                rect.setAttribute("width", 22);
                rect.setAttribute("height", 22);
                rect.setAttribute("stroke", "#1c1c1c");
                rect.setAttribute("stroke-width", isHighlighted ? "3" : "1.5");
                rect.setAttribute("fill", isHighlighted ? "#b3261e" : "#2b3a42");
                rect.style.cursor = "pointer";
                rect.addEventListener("click", () => {
                    if (window.onRegionClicked) window.onRegionClicked(id, node);
                });
                nodesGroup.appendChild(rect);
            } else {
                // Circle node for villages
                const circle = document.createElementNS("http://www.w3.org/2000/svg", "circle");
                circle.setAttribute("cx", node.x);
                circle.setAttribute("cy", node.y);
                circle.setAttribute("r", isHighlighted ? "12" : "10");
                circle.setAttribute("stroke", isIsolated ? "#b3261e" : "#1c1c1c");
                circle.setAttribute("stroke-width", isHighlighted ? "3" : (isIsolated ? "2" : "1.5"));
                circle.setAttribute("fill", isHighlighted ? "#b3261e" : (isIsolated ? "#ffffff" : "#ffffff"));
                circle.style.cursor = "pointer";
                circle.addEventListener("click", () => {
                    if (window.onRegionClicked) window.onRegionClicked(id, node);
                });
                nodesGroup.appendChild(circle);
            }

            // Node Code inside marker
            const codeText = document.createElementNS("http://www.w3.org/2000/svg", "text");
            codeText.setAttribute("x", node.x);
            codeText.setAttribute("y", node.y + 3.5);
            codeText.setAttribute("font-family", "Consolas, monospace");
            codeText.setAttribute("font-size", "9px");
            codeText.setAttribute("font-weight", "700");
            codeText.setAttribute("text-anchor", "middle");
            if (node.isDepot || isHighlighted) {
                codeText.setAttribute("fill", "#ffffff");
            } else if (isIsolated) {
                codeText.setAttribute("fill", "#b3261e");
            } else {
                codeText.setAttribute("fill", "#1c1c1c");
            }
            codeText.textContent = node.code;
            codeText.style.pointerEvents = "none";
            nodesGroup.appendChild(codeText);

            // Plain Monospace Label above / below
            const label = document.createElementNS("http://www.w3.org/2000/svg", "text");
            label.setAttribute("x", node.x);
            label.setAttribute("y", node.y + (id === 10 || id === 6 ? -18 : 22));
            label.setAttribute("font-family", "Consolas, monospace");
            label.setAttribute("font-size", "10px");
            label.setAttribute("font-weight", "700");
            label.setAttribute("text-anchor", "middle");
            label.setAttribute("fill", isIsolated ? "#b3261e" : "#1c1c1c");
            label.setAttribute("paint-order", "stroke fill");
            label.setAttribute("stroke", "#f4f1ea");
            label.setAttribute("stroke-width", "2.5px");
            label.textContent = node.name;
            nodesGroup.appendChild(label);
        });

        svgElement.appendChild(nodesGroup);
    }

    return {
        init: init,
        setRoads: setRoads,
        highlightRoute: highlightRoute,
        clearHighlight: clearHighlight
    };
})();
