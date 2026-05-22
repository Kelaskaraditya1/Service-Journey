package com.starkIndustries.serviceJourney.temporal.activity;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.starkIndustries.serviceJourney.model.Event;
import com.starkIndustries.serviceJourney.model.EventStatus;
import com.starkIndustries.serviceJourney.model.ExpiryReasons;
import com.starkIndustries.serviceJourney.model.Session;
import com.starkIndustries.serviceJourney.model.SessionStatus;
import com.starkIndustries.serviceJourney.repository.EventRepository;
import com.starkIndustries.serviceJourney.repository.SessionRepository;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class SessionActivitiesImpl implements SessionActivities {

  @Autowired
  private SessionRepository sessionRepository;

  @Autowired
  private EventRepository eventRepository;

  // ============================================================
  // SESSION OPERATIONS
  // ============================================================

  @Override
  public void createSession(String sessionId, String userId) {

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
        .workflowId(sessionId) // Workflow ID = Session ID
        .activeEventId(null)
        .eventCount(0)
        .build();

    sessionRepository.save(session);

    log.info("[Activity] Created session [{}] for user [{}]", sessionId, userId);
  }

  @Override
  public void updateSessionTracking(String sessionId, String lastPage,
      String activeEventId, int eventCount) {

    Session session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

    session.setLastPage(lastPage);
    session.setLastActivityTime(Instant.now());
    session.setActiveEventId(activeEventId);
    session.setEventCount(eventCount);

    sessionRepository.save(session);

    log.debug("[Activity] Updated session [{}] tracking — page: '{}', eventCount: {}",
        sessionId, lastPage, eventCount);
  }

  @Override
  public void completeSession(String sessionId, String reason) {  // when user logout's

    Session session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

    Instant now = Instant.now();

    session.setEndTime(now);
    session.setSessionStatus(SessionStatus.COMPLETED);
    session.setExpiryReasons(ExpiryReasons.LOGOUT);
    session.setExpired(false); // Not expired — user chose to leave
    session.setDuration(now.toEpochMilli() - session.getStartTime().toEpochMilli());
    session.setActiveEventId(null);

    sessionRepository.save(session);

    log.info("[Activity] Completed session [{}] — reason: {}, duration: {}ms",
        sessionId, reason, session.getDuration());
  }

  @Override
  public void abortSession(String sessionId, String reason) {

    Session session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

    Instant now = Instant.now();

    // Determine status based on reason string
    ExpiryReasons expiryReason;
    SessionStatus status;

    if ("ABSOLUTE".equalsIgnoreCase(reason)) {
      expiryReason = ExpiryReasons.ABSOLUTE;
      status = SessionStatus.EXPIRED_ABSOLUTE;
    } else {
      expiryReason = ExpiryReasons.INACTIVITY;
      status = SessionStatus.EXPIRED_INACTIVITY;
    }

    session.setEndTime(now);
    session.setSessionStatus(status);
    session.setExpiryReasons(expiryReason);
    session.setExpired(true);
    session.setDuration(now.toEpochMilli() - session.getStartTime().toEpochMilli());
    session.setActiveEventId(null);

    sessionRepository.save(session);

    log.warn("[Activity] Aborted session [{}] — reason: {}, duration: {}ms",
        sessionId, reason, session.getDuration());
  }

  // ============================================================
  // EVENT OPERATIONS
  // ============================================================

  @Override
  public String createEvent(String sessionId, String eventId, String page,
      int sequenceOrder, String previousEventId) {

    Session session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

    Event event = Event.builder()
        .eventId(eventId)
        .session(session)
        .page(page)
        .enterTime(Instant.now())
        .exitTime(null)
        .timeSpent(null)
        .sequenceOrder(sequenceOrder)
        .status(EventStatus.ACTIVE)
        .previousEventId(previousEventId)
        .build();

    eventRepository.save(event);

    log.info("[Activity] Created event [{}] on page '{}' for session [{}], sequence: {}",
        eventId, page, sessionId, sequenceOrder);

    return eventId;
  }

  @Override
  public void completeEvent(String eventId) {

    Event event = eventRepository.findById(eventId).orElse(null);

    if (event == null) {
      log.warn("[Activity] Event [{}] not found, skipping completion", eventId);
      return;
    }

    if (event.getExitTime() != null) {
      log.debug("[Activity] Event [{}] already completed, skipping", eventId);
      return;
    }

    Instant now = Instant.now();
    event.setExitTime(now);
    event.setTimeSpent(now.toEpochMilli() - event.getEnterTime().toEpochMilli());
    event.setStatus(EventStatus.COMPLETED);

    eventRepository.save(event);

    log.info("[Activity] Completed event [{}] on page '{}', duration: {}ms",
        eventId, event.getPage(), event.getTimeSpent());

  }

}
