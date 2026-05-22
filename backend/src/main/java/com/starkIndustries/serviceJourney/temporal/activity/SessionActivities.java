package com.starkIndustries.serviceJourney.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;


@ActivityInterface
public interface SessionActivities {

  @ActivityMethod
  void createSession(String sessionId, String userId);

  @ActivityMethod
  String createEvent(String sessionId, String eventId, String page,
      int sequenceOrder, String previousEventId);

  @ActivityMethod
  void completeEvent(String eventId);


  @ActivityMethod
  void updateSessionTracking(String sessionId, String lastPage,
      String activeEventId, int eventCount);


  @ActivityMethod
  void completeSession(String sessionId, String reason);

  @ActivityMethod
  void abortSession(String sessionId, String reason);

}
