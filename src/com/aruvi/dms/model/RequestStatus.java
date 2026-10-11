package com.aruvi.dms.model;

/**
 * RequestStatus represents the operational lifecycle states of an emergency request:
 * - PENDING: Waiting in priority queue for resources.
 * - PARTIALLY_ALLOCATED: Some resources allocated, waiting for the remainder.
 * - ALLOCATED: Request completely fulfilled.
 * - REJECTED: Dismissed by command officer with reason.
 * - CANCELLED: Withdrawn or resolved by field officer.
 * - UNREACHABLE: No open road route currently connects to the target region.
 */
public enum RequestStatus {
    PENDING,
    PARTIALLY_ALLOCATED,
    ALLOCATED,
    REJECTED,
    CANCELLED,
    UNREACHABLE
}
