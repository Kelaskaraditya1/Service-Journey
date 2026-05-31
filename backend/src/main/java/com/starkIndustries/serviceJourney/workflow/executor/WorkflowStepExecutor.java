package com.starkIndustries.serviceJourney.workflow.executor;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.starkIndustries.serviceJourney.model.Event;
import com.starkIndustries.serviceJourney.model.EventStatus;
import com.starkIndustries.serviceJourney.model.ExpiryReasons;
import com.starkIndustries.serviceJourney.model.Session;
import com.starkIndustries.serviceJourney.model.SessionStatus;
import com.starkIndustries.serviceJourney.repository.EventRepository;
import com.starkIndustries.serviceJourney.repository.SessionRepository;
import com.starkIndustries.serviceJourney.workflow.context.WorkflowContext;

import lombok.extern.slf4j.Slf4j;

/**
 * WorkflowStepExecutor — Contains all business logic for workflow steps.
 *
 * Each public method corresponds to a step name in workflow.yml.
 * Methods are invoked via reflection by WorkflowEngineService.
 *
 * All methods receive a WorkflowContext and read/write runtime data from it.
 * This class is the ONLY place that touches the database for workflow execution.
 *
 * Available steps:
 *   - completePreviousEvent  (onTransitionEvent)
 *   - createNewEvent         (onTransitionEvent)
 *   - updateSessionTracking  (onTransitionEvent)
 *   - completeSession        (onEndSession)
 *   - abortSession           (onTimeout)
 */
@Service
@Slf4j
public class WorkflowStepExecutor {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SessionRepository sessionRepository;

    // =========================================================
    // COMPLETE PREVIOUS EVENT
    // Called during: onTransitionEvent
    // =========================================================

    public void completePreviousEvent(WorkflowContext context) {

        String currentEventId = context.getString("currentEventId");

        if (currentEventId == null) {
            log.info("[Executor] completePreviousEvent — no currentEventId, skipping (first transition)");
            return;
        }

        log.info("[Executor] completePreviousEvent — closing event [{}]", currentEventId);

        Event event = eventRepository
                .findById(currentEventId)
                .orElse(null);

        if (event == null) {
            log.warn("[Executor] completePreviousEvent — event [{}] not found in DB, skipping",
                    currentEventId);
            return;
        }

        if (event.getExitTime() != null) {
            log.info("[Executor] completePreviousEvent — event [{}] already completed, skipping",
                    currentEventId);
            return;
        }

        Instant now = Instant.now();

        event.setExitTime(now);
        event.setTimeSpent(
                now.toEpochMilli() -
                event.getEnterTime().toEpochMilli());
        event.setStatus(EventStatus.COMPLETED);

        eventRepository.save(event);

        log.info("[Executor] completePreviousEvent — event [{}] closed, page='{}', duration={}ms",
                currentEventId, event.getPage(), event.getTimeSpent());
    }

    // =========================================================
    // CREATE NEW EVENT
    // Called during: onTransitionEvent
    // =========================================================

    public void createNewEvent(WorkflowContext context) {

        String sessionId = context.getString("sessionId");
        String nextScreen = context.getString("nextScreen");
        Integer sequenceNumber = context.getInteger("sequenceNumber");
        String previousEventId = context.getString("currentEventId");

        log.info("[Executor] createNewEvent — session=[{}], page='{}', sequence={}",
                sessionId, nextScreen, sequenceNumber);

        Session session = sessionRepository
                .findById(sessionId)
                .orElseThrow(() -> {
                    log.error("[Executor] createNewEvent — session [{}] not found", sessionId);
                    return new RuntimeException("Session not found: " + sessionId);
                });

        String newEventId = UUID.randomUUID().toString();

        Event event = Event.builder()
                .eventId(newEventId)
                .page(nextScreen)
                .enterTime(Instant.now())
                .session(session)
                .sequenceOrder(sequenceNumber)
                .status(EventStatus.ACTIVE)
                .previousEventId(previousEventId)
                .build();

        eventRepository.save(event);

        // Update context so downstream steps can use the new event ID
        context.put("currentEventId", newEventId);

        log.info("[Executor] createNewEvent — created event [{}] on page '{}' for session [{}]",
                newEventId, nextScreen, sessionId);
    }

    // =========================================================
    // UPDATE SESSION TRACKING
    // Called during: onTransitionEvent
    // =========================================================

