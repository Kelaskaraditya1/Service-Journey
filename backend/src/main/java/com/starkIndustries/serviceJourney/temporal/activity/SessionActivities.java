package com.starkIndustries.serviceJourney.temporal.activity;

import java.util.Map;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * SessionActivities — Temporal Activity contract.
 *
 * After YAML migration, this interface has two categories:
 *
 *   1. createSession() — standalone, runs before the YAML state machine starts
 *   2. executeWorkflow() — the SINGLE entry point for all YAML-driven business logic
 *
 * All other individual methods (completeEvent, createEvent, etc.) are removed.
 * Their logic now lives in WorkflowStepExecutor and is invoked via workflow.yml.
 */
@ActivityInterface
public interface SessionActivities {

  // =========================================================
  // SESSION CREATION — Runs once at workflow start
  // This happens BEFORE the YAML state machine begins,
  // so it remains a direct activity call.
  // =========================================================

  @ActivityMethod
  void createSession(String sessionId, String userId);

  // =========================================================
  // YAML WORKFLOW ENGINE — Single entry point for all
  // business logic execution (transitions, end, timeout)
  // =========================================================

  /**
   * Executes a YAML-defined workflow event.
   *
   * The Temporal workflow calls this ONE method instead of individual
   * activity methods. This method delegates to WorkflowEngineService
   * which reads workflow.yml, resolves the steps, and executes them.
   *
   * @param workflowName  The workflow name (e.g., "sessionFlow")
   * @param stateName     The current state (e.g., "ACTIVE")
   * @param eventType     The event type (e.g., "transitionEvent", "endSession", "timeout")
   * @param workflowData  Runtime data map (sessionId, currentEventId, nextScreen, etc.)
   */
  @ActivityMethod
  void executeWorkflow(
      String workflowName,
      String stateName,
      String eventType,
      Map<String, Object> workflowData);

}