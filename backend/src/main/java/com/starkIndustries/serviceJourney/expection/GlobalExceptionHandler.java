package com.starkIndustries.serviceJourney.expection;

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

import com.starkIndustries.serviceJourney.dto.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ApiResponse<Object>> globalExceptionHandler(CustomException customException) {

    log.error("CustomException — status: {}, message: {}",
        customException.status, customException.message);

    ApiResponse<Object> response = ApiResponse.error(customException.status, customException.message);

    return ResponseEntity.status(customException.status).body(response);

  }

  @Override
  protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
      HttpHeaders headers, HttpStatusCode status, WebRequest request) {

    Map<String, String> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .collect(
            Collectors.toMap(
                fieldError -> fieldError.getField(),
                fieldError -> fieldError.getDefaultMessage()));

    log.warn("Validation failed — {} error(s): {}", errors.size(), errors);

    ApiResponse<Object> response = ApiResponse.validationError(HttpStatus.valueOf(status.value()), errors);

    return ResponseEntity.status(status).body(response);

  }

}
