package com.starkIndustries.serviceJourney.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.starkIndustries.serviceJourney.model.Users;

public interface AuthenticationRepository extends JpaRepository<Users,String>{

  public Optional<Users> findByEmailOrUsername(String email,String username);

  public Optional<Users> findByUsername(String username);

  public Optional<Users> findByContact(String contact);

  public Optional<Users> findByEmail(String email);

  public Optional<Users> findByPanNumber(String panNumber);

  public Optional<Users> findByContactAndDateOfBirth(String contact, LocalDate dateOfBirth);

  public Optional<Users> findByContactAndPanNumber(String contact, String panNumber);

  public Optional<Users> findByContactAndUserId(String contact, String userId);

}
