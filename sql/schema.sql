-- ====================================================================
-- Database Schema: Disaster Management Resource Allocation System
-- District: Aruvi Emergency Operations Command
-- Course: 24GEP31 - Mini Project (Second Review)
-- ====================================================================

DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS allocations;
DROP TABLE IF EXISTS emergency_requests;
DROP TABLE IF EXISTS roads;
DROP TABLE IF EXISTS rescue_teams;
DROP TABLE IF EXISTS shelters;
DROP TABLE IF EXISTS depots;
DROP TABLE IF EXISTS regions;

-- 1. Affected Regions
CREATE TABLE regions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    population INT NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Resource Depots
CREATE TABLE depots (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    region_id INT NOT NULL,
    food_kits INT NOT NULL DEFAULT 0,
    medicine_kits INT NOT NULL DEFAULT 0,
    FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Shelters
CREATE TABLE shelters (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    region_id INT NOT NULL,
    capacity INT NOT NULL,
    reserved_places INT NOT NULL DEFAULT 0,
    FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Rescue Teams
CREATE TABLE rescue_teams (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    home_depot_id INT NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    current_request_id INT NULL,
    FOREIGN KEY (home_depot_id) REFERENCES depots(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Roads (Weighted Graph Edges with Open/Closed State)
CREATE TABLE roads (
    id INT PRIMARY KEY AUTO_INCREMENT,
    from_region_id INT NOT NULL,
    to_region_id INT NOT NULL,
    travel_time INT NOT NULL, -- in minutes, must be > 0
    is_open BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(255),
    FOREIGN KEY (from_region_id) REFERENCES regions(id) ON DELETE RESTRICT,
    FOREIGN KEY (to_region_id) REFERENCES regions(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Emergency Reports / Requests
-- Statuses: PENDING, PARTIALLY_ALLOCATED, ALLOCATED, REJECTED, CANCELLED, UNREACHABLE
CREATE TABLE emergency_requests (
    id INT PRIMARY KEY AUTO_INCREMENT,
    region_id INT NOT NULL,
    severity INT NOT NULL, -- 1 to 5 (5 highest)
    affected_population INT NOT NULL, -- > 0
    arrival_order INT NOT NULL, -- sequence counter
    request_type VARCHAR(20) NOT NULL, -- SUPPLY or RESCUE
    
    -- Supply request fields
    requested_food INT NOT NULL DEFAULT 0,
    allocated_food INT NOT NULL DEFAULT 0,
    outstanding_food INT NOT NULL DEFAULT 0,
    
    requested_medicine INT NOT NULL DEFAULT 0,
    allocated_medicine INT NOT NULL DEFAULT 0,
    outstanding_medicine INT NOT NULL DEFAULT 0,
    
    -- Rescue request fields
    requested_teams INT NOT NULL DEFAULT 0,
    allocated_teams INT NOT NULL DEFAULT 0,
    
    requested_shelter_places INT NOT NULL DEFAULT 0,
    allocated_shelter_places INT NOT NULL DEFAULT 0,
    outstanding_shelter_places INT NOT NULL DEFAULT 0,
    
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    reported_by VARCHAR(100) NOT NULL DEFAULT 'Field Officer',
    rejection_reason TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Allocations (Confirmed Officer Decisions)
CREATE TABLE allocations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    request_id INT NOT NULL,
    depot_id INT NULL,
    rescue_team_id INT NULL,
    shelter_id INT NULL,
    food_allocated INT NOT NULL DEFAULT 0,
    medicine_allocated INT NOT NULL DEFAULT 0,
    shelter_places_allocated INT NOT NULL DEFAULT 0,
    route_path VARCHAR(255) NULL,
    travel_time INT NOT NULL DEFAULT 0,
    officer_name VARCHAR(100) NOT NULL DEFAULT 'Command Officer',
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    confirmed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES emergency_requests(id) ON DELETE RESTRICT,
    FOREIGN KEY (depot_id) REFERENCES depots(id) ON DELETE SET NULL,
    FOREIGN KEY (rescue_team_id) REFERENCES rescue_teams(id) ON DELETE SET NULL,
    FOREIGN KEY (shelter_id) REFERENCES shelters(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Audit Logs (Complete Action History)
CREATE TABLE audit_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    action_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    performed_by VARCHAR(100) NOT NULL DEFAULT 'System',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
