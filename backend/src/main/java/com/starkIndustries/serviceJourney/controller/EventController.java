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

import com.starkIndustries.serviceJourney.dto.request.EventEndRequest;
import com.starkIndustries.serviceJourney.dto.request.EventStartRequest;
import com.starkIndustries.serviceJourney.dto.response.EventResponse;
import com.starkIndustries.serviceJourney.keys.Keys;
import com.starkIndustries.serviceJourney.service.EventService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/event")
public class EventController {

  @Autowired
  public EventService eventService;

  @PostMapping("/start")
  public ResponseEntity<?> start(@Valid @RequestBody EventStartRequest eventStartRequest){

    Map<String,Object> response = new LinkedHashMap<>();
    response.put(Keys.TIME_STAMP,System.currentTimeMillis());

    EventResponse eventResponse = this.eventService.startEvent(eventStartRequest);

    if(eventResponse!=null){
      response.put(Keys.STATUS,HttpStatus.OK.name());
      response.put(Keys.STATUS_CODE,HttpStatus.OK.value());
      response.put(Keys.DATA,eventResponse);
      return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    response.put(Keys.STATUS,HttpStatus.INTERNAL_SERVER_ERROR.name());
    response.put(Keys.STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
    response.put(Keys.MESSAGE, "Failed to create Event");

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

  }


  @PostMapping("/end")
  public ResponseEntity<?> start(@Valid @RequestBody EventEndRequest eventEndRequest){

    Map<String,Object> response = new LinkedHashMap<>();
    response.put(Keys.TIME_STAMP,System.currentTimeMillis());

    EventResponse eventResponse = this.eventService.endEvent(eventEndRequest);

    if(eventResponse!=null){
      response.put(Keys.STATUS_CODE, HttpStatus.OK.value());
      response.put(Keys.STATUS, HttpStatus.OK.name());
      response.put(Keys.DATA, eventResponse);

      return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    response.put(Keys.STATUS,HttpStatus.INTERNAL_SERVER_ERROR.name());
    response.put(Keys.STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
    response.put(Keys.MESSAGE, "Failed to end Event");

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

  }  
  
}
