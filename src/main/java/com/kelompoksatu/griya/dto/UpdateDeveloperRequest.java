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

/** DTO for updating developer information by admin All fields are optional for partial updates */
@Schema(description = "Request object for updating developer information by admin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeveloperRequest {

  @Schema(
      description = "Company name of the developer",
      example = "PT Developer Properti Indonesia Updated")
  @Size(max = 255, message = "Company name must not exceed 255 characters")
  private String companyName;

  @Schema(description = "Business license number (SIUP)", example = "SIUP-123456789-UPDATED")
  @Size(max = 100, message = "Business license must not exceed 100 characters")
  private String businessLicense;

  @Schema(description = "Developer license number", example = "IZIN-DEV-001-UPDATED")
  @Size(max = 100, message = "Developer license must not exceed 100 characters")
  private String developerLicense;

  // Contact Information
  @Schema(description = "Contact person name", example = "Jane Doe")
  @Size(max = 100, message = "Contact person must not exceed 100 characters")
  private String contactPerson;

  @Schema(description = "Phone number in Indonesian format", example = "+6281234567891")
  @Pattern(
      regexp = "^(\\+62|62|0)[0-9]{9,13}$",
      message = "Please provide a valid Indonesian phone number")
  private String phone;

  @Schema(description = "Email address", example = "updated@devcompany.com")
  @Email(message = "Please provide a valid email address")
  @Size(max = 100, message = "Email must not exceed 100 characters")
  private String email;

  @Schema(description = "Company website URL", example = "https://www.devcompany-updated.com")
  @Size(max = 255, message = "Website must not exceed 255 characters")
  private String website;

  // Address
  @Schema(description = "Company address", example = "Jl. Sudirman No. 456, Jakarta Selatan")
  private String address;

  @Schema(description = "City where the company is located", example = "Jakarta")
  @Size(max = 100, message = "City must not exceed 100 characters")
  private String city;

  @Schema(description = "Province where the company is located", example = "DKI Jakarta")
  @Size(max = 100, message = "Province must not exceed 100 characters")
  private String province;

  @Schema(description = "Postal code", example = "12191")
  @Size(max = 10, message = "Postal code must not exceed 10 characters")
  private String postalCode;

  // Business Details
  @Schema(
      description = "Year the company was established",
      example = "2015",
      minimum = "1900",
      maximum = "2100")
  @Min(value = 1900, message = "Established year must be after 1900")
  @Max(value = 2100, message = "Established year must be before 2100")
  private Integer establishedYear;

  @Schema(
      description = "Company description",
      example = "Updated leading property developer specializing in commercial projects")
  private String description;

  @Schema(
      description = "Company specialization area",
      example = "COMMERCIAL",
      allowableValues = {"RESIDENTIAL", "COMMERCIAL", "MIXED", "INDUSTRIAL"})
  private Developer.Specialization specialization;

  // Partnership
  @Schema(description = "Whether the company is a partner", example = "true")
  private Boolean isPartner;

  @Schema(
      description = "Partnership level if the company is a partner",
      example = "PLATINUM",
      allowableValues = {"BRONZE", "SILVER", "GOLD", "PLATINUM"})
  private Developer.PartnershipLevel partnershipLevel;

  @Schema(
      description = "Commission rate (decimal between 0.0000 and 1.0000)",
      example = "0.0400",
      minimum = "0.0000",
      maximum = "1.0000")
  @DecimalMin(value = "0.0000", message = "Commission rate must be non-negative")
  @DecimalMax(value = "1.0000", message = "Commission rate must not exceed 100%")
  private BigDecimal commissionRate;

  // Status
  @Schema(
      description = "Developer status",
      example = "ACTIVE",
      allowableValues = {"ACTIVE", "INACTIVE", "SUSPENDED"})
  private Developer.DeveloperStatus status;

  // Verification
  @Schema(description = "Verification timestamp", example = "2024-01-20T10:00:00")
  private LocalDateTime verifiedAt;

  @Schema(description = "Verified by user ID", example = "1")
  @Min(value = 1, message = "Verified by user ID must be positive")
  private Integer verifiedBy;
}
