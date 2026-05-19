package com.starkIndustries.serviceJourney.dto.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SignupRequest {

  @NotBlank(message = "Name is required")
  public String name;

  @NotBlank(message = "Contact is required")
  @Pattern(regexp = "^[0-9]{10}$",message = "Contact should be of 10 digits")
  public String contact;

  @NotNull(message = "Date of Birth is required")
  @JsonFormat(pattern = "dd-MM-yyyy")
  public LocalDate dateOfBirth;

  @NotBlank(message = "Pan Number is required")
  @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Please enter proper Pan Card number.")
  public String panNumber;

  @NotBlank(message = "Email is required")
  @Email
  public String email;

  @NotBlank(message = "Username is required")
  public String username;

  @NotBlank(message = "Password is required")
  public String password;
  
}
