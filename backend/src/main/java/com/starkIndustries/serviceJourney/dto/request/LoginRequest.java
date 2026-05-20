package com.starkIndustries.serviceJourney.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequest {

@NotBlank(message = "Phone number is required")
@Pattern(regexp = "^[0-9]{10}$", message = "Contact number should be exactly 10 digits")
public String contactNumber;

@NotNull(message = "Identity Type is required")
public IdentityType identityType;

@NotBlank(message = "identity is required")
public String identity;
  
}
