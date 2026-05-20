package com.starkIndustries.serviceJourney.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.starkIndustries.serviceJourney.dto.request.EventTransitionRequest;
import com.starkIndustries.serviceJourney.dto.request.SessionEndRequest;
import com.starkIndustries.serviceJourney.dto.request.SessionStartRequest;
import com.starkIndustries.serviceJourney.dto.response.EventTransitionResponse;
import com.starkIndustries.serviceJourney.dto.response.SessionResponse;
import com.starkIndustries.serviceJourney.expection.CustomException;
import com.starkIndustries.serviceJourney.keys.Keys;
import com.starkIndustries.serviceJourney.model.Event;
import com.starkIndustries.serviceJourney.model.EventStatus;
import com.starkIndustries.serviceJourney.model.ExpiryReasons;
import com.starkIndustries.serviceJourney.model.Session;
import com.starkIndustries.serviceJourney.model.SessionStatus;
import com.starkIndustries.serviceJourney.repository.EventRepository;
import com.starkIndustries.serviceJourney.repository.SessionRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * ============================================================
 * SessionService — Database Query & Legacy Orchestration Layer
 * ============================================================
 * 
 * STEP 2 STATUS:
 *   Orchestration methods (startSession, eventTransition, endSession)
 *   are now DEPRECATED because Temporal workflow handles orchestration.
 * 
 *   These methods are kept for:
 *     - Reference/learning purposes
 *     - Fallback if Temporal is not running
 *     - Understanding the business logic flow
 * 
 *   ACTIVE methods (still used by controllers):
 *     - getSession()      → DB query
 *     - getAllSessions()   → DB query
 *     - forceAbortSession() → Used by RequestInterceptor (legacy)
 */
@Service
@Slf4j
public class SessionService {

  @Autowired
  public SessionRepository sessionRepository;

  @Autowired
  public EventRepository eventRepository;

  @Autowired
  public EventService eventService;

  // ======================================================================
  // ACTIVE METHODS — Still used by controllers and interceptors
  // ======================================================================

  public List<Session> getAllSessions() {
    return this.sessionRepository.findAll();
  }

  public Session getSession(String sessionId) {
    return this.sessionRepository.findById(sessionId)
        .orElseThrow(() -> {
          log.error("Session [{}] not found", sessionId);
          return new CustomException(HttpStatus.BAD_REQUEST,
              "Session with session Id " + sessionId + " does not exist");
        });
  }

  // ======================================================================
  // FORCE ABORT — Still used by RequestInterceptor (legacy timeout check)
  // In the Temporal architecture, workflow timers handle this instead.
  // The interceptor-based approach is kept temporarily as a safety net.
  // ======================================================================

  /**
   * Force-aborts a session due to timeout (absolute or inactivity).
   * Called by the RequestInterceptor when timeout conditions are detected.
   * 
   * NOTE: With Temporal, the workflow's inactivity timer is the PRIMARY
   * timeout mechanism. This interceptor-based approach is a SECONDARY
   * safety net that can be removed once Temporal is fully trusted.
   */
  public void forceAbortSession(Session session, ExpiryReasons expiryReasons) {

    if (session.endTime != null) {
      log.debug("Session [{}] already ended, skipping force abort", session.sessionId);
      return;
    }

    Instant now = Instant.now();

    Event openEvent = eventService.findOpenEvent(session.sessionId);
    if (openEvent != null) {
      eventService.completeEvent(openEvent, now);
    }

    SessionStatus status = (expiryReasons == ExpiryReasons.ABSOLUTE)
        ? SessionStatus.EXPIRED_ABSOLUTE
        : SessionStatus.EXPIRED_INACTIVITY;

    session.setEndTime(now);
    session.setExpiryReasons(expiryReasons);
    session.setExpired(true);
    session.setSessionStatus(status);
    session.setDuration(session.endTime.toEpochMilli() - session.startTime.toEpochMilli());
    session.setActiveEventId(null);

    this.sessionRepository.save(session);

    log.warn("Session [{}] force-aborted (interceptor) — reason: {}, duration: {}ms",
        session.sessionId, expiryReasons, session.duration);
  }

  // ======================================================================
  // DEPRECATED ORCHESTRATION METHODS
  // ======================================================================
  //
  // These methods were the Step 1 orchestration layer.
  // In Step 2, Temporal workflow handles all orchestration.
  //
  // Controller now:
  //   - POST /session/start          → starts Temporal workflow
  //   - POST /session/event-transition → signals Temporal workflow
  //   - POST /session/end            → signals Temporal workflow
  //
  // These methods are preserved for reference and learning.
  // ======================================================================

  /**
   * @deprecated Temporal workflow now handles session creation.
   * See: SessionWorkflowImpl.startSession()
   * See: SessionActivitiesImpl.createSession()
   */
  @Deprecated
  public SessionResponse startSession(SessionStartRequest sessionStartRequest) {

    Instant now = Instant.now();

    Session session = Session.builder()
        .sessionId(UUID.randomUUID().toString())
        .userId(sessionStartRequest.userId)
        .startTime(now)
        .endTime(null)
        .expired(false)
        .expiryReasons(null)
        .sessionStatus(SessionStatus.ACTIVE)
        .lastPage(null)
        .duration(null)
        .lastActivityTime(now)
        .workflowId(null)
        .activeEventId(null)
        .eventCount(0)
        .build();

    this.sessionRepository.save(session);

    log.info("[DEPRECATED] Session [{}] started for user [{}]", session.sessionId, session.userId);

    return buildSessionResponse(session);
  }

