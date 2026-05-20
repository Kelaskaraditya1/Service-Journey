package com.starkIndustries.serviceJourney.temporal.workflow;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * ============================================================
 * SessionWorkflow — Temporal Workflow Contract
 * ============================================================
 * 
 * ONE instance of this workflow runs per user session.
 * Workflow ID = sessionId (guaranteed unique per session).
 * 
 * Lifecycle:
 *   1. Controller calls startSession() → starts this workflow
 *   2. Frontend navigates → Controller signals transitionEvent()
 *   3. Workflow maintains inactivity timer (resets on each signal)
 *   4. If timer expires → workflow auto-aborts session
 *   5. User logs out → Controller signals endSession()
 *   6. Workflow completes
 * 
 * Temporal Concepts Used:
 *   - @WorkflowMethod  → main execution entry point
 *   - @SignalMethod     → async messages from controller (fire-and-forget)
 *   - @QueryMethod      → synchronous state queries (no side effects)
 *   - Durable Timers    → inactivity/absolute timeout (survives restart)
 *   - Activities        → DB persistence operations
 */
@WorkflowInterface
public interface SessionWorkflow {

  /**
   * Main workflow method — starts the session lifecycle.
   * 
   * This method runs for the entire duration of the session.
   * It waits for signals (event transitions, session end) or
   * timer expiry (inactivity timeout).
   * 
   * @param sessionId  The pre-generated session ID
   * @param userId     The user who owns this session
   */
  @WorkflowMethod
  void startSession(String sessionId, String userId);

  /**
   * Signal: Event Transition
   * 
   * Called by the controller when user navigates between screens.
   * The workflow will:
   *   1. Complete the previous event (if any)
   *   2. Create a new event for the next screen
   *   3. Reset the inactivity timer
   * 
   * @param previousEventId   ID of the event to close (nullable for first transition)
   * @param previousScreen    Name of the screen being left (nullable)
   * @param nextScreen        Name of the screen being navigated to
   */
  @SignalMethod
  void transitionEvent(String previousEventId, String previousScreen, String nextScreen);

  /**
   * Signal: End Session
   * 
   * Called by the controller when user explicitly logs out.
   * The workflow will close the active event and complete the session.
   * 
   * @param reason  Why the session is ending (e.g., "LOGOUT")
   */
  @SignalMethod
  void endSession(String reason);

  /**
   * Query: Get Session State
   * 
   * Returns the current in-memory state of the workflow.
   * This is a synchronous, read-only operation — no side effects.
   * Useful for debugging and monitoring the session.
   * 
   * @return JSON-friendly string describing current workflow state
   */
  @QueryMethod
  String getSessionState();

}
