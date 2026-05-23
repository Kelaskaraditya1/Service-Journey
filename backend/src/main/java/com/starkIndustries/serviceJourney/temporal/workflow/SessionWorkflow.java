package com.starkIndustries.serviceJourney.temporal.workflow;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface SessionWorkflow {

  @WorkflowMethod
  void startSession(String sessionId, String userId);

  @SignalMethod
  void transitionEvent(String previousEventId, String previousScreen, String nextScreen);

  @SignalMethod
  void endSession(String reason);

  @QueryMethod
  String getSessionState();

}
