package com.starkIndustries.serviceJourney.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.starkIndustries.serviceJourney.dto.request.EventTransitionRequest;
import com.starkIndustries.serviceJourney.dto.request.SessionEndRequest;
import com.starkIndustries.serviceJourney.dto.request.SessionStartRequest;
import com.starkIndustries.serviceJourney.dto.response.ApiResponse;
import com.starkIndustries.serviceJourney.dto.response.SessionResponse;
import com.starkIndustries.serviceJourney.service.SessionService;
import com.starkIndustries.serviceJourney.temporal.config.TemporalConfig;
import com.starkIndustries.serviceJourney.temporal.workflow.SessionWorkflow;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * ============================================================
 * SessionController — REST API Layer (Temporal-Integrated)
 * ============================================================
 * 
 * This controller is now a THIN layer that:
 *   - Validates requests
 *   - Starts/signals Temporal workflows
 *   - Returns responses
 * 
 * It does NOT orchestrate business logic directly.
 * All orchestration happens inside the Temporal workflow.
 * 
 * API Summary:
 *   POST /session/start            → Starts a new Temporal workflow
 *   POST /session/event-transition → Signals the workflow
 *   POST /session/end              → Signals the workflow
 *   GET  /session/{id}             → Queries DB (unchanged)
 *   GET  /session                  → Queries DB (unchanged)
 *   GET  /session/{id}/state       → Queries Temporal workflow state
 */
@RestController
@RequestMapping("/session")
@Slf4j
public class SessionController {

  @Autowired
  public WorkflowClient workflowClient;

  @Autowired
  public SessionService sessionService;

  // ============================================================
  // POST /session/start — Start a Temporal Workflow
  // ============================================================

