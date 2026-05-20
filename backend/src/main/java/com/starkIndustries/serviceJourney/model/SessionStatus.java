package com.starkIndustries.serviceJourney.model;

/**
 * Represents the lifecycle status of a user session.
 * Designed to be compatible with future Temporal workflow states.
 */
public enum SessionStatus {
    ACTIVE,
    COMPLETED,
    EXPIRED_ABSOLUTE,
    EXPIRED_INACTIVITY
}
