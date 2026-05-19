package com.starkIndustries.serviceJourney.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventEndRequest {

  @NotBlank(message = "Event Id is missing")
  public String eventId;
  
}
