package com.starkIndustries.serviceJourney.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.starkIndustries.serviceJourney.dto.request.EventTransitionRequest;
import com.starkIndustries.serviceJourney.dto.request.SessionEndRequest;
import com.starkIndustries.serviceJourney.dto.request.SessionStartRequest;
import com.starkIndustries.serviceJourney.dto.response.ApiResponse;
import com.starkIndustries.serviceJourney.dto.response.EventTransitionResponse;
import com.starkIndustries.serviceJourney.dto.response.SessionResponse;
import com.starkIndustries.serviceJourney.service.SessionService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/session")
@Slf4j
public class SessionController {

  @Autowired
  public SessionService sessionService;

  /**
   * POST /session/start
   * Creates a new session for the given user.
   */
  @PostMapping("/start")
  public ResponseEntity<ApiResponse<SessionResponse>> start(
      @Valid @RequestBody SessionStartRequest sessionStartRequest) {

    log.info("POST /session/start — userId: {}", sessionStartRequest.userId);

    SessionResponse sessionResponse = this.sessionService.startSession(sessionStartRequest);

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(sessionResponse, "Session created successfully"));
  }

  /**
   * POST /session/event-transition
   * 
   * The new unified API that replaces separate /event/start and /event/end calls.
   * Handles closing the previous event and opening the next one atomically.
   */
  @PostMapping("/event-transition")
  public ResponseEntity<ApiResponse<EventTransitionResponse>> eventTransition(
      @Valid @RequestBody EventTransitionRequest eventTransitionRequest) {

    log.info("POST /session/event-transition — sessionId: {}, nextScreen: {}",
        eventTransitionRequest.sessionId, eventTransitionRequest.nextScreenName);

    EventTransitionResponse response = this.sessionService.eventTransition(eventTransitionRequest);

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(response, "Event transition successful"));
  }

  /**
   * POST /session/end
   * Ends a session (user logout or system-triggered).
   */
  @PostMapping("/end")
  public ResponseEntity<ApiResponse<SessionResponse>> end(
      @Valid @RequestBody SessionEndRequest sessionEndRequest) {

    log.info("POST /session/end — sessionId: {}", sessionEndRequest.sessionId);

    SessionResponse sessionResponse = this.sessionService.endSession(sessionEndRequest);

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(sessionResponse, "Session ended successfully"));
  }

  /**
   * GET /session/{sessionId}
   * Retrieves a single session by ID with its events.
   */
  @GetMapping("/{sessionId}")
  public ResponseEntity<ApiResponse<Object>> getSession(@PathVariable("sessionId") String sessionId) {

    log.debug("GET /session/{}", sessionId);

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(this.sessionService.getSession(sessionId)));
  }

  /**
   * GET /session
   * Retrieves all sessions.
   */
  @GetMapping("")
  public ResponseEntity<ApiResponse<Object>> getSessions() {

    log.debug("GET /session — fetching all sessions");

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.success(this.sessionService.getAllSessions()));
  }

}
