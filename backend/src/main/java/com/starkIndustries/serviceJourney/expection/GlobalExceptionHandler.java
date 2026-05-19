package com.starkIndustries.serviceJourney.expection;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.starkIndustries.serviceJourney.keys.Keys;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler{

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<?> globalExceptionHandler(CustomException customException){

    Map<String,Object> response = new LinkedHashMap<>();

    response.put(Keys.STATUS, customException.status.value());
    response.put(Keys.STATUS_CODE, HttpStatus.valueOf(customException.status.value()).name());
    response.put(Keys.MESSAGE,customException.message);
    response.put(Keys.TIME_STAMP,System.currentTimeMillis());

    return ResponseEntity.status(customException.status).body(response);

  }

  @Override
  protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
      HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String,String> errors = ex.getBindingResult()
          .getFieldErrors()
          .stream()
          .collect(
            Collectors.toMap(
              fieldError -> fieldError.getField(), // gives filed
              fieldError -> fieldError.getDefaultMessage() // gives error
            )
          );

        // List<String> errors = ex.getBindingResult()
        //   .getFieldErrors()
        //   .stream()
        //   .map(error->error.getDefaultMessage())
        //   .collect(Collectors.toList());

        Map<String,Object> response = new LinkedHashMap<>();

        response.put(Keys.STATUS, status.value());
        response.put(Keys.STATUS_CODE, HttpStatus.valueOf(status.value()).name());
        response.put(Keys.ERRORS, errors);
        response.put(Keys.TIME_STAMP, System.currentTimeMillis());

        return ResponseEntity.status(status).body(response);

  }

  
  
}
