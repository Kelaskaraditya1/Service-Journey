package com.starkIndustries.serviceJourney.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.starkIndustries.serviceJourney.dto.request.LoginRequest;
import com.starkIndustries.serviceJourney.dto.request.SignupRequest;
import com.starkIndustries.serviceJourney.keys.Keys;
import com.starkIndustries.serviceJourney.model.Users;
import com.starkIndustries.serviceJourney.service.AuthenticationService;
import jakarta.validation.Valid;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {

  @Autowired
  public AuthenticationService authenticationService;

  @PostMapping("/signup")
  public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest){

    Map<String,Object> response = new LinkedHashMap<>();

    Users users = this.authenticationService.signup(signupRequest);

    response.put(Keys.STATUS_CODE, HttpStatus.OK.value());
    response.put(Keys.STATUS,HttpStatus.OK.name());
    response.put(Keys.DATA, users);
    response.put(Keys.TIME_STAMP,System.currentTimeMillis());

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){

    Map<String,Object> response = new LinkedHashMap<>();

    Users users = this.authenticationService.login(loginRequest);

    response.put(Keys.STATUS_CODE, HttpStatus.OK.value());
    response.put(Keys.STATUS,HttpStatus.OK.name());
    response.put(Keys.DATA, users);
    response.put(Keys.TIME_STAMP,System.currentTimeMillis());

    return ResponseEntity.status(HttpStatus.OK).body(response);

  }
  
}
