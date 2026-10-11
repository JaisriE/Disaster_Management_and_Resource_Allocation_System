-- ====================================================================
-- Seed Data: Disaster Management Resource Allocation System
-- District: Aruvi Emergency Operations Command
-- Course: 24GEP31 - Mini Project (Second Review)
-- ====================================================================

-- 1. Insert 10 Affected Regions (Region 10 Solaiyur is deliberately isolated)
INSERT INTO regions (id, name, population, description) VALUES
(1, 'Aruvi Town', 45000, 'District administrative center, commercial hub and central staging zone'),
(2, 'Velliangadu', 18000, 'Low-lying agriculture zone near foothills, prone to waterlogging'),
(3, 'Thamarai Kulam', 12500, 'Lake basin settlement, experiencing flash storm runoff'),
(4, 'Vellalur', 22000, 'Suburban river delta zone with school relief shelter'),
(5, 'Siruvani Hills', 8500, 'Hilly tribal village area, vulnerable to heavy landslides'),
(6, 'Ponnaiyanur', 26000, 'Eastern river bank town hosting secondary relief depot'),
(7, 'Karamadai', 15000, 'Semi-urban junction connecting eastern and northern corridors'),
(8, 'Bhavani Bank', 19500, 'Riverfront fishing hamlet with severe flood inundation'),
(9, 'Marutham Village', 9200, 'Agricultural village facing severe road disruptions'),
(10, 'Solaiyur', 6400, 'Remote valley settlement - completely isolated due to broken bridges and landslides');

-- 2. Insert 2 Resource Depots
-- Depot 1 at Region 1 (Aruvi Town): 600 Food Kits, 400 Medicine Kits
-- Depot 2 at Region 6 (Ponnaiyanur): 450 Food Kits, 250 Medicine Kits
INSERT INTO depots (id, name, region_id, food_kits, medicine_kits) VALUES
(1, 'Central Relief Depot Alpha', 1, 600, 400),
(2, 'Eastern Staging Depot Beta', 6, 450, 250);

-- 3. Insert 2 Relief Shelters
-- Shelter 1 at Region 1 (Aruvi Town): Capacity 350, Reserved 0
-- Shelter 2 at Region 4 (Vellalur): Capacity 250, Reserved 0
INSERT INTO shelters (id, name, region_id, capacity, reserved_places) VALUES
(1, 'Aruvi Central Community Shelter', 1, 350, 0),
(2, 'Vellalur High School Camp', 4, 250, 0);

-- 4. Insert 3 Rescue Teams
-- Team 1 and 2 home at Depot 1; Team 3 home at Depot 2
INSERT INTO rescue_teams (id, name, home_depot_id, is_available) VALUES
(1, 'Alpha Rapid Water Rescue', 1, TRUE),
(2, 'Bravo Medical Evacuation Unit', 1, TRUE),
(3, 'Charlie Disaster Response Taskforce', 2, TRUE);

-- 5. Insert Roads (Positive travel times in minutes; open or closed)
-- Roads connecting the network. Both roads to Solaiyur (Region 10) are CLOSED to make it isolated.
INSERT INTO roads (id, from_region_id, to_region_id, travel_time, is_open, description) VALUES
(1, 1, 2, 15, TRUE, 'NH-Aruvi Expressway to Velliangadu'),
(2, 2, 1, 15, TRUE, 'NH-Aruvi Expressway returning to Central'),
(3, 1, 3, 25, TRUE, 'Thamarai Bypass road'),
(4, 3, 1, 25, TRUE, 'Thamarai Bypass road return'),
(5, 2, 4, 20, TRUE, 'Velliangadu-Vellalur Arterial road'),
(6, 4, 2, 20, TRUE, 'Vellalur-Velliangadu Arterial road return'),
(7, 3, 4, 18, TRUE, 'Canal Link Road'),
(8, 4, 3, 18, TRUE, 'Canal Link Road return'),
(9, 3, 5, 35, TRUE, 'Siruvani Foothill Highway'),
(10, 5, 3, 35, TRUE, 'Siruvani Foothill Highway return'),
(11, 4, 6, 30, TRUE, 'Delta Cross Corridor to Ponnaiyanur Depot'),
(12, 6, 4, 30, TRUE, 'Delta Cross Corridor return to Vellalur'),
(13, 5, 7, 40, TRUE, 'Hill Border Pass to Karamadai'),
(14, 7, 5, 40, TRUE, 'Hill Border Pass return'),
(15, 6, 7, 22, TRUE, 'Eastern State Highway between Depot 2 and Karamadai'),
(16, 7, 6, 22, TRUE, 'Eastern State Highway return'),
(17, 6, 8, 28, TRUE, 'Riverbank South Road'),
(18, 8, 6, 28, TRUE, 'Riverbank South Road return'),
(19, 7, 8, 16, TRUE, 'Karamadai-Bhavani Connector'),
(20, 8, 7, 16, TRUE, 'Bhavani-Karamadai Connector return'),
(21, 7, 9, 25, TRUE, 'Marutham Rural Access Road'),
(22, 9, 7, 25, TRUE, 'Marutham Rural Access Road return'),
(23, 8, 9, 20, TRUE, 'Delta Farm Link Road'),
(24, 9, 8, 20, TRUE, 'Delta Farm Link Road return'),
-- Isolated Village Roads (Region 10 Solaiyur): CLOSED due to debris and flash flood
(25, 5, 10, 60, FALSE, 'Solaiyur Mountain Pass (BLOCKED BY LANDSLIDE)'),
(26, 10, 5, 60, FALSE, 'Solaiyur Mountain Pass (BLOCKED BY LANDSLIDE) return'),
(27, 9, 10, 45, FALSE, 'Solaiyur River Bridge (COLLAPSED FLOOD DAMAGE)'),
(28, 10, 9, 45, FALSE, 'Solaiyur River Bridge (COLLAPSED FLOOD DAMAGE) return');

