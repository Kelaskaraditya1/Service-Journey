package com.starkIndustries.serviceJourney.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.starkIndustries.serviceJourney.dto.request.LoginRequest;
import com.starkIndustries.serviceJourney.dto.request.SignupRequest;
import com.starkIndustries.serviceJourney.dto.response.ApiResponse;
import com.starkIndustries.serviceJourney.model.Users;
import com.starkIndustries.serviceJourney.service.AuthenticationService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/auth")
@RestController
@Slf4j
public class AuthenticationController {

  @Autowired
  public AuthenticationService authenticationService;

  @PostMapping("/signup")
   public ResponseEntity<ApiResponse<Users>> signup(@Valid @RequestBody SignupRequest signupRequest) {
   
      log.info("POST /auth/signup — username: {}", signupRequest.username);
   
      Users users = this.authenticationService.signup(signupRequest);
   
      return ResponseEntity.status(HttpStatus.OK)
          .body(ApiResponse.success(users, "Signup successful"));
    }
  

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<Users>> login(@RequestBody LoginRequest loginRequest) {

    log.info("POST /auth/login — contact: {}, identityType: {}",
        loginRequest.contactNumber, loginRequest.identityType);

    Users users = this.authenticationService.login(loginRequest);

    log.info("Login successful for user [{}]", users.userId);

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(users, "Login successful"));
  }

}
