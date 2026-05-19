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
public class EventStartRequest {

  @NotBlank(message = "Session Id is missing")
  public String sessionId;

  @NotBlank(message = "page is missing")
  public String page;
  
}
