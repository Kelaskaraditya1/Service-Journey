package com.starkIndustries.serviceJourney.model;

/**
 * Represents the lifecycle status of a single event within a session.
 * Each event is either currently active (user is on that page) or completed.
 */
public enum EventStatus {
    ACTIVE,
    COMPLETED
}
