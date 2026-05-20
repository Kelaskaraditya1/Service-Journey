package com.starkIndustries.serviceJourney.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.starkIndustries.serviceJourney.dto.request.IdentityType;
import com.starkIndustries.serviceJourney.dto.request.LoginRequest;
import com.starkIndustries.serviceJourney.dto.request.SignupRequest;
import com.starkIndustries.serviceJourney.expection.CustomException;
import com.starkIndustries.serviceJourney.model.Users;
import com.starkIndustries.serviceJourney.repository.AuthenticationRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthenticationService {

  @Autowired
  public AuthenticationRepository authenticationRepository;

  public Users signup(SignupRequest signupRequest){

    if(this.authenticationRepository.findByContact(signupRequest.contact).isPresent()){
      log.error("contact {} already taken",signupRequest.contact);
      throw new CustomException(HttpStatus.BAD_REQUEST,"contact "+signupRequest.contact+" already taken");  
    }

    if(this.authenticationRepository.findByEmail(signupRequest.email).isPresent()){
            log.error("email {} already taken",signupRequest.email);
      throw new CustomException(HttpStatus.BAD_REQUEST,"email "+signupRequest.email+" already taken"); 
    }

    if(this.authenticationRepository.findByUsername(signupRequest.username).isPresent()){
            log.error("username {} already taken",signupRequest.username);
      throw new CustomException(HttpStatus.BAD_REQUEST,"username "+signupRequest.username+" already taken"); 
    }

    if(this.authenticationRepository.findByPanNumber(signupRequest.panNumber).isPresent()){
      log.error("Pan card number {} already taken",signupRequest.panNumber);
      throw new CustomException(HttpStatus.BAD_REQUEST,"Pan card number "+signupRequest.panNumber+" already exists");
    }

    Users users = Users.builder()
    .userId(UUID.randomUUID().toString())
    .name(signupRequest.name)
    .contact(signupRequest.contact)
    .email(signupRequest.email)
    .dateOfBirth(signupRequest.dateOfBirth)
    .panNumber(signupRequest.panNumber)
    .username(signupRequest.username)
    .password(signupRequest.password)
    .createdAt(System.currentTimeMillis())
    .build();

    return this.authenticationRepository.save(users);

  }

  public Users login(LoginRequest loginRequest){

    LocalDate dob = null;

    if(loginRequest.identityType == IdentityType.DOB){
      dob = LocalDate.parse(
        loginRequest.identity,
        DateTimeFormatter.ofPattern("dd-MM-yyyy")
      );
    }

    Users users;

    if(loginRequest.identityType == IdentityType.DOB){
      users = this.authenticationRepository.findByContactAndDateOfBirth(loginRequest.contactNumber, dob)
        .orElseThrow(
          ()-> {
            log.error("Login failed — no user found for contact: {} with DOB", loginRequest.contactNumber);
            throw new CustomException(HttpStatus.BAD_REQUEST,"Invalid credentials");
          }
        );
    }else if(loginRequest.identityType == IdentityType.PAN){
            users = this.authenticationRepository.findByContactAndPanNumber(loginRequest.contactNumber,loginRequest.identity)
        .orElseThrow(
          ()-> {
            log.error("Login failed — no user found for contact: {} with PAN", loginRequest.contactNumber);
            throw new CustomException(HttpStatus.BAD_REQUEST,"Invalid credentials");
          }
        );
    }else {
            users = this.authenticationRepository.findByContactAndUserId(loginRequest.contactNumber, loginRequest.identity)
        .orElseThrow(
          ()-> {
            log.error("Login failed — no user found for contact: {} with UID", loginRequest.contactNumber);
            throw new CustomException(HttpStatus.BAD_REQUEST,"Invalid credentials");
          }
        );
    }

    return users;

  }
  
}
