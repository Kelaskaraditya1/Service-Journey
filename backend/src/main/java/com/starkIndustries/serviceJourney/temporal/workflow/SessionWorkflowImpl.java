package com.starkIndustries.serviceJourney.temporal.workflow;

import java.time.Duration;
import java.util.UUID;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import com.starkIndustries.serviceJourney.temporal.activity.SessionActivities;

/**
 * ============================================================
 * SessionWorkflowImpl — Temporal Workflow Implementation
 * ============================================================
 * 
 * This is the orchestration brain of a user session.
 * ONE instance runs per session and manages:
 * 
 *   - Session creation (via activity)
 *   - Event transitions (via signals + activities)
 *   - Inactivity timeout (via Temporal durable timer)
 *   - Session completion (via signal + activities)
 * 
 * IMPORTANT TEMPORAL RULES:
 *   - NO direct DB calls from workflow code (use activities)
 *   - NO System.currentTimeMillis() (use Workflow.currentTimeMillis())
 *   - NO Thread.sleep() (use Workflow.sleep())
 *   - NO external I/O (use activities)
 *   - Workflow code must be DETERMINISTIC
 * 
 * The workflow keeps running (blocked on Workflow.await) until:
 *   - User sends endSession signal (normal logout)
 *   - Inactivity timer expires (auto-abort)
 */
public class SessionWorkflowImpl implements SessionWorkflow {

  // ============================================================
  // CONFIGURATION
  // ============================================================

  /** How long to wait for user activity before auto-aborting */
  private static final Duration INACTIVITY_TIMEOUT = Duration.ofMinutes(5);

  /** Max absolute session lifetime */
  private static final Duration ABSOLUTE_TIMEOUT = Duration.ofMinutes(30);

  // ============================================================
  // ACTIVITY STUB — connects workflow to DB operations
  // ============================================================

  private final SessionActivities activities = Workflow.newActivityStub(
      SessionActivities.class,
      ActivityOptions.newBuilder()
          .setStartToCloseTimeout(Duration.ofSeconds(10))
          .build());

  // ============================================================
  // DURABLE WORKFLOW STATE
  // These fields survive server restarts via Temporal's event sourcing.
  // ============================================================

  private String sessionId;
  private String userId;
  private String currentEventId;
  private String currentScreen;
  private int sequenceNumber = 0;
  private boolean sessionCompleted = false;
  private boolean sessionAborted = false;
  private long lastActivityTimeMs;

  // Pending signal data (used to communicate between signal handler and main loop)
  private String pendingNextScreen = null;
  private String pendingPreviousEventId = null;
  private String pendingPreviousScreen = null;
  private boolean hasTransitionSignal = false;
  private String endReason = null;

  // ============================================================
  // MAIN WORKFLOW METHOD
  // ============================================================

  @Override
  public void startSession(String sessionId, String userId) {

    this.sessionId = sessionId;
    this.userId = userId;
    this.lastActivityTimeMs = Workflow.currentTimeMillis();

    Workflow.getLogger(SessionWorkflowImpl.class)
        .info("Workflow STARTED — session [{}] for user [{}]", sessionId, userId);

    // Step 1: Persist the session to database
    activities.createSession(sessionId, userId);

    // Step 2: Main loop — wait for signals or timeout
    //
    // The workflow sits here for the entire session lifetime.
    // It wakes up when:
    //   (a) a transitionEvent signal arrives
    //   (b) an endSession signal arrives
    //   (c) the inactivity timer expires
    //
    while (!sessionCompleted && !sessionAborted) {

      // Wait for a signal OR timeout (whichever comes first)
      boolean signalReceived = Workflow.await(
          INACTIVITY_TIMEOUT,
          () -> hasTransitionSignal || endReason != null);

      // --- Case A: endSession signal received ---
      if (endReason != null) {
        handleSessionEnd();
        break;
      }

      // --- Case B: transitionEvent signal received ---
      if (hasTransitionSignal) {
        handleEventTransition();
        hasTransitionSignal = false; // reset for next signal
        continue;
      }

      // --- Case C: Timeout expired (no signal received) ---
      if (!signalReceived) {
        handleInactivityTimeout();
        break;
      }
    }

    Workflow.getLogger(SessionWorkflowImpl.class)
        .info("Workflow COMPLETED — session [{}], events: {}, aborted: {}",
            sessionId, sequenceNumber, sessionAborted);
  }

  // ============================================================
  // SIGNAL HANDLERS
  // ============================================================

