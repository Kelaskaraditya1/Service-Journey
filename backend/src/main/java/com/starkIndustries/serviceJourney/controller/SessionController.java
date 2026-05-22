package com.starkIndustries.serviceJourney.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.starkIndustries.serviceJourney.dto.request.EventTransitionRequest;
import com.starkIndustries.serviceJourney.dto.request.SessionEndRequest;
import com.starkIndustries.serviceJourney.dto.request.SessionStartRequest;
import com.starkIndustries.serviceJourney.dto.response.ApiResponse;
import com.starkIndustries.serviceJourney.service.SessionService;
import com.starkIndustries.serviceJourney.temporal.client.SessionWorkflowClient;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/session")
@Slf4j
public class SessionController {

  @Autowired
  private SessionWorkflowClient sessionWorkflowClient;

  @Autowired
  private SessionService sessionService;


  @PostMapping("/start")
  public ResponseEntity<ApiResponse<Object>> start(
      @Valid @RequestBody SessionStartRequest request) {

    log.info("POST /session/start");

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(
            sessionWorkflowClient.startSessionWorkflow(request),
            "Session created — Temporal workflow started"));
  }


  @PostMapping("/event-transition")
  public ResponseEntity<ApiResponse<Object>> eventTransition(
      @Valid @RequestBody EventTransitionRequest request) {

    log.info("POST /session/event-transition");

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(
            sessionWorkflowClient.transitionEvent(request),
            "Event transition signal sent"));
  }


  @PostMapping("/end")
  public ResponseEntity<ApiResponse<Object>> end(
      @Valid @RequestBody SessionEndRequest request) {

    log.info("POST /session/end");

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(
            sessionWorkflowClient.endSession(request),
            "Session end signal sent"));
  }

 
  @GetMapping("/{sessionId}/state")
  public ResponseEntity<ApiResponse<Object>> getWorkflowState(
      @PathVariable String sessionId) {

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(
            sessionWorkflowClient.getWorkflowState(sessionId),
            "Workflow state retrieved"));
  }


  @GetMapping("/{sessionId}")
  public ResponseEntity<ApiResponse<Object>> getSession(
      @PathVariable String sessionId) {

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(
            sessionService.getSession(sessionId)));
  }


  @GetMapping("")
  public ResponseEntity<ApiResponse<Object>> getSessions() {

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(
            sessionService.getAllSessions()));
  }

}