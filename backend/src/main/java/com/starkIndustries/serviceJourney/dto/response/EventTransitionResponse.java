package com.starkIndustries.serviceJourney.dto.response;

import java.time.Instant;

import com.starkIndustries.serviceJourney.model.EventStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after a successful event transition.
 * Contains the newly created event details plus linkage to previous event.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventTransitionResponse {

  public String sessionId;
  public String previousEventId;
  public String currentEventId;
  public String currentPage;
  public int sequenceOrder;
  public Instant eventStartTime;
  public EventStatus status;

}
