package com.starkIndustries.serviceJourney.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.starkIndustries.serviceJourney.model.Event;

@Repository
public interface EventRepository extends JpaRepository<Event,String>{

Event findBySession_SessionIdAndExitTimeIsNull(String sessionId);  
}