  @Override
  public void transitionEvent(String previousEventId, String previousScreen, String nextScreen) {

    Workflow.getLogger(SessionWorkflowImpl.class)
        .info("Signal RECEIVED: transitionEvent — session [{}], '{}' → '{}'",
            sessionId, previousScreen != null ? previousScreen : "START", nextScreen);

    // Store signal data for the main loop to process
    this.pendingPreviousEventId = previousEventId;
    this.pendingPreviousScreen = previousScreen;
    this.pendingNextScreen = nextScreen;
    this.hasTransitionSignal = true;
  }

  @Override
  public void endSession(String reason) {

    Workflow.getLogger(SessionWorkflowImpl.class)
        .info("Signal RECEIVED: endSession — session [{}], reason: {}", sessionId, reason);

    this.endReason = (reason != null) ? reason : "LOGOUT";
  }

  // ============================================================
  // QUERY HANDLER
  // ============================================================

  @Override
  public String getSessionState() {
    return String.format(
        "{\"sessionId\":\"%s\", \"userId\":\"%s\", \"currentEventId\":\"%s\", " +
            "\"currentScreen\":\"%s\", \"sequenceNumber\":%d, " +
            "\"sessionCompleted\":%b, \"sessionAborted\":%b}",
        sessionId, userId,
        currentEventId != null ? currentEventId : "null",
        currentScreen != null ? currentScreen : "null",
        sequenceNumber, sessionCompleted, sessionAborted);
  }

  // ============================================================
  // INTERNAL HANDLERS — Called from the main loop
  // ============================================================

  /**
   * Processes an event transition:
   *   1. Complete the previous event (if exists)
   *   2. Create a new event
   *   3. Update session tracking
   *   4. Reset inactivity timer
   */
  private void handleEventTransition() {

    // Step 1: Complete previous event
    String prevId = pendingPreviousEventId;
    if (prevId == null && currentEventId != null) {
      // Auto-detect: close whatever is currently active
      prevId = currentEventId;
    }

    if (prevId != null) {
      activities.completeEvent(prevId);
      Workflow.getLogger(SessionWorkflowImpl.class)
          .info("Completed previous event [{}]", prevId);
    }

    // Step 2: Create new event
    sequenceNumber++;
    String newEventId = UUID.randomUUID().toString();

    activities.createEvent(sessionId, newEventId, pendingNextScreen, sequenceNumber, prevId);

    // Step 3: Update session tracking in DB
    activities.updateSessionTracking(sessionId, pendingNextScreen, newEventId, sequenceNumber);

    // Step 4: Update workflow state
    this.currentEventId = newEventId;
    this.currentScreen = pendingNextScreen;
    this.lastActivityTimeMs = Workflow.currentTimeMillis();

    Workflow.getLogger(SessionWorkflowImpl.class)
        .info("Event transition complete — session [{}], new event [{}] on '{}', sequence: {}",
            sessionId, newEventId, pendingNextScreen, sequenceNumber);

    // Clear pending data
    pendingNextScreen = null;
    pendingPreviousEventId = null;
    pendingPreviousScreen = null;
  }

  /**
   * Handles a normal session end (user logout).
   * Completes the active event and marks session as COMPLETED.
   */
  private void handleSessionEnd() {

    Workflow.getLogger(SessionWorkflowImpl.class)
        .info("Ending session [{}] — reason: {}", sessionId, endReason);

    // Close any active event
    if (currentEventId != null) {
      activities.completeEvent(currentEventId);
      Workflow.getLogger(SessionWorkflowImpl.class)
          .info("Closed active event [{}] during session end", currentEventId);
    }

    // Mark session as completed
    activities.completeSession(sessionId, endReason);

    this.sessionCompleted = true;
    this.currentEventId = null;
    this.currentScreen = null;
  }

  /**
   * Handles inactivity timeout.
   * Auto-aborts the session and closes any active event.
   */
  private void handleInactivityTimeout() {

    Workflow.getLogger(SessionWorkflowImpl.class)
        .warn("INACTIVITY TIMEOUT — session [{}], last activity {}ms ago, aborting...",
            sessionId, Workflow.currentTimeMillis() - lastActivityTimeMs);

    // Close any active event
    if (currentEventId != null) {
      activities.completeEvent(currentEventId);
      Workflow.getLogger(SessionWorkflowImpl.class)
          .info("Closed active event [{}] during inactivity abort", currentEventId);
    }

    // Abort the session
    activities.abortSession(sessionId, "INACTIVITY");

    this.sessionAborted = true;
    this.currentEventId = null;
    this.currentScreen = null;
  }

}
