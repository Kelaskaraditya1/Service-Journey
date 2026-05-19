package com.starkIndustries.serviceJourney.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.starkIndustries.serviceJourney.model.Session;

@Repository
public interface SessionRepository extends JpaRepository<Session,String>{
  
}
