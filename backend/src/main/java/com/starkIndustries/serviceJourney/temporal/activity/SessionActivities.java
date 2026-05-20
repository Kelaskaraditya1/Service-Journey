package com.starkIndustries.serviceJourney.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * ============================================================
 * SessionActivities — Temporal Activity Contract
 * ============================================================
 * 
 * Activities contain all database operations for the session workflow.
 * They are executed by the Temporal Worker and can be retried automatically.
 * 
 * Key Rule: Activities do the actual DB work.
 *           Workflows only orchestrate and make decisions.
 * 
 * Each method here maps to a database operation using
 * the existing JPA repositories and services.
 */
@ActivityInterface
public interface SessionActivities {

  /**
   * Creates a new session record in the database.
   * Called once when the workflow starts.
   * 
   * @param sessionId  Unique session identifier (same as workflow ID)
   * @param userId     User who owns the session
   */
  @ActivityMethod
  void createSession(String sessionId, String userId);

  /**
   * Creates a new event (page visit) within a session.
   * Called during event transitions.
   * 
   * @param sessionId       The session this event belongs to
   * @param eventId         Pre-generated unique event ID
   * @param page            Screen/page name
   * @param sequenceOrder   Order within the session (1-based)
   * @param previousEventId ID of the previous event (null for first)
   * @return The created event ID (same as input, confirmed persisted)
   */
  @ActivityMethod
  String createEvent(String sessionId, String eventId, String page,
      int sequenceOrder, String previousEventId);

  /**
   * Completes an event by setting its exit time and duration.
   * Called when user navigates away from a page.
   * 
   * @param eventId  The event to complete
   */
  @ActivityMethod
  void completeEvent(String eventId);

  /**
   * Updates the session's tracking fields after an event transition.
   * Updates: lastPage, lastActivityTime, activeEventId, eventCount.
   * 
   * @param sessionId      The session to update
   * @param lastPage       Current page the user is on
   * @param activeEventId  The currently active event ID
   * @param eventCount     Updated event count
   */
  @ActivityMethod
  void updateSessionTracking(String sessionId, String lastPage,
      String activeEventId, int eventCount);

  /**
   * Completes a session normally (user logout).
   * Closes any remaining open event and marks session as COMPLETED.
   * 
   * @param sessionId  The session to complete
   * @param reason     Reason for completion (e.g., "LOGOUT")
   */
  @ActivityMethod
  void completeSession(String sessionId, String reason);

  /**
   * Aborts a session due to inactivity timeout.
   * Closes any remaining open event and marks session as EXPIRED.
   * 
   * @param sessionId  The session to abort
   * @param reason     Expiry reason (e.g., "INACTIVITY", "ABSOLUTE")
   */
  @ActivityMethod
  void abortSession(String sessionId, String reason);

}
