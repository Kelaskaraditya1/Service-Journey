package com.starkIndustries.serviceJourney.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.starkIndustries.serviceJourney.dto.request.SessionEndRequest;
import com.starkIndustries.serviceJourney.dto.request.SessionStartRequest;
import com.starkIndustries.serviceJourney.dto.response.SessionResponse;
import com.starkIndustries.serviceJourney.keys.Keys;
import com.starkIndustries.serviceJourney.service.SessionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/session")
public class SessionController {

  @Autowired
  public SessionService sessionService;

  @PostMapping("/start")
  public ResponseEntity<?> start(@Valid @RequestBody SessionStartRequest sessionStartRequest){

    Map<String,Object> response = new LinkedHashMap<>();
    response.put(Keys.TIME_STAMP,System.currentTimeMillis());
    
    SessionResponse sessionResponse = this.sessionService.startSession(sessionStartRequest);
    if(sessionResponse!=null){
      response.put(Keys.STATUS_CODE,HttpStatus.OK.value());
      response.put(Keys.STATUS,HttpStatus.OK.name());
      response.put(Keys.DATA,sessionResponse);

      return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    response.put(Keys.STATUS,HttpStatus.INTERNAL_SERVER_ERROR.name());
    response.put(Keys.STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
    response.put(Keys.MESSAGE, "Failed to create Session");

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

  }

  @PostMapping("/end")
  public ResponseEntity<?> end(@Valid @RequestBody SessionEndRequest sessionEndRequest){

    Map<String,Object> response = new LinkedHashMap<>();
    response.put(Keys.TIME_STAMP,System.currentTimeMillis());

    SessionResponse sessionResponse = this.sessionService.endSession(sessionEndRequest);
    if(sessionResponse!=null){
      response.put(Keys.STATUS_CODE,HttpStatus.OK.value());
      response.put(Keys.STATUS, HttpStatus.OK.name());
      response.put(Keys.DATA, sessionResponse);

      return ResponseEntity.status(HttpStatus.OK).body(response);
    }
      
    response.put(Keys.STATUS,HttpStatus.INTERNAL_SERVER_ERROR.name());
    response.put(Keys.STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
    response.put(Keys.MESSAGE, "Failed to end Session");

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

  }

  @GetMapping("/{sessionId}")
  public ResponseEntity<?> getSession(@PathVariable("sessionId") String sessionId){

    return ResponseEntity.status(HttpStatus.OK).body(this.sessionService.getSession(sessionId));

  }

  @GetMapping("")
  public ResponseEntity<?> getSessions(){

    return ResponseEntity.status(HttpStatus.OK).body(this.sessionService.getAllSessions());

  }
  
}