-- 6. Insert 16 Emergency Requests (Meeting >= 15 requirement)
-- Different severities (1-5), affected populations, arrival orders, SUPPLY and RESCUE types
-- Note: Region 10 has requests that will demonstrate UNREACHABLE status until road is cleared.
INSERT INTO emergency_requests 
(id, region_id, severity, affected_population, arrival_order, request_type, 
 requested_food, allocated_food, outstanding_food, 
 requested_medicine, allocated_medicine, outstanding_medicine, 
 requested_teams, allocated_teams, 
 requested_shelter_places, allocated_shelter_places, outstanding_shelter_places, 
 status, reported_by) 
VALUES
(1, 2, 5, 2400, 1, 'SUPPLY', 120, 0, 120, 80, 0, 80, 0, 0, 0, 0, 0, 'PENDING', 'Officer Murugan'),
(2, 5, 5, 1800, 2, 'RESCUE', 0, 0, 0, 0, 0, 0, 1, 0, 120, 0, 120, 'PENDING', 'Officer Radhika'),
(3, 8, 5, 3100, 3, 'SUPPLY', 150, 0, 150, 100, 0, 100, 0, 0, 0, 0, 0, 'PENDING', 'Officer Anand'),
(4, 3, 4, 1500, 4, 'SUPPLY', 80, 0, 80, 50, 0, 50, 0, 0, 0, 0, 0, 'PENDING', 'Officer Selvam'),
(5, 7, 4, 2200, 5, 'RESCUE', 0, 0, 0, 0, 0, 0, 1, 0, 80, 0, 80, 'PENDING', 'Officer Karthik'),
(6, 4, 4, 1900, 6, 'SUPPLY', 90, 0, 90, 60, 0, 60, 0, 0, 0, 0, 0, 'PENDING', 'Officer Priya'),
(7, 9, 3, 1100, 7, 'SUPPLY', 60, 0, 60, 40, 0, 40, 0, 0, 0, 0, 0, 'PENDING', 'Officer Dinesh'),
(8, 6, 3, 1400, 8, 'RESCUE', 0, 0, 0, 0, 0, 0, 1, 0, 50, 0, 50, 'PENDING', 'Officer Meena'),
(9, 2, 3, 950, 9, 'SUPPLY', 50, 0, 50, 30, 0, 30, 0, 0, 0, 0, 0, 'PENDING', 'Officer Murugan'),
(10, 10, 5, 2800, 10, 'SUPPLY', 100, 0, 100, 75, 0, 75, 0, 0, 0, 0, 0, 'PENDING', 'Officer Senthil (Remote Radio)'),
(11, 10, 5, 1200, 11, 'RESCUE', 0, 0, 0, 0, 0, 0, 1, 0, 90, 0, 90, 'PENDING', 'Officer Senthil (Remote Radio)'),
(12, 1, 2, 600, 12, 'SUPPLY', 40, 0, 40, 20, 0, 20, 0, 0, 0, 0, 0, 'PENDING', 'Officer Kumar'),
(13, 8, 4, 2500, 13, 'RESCUE', 0, 0, 0, 0, 0, 0, 1, 0, 100, 0, 100, 'PENDING', 'Officer Anand'),
(14, 3, 2, 700, 14, 'SUPPLY', 30, 0, 30, 20, 0, 20, 0, 0, 0, 0, 0, 'PENDING', 'Officer Selvam'),
(15, 7, 2, 850, 15, 'SUPPLY', 45, 0, 45, 25, 0, 25, 0, 0, 0, 0, 0, 'PENDING', 'Officer Karthik'),
(16, 9, 1, 400, 16, 'SUPPLY', 25, 0, 25, 15, 0, 15, 0, 0, 0, 0, 0, 'PENDING', 'Officer Dinesh');

-- 7. Audit log initial record
INSERT INTO audit_logs (action_type, description, performed_by) VALUES
('SYSTEM_INITIALIZATION', 'Aruvi Disaster Management System initialized with 10 regions, 2 depots, 2 shelters, 3 rescue teams and 16 emergency requests.', 'System Setup');