  /**
   * Creates a new session by starting a Temporal workflow.
   * 
   * What happens:
   *   1. Generate a unique sessionId
   *   2. Start a SessionWorkflow with workflowId = sessionId
   *   3. Workflow's first activity creates the DB record
   *   4. Return session info to frontend
   * 
   * The workflow then sits and waits for signals (transitions, end)
   * or times out after the inactivity period.
   */
  @PostMapping("/start")
  public ResponseEntity<ApiResponse<Object>> start(
      @Valid @RequestBody SessionStartRequest sessionStartRequest) {

    String sessionId = UUID.randomUUID().toString();

    log.info("POST /session/start — userId: {}, starting workflow [{}]",
        sessionStartRequest.userId, sessionId);

    // Create workflow stub with options
    WorkflowOptions options = WorkflowOptions.newBuilder()
        .setWorkflowId(sessionId)                      // Workflow ID = Session ID
        .setTaskQueue(TemporalConfig.TASK_QUEUE)       // Route to our worker
        .build();

    SessionWorkflow workflow = workflowClient.newWorkflowStub(SessionWorkflow.class, options);

    // Start the workflow asynchronously (non-blocking)
    // The workflow will persist the session via its first activity
    WorkflowClient.start(workflow::startSession, sessionId, sessionStartRequest.userId);

    log.info("Temporal workflow [{}] started for user [{}]", sessionId, sessionStartRequest.userId);

    // Build a response with the session ID so the frontend can reference it
    var responseData = java.util.Map.of(
        "sessionId", sessionId,
        "userId", sessionStartRequest.userId,
        "workflowId", sessionId,
        "message", "Session workflow started"
    );

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(responseData, "Session created — Temporal workflow started"));
  }

  // ============================================================
  // POST /session/event-transition — Signal the Workflow
  // ============================================================

  /**
   * Sends an event-transition signal to the session's Temporal workflow.
   * 
   * What happens:
   *   1. Get a workflow stub by sessionId (workflowId)
   *   2. Send the transitionEvent signal
   *   3. Workflow processes: close previous event → create next event
   *   4. Workflow resets inactivity timer
   * 
   * This is fire-and-forget — the signal is queued and processed by the workflow.
   * The response is returned immediately without waiting for DB operations.
   */
  @PostMapping("/event-transition")
  public ResponseEntity<ApiResponse<Object>> eventTransition(
      @Valid @RequestBody EventTransitionRequest request) {

    log.info("POST /session/event-transition — sessionId: {}, '{}' → '{}'",
        request.sessionId,
        request.previousScreenName != null ? request.previousScreenName : "START",
        request.nextScreenName);

    // Get existing workflow stub by ID
    SessionWorkflow workflow = workflowClient.newWorkflowStub(
        SessionWorkflow.class, request.sessionId);

    // Send signal (fire-and-forget — does not block)
    workflow.transitionEvent(
        request.previousEventId,
        request.previousScreenName,
        request.nextScreenName);

    log.info("Transition signal sent to workflow [{}]", request.sessionId);

    var responseData = java.util.Map.of(
        "sessionId", request.sessionId,
        "nextScreen", request.nextScreenName,
        "message", "Event transition signal sent to workflow"
    );

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(responseData, "Event transition signal sent"));
  }

  // ============================================================
  // POST /session/end — Signal the Workflow to End
  // ============================================================

  /**
   * Sends an end-session signal to the Temporal workflow.
   * 
   * What happens:
   *   1. Get a workflow stub by sessionId
   *   2. Send the endSession signal with reason
   *   3. Workflow processes: close active event → complete session
   *   4. Workflow exits naturally
   */
  @PostMapping("/end")
  public ResponseEntity<ApiResponse<Object>> end(
      @Valid @RequestBody SessionEndRequest sessionEndRequest) {

    log.info("POST /session/end — sessionId: {}", sessionEndRequest.sessionId);

    // Get existing workflow stub by ID
    SessionWorkflow workflow = workflowClient.newWorkflowStub(
        SessionWorkflow.class, sessionEndRequest.sessionId);

    // Determine reason string
    String reason = (sessionEndRequest.getExpiryReasons() != null)
        ? sessionEndRequest.getExpiryReasons().name()
        : "LOGOUT";

    // Send signal
    workflow.endSession(reason);

    log.info("End session signal sent to workflow [{}], reason: {}",
        sessionEndRequest.sessionId, reason);

    var responseData = java.util.Map.of(
        "sessionId", sessionEndRequest.sessionId,
        "reason", reason,
        "message", "Session end signal sent to workflow"
    );

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(responseData, "Session end signal sent"));
  }

  // ============================================================
  // GET /session/{id}/state — Query Temporal Workflow State
  // ============================================================

  /**
   * Queries the Temporal workflow's in-memory state.
   * This is a synchronous read-only operation.
   * Returns the workflow's current view of the session (not from DB).
   */
  @GetMapping("/{sessionId}/state")
  public ResponseEntity<ApiResponse<Object>> getWorkflowState(
      @PathVariable("sessionId") String sessionId) {

    log.debug("GET /session/{}/state — querying Temporal workflow", sessionId);

    SessionWorkflow workflow = workflowClient.newWorkflowStub(
        SessionWorkflow.class, sessionId);

    String state = workflow.getSessionState();

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(state, "Workflow state retrieved"));
  }

  // ============================================================
  // GET /session/{id} — Query Database (unchanged from Step 1)
  // ============================================================

  /**
   * Retrieves a session from the database.
   * This bypasses Temporal — reads directly from PostgreSQL.
   */
  @GetMapping("/{sessionId}")
  public ResponseEntity<ApiResponse<Object>> getSession(
      @PathVariable("sessionId") String sessionId) {

    log.debug("GET /session/{}", sessionId);

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(this.sessionService.getSession(sessionId)));
  }

  /**
   * Retrieves all sessions from the database.
   */
  @GetMapping("")
  public ResponseEntity<ApiResponse<Object>> getSessions() {

    log.debug("GET /session — fetching all sessions");

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(this.sessionService.getAllSessions()));
  }

}
