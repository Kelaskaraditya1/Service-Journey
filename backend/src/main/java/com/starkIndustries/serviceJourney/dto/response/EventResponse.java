package com.starkIndustries.serviceJourney.dto.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventResponse {

  public String eventId;
  public String sessionId;
  public String page;
  public Instant enterTime;
  public Instant exitTime;
  public Long timeSpent;
  
}
