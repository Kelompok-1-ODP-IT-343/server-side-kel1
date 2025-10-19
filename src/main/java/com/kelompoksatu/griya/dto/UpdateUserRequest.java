package com.kelompoksatu.griya.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating user information.
 * All fields are optional for partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating user information with optional fields")
public class UpdateUserRequest {

  @Schema(
      description = "Username for the user account",
      example = "john_doe",
      maxLength = 50)
  @Size(max = 50, message = "Username must not exceed 50 characters")
  @Pattern(
      regexp = "^[a-zA-Z0-9_]+$",
      message = "Username can only contain letters, numbers, and underscores")
  private String username;

  @Schema(
      description = "Email address of the user",
      example = "john.doe@example.com",
      maxLength = 100)
  @Email(message = "Email must be a valid email address")
  @Size(max = 100, message = "Email must not exceed 100 characters")
  private String email;

  @Schema(
      description = "Phone number of the user",
      example = "+6281234567890",
      maxLength = 20)
  @Size(max = 20, message = "Phone number must not exceed 20 characters")
  @Pattern(
      regexp = "^[+]?[0-9\\s\\-()]+$",
      message = "Phone number can only contain numbers, spaces, hyphens, parentheses, and optional plus sign")
  private String phone;

  @Schema(
      description = "User account status",
      example = "ACTIVE",
      allowableValues = {"PENDING_VERIFICATION", "ACTIVE", "INACTIVE", "SUSPENDED"})
  private String status;

  @Schema(
      description = "New password for the user account",
      example = "NewSecurePassword123!",
      minLength = 8,
      maxLength = 100)
  @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]",
      message = "Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character")
  private String password;

  @Schema(
      description = "Confirmation of the new password",
      example = "NewSecurePassword123!")
  private String confirmPassword;

  /**
   * Check if password and confirmation password match.
   * 
   * @return true if passwords match or both are null/empty
   */
  public boolean isPasswordMatching() {
    if (password == null && confirmPassword == null) {
      return true; // No password update requested
    }
    if (password == null || confirmPassword == null) {
      return false; // One is null, other is not
    }
    return password.equals(confirmPassword);
  }

  /**
   * Check if password update is requested.
   * 
   * @return true if password is provided
   */
  public boolean isPasswordUpdateRequested() {
    return password != null && !password.trim().isEmpty();
  }
}
