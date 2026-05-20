package com.starkIndustries.serviceJourney.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.starkIndustries.serviceJourney.model.Session;
import com.starkIndustries.serviceJourney.model.SessionStatus;

@Repository
public interface SessionRepository extends JpaRepository<Session, String> {

  /** Find all active sessions for a user (should typically be 0 or 1) */
  List<Session> findByUserIdAndSessionStatus(String userId, SessionStatus sessionStatus);

}
