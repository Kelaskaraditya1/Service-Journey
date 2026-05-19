package com.starkIndustries.serviceJourney.service;

import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.starkIndustries.serviceJourney.dto.request.EventEndRequest;
import com.starkIndustries.serviceJourney.dto.request.EventStartRequest;
import com.starkIndustries.serviceJourney.dto.response.EventResponse;
import com.starkIndustries.serviceJourney.expection.CustomException;
import com.starkIndustries.serviceJourney.model.Event;
import com.starkIndustries.serviceJourney.model.Session;
import com.starkIndustries.serviceJourney.repository.EventRepository;
import com.starkIndustries.serviceJourney.repository.SessionRepository;

@Service
public class EventService {

  @Autowired
  public EventRepository eventRepository;

  @Autowired
  public SessionRepository sessionRepository;

  public EventResponse startEvent(EventStartRequest eventStartRequest){

          Instant now = Instant.now();


    Session session = this.sessionRepository.findById(eventStartRequest.sessionId)
      .orElseThrow(
        ()->new CustomException(HttpStatus.BAD_REQUEST,"Session with session id "+eventStartRequest.sessionId+" does not exist")
      );

      session.lastPage=eventStartRequest.page;
      session.lastActivityTime= now;

      this.sessionRepository.save(session);

      Event openEvent = this.eventRepository.findBySession_SessionIdAndExitTimeIsNull(eventStartRequest.sessionId);

      if(openEvent!=null){
        openEvent.setExitTime(now);
        openEvent.setTimeSpent(openEvent.exitTime.toEpochMilli()-openEvent.enterTime.toEpochMilli());

        this.eventRepository.save(openEvent);
      }

    
    Event event = Event.builder()
      .eventId(UUID.randomUUID().toString())
      .session(session)
      .page(eventStartRequest.page)
      .enterTime(now)
      .exitTime(null)
      .timeSpent(null)
      .build();

      this.eventRepository.save(event);

      return EventResponse.builder()
      .eventId(event.eventId)
      .sessionId(session.sessionId)
      .page(event.page)
      .enterTime(event.enterTime)
      .exitTime(event.exitTime)
      .timeSpent(event.timeSpent)
      .build();

  }

  public EventResponse endEvent(EventEndRequest eventEndRequest){

    Event event = this.eventRepository.findById(eventEndRequest.eventId)
      .orElseThrow(
        ()-> new CustomException(HttpStatus.BAD_REQUEST, "Event for the event Id "+eventEndRequest.eventId+" does not exist")
      );

      if(event.exitTime==null){
      event.exitTime=Instant.now();
      event.timeSpent= event.exitTime.toEpochMilli()-event.enterTime.toEpochMilli();
      }

      this.eventRepository.save(event);

      return EventResponse.builder()
        .eventId(event.eventId)
        .sessionId(event.session.sessionId)
        .page(event.page)
        .enterTime(event.enterTime)
        .exitTime(event.exitTime)
        .timeSpent(event.timeSpent)
        .build();

  }
  
}
