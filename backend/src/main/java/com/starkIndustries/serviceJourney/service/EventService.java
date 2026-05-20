package com.starkIndustries.serviceJourney.service;

import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.starkIndustries.serviceJourney.dto.request.EventEndRequest;
import com.starkIndustries.serviceJourney.dto.request.EventStartRequest;
import com.starkIndustries.serviceJourney.dto.response.EventResponse;
import com.starkIndustries.serviceJourney.expection.CustomException;
import com.starkIndustries.serviceJourney.model.Event;
import com.starkIndustries.serviceJourney.model.EventStatus;
import com.starkIndustries.serviceJourney.model.Session;
import com.starkIndustries.serviceJourney.repository.EventRepository;
import com.starkIndustries.serviceJourney.repository.SessionRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * EventService handles low-level event operations.
 * 
 * In the new architecture, this acts as an internal helper layer.
 * The public-facing orchestration (event transitions) is handled by SessionService.
 * 
 * In future Temporal integration (Step 2), these methods will become Temporal Activities.
 */
@Service
@Slf4j
public class EventService {

  @Autowired
  public EventRepository eventRepository;

  @Autowired
  public SessionRepository sessionRepository;

  // ======================================================================
  // NEW INTERNAL HELPER METHODS — Used by SessionService for transitions
  // These will become Temporal Activities in Step 2
  // ======================================================================

  /**
   * Creates a new event linked to the given session.
   * This is an internal method — not exposed as an API endpoint.
   */
  public Event createEvent(Session session, String page, int sequenceOrder, String previousEventId) {

    Instant now = Instant.now();

    Event event = Event.builder()
        .eventId(UUID.randomUUID().toString())
        .session(session)
        .page(page)
        .enterTime(now)
        .exitTime(null)
        .timeSpent(null)
        .sequenceOrder(sequenceOrder)
        .status(EventStatus.ACTIVE)
        .previousEventId(previousEventId)
        .build();

    this.eventRepository.save(event);

    log.info("Created event [{}] on page '{}' for session [{}], sequence: {}",
        event.eventId, page, session.sessionId, sequenceOrder);

    return event;
  }

  /**
   * Completes an event by setting its exit time and calculating duration.
   * This is an internal method — not exposed as an API endpoint.
   */
  public Event completeEvent(Event event, Instant exitTime) {

    if (event.exitTime != null) {
      log.warn("Event [{}] is already completed, skipping", event.eventId);
      return event;
    }

    event.setExitTime(exitTime);
    event.setTimeSpent(exitTime.toEpochMilli() - event.enterTime.toEpochMilli());
    event.setStatus(EventStatus.COMPLETED);

    this.eventRepository.save(event);

    log.info("Completed event [{}] on page '{}', duration: {}ms",
        event.eventId, event.page, event.timeSpent);

    return event;
  }

  /**
   * Finds the currently open/active event for a given session.
   * Tries the new status-based query first, falls back to exitTime-based query.
   */
  public Event findOpenEvent(String sessionId) {
    return this.eventRepository.findBySession_SessionIdAndStatus(sessionId, EventStatus.ACTIVE)
        .orElse(this.eventRepository.findBySession_SessionIdAndExitTimeIsNull(sessionId));
  }

  // ======================================================================
  // DEPRECATED METHODS — Original /event/start and /event/end logic.
  // Kept for reference. These were the old public API handlers.
  // In the new architecture, use SessionService.eventTransition() instead.
  // ======================================================================

  /**
   * @deprecated Use {@link SessionService#eventTransition} instead.
   * This was the handler for the old POST /event/start API.
   * The logic has been absorbed into the event-transition flow.
   */
  @Deprecated
  public EventResponse startEvent(EventStartRequest eventStartRequest) {

    Instant now = Instant.now();

    Session session = this.sessionRepository.findById(eventStartRequest.sessionId)
        .orElseThrow(
            () -> new CustomException(HttpStatus.BAD_REQUEST,
                "Session with session id " + eventStartRequest.sessionId + " does not exist"));

    session.lastPage = eventStartRequest.page;
    session.lastActivityTime = now;

    this.sessionRepository.save(session);

    Event openEvent = this.eventRepository.findBySession_SessionIdAndExitTimeIsNull(eventStartRequest.sessionId);

    if (openEvent != null) {
      openEvent.setExitTime(now);
      openEvent.setTimeSpent(openEvent.exitTime.toEpochMilli() - openEvent.enterTime.toEpochMilli());

      this.eventRepository.save(openEvent);
    }

    Event event = Event.builder()
        .eventId(UUID.randomUUID().toString())
        .session(session)
        .page(eventStartRequest.page)
        .enterTime(now)
        .exitTime(null)
        .timeSpent(null)
        .build();

    this.eventRepository.save(event);

    return EventResponse.builder()
        .eventId(event.eventId)
        .sessionId(session.sessionId)
        .page(event.page)
        .enterTime(event.enterTime)
        .exitTime(event.exitTime)
        .timeSpent(event.timeSpent)
        .build();

  }

  /**
   * @deprecated Use {@link SessionService#eventTransition} instead.
   * This was the handler for the old POST /event/end API.
   */
  @Deprecated
  public EventResponse endEvent(EventEndRequest eventEndRequest) {

    Event event = this.eventRepository.findById(eventEndRequest.eventId)
        .orElseThrow(
            () -> new CustomException(HttpStatus.BAD_REQUEST,
                "Event for the event Id " + eventEndRequest.eventId + " does not exist"));

    if (event.exitTime == null) {
      event.exitTime = Instant.now();
      event.timeSpent = event.exitTime.toEpochMilli() - event.enterTime.toEpochMilli();
    }

    this.eventRepository.save(event);

    return EventResponse.builder()
        .eventId(event.eventId)
        .sessionId(event.session.sessionId)
        .page(event.page)
        .enterTime(event.enterTime)
        .exitTime(event.exitTime)
        .timeSpent(event.timeSpent)
        .build();

  }

}
