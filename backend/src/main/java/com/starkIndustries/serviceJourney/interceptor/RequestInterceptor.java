package com.starkIndustries.serviceJourney.interceptor;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.starkIndustries.serviceJourney.expection.CustomException;
import com.starkIndustries.serviceJourney.keys.Keys;
import com.starkIndustries.serviceJourney.model.ExpiryReasons;
import com.starkIndustries.serviceJourney.model.Session;
import com.starkIndustries.serviceJourney.repository.SessionRepository;
import com.starkIndustries.serviceJourney.service.SessionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RequestInterceptor implements HandlerInterceptor {

  /* 

  Logic: 
    1) it intercepts the request before reaching the controller layer.
    2) we are passing a session Id as header value of the active session.
    3) we fetch the session and check the start time and lastActivityTime.
    4) if now > startTime + Absollute threshold, end the session
    5) lastActivityTime - startTimem > Inactivity Threshold , end the session.
  
  */

  @Autowired
  public SessionRepository sessionRepository;

  @Autowired
  public SessionService sessionService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

    log.info("Interceptor HIT for {}", request.getRequestURI());

    String sessionId = request.getHeader(Keys.SESSION_HEADER);
    log.debug("Session Id: {}",sessionId);

    if(sessionId==null)
      return true;

    Session session = this.sessionRepository.findById(sessionId)
      .orElseThrow(
        ()-> new CustomException(HttpStatus.BAD_REQUEST, "Session with session Id "+sessionId+" does not exist")
      );

      Instant now = Instant.now();

      if(now.isAfter(session.startTime.plusSeconds(Keys.ABSOLUTE))){
        log.info("Session with session Id {} expired Absolutely",session.sessionId);
        this.sessionService.forceAbortSession(session,ExpiryReasons.ABSOLUTE);
        throw new CustomException(HttpStatus.UNAUTHORIZED,"Session with session Id "+session.sessionId+" expired Absolutely");
      }

      if((now.toEpochMilli() - session.lastActivityTime.toEpochMilli())>Keys.INACTIVITY_TIME){
        log.info("Session with session Id {} expired due to Inactivity",session.sessionId);
        this.sessionService.forceAbortSession(session,ExpiryReasons.INACTIVITY);
        throw new CustomException(HttpStatus.UNAUTHORIZED,"Session with session Id "+session.sessionId+" expired due to Inactivity");        
      }

      return true;
  }
  
}
