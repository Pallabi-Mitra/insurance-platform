package com.pallabi.insurance.policy.model;

/**
 * The lifecycle of a policy:
 *
 *   PENDING_PAYMENT -> ACTIVE -> EXPIRED   (normal path)
 *                        |
 *                        +-> LAPSED        (customer stopped paying)
 *                        +-> CANCELLED     (customer cancelled)
 *
 * Matches the CHECK constraint on the status column.
 */
public enum PolicyStatus {
    PENDING_PAYMENT,
    ACTIVE,
    LAPSED,
    CANCELLED,
    EXPIRED
}
