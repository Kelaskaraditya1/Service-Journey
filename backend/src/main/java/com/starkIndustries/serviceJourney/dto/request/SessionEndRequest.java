package com.starkIndustries.serviceJourney.dto.request;

import com.starkIndustries.serviceJourney.model.ExpiryReasons;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public class SessionEndRequest {

    @NotBlank(message = "Session Id is missing")
    public String sessionId;

    public ExpiryReasons expiryReasons;
    
  }
