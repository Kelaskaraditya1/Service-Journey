package com.starkIndustries.serviceJourney.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.starkIndustries.serviceJourney.model.Event;
import com.starkIndustries.serviceJourney.model.EventStatus;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

  /** Find the currently open event (no exit time) for a session — used by legacy and transition logic */
  Event findBySession_SessionIdAndExitTimeIsNull(String sessionId);

  /** Find the currently ACTIVE event for a session using the new status field */
  Optional<Event> findBySession_SessionIdAndStatus(String sessionId, EventStatus status);

  /** Get all events for a session ordered by sequence */
  List<Event> findBySession_SessionIdOrderBySequenceOrderAsc(String sessionId);

}