    public void updateSessionTracking(WorkflowContext context) {

        String sessionId = context.getString("sessionId");
        String nextScreen = context.getString("nextScreen");
        String activeEventId = context.getString("currentEventId");
        Integer sequenceNumber = context.getInteger("sequenceNumber");

        log.info("[Executor] updateSessionTracking — session=[{}], page='{}', eventCount={}",
                sessionId, nextScreen, sequenceNumber);

        Session session = sessionRepository
                .findById(sessionId)
                .orElseThrow(() -> {
                    log.error("[Executor] updateSessionTracking — session [{}] not found", sessionId);
                    return new RuntimeException("Session not found: " + sessionId);
                });

        session.setLastPage(nextScreen);
        session.setActiveEventId(activeEventId);
        session.setEventCount(sequenceNumber);
        session.setLastActivityTime(Instant.now());

        sessionRepository.save(session);

        log.info("[Executor] updateSessionTracking — session [{}] updated successfully", sessionId);
    }

    // =========================================================
    // COMPLETE SESSION (Logout)
    // Called during: onEndSession
    // =========================================================

    public void completeSession(WorkflowContext context) {

        String sessionId = context.getString("sessionId");
        String currentEventId = context.getString("currentEventId");
        String endReason = context.getString("endReason");

        log.info("[Executor] completeSession — session=[{}], reason='{}', activeEvent=[{}]",
                sessionId, endReason, currentEventId);

        // Close any active event first
        if (currentEventId != null) {
            Event event = eventRepository.findById(currentEventId).orElse(null);

            if (event != null && event.getExitTime() == null) {
                Instant now = Instant.now();
                event.setExitTime(now);
                event.setTimeSpent(now.toEpochMilli() - event.getEnterTime().toEpochMilli());
                event.setStatus(EventStatus.COMPLETED);
                eventRepository.save(event);

                log.info("[Executor] completeSession — closed active event [{}], duration={}ms",
                        currentEventId, event.getTimeSpent());
            }
        }

        // Complete the session
        Session session = sessionRepository
                .findById(sessionId)
                .orElseThrow(() -> {
                    log.error("[Executor] completeSession — session [{}] not found", sessionId);
                    return new RuntimeException("Session not found: " + sessionId);
                });

        Instant now = Instant.now();

        session.setEndTime(now);
        session.setSessionStatus(SessionStatus.COMPLETED);
        session.setExpiryReasons(ExpiryReasons.LOGOUT);
        session.setExpired(false);
        session.setDuration(now.toEpochMilli() - session.getStartTime().toEpochMilli());
        session.setActiveEventId(null);

        sessionRepository.save(session);

        log.info("[Executor] completeSession — session [{}] completed, duration={}ms",
                sessionId, session.getDuration());
    }

    // =========================================================
    // ABORT SESSION (Inactivity Timeout)
    // Called during: onTimeout
    // =========================================================

    public void abortSession(WorkflowContext context) {

        String sessionId = context.getString("sessionId");
        String currentEventId = context.getString("currentEventId");
        String reason = context.getString("endReason");

        if (reason == null) {
            reason = "INACTIVITY";
        }

        log.warn("[Executor] abortSession — session=[{}], reason='{}', activeEvent=[{}]",
                sessionId, reason, currentEventId);

        // Close any active event first
        if (currentEventId != null) {
            Event event = eventRepository.findById(currentEventId).orElse(null);

            if (event != null && event.getExitTime() == null) {
                Instant now = Instant.now();
                event.setExitTime(now);
                event.setTimeSpent(now.toEpochMilli() - event.getEnterTime().toEpochMilli());
                event.setStatus(EventStatus.COMPLETED);
                eventRepository.save(event);

                log.info("[Executor] abortSession — closed active event [{}], duration={}ms",
                        currentEventId, event.getTimeSpent());
            }
        }

        // Abort the session
        Session session = sessionRepository
                .findById(sessionId)
                .orElseThrow(() -> {
                    log.error("[Executor] abortSession — session [{}] not found", sessionId);
                    return new RuntimeException("Session not found: " + sessionId);
                });

        Instant now = Instant.now();

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

        log.warn("[Executor] abortSession — session [{}] aborted, reason='{}', duration={}ms",
                sessionId, reason, session.getDuration());
    }
}