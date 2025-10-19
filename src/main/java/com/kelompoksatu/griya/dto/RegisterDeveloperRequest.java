package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.Developer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for developer registration request by admin This combines user account creation with
 * developer profile information
 */
@Schema(description = "Request object for registering a new developer with user account and profile information")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDeveloperRequest {

  // User Account Information (required for login)
  @Schema(
      description = "Unique username for the developer account",
      example = "dev_company_001")
  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  @Pattern(
      regexp = "^[a-zA-Z0-9_]+$",
      message = "Username can only contain letters, numbers, and underscores")
  private String username;

  @Schema(
      description = "Email address for the developer account",
      example = "admin@devcompany.com")
  @NotBlank(message = "Email is required")
  @Email(message = "Please provide a valid email address")
  @Size(max = 100, message = "Email must not exceed 100 characters")
  private String email;

  @Schema(
      description = "Password for the developer account (must contain uppercase, lowercase, number, and special character)",
      example = "SecurePass123!")
  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
      message =
          "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character")
  private String password;

  @Schema(
      description = "Phone number in Indonesian format",
      example = "+6281234567890")
  @NotBlank(message = "Phone number is required")
  @Pattern(
      regexp = "^(\\+62|62|0)[0-9]{9,13}$",
      message = "Please provide a valid Indonesian phone number")
  private String phone;

  // Developer Company Information
  @Schema(
      description = "Company name of the developer",
      example = "PT Developer Properti Indonesia")
  @NotBlank(message = "Company name is required")
  @Size(max = 255, message = "Company name must not exceed 255 characters")
  private String companyName;

  @Schema(
      description = "Unique company code identifier",
      example = "DEV001")
  @NotBlank(message = "Company code is required")
  @Size(max = 20, message = "Company code must not exceed 20 characters")
  private String companyCode;

  @NotBlank(message = "Business license is required")
  @Size(max = 100, message = "Business license must not exceed 100 characters")
  private String businessLicense; // SIUP

  @NotBlank(message = "Developer license is required")
  @Size(max = 100, message = "Developer license must not exceed 100 characters")
  private String developerLicense; // Izin Developer

  // Contact Information
  @NotBlank(message = "Contact person is required")
  @Size(max = 100, message = "Contact person must not exceed 100 characters")
  private String contactPerson;

  @Size(max = 255, message = "Website must not exceed 255 characters")
  private String website;

  // Address
  @NotBlank(message = "Address is required")
  private String address;

  @NotBlank(message = "City is required")
  @Size(max = 100, message = "City must not exceed 100 characters")
  private String city;

  @NotBlank(message = "Province is required")
  @Size(max = 100, message = "Province must not exceed 100 characters")
  private String province;

  @NotBlank(message = "Postal code is required")
  @Size(max = 10, message = "Postal code must not exceed 10 characters")
  private String postalCode;

  // Business Details
  @Min(value = 1900, message = "Established year must be after 1900")
  @Max(value = 2100, message = "Established year must be before 2100")
  private Integer establishedYear;

  private String description;

  @Schema(
      description = "Company specialization area",
      example = "RESIDENTIAL",
      allowableValues = {"RESIDENTIAL", "COMMERCIAL", "MIXED", "INDUSTRIAL"})
  @NotNull(message = "Specialization is required") 
  private Developer.Specialization specialization;

  // Partnership
  @Schema(
      description = "Whether the company is a partner",
      example = "true")
  @NotNull(message = "Partnership status is required") 
  private Boolean isPartner = false;

  private Developer.PartnershipLevel partnershipLevel;

  @DecimalMin(value = "0.0000", message = "Commission rate must be non-negative")
  @DecimalMax(value = "1.0000", message = "Commission rate must not exceed 100%")
  private BigDecimal commissionRate = new BigDecimal("0.0250"); // 2.5% default

  // Status (admin can set initial status)
  private Developer.DeveloperStatus status = Developer.DeveloperStatus.ACTIVE;

  // Consent timestamp (required for user creation)
  @Schema(
      description = "Timestamp when user consented to terms and conditions",
      example = "2024-01-20T10:00:00")
  @NotNull(message = "Consent timestamp is required") 
  private LocalDateTime consentAt;

  // Utility method to check if password matches (for consistency with RegisterRequest)
  public boolean isPasswordMatching() {
    // For admin registration, we don't have confirmPassword field
    // Password validation is handled by the validation annotations
    return password != null && !password.trim().isEmpty();
  }
}
