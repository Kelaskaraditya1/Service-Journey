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
 * SessionService is the primary orchestration layer for session lifecycle.
 * 
 * It handles:
 *   - Session creation and teardown
 *   - Event transitions (the new unified API)
 *   - Session timeout/abort logic
 * 
 * In future Temporal integration (Step 2), this class maps to:
 *   - startSession()       → Workflow start
 *   - eventTransition()    → Workflow signal
 *   - endSession()         → Workflow signal
 *   - forceAbortSession()  → Workflow timer callback
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
  // SESSION LIFECYCLE
  // ======================================================================

  /**
   * Creates a new session for the given user.
   * In Step 2, this will also start a Temporal workflow.
   */
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

    log.info("Session [{}] started for user [{}]", session.sessionId, session.userId);

    return buildSessionResponse(session);
  }

  /**
   * Ends a session gracefully (user-initiated or system-initiated).
   * Closes any open event, sets end time and computes duration.
   */
  public SessionResponse endSession(SessionEndRequest sessionEndRequest) {

    Instant now = Instant.now();

    Session session = this.sessionRepository.findById(sessionEndRequest.sessionId)
        .orElseThrow(
            () -> {
              log.error("Session end failed — session [{}] not found", sessionEndRequest.sessionId);
              return new CustomException(HttpStatus.BAD_REQUEST,
                  "Session with session id " + sessionEndRequest.sessionId + " does not exist");
            });

    // Close any currently open event
    Event openEvent = eventService.findOpenEvent(session.sessionId);
    if (openEvent != null) {
      eventService.completeEvent(openEvent, now);
      log.debug("Auto-closed open event [{}] during session end", openEvent.eventId);
    }

    // Determine expiry reason
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

    log.info("Session [{}] ended — reason: {}, duration: {}ms",
        session.sessionId, reason, session.duration);

    return buildSessionResponse(session);
  }

  // ======================================================================
  // EVENT TRANSITION — The new unified API
  // ======================================================================

  /**
   * Handles navigation between screens within a session.
   * 
   * This single method replaces the old /event/start + /event/end pattern.
   * It atomically:
   *   1. Validates the session is active
   *   2. Completes the previous event (if any)
   *   3. Creates the next event with proper sequencing
   *   4. Updates session tracking fields
   * 
   * In Step 2, this becomes a Temporal Signal on the session workflow.
   */
  public EventTransitionResponse eventTransition(EventTransitionRequest request) {

    Instant now = Instant.now();

    // 1 — Validate session exists and is active
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

    // 2 — Complete previous event
    String previousEventId = null;

    if (request.previousEventId != null) {
      // Explicit previous event ID provided
      Event previousEvent = this.eventRepository.findById(request.previousEventId)
          .orElseThrow(() -> {
            log.error("Event transition failed — previous event [{}] not found", request.previousEventId);
            return new CustomException(HttpStatus.BAD_REQUEST,
                "Previous event " + request.previousEventId + " does not exist");
          });

      eventService.completeEvent(previousEvent, now);
      previousEventId = previousEvent.eventId;
    } else {
      // Auto-detect: find any open event for this session and close it
      Event openEvent = eventService.findOpenEvent(session.sessionId);
      if (openEvent != null) {
        eventService.completeEvent(openEvent, now);
        previousEventId = openEvent.eventId;
        log.debug("Auto-detected and closed open event [{}]", openEvent.eventId);
      }
    }

    // 3 — Calculate next sequence order
    int nextSequence = (session.getEventCount() != null ? session.getEventCount() : 0) + 1;

    // 4 — Create the new event
    Event newEvent = eventService.createEvent(session, request.nextScreenName, nextSequence, previousEventId);

    // 5 — Update session tracking
    session.setLastPage(request.nextScreenName);
    session.setLastActivityTime(now);
    session.setActiveEventId(newEvent.eventId);
    session.setEventCount(nextSequence);

    this.sessionRepository.save(session);

    log.info("Event transition in session [{}]: '{}' → '{}' (sequence: {})",
        session.sessionId,
        request.previousScreenName != null ? request.previousScreenName : "START",
        request.nextScreenName,
        nextSequence);

    // 6 — Build and return response
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
  // SESSION TIMEOUT / FORCE ABORT
  // In Step 2, this logic moves into Temporal timer callbacks.
  // ======================================================================

  /**
   * Force-aborts a session due to timeout (absolute or inactivity).
   * Called by the RequestInterceptor when timeout conditions are detected.
   * In Step 2, Temporal workflow timers will handle this instead.
   */
  public void forceAbortSession(Session session, ExpiryReasons expiryReasons) {

    if (session.endTime != null) {
      log.debug("Session [{}] already ended, skipping force abort", session.sessionId);
      return;
    }

    Instant now = Instant.now();

    // Close any open event
    Event openEvent = eventService.findOpenEvent(session.sessionId);
    if (openEvent != null) {
      eventService.completeEvent(openEvent, now);
    }

    // Determine session status from expiry reason
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

    log.warn("Session [{}] force-aborted — reason: {}, duration: {}ms",
        session.sessionId, expiryReasons, session.duration);
  }

  // ======================================================================
  // QUERIES
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
