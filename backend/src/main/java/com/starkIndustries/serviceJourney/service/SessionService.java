package com.starkIndustries.serviceJourney.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.starkIndustries.serviceJourney.dto.request.SessionEndRequest;
import com.starkIndustries.serviceJourney.dto.request.SessionStartRequest;
import com.starkIndustries.serviceJourney.dto.response.SessionResponse;
import com.starkIndustries.serviceJourney.expection.CustomException;
import com.starkIndustries.serviceJourney.keys.Keys;
import com.starkIndustries.serviceJourney.model.Event;
import com.starkIndustries.serviceJourney.model.ExpiryReasons;
import com.starkIndustries.serviceJourney.model.Session;
import com.starkIndustries.serviceJourney.repository.EventRepository;
import com.starkIndustries.serviceJourney.repository.SessionRepository;

@Service
public class SessionService {

  @Autowired
  public SessionRepository sessionRepository;

  @Autowired
  public EventRepository eventRepository;

  public SessionResponse startSession(SessionStartRequest sessionStartRequest){

    Instant now = Instant.now();

    Session session = Session.builder()
      .sessionId(UUID.randomUUID().toString())
      .userId(sessionStartRequest.userId)
      .startTime(now)
      .endTime(null)
      .expired(false)
      .expiryReasons(null)
      .lastPage(null)
      .duration(null)
      .lastActivityTime(now)
      .build();

      this.sessionRepository.save(session);

      return SessionResponse.builder()
      .sessionId(session.sessionId)
      .userId(session.userId)
      .startTime(session.startTime)
      .endTime(session.endTime)
      .expired(session.expired)
      .expiryReasons(session.expiryReasons)
      .lastPage(session.lastPage)
      .duration(session.duration)
      .lastActivityTime(session.lastActivityTime)
      .build();

  }

  public SessionResponse endSession(SessionEndRequest sessionEndRequest){

    Instant now = Instant.now();

    Session session = this.sessionRepository.findById(sessionEndRequest.sessionId)
      .orElseThrow(
        ()-> new CustomException(HttpStatus.BAD_REQUEST, "Session wth session id "+sessionEndRequest.sessionId+" does not exist")
      );

      Event openEvent = this.eventRepository.findBySession_SessionIdAndExitTimeIsNull(session.sessionId);

      if(openEvent != null && openEvent.getExitTime() == null){
        openEvent.exitTime = now;
        openEvent.timeSpent = openEvent.exitTime.toEpochMilli()-openEvent.enterTime.toEpochMilli();

        this.eventRepository.save(openEvent);
      }

    ExpiryReasons reason;

    if (sessionEndRequest.getExpiryReasons() != null && sessionEndRequest.getExpiryReasons() == ExpiryReasons.LOGOUT) {
        reason = ExpiryReasons.LOGOUT;
    } else if (now.isAfter(session.getStartTime().plusSeconds(Keys.ABSOLUTE))) { 
        reason = ExpiryReasons.ABSOLUTE;
    } else {
        reason = ExpiryReasons.INACTIVITY;
    }

    session.setEndTime(now);
    session.setExpired(reason!=ExpiryReasons.LOGOUT);
    session.setExpiryReasons(reason);
    session.setDuration(now.toEpochMilli()-session.startTime.toEpochMilli());
    session.setLastActivityTime(now);

    this.sessionRepository.save(session);

    return SessionResponse.builder()
      .sessionId(session.sessionId)
      .userId(session.userId)
      .startTime(session.startTime)
      .endTime(session.endTime)
      .expired(session.expired)
      .expiryReasons(session.expiryReasons)
      .lastPage(session.lastPage)
      .duration(session.duration)
      .lastActivityTime(session.lastActivityTime)
      .build();

  }

  public void forceAbortSession(Session session, ExpiryReasons expiryReasons){

    if(session.endTime!=null)
      return ;

    Event openEvent = this.eventRepository.findBySession_SessionIdAndExitTimeIsNull(session.sessionId);

    Instant now = Instant.now();

    if(openEvent!=null){
      openEvent.exitTime=now;
      openEvent.timeSpent = now.toEpochMilli() - openEvent.enterTime.toEpochMilli();

      this.eventRepository.save(openEvent);
    }

    session.setEndTime(now);
    session.setExpiryReasons(expiryReasons);
    session.setExpired(true);
    session.setDuration(session.endTime.toEpochMilli()-session.startTime.toEpochMilli());

    this.sessionRepository.save(session);

  }


  public List<Session> getAllSessions(){
    return this.sessionRepository.findAll();
  }

  public Session getSession(String sessionId){

    return this.sessionRepository.findById(sessionId)
      .orElseThrow(
        ()-> new CustomException(HttpStatus.BAD_REQUEST,"Session with session Id "+sessionId+" does not exist")
      );

  }


  
}
