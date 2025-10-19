package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.Gender;
import com.kelompoksatu.griya.entity.MaritalStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for updating user account and profile information.
 * All fields are optional for partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating user account and profile information with optional fields")
public class UpdateUserRequest {

  // User Account Information
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

  // User Profile Information
  @Schema(
      description = "Full name of the user",
      example = "John Doe",
      maxLength = 100)
  @Size(max = 100, message = "Full name must not exceed 100 characters")
  private String fullName;

  @Schema(
      description = "NIK (Nomor Induk Kependudukan) - Indonesian ID number",
      example = "1234567890123456",
      maxLength = 16)
  @Size(max = 16, message = "NIK must not exceed 16 characters")
  @Pattern(
      regexp = "^[0-9]{16}$",
      message = "NIK must be exactly 16 digits")
  private String nik;

  @Schema(
      description = "NPWP (Nomor Pokok Wajib Pajak) - Indonesian tax number",
      example = "1234567890123456",
      maxLength = 16)
  @Size(max = 16, message = "NPWP must not exceed 16 characters")
  @Pattern(
      regexp = "^[0-9]{16}$",
      message = "NPWP must be exactly 16 digits")
  private String npwp;

  @Schema(
      description = "Birth date of the user",
      example = "1990-01-15")
  private LocalDate birthDate;

  @Schema(
      description = "Birth place of the user",
      example = "Jakarta",
      maxLength = 100)
  @Size(max = 100, message = "Birth place must not exceed 100 characters")
  private String birthPlace;

  @Schema(
      description = "Gender of the user",
      example = "MALE",
      allowableValues = {"MALE", "FEMALE"})
  private String gender;

  @Schema(
      description = "Marital status of the user",
      example = "SINGLE",
      allowableValues = {"SINGLE", "MARRIED", "DIVORCED", "WIDOWED"})
  private String maritalStatus;

  @Schema(
      description = "Address of the user",
      example = "Jl. Sudirman No. 123, Jakarta Selatan")
  @Size(max = 1000, message = "Address must not exceed 1000 characters")
  private String address;

  @Schema(
      description = "City where the user lives",
      example = "Jakarta Selatan",
      maxLength = 100)
  @Size(max = 100, message = "City must not exceed 100 characters")
  private String city;

  @Schema(
      description = "Province where the user lives",
      example = "DKI Jakarta",
      maxLength = 100)
  @Size(max = 100, message = "Province must not exceed 100 characters")
  private String province;

  @Schema(
      description = "Postal code of the user's address",
      example = "12190",
      maxLength = 10)
  @Size(max = 10, message = "Postal code must not exceed 10 characters")
  @Pattern(
      regexp = "^[0-9]{5,10}$",
      message = "Postal code must be 5-10 digits")
  private String postalCode;

  @Schema(
      description = "Occupation of the user",
      example = "Software Engineer",
      maxLength = 100)
  @Size(max = 100, message = "Occupation must not exceed 100 characters")
  private String occupation;

  @Schema(
      description = "Company name where the user works",
      example = "PT. Tech Solutions",
      maxLength = 100)
  @Size(max = 100, message = "Company name must not exceed 100 characters")
  private String companyName;

  @Schema(
      description = "Monthly income of the user",
      example = "15000000.00")
  @DecimalMin(value = "0.0", message = "Monthly income must be positive")
  private BigDecimal monthlyIncome;

  @Schema(
      description = "Work experience in years",
      example = "5")
  @Min(value = 0, message = "Work experience must be non-negative")
  private Integer workExperience;

  /**
   * Convert string gender to Gender enum.
   * 
   * @return Gender enum or null if gender is null/empty
   */
  public Gender getGenderEnum() {
    if (gender == null || gender.trim().isEmpty()) {
      return null;
    }
    try {
      return Gender.valueOf(gender.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null; // Invalid gender, ignore update
    }
  }

  /**
   * Convert string marital status to MaritalStatus enum.
   * 
   * @return MaritalStatus enum or null if marital status is null/empty
   */
  public MaritalStatus getMaritalStatusEnum() {
    if (maritalStatus == null || maritalStatus.trim().isEmpty()) {
      return null;
    }
    try {
      return MaritalStatus.valueOf(maritalStatus.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null; // Invalid marital status, ignore update
    }
  }

  /**
   * Convert string status to UserStatus enum.
   * 
   * @return UserStatus enum or null if status is null/empty
   */
  public com.kelompoksatu.griya.entity.UserStatus getUserStatusEnum() {
    if (status == null || status.trim().isEmpty()) {
      return null;
    }
    try {
      return com.kelompoksatu.griya.entity.UserStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null; // Invalid status, ignore update
    }
  }
}
