package com.starkIndustries.serviceJourney.temporal.workflow;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import com.starkIndustries.serviceJourney.temporal.activity.SessionActivities;

/**
 * SessionWorkflowImpl — Temporal workflow for session lifecycle management.
 *
 * After YAML migration, this workflow is responsible ONLY for:
 *   - Durable state management (sessionId, currentEventId, sequenceNumber, etc.)
 *   - Signal handling (transitionEvent, endSession)
 *   - Timer management (inactivity timeout via Workflow.await)
 *   - Workflow lifecycle (start → loop → complete)
 *
 * All business execution is delegated to activities.executeWorkflow()
 * which flows through:
 *   WorkflowEngineService → workflow.yml → WorkflowStepExecutor → Repositories
 */
public class SessionWorkflowImpl implements SessionWorkflow {

  private static final Duration INACTIVITY_TIMEOUT = Duration.ofMinutes(5);

  private final SessionActivities activities = Workflow.newActivityStub(
      SessionActivities.class,
      ActivityOptions.newBuilder()
          .setStartToCloseTimeout(Duration.ofSeconds(10))
          .setRetryOptions(
            RetryOptions.newBuilder()
            .setMaximumAttempts(3)
            .build()
          )
          .build());


  // =========================================================
  // DURABLE WORKFLOW STATE — Temporal manages these fields
  // =========================================================

  private String sessionId;
  private String userId;
  private String currentEventId;
  private String currentScreen;
  private int sequenceNumber = 0;
  private boolean sessionCompleted = false;
  private boolean sessionAborted = false;
  private long lastActivityTimeMs;

  // Signal data (set by signal methods, consumed by main loop)
  private String pendingNextScreen = null;
  private String pendingPreviousEventId = null;
  private String pendingPreviousScreen = null;
  private boolean hasTransitionSignal = false;
  private String endReason = null;


  // =========================================================
  // WORKFLOW MAIN METHOD
  // =========================================================

