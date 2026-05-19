package com.starkIndustries.serviceJourney.expection;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException{

  public HttpStatus status;
  public String message;
  public long timeStamp;

  public CustomException(HttpStatus status, String message){
    super(message);
    this.status = status;
    this.message = message;
    this.timeStamp = System.currentTimeMillis();

  }

  
}
