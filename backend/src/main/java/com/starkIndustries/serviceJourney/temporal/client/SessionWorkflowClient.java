package com.starkIndustries.serviceJourney.temporal.client;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.starkIndustries.serviceJourney.dto.request.EventTransitionRequest;
import com.starkIndustries.serviceJourney.dto.request.SessionEndRequest;
import com.starkIndustries.serviceJourney.dto.request.SessionStartRequest;
import com.starkIndustries.serviceJourney.temporal.config.TemporalConfig;
import com.starkIndustries.serviceJourney.temporal.workflow.SessionWorkflow;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SessionWorkflowClient {

  @Autowired
  private WorkflowClient workflowClient;


  public Map<String, Object> startSessionWorkflow(
      SessionStartRequest request) {

    String sessionId = UUID.randomUUID().toString();

    log.info("Starting Temporal workflow [{}] for user [{}]",
        sessionId, request.userId);

    WorkflowOptions options = WorkflowOptions.newBuilder()
        .setWorkflowId(sessionId)
        .setTaskQueue(TemporalConfig.TASK_QUEUE)
        .build();

    SessionWorkflow workflow = workflowClient.newWorkflowStub(
        SessionWorkflow.class,
        options);

    WorkflowClient.start(
        workflow::startSession,
        sessionId,
        request.userId);

    log.info("Workflow [{}] started successfully", sessionId);

    return Map.of(
        "sessionId", sessionId,
        "userId", request.userId,
        "workflowId", sessionId,
        "message", "Session workflow started");
  }

 
  public Map<String, Object> transitionEvent(
      EventTransitionRequest request) {

    log.info("Sending transition signal to workflow [{}]",
        request.sessionId);

    SessionWorkflow workflow = workflowClient.newWorkflowStub(
        SessionWorkflow.class,
        request.sessionId);

    workflow.transitionEvent(
        request.previousEventId,
        request.previousScreenName,
        request.nextScreenName);

    log.info("Transition signal sent successfully to workflow [{}]",
        request.sessionId);

    return Map.of(
        "sessionId", request.sessionId,
        "nextScreen", request.nextScreenName,
        "message", "Event transition signal sent");
  }


  public Map<String, Object> endSession(
      SessionEndRequest request) {

    log.info("Sending endSession signal to workflow [{}]",
        request.sessionId);

    SessionWorkflow workflow = workflowClient.newWorkflowStub(
        SessionWorkflow.class,
        request.sessionId);

    String reason = (request.getExpiryReasons() != null)
        ? request.getExpiryReasons().name()
        : "LOGOUT";

    workflow.endSession(reason);

    log.info("End session signal sent to workflow [{}]",
        request.sessionId);

    return Map.of(
        "sessionId", request.sessionId,
        "reason", reason,
        "message", "Session end signal sent");
  }


  public String getWorkflowState(String sessionId) {

    SessionWorkflow workflow = workflowClient.newWorkflowStub(
        SessionWorkflow.class,
        sessionId);

    return workflow.getSessionState();
  }

}