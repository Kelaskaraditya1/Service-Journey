package com.starkIndustries.serviceJourney.temporal.activity;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.starkIndustries.serviceJourney.model.Session;
import com.starkIndustries.serviceJourney.model.SessionStatus;
import com.starkIndustries.serviceJourney.repository.SessionRepository;
import com.starkIndustries.serviceJourney.workflow.context.WorkflowContext;
import com.starkIndustries.serviceJourney.workflow.engine.WorkflowEngineService;

import lombok.extern.slf4j.Slf4j;

/**
 * SessionActivitiesImpl — Temporal Activity implementation.
 *
 * This is the bridge between Temporal and the YAML workflow engine.
 *
 * Contains two methods:
 *   1. createSession() — direct DB write (pre-state-machine)
 *   2. executeWorkflow() — builds WorkflowContext, delegates to WorkflowEngineService
 *
 * All business logic for transitions, session end, and timeout
 * flows through executeWorkflow() → WorkflowEngineService → WorkflowStepExecutor.
 */
@Component
@Slf4j
public class SessionActivitiesImpl implements SessionActivities {

  @Autowired
  private SessionRepository sessionRepository;

  @Autowired
  private WorkflowEngineService workflowEngineService;

  // ============================================================
  // SESSION CREATION — Direct DB write, runs once at workflow start
  // ============================================================

  @Override
  public void createSession(String sessionId, String userId) {

    log.info("[Activity] createSession — sessionId=[{}], userId=[{}]", sessionId, userId);

    Instant now = Instant.now();

    Session session = Session.builder()
        .sessionId(sessionId)
        .userId(userId)
        .startTime(now)
        .endTime(null)
        .expired(false)
        .expiryReasons(null)
        .sessionStatus(SessionStatus.ACTIVE)
        .lastPage(null)
        .duration(null)
        .lastActivityTime(now)
        .workflowId(sessionId)
        .activeEventId(null)
        .eventCount(0)
        .build();

    sessionRepository.save(session);

    log.info("[Activity] createSession — session [{}] persisted for user [{}]", sessionId, userId);
  }

  // ============================================================
  // YAML WORKFLOW ENGINE — Bridge from Temporal to WorkflowEngineService
  // ============================================================

  @Override
  public void executeWorkflow(String workflowName, String stateName,
      String eventType, Map<String, Object> workflowData) {

    log.info("[Activity] executeWorkflow — workflow='{}', state='{}', eventType='{}'",
        workflowName, stateName, eventType);

    // Build WorkflowContext from the data map
    WorkflowContext workflowContext = new WorkflowContext();

    for (Map.Entry<String, Object> entry : workflowData.entrySet()) {
      workflowContext.put(entry.getKey(), entry.getValue());
    }

    log.info("[Activity] executeWorkflow — context populated with {} entries: {}",
        workflowData.size(), workflowData.keySet());

    // Delegate to the YAML workflow engine
    workflowEngineService.execute(workflowName, stateName, eventType, workflowContext);

    log.info("[Activity] executeWorkflow — completed for eventType='{}'", eventType);
  }

}