  @Override
  public void startSession(String sessionId, String userId) {

    this.sessionId = sessionId;
    this.userId = userId;
    this.lastActivityTimeMs = Workflow.currentTimeMillis();

    Workflow.getLogger(SessionWorkflowImpl.class)
        .info("Workflow STARTED — session [{}] for user [{}]", sessionId, userId);

    // Direct activity call — session creation runs BEFORE the YAML state machine
    activities.createSession(sessionId, userId);

    Workflow.getLogger(SessionWorkflowImpl.class)
        .info("Session [{}] persisted, entering main event loop", sessionId);

    // -------------------------------------------------------
    // MAIN EVENT LOOP — Wait for signals or timeout
    // -------------------------------------------------------

    while (!this.sessionCompleted && !this.sessionAborted) {

      boolean signalReceived = Workflow.await(
          INACTIVITY_TIMEOUT,
          () -> this.hasTransitionSignal || this.endReason != null);

      // --- Case A: endSession signal received ---
      if (endReason != null) {
        handleSessionEnd();
        break;
      }

      // --- Case B: transitionEvent signal received ---
      if (hasTransitionSignal) {
        handleEventTransition();
        hasTransitionSignal = false;
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


  // =========================================================
  // SIGNAL METHODS — Set state, consumed by main loop
  // =========================================================

  @Override
  public void transitionEvent(String previousEventId, String previousScreen, String nextScreen) {

    Workflow.getLogger(SessionWorkflowImpl.class)
        .info("Signal RECEIVED: transitionEvent — session [{}], '{}' → '{}'",
            sessionId, previousScreen != null ? previousScreen : "START", nextScreen);

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


  // =========================================================
  // HANDLER: Event Transition → YAML onTransitionEvent
  // =========================================================

  private void handleEventTransition() {

    // Auto-detect previous event ID — workflow state logic stays here
    String prevId = pendingPreviousEventId;
    if (prevId == null && currentEventId != null) {
      prevId = currentEventId;
    }

    // Prepare sequence number for the new event
    sequenceNumber++;

    Workflow.getLogger(SessionWorkflowImpl.class)
        .info("handleEventTransition — session [{}], prevEvent=[{}], nextScreen='{}', sequence={}",
            sessionId, prevId, pendingNextScreen, sequenceNumber);

    // Build workflow data map
    Map<String, Object> workflowData = new HashMap<>();
    workflowData.put("sessionId", sessionId);
    workflowData.put("currentEventId", prevId);
    workflowData.put("nextScreen", pendingNextScreen);
    workflowData.put("currentScreen", currentScreen);
    workflowData.put("sequenceNumber", sequenceNumber);
    workflowData.put("previousEventId", prevId);

    Workflow.getLogger(SessionWorkflowImpl.class)
        .info("handleEventTransition — calling executeWorkflow with data: {}", workflowData.keySet());

    // YAML-driven execution: onTransitionEvent
    activities.executeWorkflow("sessionFlow", "ACTIVE", "transitionEvent", workflowData);

    // Update workflow state after successful execution
    // The new eventId is generated inside WorkflowStepExecutor.createNewEvent()
    // We need to track it for future transitions — use a convention-based approach
    this.currentScreen = pendingNextScreen;
    this.lastActivityTimeMs = Workflow.currentTimeMillis();

    // The currentEventId will be the one created by the executor.
    // Since we can't read it back from the YAML engine in this architecture,
    // we let the next transition auto-detect via the DB's session.activeEventId.
    // For workflow-internal tracking, we mark it as "managed by engine".
    this.currentEventId = "YAML_MANAGED";

    Workflow.getLogger(SessionWorkflowImpl.class)
        .info("handleEventTransition — completed, screen='{}', sequence={}",
            currentScreen, sequenceNumber);

    // Clear pending signal data
    pendingNextScreen = null;
    pendingPreviousEventId = null;
    pendingPreviousScreen = null;
  }


  // =========================================================
  // HANDLER: Session End → YAML onEndSession
  // =========================================================

  private void handleSessionEnd() {

    Workflow.getLogger(SessionWorkflowImpl.class)
        .info("handleSessionEnd — session [{}], reason='{}', activeEvent=[{}]",
            sessionId, endReason, currentEventId);

    // Build workflow data map
    Map<String, Object> workflowData = new HashMap<>();
    workflowData.put("sessionId", sessionId);
    workflowData.put("currentEventId", currentEventId);
    workflowData.put("endReason", endReason);
    workflowData.put("currentScreen", currentScreen);

    Workflow.getLogger(SessionWorkflowImpl.class)
        .info("handleSessionEnd — calling executeWorkflow with data: {}", workflowData.keySet());

    // YAML-driven execution: onEndSession
    activities.executeWorkflow("sessionFlow", "ACTIVE", "endSession", workflowData);

    // Update workflow state
    this.sessionCompleted = true;
    this.currentEventId = null;
    this.currentScreen = null;

    Workflow.getLogger(SessionWorkflowImpl.class)
        .info("handleSessionEnd — session [{}] ended successfully", sessionId);
  }


  // =========================================================
  // HANDLER: Inactivity Timeout → YAML onTimeout
  // =========================================================

  private void handleInactivityTimeout() {

    Workflow.getLogger(SessionWorkflowImpl.class)
        .warn("INACTIVITY TIMEOUT — session [{}], last activity {}ms ago",
            sessionId, Workflow.currentTimeMillis() - lastActivityTimeMs);

    // Build workflow data map
    Map<String, Object> workflowData = new HashMap<>();
    workflowData.put("sessionId", sessionId);
    workflowData.put("currentEventId", currentEventId);
    workflowData.put("endReason", "INACTIVITY");
    workflowData.put("currentScreen", currentScreen);

    Workflow.getLogger(SessionWorkflowImpl.class)
        .info("handleInactivityTimeout — calling executeWorkflow with data: {}", workflowData.keySet());

    // YAML-driven execution: onTimeout
    activities.executeWorkflow("sessionFlow", "ACTIVE", "timeout", workflowData);

    // Update workflow state
    this.sessionAborted = true;
    this.currentEventId = null;
    this.currentScreen = null;

    Workflow.getLogger(SessionWorkflowImpl.class)
        .warn("handleInactivityTimeout — session [{}] aborted", sessionId);
  }

}
