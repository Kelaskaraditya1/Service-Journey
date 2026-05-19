package com.starkIndustries.serviceJourney.dto.response;

import java.time.Instant;
import com.starkIndustries.serviceJourney.model.ExpiryReasons;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionResponse {

  public String sessionId;
  public String userId;
  public Instant startTime;
  public Instant endTime;
  public boolean expired;
  public ExpiryReasons expiryReasons;
  public String lastPage;
  public Long duration;
  public Instant lastActivityTime;
  
}
