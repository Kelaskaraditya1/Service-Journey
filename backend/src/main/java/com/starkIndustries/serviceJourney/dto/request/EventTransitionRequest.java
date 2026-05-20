package com.starkIndustries.serviceJourney.dto.request;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for the unified event-transition API.
 * 
 * This replaces the old separate /event/start + /event/end calls.
 * Frontend sends a single request when user navigates between screens.
 * 
 * In future Temporal integration, this maps to a Signal on the session workflow.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventTransitionRequest {

  @NotBlank(message = "Session Id is required")
  public String sessionId;

  // Nullable — will be null for the very first transition in a session
  public String previousEventId;

  // The screen the user is leaving (nullable for first transition)
  public String previousScreenName;

  // The screen the user is navigating to
  @NotBlank(message = "Next screen name is required")
  public String nextScreenName;

  // Client-side timestamp of the transition (optional, server uses Instant.now() if null)
  public Long timestamp;

  // Optional key-value metadata for future use (e.g., device info, action context)
  public Map<String, Object> metadata;

}