  /**
   * @deprecated Temporal workflow now handles session end.
   * See: SessionWorkflowImpl.handleSessionEnd()
   * See: SessionActivitiesImpl.completeSession()
   */
  @Deprecated
  public SessionResponse endSession(SessionEndRequest sessionEndRequest) {

    Instant now = Instant.now();

    Session session = this.sessionRepository.findById(sessionEndRequest.sessionId)
        .orElseThrow(
            () -> {
              log.error("Session end failed — session [{}] not found", sessionEndRequest.sessionId);
              return new CustomException(HttpStatus.BAD_REQUEST,
                  "Session with session id " + sessionEndRequest.sessionId + " does not exist");
            });

    Event openEvent = eventService.findOpenEvent(session.sessionId);
    if (openEvent != null) {
      eventService.completeEvent(openEvent, now);
      log.debug("Auto-closed open event [{}] during session end", openEvent.eventId);
    }

    ExpiryReasons reason;
    SessionStatus status;

    if (sessionEndRequest.getExpiryReasons() != null
        && sessionEndRequest.getExpiryReasons() == ExpiryReasons.LOGOUT) {
      reason = ExpiryReasons.LOGOUT;
      status = SessionStatus.COMPLETED;
    } else if (now.isAfter(session.getStartTime().plusSeconds(Keys.ABSOLUTE))) {
      reason = ExpiryReasons.ABSOLUTE;
      status = SessionStatus.EXPIRED_ABSOLUTE;
    } else {
      reason = ExpiryReasons.INACTIVITY;
      status = SessionStatus.EXPIRED_INACTIVITY;
    }

    session.setEndTime(now);
    session.setExpired(reason != ExpiryReasons.LOGOUT);
    session.setExpiryReasons(reason);
    session.setSessionStatus(status);
    session.setDuration(now.toEpochMilli() - session.startTime.toEpochMilli());
    session.setLastActivityTime(now);
    session.setActiveEventId(null);

    this.sessionRepository.save(session);

    log.info("[DEPRECATED] Session [{}] ended — reason: {}, duration: {}ms",
        session.sessionId, reason, session.duration);

    return buildSessionResponse(session);
  }

  /**
   * @deprecated Temporal workflow now handles event transitions.
   * See: SessionWorkflowImpl.handleEventTransition()
   * See: SessionActivitiesImpl.createEvent(), completeEvent()
   */
  @Deprecated
  public EventTransitionResponse eventTransition(EventTransitionRequest request) {

    Instant now = Instant.now();

    Session session = this.sessionRepository.findById(request.sessionId)
        .orElseThrow(() -> {
          log.error("Event transition failed — session [{}] not found", request.sessionId);
          return new CustomException(HttpStatus.BAD_REQUEST,
              "Session with session id " + request.sessionId + " does not exist");
        });

    if (session.getSessionStatus() != SessionStatus.ACTIVE) {
      log.warn("Event transition rejected — session [{}] is {}", session.sessionId, session.getSessionStatus());
      throw new CustomException(HttpStatus.BAD_REQUEST,
          "Session " + request.sessionId + " is not active (status: " + session.getSessionStatus() + ")");
    }

    String previousEventId = null;

    if (request.previousEventId != null) {
      Event previousEvent = this.eventRepository.findById(request.previousEventId)
          .orElseThrow(() -> {
            log.error("Event transition failed — previous event [{}] not found", request.previousEventId);
            return new CustomException(HttpStatus.BAD_REQUEST,
                "Previous event " + request.previousEventId + " does not exist");
          });

      eventService.completeEvent(previousEvent, now);
      previousEventId = previousEvent.eventId;
    } else {
      Event openEvent = eventService.findOpenEvent(session.sessionId);
      if (openEvent != null) {
        eventService.completeEvent(openEvent, now);
        previousEventId = openEvent.eventId;
        log.debug("Auto-detected and closed open event [{}]", openEvent.eventId);
      }
    }

    int nextSequence = (session.getEventCount() != null ? session.getEventCount() : 0) + 1;

    Event newEvent = eventService.createEvent(session, request.nextScreenName, nextSequence, previousEventId);

    session.setLastPage(request.nextScreenName);
    session.setLastActivityTime(now);
    session.setActiveEventId(newEvent.eventId);
    session.setEventCount(nextSequence);

    this.sessionRepository.save(session);

    log.info("[DEPRECATED] Event transition in session [{}]: '{}' → '{}' (sequence: {})",
        session.sessionId,
        request.previousScreenName != null ? request.previousScreenName : "START",
        request.nextScreenName,
        nextSequence);

    return EventTransitionResponse.builder()
        .sessionId(session.sessionId)
        .previousEventId(previousEventId)
        .currentEventId(newEvent.eventId)
        .currentPage(newEvent.page)
        .sequenceOrder(newEvent.sequenceOrder)
        .eventStartTime(newEvent.enterTime)
        .status(newEvent.status)
        .build();
  }

  // ======================================================================
  // PRIVATE HELPERS
  // ======================================================================

  private SessionResponse buildSessionResponse(Session session) {
    return SessionResponse.builder()
        .sessionId(session.sessionId)
        .userId(session.userId)
        .startTime(session.startTime)
        .endTime(session.endTime)
        .expired(session.expired)
        .expiryReasons(session.expiryReasons)
        .lastPage(session.lastPage)
        .duration(session.duration)
        .lastActivityTime(session.lastActivityTime)
        .build();
  }

}
