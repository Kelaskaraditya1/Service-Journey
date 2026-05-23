package com.starkIndustries.serviceJourney.dto.response;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {

  private long timeStamp;
  private int statusCode;
  private String status;
  private String message;
  private T data;
  private boolean success;
  private Object errors;

  public static <T> ApiResponse<T> success(T data, String message) {
    return ApiResponse.<T>builder()
        .success(true)
        .statusCode(HttpStatus.OK.value())
        .status(HttpStatus.OK.name())
        .message(message)
        .data(data)
        .timeStamp(System.currentTimeMillis())
        .build();
  }

  public static <T> ApiResponse<T> success(T data) {
    return success(data, null);
  }

  public static <T> ApiResponse<T> error(HttpStatus httpStatus, String message) {
    return ApiResponse.<T>builder()
        .success(false)
        .statusCode(httpStatus.value())
        .status(httpStatus.name())
        .message(message)
        .timeStamp(System.currentTimeMillis())
        .build();
  }

  public static <T> ApiResponse<T> validationError(HttpStatus httpStatus, Object errors) {
    return ApiResponse.<T>builder()
        .success(false)
        .statusCode(httpStatus.value())
        .status(httpStatus.name())
        .errors(errors)
        .timeStamp(System.currentTimeMillis())
        .build();
  }

}
