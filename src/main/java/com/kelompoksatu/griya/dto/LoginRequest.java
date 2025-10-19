package com.kelompoksatu.griya.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO for user login request */
public class LoginRequest {

  @NotBlank(message = "Username or email is required")
  @Size(max = 100, message = "Username or email must not exceed 100 characters")
  private String identifier; // Can be username or email

  @NotBlank(message = "Password is required")
  @Size(max = 100, message = "Password must not exceed 100 characters")
  private String password;

  // Default constructor
  public LoginRequest() {}

  // Constructor with parameters
  public LoginRequest(String identifier, String password) {
    this.identifier = identifier;
    this.password = password;
  }

  // Getters and Setters
  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
