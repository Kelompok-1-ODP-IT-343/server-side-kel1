package com.kelompoksatu.griya.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** Generic DTO for standardized API responses */
@Setter
@Getter
public class ApiResponse<T> {

  // Getters and Setters
  private boolean success;
  private String message;
  private T data;
  private LocalDateTime timestamp;
  private String path;

  // Default constructor
  public ApiResponse() {
    this.timestamp = LocalDateTime.now();
  }

  // Constructor for success response with data
  public ApiResponse(boolean success, String message, T data) {
    this.success = success;
    this.message = message;
    this.data = data;
    this.timestamp = LocalDateTime.now();
  }

  // Constructor for response without data
  public ApiResponse(boolean success, String message) {
    this.success = success;
    this.message = message;
    this.timestamp = LocalDateTime.now();
  }

  // Static methods for common responses
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, "Success", data);
  }

  public static <T> ApiResponse<T> success(String message, T data) {
    return new ApiResponse<>(true, message, data);
  }

  public static <T> ApiResponse<T> success(String message) {
    return new ApiResponse<>(true, message);
  }

  public static <T> ApiResponse<T> error(String message) {
    return new ApiResponse<>(false, message);
  }

  public static <T> ApiResponse<T> error(String message, T data) {
    return new ApiResponse<>(false, message, data);
  }

  // Static methods with path parameter
  public static <T> ApiResponse<T> success(T data, String message, String path) {
    ApiResponse<T> response = new ApiResponse<>(true, message, data);
    response.setPath(path);
    return response;
  }

  public static <T> ApiResponse<T> error(String message, String path) {
    ApiResponse<T> response = new ApiResponse<>(false, message);
    response.setPath(path);
    return response;
  }
}
